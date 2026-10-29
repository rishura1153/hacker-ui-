package com.example.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberBackgroundDark
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberBorderGlow
import com.example.ui.theme.CyberFontFamily
import com.example.ui.theme.CyberRed
import com.example.ui.theme.CyberRedBright
import com.example.ui.theme.CyberRedDark
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary

fun triggerCyberHaptic(context: Context, durationMs: Long = 20, isWarning: Boolean = false) {
    try {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val amplitude = if (isWarning) 255 else 120
                vibrator.vibrate(VibrationEffect.createOneShot(durationMs, amplitude))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(durationMs)
            }
        }
    } catch (_: Exception) {}
}

@Composable
fun CyberBox(
    modifier: Modifier = Modifier,
    title: String? = null,
    titleBadge: String? = null,
    borderColor: Color = CyberBorder,
    borderGlow: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(12.dp),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(
                Brush.verticalGradient(
                    listOf(CyberSurface, CyberBackgroundDark)
                )
            )
            .border(
                width = if (borderGlow) 1.5.dp else 1.dp,
                color = if (borderGlow) CyberBorderGlow else borderColor,
                shape = RoundedCornerShape(4.dp)
            )
    ) {
        // Corner HUD Reticles
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cornerLen = 14.dp.toPx()
            val strokeW = 2.dp.toPx()
            val glowColor = if (borderGlow) CyberRedBright else CyberRed

            // Top-Left
            drawLine(glowColor, Offset(0f, 0f), Offset(cornerLen, 0f), strokeW)
            drawLine(glowColor, Offset(0f, 0f), Offset(0f, cornerLen), strokeW)

            // Top-Right
            drawLine(glowColor, Offset(size.width, 0f), Offset(size.width - cornerLen, 0f), strokeW)
            drawLine(glowColor, Offset(size.width, 0f), Offset(size.width, cornerLen), strokeW)

            // Bottom-Left
            drawLine(glowColor, Offset(0f, size.height), Offset(cornerLen, size.height), strokeW)
            drawLine(glowColor, Offset(0f, size.height), Offset(0f, size.height - cornerLen), strokeW)

            // Bottom-Right
            drawLine(glowColor, Offset(size.width, size.height), Offset(size.width - cornerLen, size.height), strokeW)
            drawLine(glowColor, Offset(size.width, size.height), Offset(size.width, size.height - cornerLen), strokeW)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            content()
        }
    }
}

@Composable
fun CyberScanlines(modifier: Modifier = Modifier, spacingDp: Dp = 4.dp, alpha: Float = 0.07f) {
    Canvas(modifier = modifier) {
        val spacing = spacingDp.toPx()
        val lineCount = (size.height / spacing).toInt()
        val scanlineColor = CyberRed.copy(alpha = alpha)

        for (i in 0..lineCount) {
            val y = i * spacing
            drawLine(
                color = scanlineColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
        }
    }
}

@Composable
fun CyberButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false,
    isDestructive: Boolean = false,
    enabled: Boolean = true
) {
    val context = LocalContext.current
    val bg = when {
        !enabled -> CyberSurface
        isPrimary -> CyberRedDark
        isDestructive -> CyberRedDark.copy(alpha = 0.8f)
        else -> CyberSurface
    }
    val border = when {
        !enabled -> CyberBorder.copy(alpha = 0.4f)
        isPrimary -> CyberRedBright
        isDestructive -> CyberRed
        else -> CyberBorder
    }
    val textColor = when {
        !enabled -> CyberTextSecondary.copy(alpha = 0.4f)
        isPrimary -> CyberRedBright
        else -> CyberTextPrimary
    }

    Box(
        modifier = modifier
            .clip(CutCornerShape(topStart = 6.dp, bottomEnd = 6.dp))
            .background(bg)
            .border(1.dp, border, CutCornerShape(topStart = 6.dp, bottomEnd = 6.dp))
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = CyberRedBright)
            ) {
                triggerCyberHaptic(context, 25, isWarning = isDestructive)
                onClick()
            }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "[ $text ]",
            fontFamily = CyberFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = textColor,
            letterSpacing = 0.8.sp
        )
    }
}

@Composable
fun CyberBadge(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = CyberRed,
    pulsing: Boolean = false
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(2.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            fontFamily = CyberFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            color = color,
            letterSpacing = 0.5.sp
        )
    }
}
