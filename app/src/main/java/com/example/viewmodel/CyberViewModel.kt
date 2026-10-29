package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.AuthState
import com.example.model.ChartMetric
import com.example.model.CyberAttackArc
import com.example.model.CyberNode
import com.example.model.LogSeverity
import com.example.model.SystemLog
import com.example.model.TelemetryPoint
import com.example.model.TerminalLine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class CyberViewModel : ViewModel() {

    // --- Authentication ---
    private val correctPasscode = "9955"

    private val _authState = MutableStateFlow(AuthState.LOCKED)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _enteredPin = MutableStateFlow("")
    val enteredPin: StateFlow<String> = _enteredPin.asStateFlow()

    private val _authMessage = MutableStateFlow("ENTER AUTHORIZATION CODE")
    val authMessage: StateFlow<String> = _authMessage.asStateFlow()

    private val _failedAttempts = MutableStateFlow(0)
    val failedAttempts: StateFlow<Int> = _failedAttempts.asStateFlow()

    // --- Terminal Engine ---
    private val _terminalLines = MutableStateFlow<List<TerminalLine>>(emptyList())
    val terminalLines: StateFlow<List<TerminalLine>> = _terminalLines.asStateFlow()

    private val _isStreaming = MutableStateFlow(true)
    val isStreaming: StateFlow<Boolean> = _isStreaming.asStateFlow()

    private val _streamSpeed = MutableStateFlow(1) // 1x, 2x, 5x
    val streamSpeed: StateFlow<Int> = _streamSpeed.asStateFlow()

    private val _commandInput = MutableStateFlow("")
    val commandInput: StateFlow<String> = _commandInput.asStateFlow()

    // --- Telemetry Chart Engine ---
    private val _telemetryHistory = MutableStateFlow<List<TelemetryPoint>>(emptyList())
    val telemetryHistory: StateFlow<List<TelemetryPoint>> = _telemetryHistory.asStateFlow()

    private val _selectedMetric = MutableStateFlow(ChartMetric.BANDWIDTH)
    val selectedMetric: StateFlow<ChartMetric> = _selectedMetric.asStateFlow()

    // --- World Map & Attack Tracker ---
    val worldNodes = listOf(
        CyberNode("tokyo", "TOKYO CORE", "JP", 35.68f, 139.69f, "133.242.18.99", "BREACHED", 14, 88),
        CyberNode("frankfurt", "FRANKFURT RELAY", "DE", 50.11f, 8.68f, "185.220.101.4", "ACTIVE", 9, 64),
        CyberNode("sf", "SILICON VALLEY", "US", 37.77f, -122.41f, "104.244.42.1", "TARGET", 24, 92),
        CyberNode("london", "LONDON GATEWAY", "UK", 51.50f, -0.12f, "194.109.6.93", "COMPROMISED", 11, 75),
        CyberNode("moscow", "MOSCOW ROOT", "RU", 55.75f, 37.61f, "95.173.136.72", "INTERCEPTING", 18, 59),
        CyberNode("singapore", "SINGAPORE SAT", "SG", 1.35f, 103.82f, "203.116.1.18", "ACTIVE", 16, 81),
        CyberNode("mumbai", "MUMBAI SUBSEA", "IN", 19.07f, 72.87f, "115.112.9.44", "INTERCEPTING", 12, 70),
        CyberNode("sydney", "SYDNEY DOWNLINK", "AU", -33.86f, 151.20f, "139.130.4.5", "ACTIVE", 28, 45),
        CyberNode("saopaulo", "SÃO PAULO HUB", "BR", -23.55f, -46.63f, "177.18.42.10", "BREACHED", 32, 63),
        CyberNode("capetown", "CAPE POINT SAT", "ZA", -33.92f, 18.42f, "196.25.1.1", "ACTIVE", 35, 52)
    )

    private val _selectedNode = MutableStateFlow<CyberNode?>(worldNodes[0])
    val selectedNode: StateFlow<CyberNode?> = _selectedNode.asStateFlow()

    private val _activeAttackArcs = MutableStateFlow<List<CyberAttackArc>>(emptyList())
    val activeAttackArcs: StateFlow<List<CyberAttackArc>> = _activeAttackArcs.asStateFlow()

    // --- System Logs Stream ---
    private val _systemLogs = MutableStateFlow<List<SystemLog>>(emptyList())
    val systemLogs: StateFlow<List<SystemLog>> = _systemLogs.asStateFlow()

    private val _logFilter = MutableStateFlow<LogSeverity?>(null) // null = ALL
    val logFilter: StateFlow<LogSeverity?> = _logFilter.asStateFlow()

    private val _logSearchQuery = MutableStateFlow("")
    val logSearchQuery: StateFlow<String> = _logSearchQuery.asStateFlow()

    private val _isLogsPaused = MutableStateFlow(false)
    val isLogsPaused: StateFlow<Boolean> = _isLogsPaused.asStateFlow()

    private var terminalJob: Job? = null
    private var telemetryJob: Job? = null
    private var attackArcJob: Job? = null
    private var logsJob: Job? = null

    private var lineCounter = 0L
    private var logCounter = 0L

    init {
        // Pre-seed some telemetry
        val initialPoints = mutableListOf<TelemetryPoint>()
        val now = System.currentTimeMillis()
        for (i in 30 downTo 0) {
            initialPoints.add(
                TelemetryPoint(
                    timestamp = now - (i * 1000L),
                    bandwidth = 3.5f + Random.nextFloat() * 4.5f,
                    threatLevel = 40f + Random.nextFloat() * 45f,
                    cpuLoad = 55f + Random.nextFloat() * 35f,
                    packetRate = 60f + Random.nextFloat() * 40f
                )
            )
        }
        _telemetryHistory.value = initialPoints

        // Pre-seed initial terminal lines
        _terminalLines.value = listOf(
            TerminalLine(lineCounter++, "[SYS_BOOT] INITIALIZING SECURE KERNEL 5.15.99-CYBER", isHighlight = true),
            TerminalLine(lineCounter++, "[SYS_INIT] LOADING CRYPTOGRAPHIC PROTOCOL SHA-512/AES-GCM", isHighlight = true),
            TerminalLine(lineCounter++, "[AUTH_DAEMON] LEVEL 5 CLEARANCE REQUIRED", isWarning = true),
            TerminalLine(lineCounter++, "[AUTH_DAEMON] AWAITING PIN ENTRY FOR OPERATOR 0x9955...")
        )

        // Pre-seed initial logs
        addSystemLog("KERNEL", LogSeverity.INFO, "Secure subsystem memory mapped 0x0000..0xFFFF")
        addSystemLog("SECURITY", LogSeverity.WARN, "Terminal locked. Access gate restricted to keycode.")
    }

    // --- Authentication Actions ---
    fun onDigitEntered(digit: String) {
        if (_authState.value != AuthState.LOCKED) return
        val current = _enteredPin.value
        if (current.length < 4) {
            val updated = current + digit
            _enteredPin.value = updated
            if (updated.length == 4) {
                verifyPin(updated)
            }
        }
    }

    fun onBackspace() {
        if (_authState.value != AuthState.LOCKED) return
        val current = _enteredPin.value
        if (current.isNotEmpty()) {
            _enteredPin.value = current.dropLast(1)
            _authMessage.value = "ENTER AUTHORIZATION CODE"
        }
    }

    fun onClearPin() {
        if (_authState.value != AuthState.LOCKED) return
        _enteredPin.value = ""
        _authMessage.value = "ENTER AUTHORIZATION CODE"
    }

    private fun verifyPin(code: String) {
        viewModelScope.launch {
            if (code == correctPasscode) {
                _authState.value = AuthState.AUTHENTICATING
                _authMessage.value = "DECRYPTING SECURITY KEY..."
                delay(600)
                _authMessage.value = "CLEARANCE CONFIRMED // ACCESS GRANTED"
                delay(700)
                _authState.value = AuthState.GRANTED
                _enteredPin.value = ""
                onTerminalUnlocked()
            } else {
                _authState.value = AuthState.DENIED
                _failedAttempts.value = _failedAttempts.value + 1
                _authMessage.value = "ACCESS DENIED // CODE INVALID"
                delay(1200)
                _enteredPin.value = ""
                _authState.value = AuthState.LOCKED
                _authMessage.value = "ENTER AUTHORIZATION CODE"
            }
        }
    }

    fun lockTerminal() {
        _authState.value = AuthState.LOCKED
        _enteredPin.value = ""
        _authMessage.value = "ENTER AUTHORIZATION CODE"
        stopHackerEngines()
        appendTerminalLine("[LOCKDOWN] OPERATOR LOCKED CONSOLE. RE-AUTHENTICATION REQUIRED.", isWarning = true)
        addSystemLog("CONSOLE", LogSeverity.WARN, "Manual lock initiated by operator.")
    }

    private fun onTerminalUnlocked() {
        appendTerminalLine("[AUTH_SUCCESS] CLEARANCE GRANTED: OPERATOR 0x9955", isSuccess = true)
        appendTerminalLine("[SYS_READY] INITIATING FULL SPECTRUM CYBER MATRIX...", isHighlight = true)
        startHackerEngines()
    }

    // --- Continuous Hacker Simulation Engines ---
    private fun startHackerEngines() {
        startTerminalStream()
        startTelemetryStream()
        startAttackArcStream()
        startLogsStream()
    }

    private fun stopHackerEngines() {
        terminalJob?.cancel()
        telemetryJob?.cancel()
        attackArcJob?.cancel()
        logsJob?.cancel()
    }

    private val hackerCommandsPool = listOf(
        "SCANNING SUBNET 192.168.4.0/24... 254 NODES FOUND",
        "PORT SCAN 192.168.4.102: [21:FTP, 22:SSH, 80:HTTP, 443:HTTPS, 445:SMB, 3389:RDP]",
        "PROBING SMB SERVICE (MS17-010 ETERNALBLUE VULNERABILITY DETECTED)",
        "DEPLOYING EXPLOIT PAYLOAD: staged_meterpreter_x64.bin (104 KB)",
        "EXPLOIT FIRED -> TARGET 192.168.4.102:445 [RETURN CODE: 0x00000000 SUCCESS]",
        "METERPRETER SESSION 1 OPENED (192.168.4.102:4444 <- 192.168.4.1:58491)",
        "PRIVILEGE ESCALATION: injecting thread into lsass.exe (PID 648)",
        "NTLM HASH DUMPED: Administrator:500:aad3b435b51404eeaad3b435b51404ee:31d6cfe0d16ae931b73c59d7e0c089c0:::",
        "QUANTUM CIPHER DECODER: spinning up 4096-bit prime sieve matrix...",
        "DECRYPTING HASH -> PLAINTEXT KEY: 'alpha_omega_9955_root'",
        "PIVOTING TO SUBNET 10.0.0.0/8 VIA COMPROMISED DC (CORP-DC-01)",
        "BYPASSING FIREWALL RULE #4489: IPTABLES ACCEPT tcp/udp 1337-9955",
        "SATCOM UPLINK SYNCHED: MILSTAR-4 (AZ: 218.4°, EL: 42.1°, FREQ: 44.5 GHz)",
        "DOWNLINK TELEMETRY: 4.82 Gbps encrypted military packet stream captured",
        "SPOOFING BGP ROUTE: AS15169 -> ADVERTISING PREFIX 198.51.100.0/24",
        "ARP CACHE POISONING ON GATEWAY 192.168.4.1 (MAC: 00:50:56:C0:00:08)",
        "INJECTING PACKET SEQUENCE: 0x8849FA20 0x11223344 [RAW_SOCKET]",
        "EVADING EDR HOOKS: unhooking ntdll.dll syscalls (NtAllocateVirtualMemory)",
        "ZERO-DAY HEAP SPRAY: filling heap segments 0x0c0c0c0c with NOP sleds",
        "ROOTKIT INSTALLED: kernel driver /dev/cyber_hook loaded stealth mode",
        "DNS EXFILTRATION: encoding data chunks to *.corp.internal-dns.org",
        "OVERCLOCKING CIPHER CORES: GPU cluster temperature 68°C [LOAD: 98%]",
        "TARGET MAINFRAME ACCESSED: /etc/shadow, /var/log/audit purged",
        "PROXY HOP CHAIN: [FRANKFURT -> TOKYO -> LONDON -> SF -> MUMBAI]"
    )

    private fun startTerminalStream() {
        terminalJob?.cancel()
        terminalJob = viewModelScope.launch {
            var poolIndex = 0
            while (isActive) {
                if (_isStreaming.value) {
                    val cmd = hackerCommandsPool[poolIndex % hackerCommandsPool.size]
                    poolIndex++

                    val isHighlight = cmd.contains("ROOT") || cmd.contains("SUCCESS") || cmd.contains("DECRYPTING")
                    val isWarning = cmd.contains("SPOOFING") || cmd.contains("POISONING") || cmd.contains("EXPLOIT")
                    appendTerminalLine("[CMD] $cmd", isHighlight = isHighlight, isWarning = isWarning)

                    val baseDelay = when (_streamSpeed.value) {
                        2 -> 500L
                        5 -> 200L
                        else -> 1100L
                    }
                    delay(baseDelay + Random.nextLong(100, 300))
                } else {
                    delay(500)
                }
            }
        }
    }

    private fun startTelemetryStream() {
        telemetryJob?.cancel()
        telemetryJob = viewModelScope.launch {
            while (isActive) {
                val last = _telemetryHistory.value.lastOrNull() ?: TelemetryPoint(
                    System.currentTimeMillis(), 4.5f, 50f, 65f, 75f
                )

                // Smooth jitter
                val newBw = (last.bandwidth + (Random.nextFloat() - 0.48f) * 1.5f).coerceIn(1.2f, 9.8f)
                val newThreat = (last.threatLevel + (Random.nextFloat() - 0.49f) * 12f).coerceIn(15f, 98f)
                val newCpu = (last.cpuLoad + (Random.nextFloat() - 0.49f) * 8f).coerceIn(30f, 99f)
                val newPackets = (last.packetRate + (Random.nextFloat() - 0.48f) * 14f).coerceIn(20f, 115f)

                val newPoint = TelemetryPoint(
                    timestamp = System.currentTimeMillis(),
                    bandwidth = newBw,
                    threatLevel = newThreat,
                    cpuLoad = newCpu,
                    packetRate = newPackets
                )

                val updated = (_telemetryHistory.value + newPoint).takeLast(40)
                _telemetryHistory.value = updated

                delay(600)
            }
        }
    }

    private fun startAttackArcStream() {
        attackArcJob?.cancel()
        attackArcJob = viewModelScope.launch {
            var arcId = 1L
            while (isActive) {
                // Advance existing arcs
                val current = _activeAttackArcs.value.map { arc ->
                    arc.copy(progress = arc.progress + 0.08f)
                }.filter { it.progress < 1.0f }

                // Add a new arc periodically
                val updatedArcs = current.toMutableList()
                if (updatedArcs.size < 4 && Random.nextFloat() > 0.35f) {
                    val src = worldNodes.random()
                    var tgt = worldNodes.random()
                    while (tgt == src) {
                        tgt = worldNodes.random()
                    }
                    val protocols = listOf("SSH-EXPLOIT", "SYN-FLOOD", "BGP-SPOOF", "RCE-0x9955", "ZERO-DAY", "SAT-BEAM")
                    val payloads = listOf("STAGED_X64", "METERPRETER", "CIPHER_SEED", "KERNEL_SHELL", "RAW_ETH")
                    updatedArcs.add(
                        CyberAttackArc(
                            id = arcId++,
                            sourceNode = src,
                            targetNode = tgt,
                            protocol = protocols.random(),
                            payload = payloads.random(),
                            progress = 0f
                        )
                    )
                }
                _activeAttackArcs.value = updatedArcs
                delay(120)
            }
        }
    }

    private fun startLogsStream() {
        logsJob?.cancel()
        logsJob = viewModelScope.launch {
            val subsystems = listOf("FIREWALL", "KERNEL", "QUANTUM", "NEURAL", "SAT_LINK", "PROXY", "CRYPTO")
            val messages = listOf(
                Pair(LogSeverity.CRIT, "Unauthorized buffer overflow detected in memory segment 0x7FFF"),
                Pair(LogSeverity.WARN, "Rate limit threshold exceeded on node 185.220.101.4"),
                Pair(LogSeverity.INFO, "Synchronizing orbital telemetry ephemeris with SAT-4"),
                Pair(LogSeverity.TRACE, "Encrypted heartbeat received from relay [LATENCY: 9ms]"),
                Pair(LogSeverity.CRIT, "Signature match: CVE-2026-9955 privilege elevation attempt"),
                Pair(LogSeverity.INFO, "Key rotation completed for session cipher 0x9955-ALPHA"),
                Pair(LogSeverity.WARN, "High packet loss detected on Frankfurt subsea backbone"),
                Pair(LogSeverity.TRACE, "ARP table broadcast refreshed: 254 entries verified"),
                Pair(LogSeverity.INFO, "Quantum entropy pool restocked: 8192 bits generated")
            )

            while (isActive) {
                if (!_isLogsPaused.value) {
                    val sub = subsystems.random()
                    val msgPair = messages.random()
                    addSystemLog(sub, msgPair.first, msgPair.second)
                }
                delay(Random.nextLong(1200, 2800))
            }
        }
    }

    fun toggleStreaming() {
        _isStreaming.value = !_isStreaming.value
    }

    fun cycleSpeed() {
        _streamSpeed.value = when (_streamSpeed.value) {
            1 -> 2
            2 -> 5
            else -> 1
        }
    }

    fun clearTerminal() {
        _terminalLines.value = emptyList()
        appendTerminalLine("[TERMINAL CLEARED] CONSOLE BUFFER RESET", isHighlight = true)
    }

    fun onCommandInputChange(input: String) {
        _commandInput.value = input
    }

    fun executeCustomCommand(cmd: String = _commandInput.value) {
        val trimmed = cmd.trim()
        if (trimmed.isEmpty()) return

        appendTerminalLine("> $trimmed", isPrompt = true)
        _commandInput.value = ""

        val parts = trimmed.split(" ")
        val mainCmd = parts[0].lowercase()

        when (mainCmd) {
            "help" -> {
                appendTerminalLine("CYBER//TERMINAL AVAILABLE COMMANDS:", isHighlight = true)
                appendTerminalLine("  help          - Show this command reference")
                appendTerminalLine("  scan [ip]     - Execute port scanner & OS fingerprint")
                appendTerminalLine("  breach [tgt]  - Deploy automated exploit payload")
                appendTerminalLine("  trace [node]  - Trace packet routes across global nodes")
                appendTerminalLine("  decrypt       - Run quantum brute-force key decipher")
                appendTerminalLine("  matrix        - Activate matrix neural visual rain")
                appendTerminalLine("  status        - Inspect operator clearance and telemetry")
                appendTerminalLine("  ping [target] - Ping remote cyber node")
                appendTerminalLine("  ddos [ip]     - Simulated distributed traffic flood")
                appendTerminalLine("  clear         - Clear console screen buffer")
                appendTerminalLine("  lock          - Lock console and return to PIN screen")
            }
            "scan" -> {
                val target = parts.getOrNull(1) ?: "192.168.4.102"
                appendTerminalLine("[SCAN] Target: $target ... SCANNING PORTS 1-65535", isHighlight = true)
                appendTerminalLine("[+] PORT 22/tcp  OPEN  OpenSSH 9.2 (Vulnerable)")
                appendTerminalLine("[+] PORT 443/tcp OPEN  TLS 1.3 (Nginx 1.24)")
                appendTerminalLine("[+] PORT 445/tcp OPEN  Microsoft-DS (MS17-010 vulnerable!)", isWarning = true)
                appendTerminalLine("[+] PORT 9955/tcp OPEN BACKDOOR_OPERATOR [AUTHORIZED]", isSuccess = true)
            }
            "breach" -> {
                appendTerminalLine("[BREACH] DEPLOYING ZERO-DAY INJECTION 0x9955...", isWarning = true)
                appendTerminalLine("[*] Memory heap spray verified at 0x0c0c0c0c")
                appendTerminalLine("[+] ROOT ACCESS OBTAINED: uid=0(root) gid=0(root)", isSuccess = true)
            }
            "decrypt" -> {
                appendTerminalLine("[CRYPTO] QUANTUM CORE ACTIVATED: 4096-BIT RSA CRACK", isHighlight = true)
                appendTerminalLine("[*] Searching prime factor lattice...")
                appendTerminalLine("[+] CIPHER SOLVED: MASTER KEY = 'CYBER_CORE_9955_CLEARANCE'", isSuccess = true)
            }
            "trace" -> {
                val nodeName = parts.getOrNull(1) ?: "TOKYO"
                appendTerminalLine("[TRACE] TRACING ROUTE TO $nodeName...", isHighlight = true)
                appendTerminalLine("  1  192.168.1.1 (GW) 1.2ms")
                appendTerminalLine("  2  10.24.0.1 (ISP-BACKBONE) 4.5ms")
                appendTerminalLine("  3  185.220.101.4 (FRANKFURT) 9.1ms")
                appendTerminalLine("  4  133.242.18.99 ($nodeName CORE) 14.2ms [TARGET REACHED]", isSuccess = true)
            }
            "ping" -> {
                val tgt = parts.getOrNull(1) ?: "8.8.8.8"
                appendTerminalLine("[PING] Sending 64 bytes to $tgt: icmp_seq=1 ttl=56 time=9.4 ms", isSuccess = true)
                appendTerminalLine("[PING] Sending 64 bytes to $tgt: icmp_seq=2 ttl=56 time=8.8 ms", isSuccess = true)
            }
            "ddos" -> {
                val tgt = parts.getOrNull(1) ?: "192.168.4.1"
                appendTerminalLine("[DDOS] LAUNCHING SYN FLOOD BOTNET AGAINST $tgt", isWarning = true)
                appendTerminalLine("[+] 48,200 bot agents connected. Outbound: 8.92 Gbps", isSuccess = true)
            }
            "status" -> {
                appendTerminalLine("[STATUS] SYSTEM: ONLINE // NODE_ID: 0x9955", isHighlight = true)
                appendTerminalLine("  Clearance: LEVEL 5 (GOD_MODE)")
                appendTerminalLine("  Active Nodes: ${worldNodes.size}")
                appendTerminalLine("  Bandwidth: ${String.format(Locale.US, "%.2f", _telemetryHistory.value.lastOrNull()?.bandwidth ?: 5f)} Gbps")
                appendTerminalLine("  Threat Level: ${String.format(Locale.US, "%.1f", _telemetryHistory.value.lastOrNull()?.threatLevel ?: 50f)}%")
            }
            "matrix" -> {
                appendTerminalLine("01000011 01011001 01000010 01000101 01010010", isHighlight = true)
                appendTerminalLine("01010100 01000101 01010010 01001101 00111001", isHighlight = true)
                appendTerminalLine("00111001 00110101 00110101 00100000 01001111", isHighlight = true)
            }
            "clear" -> {
                clearTerminal()
            }
            "lock" -> {
                lockTerminal()
            }
            else -> {
                appendTerminalLine("[UNKNOWN COMMAND] '$trimmed'. Type 'help' for available commands.", isWarning = true)
            }
        }
    }

    private fun appendTerminalLine(
        text: String,
        isPrompt: Boolean = false,
        isSuccess: Boolean = false,
        isWarning: Boolean = false,
        isError: Boolean = false,
        isHighlight: Boolean = false
    ) {
        val line = TerminalLine(
            id = lineCounter++,
            text = text,
            isPrompt = isPrompt,
            isSuccess = isSuccess,
            isWarning = isWarning,
            isError = isError,
            isHighlight = isHighlight
        )
        val current = _terminalLines.value
        _terminalLines.value = (current + line).takeLast(250) // keep last 250 lines
    }

    // --- Metric Selection ---
    fun selectMetric(metric: ChartMetric) {
        _selectedMetric.value = metric
    }

    // --- Node Selection & Tracking ---
    fun selectNode(node: CyberNode) {
        _selectedNode.value = node
        appendTerminalLine("[TRACKER] FOCUSED ON NODE ${node.name} (${node.ip})", isHighlight = true)
        addSystemLog("TRACKER", LogSeverity.INFO, "Focus switched to node ${node.id} [${node.ip}]")
    }

    fun triggerNodeAttack(node: CyberNode) {
        val src = worldNodes.filter { it != node }.random()
        val arc = CyberAttackArc(
            id = System.currentTimeMillis(),
            sourceNode = src,
            targetNode = node,
            protocol = "EXPLOIT-0x9955",
            payload = "PAYLOAD_STAGED",
            progress = 0f
        )
        _activeAttackArcs.value = _activeAttackArcs.value + arc
        appendTerminalLine("[ATTACK] TARGET LOCKED: ${node.name} [IP: ${node.ip}]. INJECTING VECTOR...", isWarning = true)
        addSystemLog("ATTACK", LogSeverity.CRIT, "Vector injected into ${node.name} via ${src.name}")
    }

    // --- System Logs ---
    private fun addSystemLog(subsystem: String, severity: LogSeverity, message: String) {
        val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
        val log = SystemLog(
            id = logCounter++,
            timestamp = timeFormat.format(Date()),
            subsystem = subsystem,
            severity = severity,
            message = message
        )
        _systemLogs.value = (_systemLogs.value + log).takeLast(200)
    }

    fun setLogFilter(severity: LogSeverity?) {
        _logFilter.value = severity
    }

    fun setLogSearchQuery(query: String) {
        _logSearchQuery.value = query
    }

    fun togglePauseLogs() {
        _isLogsPaused.value = !_isLogsPaused.value
    }

    fun clearLogs() {
        _systemLogs.value = emptyList()
        addSystemLog("AUDIT", LogSeverity.INFO, "Log buffer purged by operator.")
    }
}
