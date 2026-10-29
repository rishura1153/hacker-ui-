package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.triggerCyberHaptic
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberBorderGlow
import com.example.ui.theme.CyberFontFamily
import com.example.ui.theme.CyberRed
import com.example.ui.theme.CyberRedBright
import com.example.ui.theme.CyberRedDark
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.CyberTextDim
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.CyberWarning
import com.example.viewmodel.CyberViewModel
import java.util.Locale

enum class CyberTab(val label: String, val icon: ImageVector) {
    OVERVIEW("OVERVIEW", Icons.Default.Dashboard),
    TERMINAL("TERMINAL", Icons.Default.Terminal),
    MAP("WORLD MAP", Icons.Default.Public),
    CHART("TELEMETRY", Icons.Default.Analytics),
    LOGS("SYSTEM LOGS", Icons.Default.ListAlt)
}

@Composable
fun CyberDashboard(
    viewModel: CyberViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(CyberTab.OVERVIEW) }
    val context = LocalContext.current
    val telemetryHistory by viewModel.telemetryHistory.collectAsState()
    val currentThreat = telemetryHistory.lastOrNull()?.threatLevel ?: 65f

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top Global HUD Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberSurfaceVariant)
                .border(0.5.dp, CyberBorder)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(CyberRedBright)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "CYBER//CORE 0x9955",
                        fontFamily = CyberFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberRedBright
                    )
                }
                Text(
                    text = "OPERATOR: 0x9955 // THREAT: ${String.format(Locale.US, "%.1f", currentThreat)}%",
                    fontFamily = CyberFontFamily,
                    fontSize = 10.sp,
                    color = CyberTextSecondary
                )
            }

            // Lock Console Action Button
            Box(
                modifier = Modifier
                    .clip(CutCornerShape(4.dp))
                    .background(CyberRedDark)
                    .border(1.dp, CyberBorderGlow, CutCornerShape(4.dp))
                    .clickable {
                        triggerCyberHaptic(context, 40, isWarning = true)
                        viewModel.lockTerminal()
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("lock_console_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lock",
                        tint = CyberRedBright,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "LOCK",
                        fontFamily = CyberFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberRedBright
                    )
                }
            }
        }

        // Tab Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberSurface)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (tab in CyberTab.values()) {
                val isSelected = tab == selectedTab
                Box(
                    modifier = Modifier
                        .clip(CutCornerShape(4.dp))
                        .background(if (isSelected) CyberRedDark else CyberSurfaceVariant)
                        .border(
                            1.dp,
                            if (isSelected) CyberBorderGlow else CyberBorder,
                            CutCornerShape(4.dp)
                        )
                        .clickable {
                            triggerCyberHaptic(context, 15)
                            selectedTab = tab
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("tab_${tab.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                            tint = if (isSelected) CyberRedBright else CyberTextDim,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = tab.label,
                            fontFamily = CyberFontFamily,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) CyberRedBright else CyberTextSecondary
                        )
                    }
                }
            }
        }

        // Responsive Main Content Container
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(8.dp)
        ) {
            val isExpanded = maxWidth > 700.dp

            when (selectedTab) {
                CyberTab.OVERVIEW -> {
                    if (isExpanded) {
                        // Multi-Pane Tablet / Landscape Battle-Station
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Left Pane: Terminal (50% width)
                            TerminalView(
                                viewModel = viewModel,
                                modifier = Modifier
                                    .weight(1.1f)
                                    .fillMaxSize()
                            )

                            // Right Pane: Top World Map & Bottom Telemetry Chart & System Logs
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                WorldMapView(
                                    viewModel = viewModel,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                )

                                TelemetryChartView(
                                    viewModel = viewModel,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(0.9f)
                                )

                                SystemLogsView(
                                    viewModel = viewModel,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(130.dp),
                                    isCompact = true
                                )
                            }
                        }
                    } else {
                        // Compact Portrait Mobile: Scrollable HUD Dashboard with all widgets
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Live Red Streaming Terminal Card
                            TerminalView(
                                viewModel = viewModel,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp),
                                isCompact = true
                            )

                            // Live Telemetry Waveform Graphical Chart Card
                            TelemetryChartView(
                                viewModel = viewModel,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(230.dp)
                            )

                            // Live Cyber World Map Card
                            WorldMapView(
                                viewModel = viewModel,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp)
                            )

                            // Live System Audit Logs Card
                            SystemLogsView(
                                viewModel = viewModel,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                isCompact = true
                            )
                        }
                    }
                }

                CyberTab.TERMINAL -> {
                    TerminalView(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                CyberTab.MAP -> {
                    WorldMapView(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                CyberTab.CHART -> {
                    TelemetryChartView(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                CyberTab.LOGS -> {
                    SystemLogsView(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
