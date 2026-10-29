package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TerminalLine
import com.example.ui.components.CyberScanlines
import com.example.ui.components.triggerCyberHaptic
import com.example.ui.theme.CyberBackgroundDark
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberBorderGlow
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

@Composable
fun TerminalView(
    viewModel: CyberViewModel,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    val lines by viewModel.terminalLines.collectAsState()
    val isStreaming by viewModel.isStreaming.collectAsState()
    val streamSpeed by viewModel.streamSpeed.collectAsState()
    val commandInput by viewModel.commandInput.collectAsState()
    val listState = rememberLazyListState()
    val context = LocalContext.current

    // Auto-scroll to bottom when new line arrives
    LaunchedEffect(lines.size) {
        if (lines.isNotEmpty()) {
            listState.animateScrollToItem(lines.size - 1)
        }
    }

    // Flashing cursor animation
    val infiniteTransition = rememberInfiniteTransition(label = "cursor")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor"
    )

    Column(
        modifier = modifier
            .clip(CutCornerShape(topStart = 8.dp, bottomEnd = 8.dp))
            .background(CyberBackgroundDark)
            .border(1.dp, CyberBorderGlow, CutCornerShape(topStart = 8.dp, bottomEnd = 8.dp))
    ) {
        // Top Terminal Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberSurfaceVariant)
                .border(width = 0.5.dp, color = CyberBorder)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isStreaming) CyberRedBright else CyberWarning)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "TERMINAL://0x9955 [ROOT]",
                    fontFamily = CyberFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberRedBright
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Speed control
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(CyberSurface)
                        .border(1.dp, CyberBorder, RoundedCornerShape(4.dp))
                        .clickable {
                            triggerCyberHaptic(context, 20)
                            viewModel.cycleSpeed()
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${streamSpeed}x SPEED",
                        fontFamily = CyberFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberRedBright
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Pause / Stream toggle
                IconButton(
                    onClick = {
                        triggerCyberHaptic(context, 20)
                        viewModel.toggleStreaming()
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (isStreaming) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isStreaming) "Pause" else "Resume",
                        tint = CyberRedBright,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Clear button
                IconButton(
                    onClick = {
                        triggerCyberHaptic(context, 20)
                        viewModel.clearTerminal()
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Clear",
                        tint = CyberTextDim,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Terminal Output Screen
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            CyberScanlines(modifier = Modifier.fillMaxSize(), spacingDp = 3.dp, alpha = 0.05f)

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(3.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(lines, key = { it.id }) { line ->
                    TerminalLineRow(line)
                }

                // Blinking cursor active prompt line
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "root@cyber-core:~# ",
                            fontFamily = CyberFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberRedBright
                        )
                        Box(
                            modifier = Modifier
                                .size(width = 8.dp, height = 14.dp)
                                .background(CyberRedBright.copy(alpha = cursorAlpha))
                        )
                    }
                }
            }
        }

        // Quick Command Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberSurface)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val quickCmds = listOf("help", "scan", "breach", "decrypt", "trace", "ping", "ddos", "matrix", "status")
            for (qc in quickCmds) {
                Box(
                    modifier = Modifier
                        .clip(CutCornerShape(4.dp))
                        .background(CyberRedDark.copy(alpha = 0.4f))
                        .border(1.dp, CyberBorder, CutCornerShape(4.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = CyberRedBright)
                        ) {
                            triggerCyberHaptic(context, 15)
                            viewModel.executeCustomCommand(qc)
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = qc,
                        fontFamily = CyberFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberRedBright
                    )
                }
            }
        }

        // Command Prompt Input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberSurfaceVariant)
                .border(width = 0.5.dp, color = CyberBorder)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "> ",
                fontFamily = CyberFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = CyberRedBright
            )

            BasicTextField(
                value = commandInput,
                onValueChange = { viewModel.onCommandInputChange(it) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("terminal_input_field"),
                textStyle = TextStyle(
                    fontFamily = CyberFontFamily,
                    fontSize = 13.sp,
                    color = CyberTextPrimary,
                    fontWeight = FontWeight.Normal
                ),
                cursorBrush = SolidColor(CyberRedBright),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        triggerCyberHaptic(context, 25)
                        viewModel.executeCustomCommand()
                    }
                ),
                decorationBox = { innerTextField ->
                    if (commandInput.isEmpty()) {
                        Text(
                            text = "type command (e.g. 'scan', 'breach', 'help')...",
                            fontFamily = CyberFontFamily,
                            fontSize = 12.sp,
                            color = CyberTextDim
                        )
                    }
                    innerTextField()
                }
            )

            IconButton(
                onClick = {
                    triggerCyberHaptic(context, 25)
                    viewModel.executeCustomCommand()
                },
                modifier = Modifier
                    .size(32.dp)
                    .testTag("send_command_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Execute Command",
                    tint = CyberRedBright,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun TerminalLineRow(line: TerminalLine) {
    val textColor = when {
        line.isSuccess -> CyberRedBright
        line.isWarning -> CyberWarning
        line.isError -> CyberRed
        line.isHighlight -> CyberRedGlow
        line.isPrompt -> CyberRedBright
        else -> CyberTextPrimary
    }

    Text(
        text = line.text,
        fontFamily = CyberFontFamily,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = if (line.isHighlight || line.isPrompt) FontWeight.Bold else FontWeight.Normal,
        color = textColor,
        letterSpacing = 0.3.sp
    )
}
