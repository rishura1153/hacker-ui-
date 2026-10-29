package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChartMetric
import com.example.ui.components.triggerCyberHaptic
import com.example.ui.theme.CyberBackgroundDark
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberBorderGlow
import com.example.ui.theme.CyberBorderSubtle
import com.example.ui.theme.CyberFontFamily
import com.example.ui.theme.CyberRed
import com.example.ui.theme.CyberRedBright
import com.example.ui.theme.CyberRedDark
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.CyberTextDim
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.viewmodel.CyberViewModel
import java.util.Locale

@Composable
fun TelemetryChartView(
    viewModel: CyberViewModel,
    modifier: Modifier = Modifier
) {
    val telemetryHistory by viewModel.telemetryHistory.collectAsState()
    val selectedMetric by viewModel.selectedMetric.collectAsState()
    val context = LocalContext.current

    val infiniteTransition = rememberInfiniteTransition(label = "scan")
    val scanPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scan"
    )

    // Extract values based on metric
    val values = telemetryHistory.map { point ->
        when (selectedMetric) {
            ChartMetric.BANDWIDTH -> point.bandwidth
            ChartMetric.THREAT_LEVEL -> point.threatLevel
            ChartMetric.CPU_CORE -> point.cpuLoad
            ChartMetric.PACKET_RATE -> point.packetRate
        }
    }

    val currentValue = values.lastOrNull() ?: 0f
    val maxValue = values.maxOrNull() ?: 1f
    val avgValue = if (values.isNotEmpty()) values.average().toFloat() else 0f

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
                        .background(CyberRedBright)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "TELEMETRY GRAPH // ${selectedMetric.label}",
                    fontFamily = CyberFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberRedBright
                )
            }

            Text(
                text = "VAL: ${String.format(Locale.US, "%.1f", currentValue)} ${selectedMetric.unit}",
                fontFamily = CyberFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = CyberRedBright
            )
        }

        // Metric Selector Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberSurface)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (metric in ChartMetric.values()) {
                val isSelected = metric == selectedMetric
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
                            viewModel.selectMetric(metric)
                        }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = metric.label,
                        fontFamily = CyberFontFamily,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) CyberRedBright else CyberTextSecondary
                    )
                }
            }
        }

        // Animated Canvas Waveform Chart
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Draw Background Grid
                val gridLinesX = 5
                val gridLinesY = 4

                for (i in 0..gridLinesX) {
                    val x = (w / gridLinesX) * i
                    drawLine(
                        color = CyberBorderSubtle,
                        start = Offset(x, 0f),
                        end = Offset(x, h),
                        strokeWidth = 1f
                    )
                }

                for (i in 0..gridLinesY) {
                    val y = (h / gridLinesY) * i
                    drawLine(
                        color = CyberBorderSubtle,
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1f
                    )
                }

                // Vertical Radar Scan Line
                val scanX = w * scanPhase
                drawLine(
                    color = CyberRedBright.copy(alpha = 0.35f),
                    start = Offset(scanX, 0f),
                    end = Offset(scanX, h),
                    strokeWidth = 2.dp.toPx()
                )

                if (values.size >= 2) {
                    val maxScale = selectedMetric.maxScale
                    val stepX = w / (values.size - 1)

                    val linePath = Path()
                    val fillPath = Path()

                    fillPath.moveTo(0f, h)

                    values.forEachIndexed { index, v ->
                        val norm = (v / maxScale).coerceIn(0.05f, 0.95f)
                        val x = index * stepX
                        val y = h - (norm * h)

                        if (index == 0) {
                            linePath.moveTo(x, y)
                            fillPath.lineTo(x, y)
                        } else {
                            val prevNorm = (values[index - 1] / maxScale).coerceIn(0.05f, 0.95f)
                            val prevX = (index - 1) * stepX
                            val prevY = h - (prevNorm * h)

                            val midX = (prevX + x) / 2
                            linePath.cubicTo(midX, prevY, midX, y, x, y)
                            fillPath.cubicTo(midX, prevY, midX, y, x, y)
                        }
                    }

                    fillPath.lineTo(w, h)
                    fillPath.close()

                    // Draw Glowing Area Gradient
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            listOf(
                                CyberRed.copy(alpha = 0.35f),
                                CyberRedDark.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    )

                    // Draw Primary Neon Waveform Line
                    drawPath(
                        path = linePath,
                        color = CyberRedBright,
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw Pulsing Circle on current latest value
                    val lastNorm = (currentValue / maxScale).coerceIn(0.05f, 0.95f)
                    val lastX = w
                    val lastY = h - (lastNorm * h)

                    drawCircle(
                        color = CyberRedBright.copy(alpha = 0.3f),
                        radius = 12.dp.toPx(),
                        center = Offset(lastX, lastY)
                    )
                    drawCircle(
                        color = CyberRedBright,
                        radius = 4.dp.toPx(),
                        center = Offset(lastX, lastY)
                    )
                }
            }
        }

        // Bottom KPI Summary Tiles
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberSurface)
                .border(0.5.dp, CyberBorder)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            KpiTile(
                label = "CURRENT",
                value = "${String.format(Locale.US, "%.1f", currentValue)} ${selectedMetric.unit}",
                color = CyberRedBright
            )
            KpiTile(
                label = "PEAK",
                value = "${String.format(Locale.US, "%.1f", maxValue)} ${selectedMetric.unit}",
                color = CyberTextPrimary
            )
            KpiTile(
                label = "AVERAGE",
                value = "${String.format(Locale.US, "%.1f", avgValue)} ${selectedMetric.unit}",
                color = CyberTextSecondary
            )
            KpiTile(
                label = "LATENCY",
                value = "9 ms",
                color = CyberRedBright
            )
        }
    }
}

@Composable
private fun KpiTile(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontFamily = CyberFontFamily,
            fontSize = 9.sp,
            color = CyberTextDim,
            letterSpacing = 0.5.sp
        )
        Text(
            text = value,
            fontFamily = CyberFontFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
