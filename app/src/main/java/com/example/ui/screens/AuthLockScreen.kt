package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AuthState
import com.example.ui.components.CyberScanlines
import com.example.ui.components.triggerCyberHaptic
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberBackgroundDark
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberBorderGlow
import com.example.ui.theme.CyberFontFamily
import com.example.ui.theme.CyberRed
import com.example.ui.theme.CyberRedBright
import com.example.ui.theme.CyberRedDark
import com.example.ui.theme.CyberRedDim
import com.example.ui.theme.CyberRedGlow
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.CyberTextDim
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.CyberWarning
import com.example.viewmodel.CyberViewModel
import kotlin.math.roundToInt

@Composable
fun AuthLockScreen(
    viewModel: CyberViewModel,
    modifier: Modifier = Modifier
) {
    val authState by viewModel.authState.collectAsState()
    val enteredPin by viewModel.enteredPin.collectAsState()
    val authMessage by viewModel.authMessage.collectAsState()
    val failedAttempts by viewModel.failedAttempts.collectAsState()
    val context = LocalContext.current

    // Shake animation for ACCESS DENIED
    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(authState) {
        if (authState == AuthState.DENIED) {
            triggerCyberHaptic(context, 100, isWarning = true)
            for (i in 0..4) {
                shakeOffset.animateTo(16f, tween(30))
                shakeOffset.animateTo(-16f, tween(30))
            }
            shakeOffset.animateTo(0f, tween(30))
        } else if (authState == AuthState.AUTHENTICATING) {
            triggerCyberHaptic(context, 40)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(CyberBackground, CyberBackgroundDark, Color(0xFF100204))
                )
            )
            .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
    ) {
        // Subtle cyber scanlines background
        CyberScanlines(modifier = Modifier.fillMaxSize(), spacingDp = 4.dp, alpha = 0.08f)

        // Corner decorative HUD brackets
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeW = 2.dp.toPx()
            val len = 24.dp.toPx()
            val glow = CyberRed.copy(alpha = 0.5f)
            // TL
            drawLine(glow, Offset(16.dp.toPx(), 16.dp.toPx()), Offset(16.dp.toPx() + len, 16.dp.toPx()), strokeW)
            drawLine(glow, Offset(16.dp.toPx(), 16.dp.toPx()), Offset(16.dp.toPx(), 16.dp.toPx() + len), strokeW)
            // TR
            drawLine(glow, Offset(size.width - 16.dp.toPx(), 16.dp.toPx()), Offset(size.width - 16.dp.toPx() - len, 16.dp.toPx()), strokeW)
            drawLine(glow, Offset(size.width - 16.dp.toPx(), 16.dp.toPx()), Offset(size.width - 16.dp.toPx(), 16.dp.toPx() + len), strokeW)
            // BL
            drawLine(glow, Offset(16.dp.toPx(), size.height - 16.dp.toPx()), Offset(16.dp.toPx() + len, size.height - 16.dp.toPx()), strokeW)
            drawLine(glow, Offset(16.dp.toPx(), size.height - 16.dp.toPx()), Offset(16.dp.toPx(), size.height - 16.dp.toPx() - len), strokeW)
            // BR
            drawLine(glow, Offset(size.width - 16.dp.toPx(), size.height - 16.dp.toPx()), Offset(size.width - 16.dp.toPx() - len, size.height - 16.dp.toPx()), strokeW)
            drawLine(glow, Offset(size.width - 16.dp.toPx(), size.height - 16.dp.toPx()), Offset(size.width - 16.dp.toPx(), size.height - 16.dp.toPx() - len), strokeW)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .clip(CutCornerShape(4.dp))
                        .background(CyberSurfaceVariant)
                        .border(1.dp, CyberBorder, CutCornerShape(4.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(CyberRedBright.copy(alpha = pulseAlpha))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SECURE TERMINAL // NODE 0x9955",
                        fontFamily = CyberFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberRedBright,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Big Icon / Logo
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CutCornerShape(12.dp))
                        .background(CyberRedDark.copy(alpha = 0.5f))
                        .border(2.dp, CyberRed, CutCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (authState == AuthState.AUTHENTICATING) Icons.Default.LockOpen else Icons.Default.Security,
                        contentDescription = "Security lock",
                        tint = if (authState == AuthState.AUTHENTICATING) CyberRedBright else CyberRed,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "CYBER CORE OS",
                    fontFamily = CyberFontFamily,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberRedBright,
                    letterSpacing = 2.sp
                )

                Text(
                    text = "MILITARY SPECIFICATION // LEVEL 5 ACCESS",
                    fontFamily = CyberFontFamily,
                    fontSize = 10.sp,
                    color = CyberTextDim,
                    letterSpacing = 1.sp
                )
            }

            // Middle Section: PIN boxes or ACCESS GRANTED sequence
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 380.dp)
            ) {
                if (authState == AuthState.AUTHENTICATING) {
                    // Dramatic "ACCESS GRANTED" screen
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 24.dp)
                    ) {
                        Text(
                            text = "ACCESS GRANTED",
                            fontFamily = CyberFontFamily,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberRedBright,
                            letterSpacing = 3.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag("access_granted_text")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "CLEARANCE VERIFIED: OPERATOR 0x9955",
                            fontFamily = CyberFontFamily,
                            fontSize = 12.sp,
                            color = CyberTextPrimary,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = CyberRedBright,
                            trackColor = CyberRedDark
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "DECRYPTING SYSTEM PROTOCOLS...",
                            fontFamily = CyberFontFamily,
                            fontSize = 11.sp,
                            color = CyberRedDim,
                            letterSpacing = 0.8.sp
                        )
                    }
                } else {
                    // Status banner
                    val bannerBg = when (authState) {
                        AuthState.DENIED -> CyberRedDark
                        else -> CyberSurface
                    }
                    val bannerBorder = when (authState) {
                        AuthState.DENIED -> CyberRedBright
                        else -> CyberBorder
                    }
                    val bannerTextColor = when (authState) {
                        AuthState.DENIED -> CyberRedBright
                        else -> CyberTextPrimary
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CutCornerShape(4.dp))
                            .background(bannerBg)
                            .border(1.dp, bannerBorder, CutCornerShape(4.dp))
                            .padding(vertical = 8.dp, horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (authState == AuthState.DENIED) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Warning",
                                    tint = CyberRedBright,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = authMessage,
                                fontFamily = CyberFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = bannerTextColor,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 4-Digit PIN Boxes
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0..3) {
                            val isEntered = i < enteredPin.length
                            val charAtSlot = if (isEntered) enteredPin[i].toString() else ""
                            val isCurrentSlot = i == enteredPin.length

                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CutCornerShape(6.dp))
                                    .background(if (isEntered) CyberRedDark.copy(alpha = 0.7f) else CyberSurface)
                                    .border(
                                        width = if (isCurrentSlot) 2.dp else 1.dp,
                                        color = when {
                                            authState == AuthState.DENIED -> CyberRedBright
                                            isEntered -> CyberRedBright
                                            isCurrentSlot -> CyberBorderGlow
                                            else -> CyberBorder
                                        },
                                        shape = CutCornerShape(6.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isEntered) {
                                    Text(
                                        text = charAtSlot,
                                        fontFamily = CyberFontFamily,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CyberRedBright
                                    )
                                } else if (isCurrentSlot) {
                                    Text(
                                        text = "_",
                                        fontFamily = CyberFontFamily,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CyberRed.copy(alpha = pulseAlpha)
                                    )
                                }
                            }
                        }
                    }

                    if (failedAttempts > 0) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "FAILED ATTEMPTS: $failedAttempts // LOCKOUT THRESHOLD: 5",
                            fontFamily = CyberFontFamily,
                            fontSize = 10.sp,
                            color = CyberWarning,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "AUTHENTICATION HINT: CODE IS 9955",
                        fontFamily = CyberFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = CyberTextDim,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Bottom Section: Cyber Numeric Keypad
            AnimatedVisibility(
                visible = authState != AuthState.AUTHENTICATING,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 340.dp)
                        .padding(bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val keys = listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf("CLR", "0", "DEL")
                    )

                    for (row in keys) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            for (key in row) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp)
                                        .clip(CutCornerShape(6.dp))
                                        .background(CyberSurface)
                                        .border(1.dp, CyberBorder, CutCornerShape(6.dp))
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = ripple(color = CyberRedBright)
                                        ) {
                                            triggerCyberHaptic(context, 20)
                                            when (key) {
                                                "CLR" -> viewModel.onClearPin()
                                                "DEL" -> viewModel.onBackspace()
                                                else -> viewModel.onDigitEntered(key)
                                            }
                                        }
                                        .testTag("keypad_$key"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (key == "DEL") {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                                            contentDescription = "Backspace",
                                            tint = CyberRed,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    } else {
                                        Text(
                                            text = key,
                                            fontFamily = CyberFontFamily,
                                            fontSize = if (key == "CLR") 14.sp else 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (key == "CLR") CyberWarning else CyberRedBright
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
