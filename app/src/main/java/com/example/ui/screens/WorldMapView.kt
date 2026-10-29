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
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CyberNode
import com.example.ui.components.triggerCyberHaptic
import com.example.ui.theme.CyberBackgroundDark
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberBorderGlow
import com.example.ui.theme.CyberBorderSubtle
import com.example.ui.theme.CyberFontFamily
import com.example.ui.theme.CyberRed
import com.example.ui.theme.CyberRedBright
import com.example.ui.theme.CyberRedDark
import com.example.ui.theme.CyberRedGlow
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.CyberTextDim
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.CyberWarning
import com.example.viewmodel.CyberViewModel
import kotlin.math.hypot

// Helper to convert lat/lon to canvas coordinates
private fun projectCoordinates(lat: Float, lon: Float, w: Float, h: Float): Offset {
    val x = ((lon + 180f) / 360f) * w
    // Invert lat for canvas (North is top = smaller Y)
    val y = ((90f - lat) / 180f) * h
    return Offset(x, y)
}

@Composable
fun WorldMapView(
    viewModel: CyberViewModel,
    modifier: Modifier = Modifier
) {
    val worldNodes = viewModel.worldNodes
    val selectedNode by viewModel.selectedNode.collectAsState()
    val activeAttackArcs by viewModel.activeAttackArcs.collectAsState()
    val context = LocalContext.current

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseSize by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )

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
                    text = "WORLD MAP // GLOBAL ATTACK RADAR",
                    fontFamily = CyberFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberRedBright
                )
            }

            Text(
                text = "${activeAttackArcs.size} ACTIVE VECTORS",
                fontFamily = CyberFontFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CyberRed
            )
        }

        // Live Tracking Telemetry Strip
        val activeArc = activeAttackArcs.lastOrNull()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberSurface)
                .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (activeArc != null) {
                    "TRACKING: ${activeArc.sourceNode.name} -> ${activeArc.targetNode.name} [${activeArc.protocol}]"
                } else {
                    "TRACKING: GLOBAL LISTENING POSTS SYNCHRONIZED [IDLE]"
                },
                fontFamily = CyberFontFamily,
                fontSize = 10.sp,
                color = if (activeArc != null) CyberRedBright else CyberTextDim,
                letterSpacing = 0.5.sp
            )
        }

        // Interactive World Map Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(worldNodes) {
                        detectTapGestures { tapOffset ->
                            val w = size.width.toFloat()
                            val h = size.height.toFloat()

                            // Find nearest node
                            var closest: CyberNode? = null
                            var minDistance = 60.dp.toPx()

                            for (node in worldNodes) {
                                val nodePos = projectCoordinates(node.lat, node.lon, w, h)
                                val dist = hypot(tapOffset.x - nodePos.x, tapOffset.y - nodePos.y)
                                if (dist < minDistance) {
                                    minDistance = dist
                                    closest = node
                                }
                            }

                            if (closest != null) {
                                triggerCyberHaptic(context, 25)
                                viewModel.selectNode(closest)
                            }
                        }
                    }
            ) {
                val w = size.width
                val h = size.height

                // Draw Grid Lat/Lon Lines
                val gridX = 8
                val gridY = 6
                for (i in 0..gridX) {
                    val x = (w / gridX) * i
                    drawLine(
                        color = CyberBorderSubtle,
                        start = Offset(x, 0f),
                        end = Offset(x, h),
                        strokeWidth = 0.8f
                    )
                }
                for (i in 0..gridY) {
                    val y = (h / gridY) * i
                    drawLine(
                        color = CyberBorderSubtle,
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 0.8f
                    )
                }

                // Draw Stylized Cyber Landmass Clusters (Continents)
                val landmassColor = CyberRedDark.copy(alpha = 0.35f)

                // North America
                val naPath = Path().apply {
                    val p1 = projectCoordinates(60f, -140f, w, h)
                    val p2 = projectCoordinates(65f, -60f, w, h)
                    val p3 = projectCoordinates(45f, -65f, w, h)
                    val p4 = projectCoordinates(25f, -80f, w, h)
                    val p5 = projectCoordinates(15f, -90f, w, h)
                    val p6 = projectCoordinates(30f, -120f, w, h)
                    moveTo(p1.x, p1.y)
                    lineTo(p2.x, p2.y)
                    lineTo(p3.x, p3.y)
                    lineTo(p4.x, p4.y)
                    lineTo(p5.x, p5.y)
                    lineTo(p6.x, p6.y)
                    close()
                }
                drawPath(naPath, landmassColor)

                // South America
                val saPath = Path().apply {
                    val p1 = projectCoordinates(10f, -75f, w, h)
                    val p2 = projectCoordinates(-5f, -35f, w, h)
                    val p3 = projectCoordinates(-25f, -40f, w, h)
                    val p4 = projectCoordinates(-55f, -70f, w, h)
                    val p5 = projectCoordinates(-20f, -70f, w, h)
                    moveTo(p1.x, p1.y)
                    lineTo(p2.x, p2.y)
                    lineTo(p3.x, p3.y)
                    lineTo(p4.x, p4.y)
                    lineTo(p5.x, p5.y)
                    close()
                }
                drawPath(saPath, landmassColor)

                // Europe
                val euPath = Path().apply {
                    val p1 = projectCoordinates(70f, 10f, w, h)
                    val p2 = projectCoordinates(65f, 40f, w, h)
                    val p3 = projectCoordinates(45f, 35f, w, h)
                    val p4 = projectCoordinates(36f, -5f, w, h)
                    val p5 = projectCoordinates(50f, 0f, w, h)
                    moveTo(p1.x, p1.y)
                    lineTo(p2.x, p2.y)
                    lineTo(p3.x, p3.y)
                    lineTo(p4.x, p4.y)
                    lineTo(p5.x, p5.y)
                    close()
                }
                drawPath(euPath, landmassColor)

                // Africa
                val afPath = Path().apply {
                    val p1 = projectCoordinates(35f, -5f, w, h)
                    val p2 = projectCoordinates(30f, 35f, w, h)
                    val p3 = projectCoordinates(10f, 50f, w, h)
                    val p4 = projectCoordinates(-35f, 20f, w, h)
                    val p5 = projectCoordinates(5f, 10f, w, h)
                    moveTo(p1.x, p1.y)
                    lineTo(p2.x, p2.y)
                    lineTo(p3.x, p3.y)
                    lineTo(p4.x, p4.y)
                    lineTo(p5.x, p5.y)
                    close()
                }
                drawPath(afPath, landmassColor)

                // Asia
                val asPath = Path().apply {
                    val p1 = projectCoordinates(70f, 60f, w, h)
                    val p2 = projectCoordinates(65f, 170f, w, h)
                    val p3 = projectCoordinates(40f, 140f, w, h)
                    val p4 = projectCoordinates(10f, 105f, w, h)
                    val p5 = projectCoordinates(25f, 70f, w, h)
                    val p6 = projectCoordinates(45f, 50f, w, h)
                    moveTo(p1.x, p1.y)
                    lineTo(p2.x, p2.y)
                    lineTo(p3.x, p3.y)
                    lineTo(p4.x, p4.y)
                    lineTo(p5.x, p5.y)
                    lineTo(p6.x, p6.y)
                    close()
                }
                drawPath(asPath, landmassColor)

                // Australia
                val auPath = Path().apply {
                    val p1 = projectCoordinates(-15f, 125f, w, h)
                    val p2 = projectCoordinates(-15f, 150f, w, h)
                    val p3 = projectCoordinates(-35f, 150f, w, h)
                    val p4 = projectCoordinates(-35f, 115f, w, h)
                    moveTo(p1.x, p1.y)
                    lineTo(p2.x, p2.y)
                    lineTo(p3.x, p3.y)
                    lineTo(p4.x, p4.y)
                    close()
                }
                drawPath(auPath, landmassColor)

                // Draw Attack Trajectories / Curved Arcs
                for (arc in activeAttackArcs) {
                    val pSrc = projectCoordinates(arc.sourceNode.lat, arc.sourceNode.lon, w, h)
                    val pTgt = projectCoordinates(arc.targetNode.lat, arc.targetNode.lon, w, h)

                    // Bezier curve with arched control point
                    val midX = (pSrc.x + pTgt.x) / 2
                    val midY = (pSrc.y + pTgt.y) / 2 - 35.dp.toPx()

                    val arcPath = Path().apply {
                        moveTo(pSrc.x, pSrc.y)
                        quadraticTo(midX, midY, pTgt.x, pTgt.y)
                    }

                    // Background arc trace
                    drawPath(
                        path = arcPath,
                        color = CyberRed.copy(alpha = 0.3f),
                        style = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Moving Pulse Particle along quadratic bezier curve: B(t) = (1-t)^2 P0 + 2(1-t)t P1 + t^2 P2
                    val t = arc.progress
                    val oneMinusT = 1f - t
                    val curX = (oneMinusT * oneMinusT * pSrc.x) + (2 * oneMinusT * t * midX) + (t * t * pTgt.x)
                    val curY = (oneMinusT * oneMinusT * pSrc.y) + (2 * oneMinusT * t * midY) + (t * t * pTgt.y)

                    drawCircle(
                        color = CyberRedBright,
                        radius = 4.dp.toPx(),
                        center = Offset(curX, curY)
                    )
                    drawCircle(
                        color = CyberRedGlow.copy(alpha = 0.5f),
                        radius = 8.dp.toPx(),
                        center = Offset(curX, curY)
                    )
                }

                // Draw Cyber Nodes
                for (node in worldNodes) {
                    val pos = projectCoordinates(node.lat, node.lon, w, h)
                    val isSelected = node == selectedNode

                    // Concentric radar ring if selected or target
                    if (isSelected) {
                        drawCircle(
                            color = CyberRedBright.copy(alpha = 0.4f),
                            radius = pulseSize.dp.toPx(),
                            center = pos,
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                    }

                    // Outer node dot
                    val nodeColor = when (node.status) {
                        "BREACHED" -> CyberRedBright
                        "TARGET" -> CyberWarning
                        "COMPROMISED" -> CyberRed
                        else -> CyberRed
                    }

                    drawCircle(
                        color = nodeColor,
                        radius = if (isSelected) 5.dp.toPx() else 3.5.dp.toPx(),
                        center = pos
                    )

                    // Inner core
                    drawCircle(
                        color = Color.White,
                        radius = 1.5.dp.toPx(),
                        center = pos
                    )
                }
            }
        }

        // Selected Node Inspection Card
        if (selectedNode != null) {
            val node = selectedNode!!
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberSurface)
                    .border(0.5.dp, CyberBorder)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = node.name,
                            fontFamily = CyberFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberRedBright
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(2.dp))
                                .background(CyberRedDark)
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = node.status,
                                fontFamily = CyberFontFamily,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberRedBright
                            )
                        }
                    }
                    Text(
                        text = "IP: ${node.ip} // LATENCY: ${node.latencyMs}ms // LOAD: ${node.loadPercent}%",
                        fontFamily = CyberFontFamily,
                        fontSize = 10.sp,
                        color = CyberTextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(CutCornerShape(4.dp))
                        .background(CyberRedDark)
                        .border(1.dp, CyberBorderGlow, CutCornerShape(4.dp))
                        .clickable {
                            triggerCyberHaptic(context, 35, isWarning = true)
                            viewModel.triggerNodeAttack(node)
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "[ ATTACK ]",
                        fontFamily = CyberFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberRedBright
                    )
                }
            }
        }

        // Global Node Selector Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberSurfaceVariant)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (node in worldNodes) {
                val isSelected = node == selectedNode
                Box(
                    modifier = Modifier
                        .clip(CutCornerShape(4.dp))
                        .background(if (isSelected) CyberRedDark else CyberSurface)
                        .border(1.dp, if (isSelected) CyberBorderGlow else CyberBorder, CutCornerShape(4.dp))
                        .clickable {
                            triggerCyberHaptic(context, 15)
                            viewModel.selectNode(node)
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = node.region,
                        fontFamily = CyberFontFamily,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) CyberRedBright else CyberTextSecondary
                    )
                }
            }
        }
    }
}
