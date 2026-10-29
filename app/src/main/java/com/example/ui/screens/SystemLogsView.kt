package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LogSeverity
import com.example.model.SystemLog
import com.example.ui.components.CyberScanlines
import com.example.ui.components.triggerCyberHaptic
import com.example.ui.theme.CyberBackgroundDark
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

@Composable
fun SystemLogsView(
    viewModel: CyberViewModel,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    val logs by viewModel.systemLogs.collectAsState()
    val logFilter by viewModel.logFilter.collectAsState()
    val logSearchQuery by viewModel.logSearchQuery.collectAsState()
    val isPaused by viewModel.isLogsPaused.collectAsState()
    val listState = rememberLazyListState()
    val context = LocalContext.current

    // Filter logs
    val filteredLogs = logs.filter { log ->
        val matchesSeverity = logFilter == null || log.severity == logFilter
        val matchesSearch = logSearchQuery.isEmpty() ||
                log.message.contains(logSearchQuery, ignoreCase = true) ||
                log.subsystem.contains(logSearchQuery, ignoreCase = true)
        matchesSeverity && matchesSearch
    }

    // Auto-scroll when new log arrives and not paused
    LaunchedEffect(filteredLogs.size) {
        if (filteredLogs.isNotEmpty() && !isPaused) {
            listState.animateScrollToItem(filteredLogs.size - 1)
        }
    }

    Column(
        modifier = modifier
            .clip(CutCornerShape(topStart = 8.dp, bottomEnd = 8.dp))
            .background(CyberBackgroundDark)
            .border(1.dp, CyberBorder, CutCornerShape(topStart = 8.dp, bottomEnd = 8.dp))
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberSurfaceVariant)
                .border(0.5.dp, CyberBorder)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isPaused) CyberWarning else CyberRedBright)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SYSTEM AUDIT LOGS [${filteredLogs.size}]",
                    fontFamily = CyberFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberRedBright
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        triggerCyberHaptic(context, 15)
                        viewModel.togglePauseLogs()
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = if (isPaused) "Resume" else "Pause",
                        tint = CyberRedBright,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = {
                        triggerCyberHaptic(context, 20)
                        viewModel.clearLogs()
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear Logs",
                        tint = CyberTextDim,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Severity Filter Chips & Search Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberSurface)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Severity Chips
            FilterChip(
                label = "ALL",
                isSelected = logFilter == null,
                onClick = {
                    triggerCyberHaptic(context, 15)
                    viewModel.setLogFilter(null)
                }
            )
            FilterChip(
                label = "CRIT",
                isSelected = logFilter == LogSeverity.CRIT,
                onClick = {
                    triggerCyberHaptic(context, 15)
                    viewModel.setLogFilter(LogSeverity.CRIT)
                }
            )
            FilterChip(
                label = "WARN",
                isSelected = logFilter == LogSeverity.WARN,
                onClick = {
                    triggerCyberHaptic(context, 15)
                    viewModel.setLogFilter(LogSeverity.WARN)
                }
            )
            FilterChip(
                label = "INFO",
                isSelected = logFilter == LogSeverity.INFO,
                onClick = {
                    triggerCyberHaptic(context, 15)
                    viewModel.setLogFilter(LogSeverity.INFO)
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Search Bar Input
            Row(
                modifier = Modifier
                    .width(110.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(CyberSurfaceVariant)
                    .border(1.dp, CyberBorder, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = CyberTextDim,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                BasicTextField(
                    value = logSearchQuery,
                    onValueChange = { viewModel.setLogSearchQuery(it) },
                    textStyle = TextStyle(
                        fontFamily = CyberFontFamily,
                        fontSize = 10.sp,
                        color = CyberTextPrimary
                    ),
                    cursorBrush = SolidColor(CyberRedBright),
                    singleLine = true,
                    decorationBox = { inner ->
                        if (logSearchQuery.isEmpty()) {
                            Text(
                                text = "filter...",
                                fontFamily = CyberFontFamily,
                                fontSize = 10.sp,
                                color = CyberTextDim
                            )
                        }
                        inner()
                    }
                )
            }
        }

        // Logs Output List
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            CyberScanlines(modifier = Modifier.fillMaxSize(), spacingDp = 3.dp, alpha = 0.04f)

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(bottom = 6.dp)
            ) {
                items(filteredLogs, key = { it.id }) { log ->
                    SystemLogRow(log)
                }
            }
        }
    }
}

@Composable
private fun FilterChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(CutCornerShape(4.dp))
            .background(if (isSelected) CyberRedDark else CyberSurfaceVariant)
            .border(1.dp, if (isSelected) CyberBorderGlow else CyberBorder, CutCornerShape(4.dp))
            .clickable { onClick() }
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            fontFamily = CyberFontFamily,
            fontSize = 9.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) CyberRedBright else CyberTextSecondary
        )
    }
}

@Composable
private fun SystemLogRow(log: SystemLog) {
    val tagColor = when (log.severity) {
        LogSeverity.CRIT -> CyberRedBright
        LogSeverity.WARN -> CyberWarning
        LogSeverity.INFO -> CyberTextPrimary
        LogSeverity.TRACE -> CyberTextDim
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Timestamp
        Text(
            text = log.timestamp,
            fontFamily = CyberFontFamily,
            fontSize = 10.sp,
            color = CyberTextDim,
            letterSpacing = 0.2.sp
        )
        Spacer(modifier = Modifier.width(6.dp))

        // Severity Tag
        Text(
            text = "[${log.severity.name}]",
            fontFamily = CyberFontFamily,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = tagColor
        )
        Spacer(modifier = Modifier.width(6.dp))

        // Subsystem
        Text(
            text = "${log.subsystem}:",
            fontFamily = CyberFontFamily,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = CyberRed
        )
        Spacer(modifier = Modifier.width(6.dp))

        // Message
        Text(
            text = log.message,
            fontFamily = CyberFontFamily,
            fontSize = 11.sp,
            color = CyberTextPrimary,
            letterSpacing = 0.2.sp,
            modifier = Modifier.weight(1f)
        )
    }
}
