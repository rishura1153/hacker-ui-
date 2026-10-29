package com.example.model

enum class AuthState {
    LOCKED,
    AUTHENTICATING,
    GRANTED,
    DENIED
}

enum class LogSeverity {
    CRIT,
    WARN,
    INFO,
    TRACE
}

data class SystemLog(
    val id: Long,
    val timestamp: String,
    val subsystem: String,
    val severity: LogSeverity,
    val message: String
)

data class TerminalLine(
    val id: Long,
    val text: String,
    val isPrompt: Boolean = false,
    val isSuccess: Boolean = false,
    val isWarning: Boolean = false,
    val isError: Boolean = false,
    val isHighlight: Boolean = false
)

data class CyberNode(
    val id: String,
    val name: String,
    val region: String,
    val lat: Float, // -90 to +90
    val lon: Float, // -180 to +180
    val ip: String,
    val status: String,
    val latencyMs: Int,
    val loadPercent: Int
)

data class CyberAttackArc(
    val id: Long,
    val sourceNode: CyberNode,
    val targetNode: CyberNode,
    val protocol: String,
    val payload: String,
    val progress: Float = 0f // 0f to 1f
)

enum class ChartMetric(val label: String, val unit: String, val maxScale: Float) {
    BANDWIDTH("BANDWIDTH", "Gbps", 10f),
    THREAT_LEVEL("THREAT LEVEL", "%", 100f),
    CPU_CORE("CPU LOAD", "%", 100f),
    PACKET_RATE("PACKET I/O", "k/s", 120f)
}

data class TelemetryPoint(
    val timestamp: Long,
    val bandwidth: Float,
    val threatLevel: Float,
    val cpuLoad: Float,
    val packetRate: Float
)
