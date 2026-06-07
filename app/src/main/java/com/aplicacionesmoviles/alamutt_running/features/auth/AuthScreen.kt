package com.aplicacionesmoviles.alamutt_running.features.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.aplicacionesmoviles.alamutt_running.R
import com.aplicacionesmoviles.alamutt_running.core.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun AuthScreen(
    viewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onLoginSuccess: () -> Unit,
    onGoogleSignInClick: () -> Unit
) {
    val state = viewModel.uiState
    LaunchedEffect(state) { if (state is AuthUiState.Success) onLoginSuccess() }

    val frasesMotivacionales = remember {
        listOf(
            R.string.quote_1,
            R.string.quote_2,
            R.string.quote_3,
            R.string.quote_4,
            R.string.quote_5,
            R.string.quote_6
        )
    }
    val fraseDelDia = remember { frasesMotivacionales.random() }

    var mostrarFormularioLogin by remember { mutableStateOf(false) }
    var mostrarFormularioRegistro by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var indiceImagenActual by remember { mutableStateOf(0) }
    val imagenesFondo = listOf(R.drawable.run1, R.drawable.run2, R.drawable.run3)

    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            indiceImagenActual = (indiceImagenActual + 1) % imagenesFondo.size
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AnimatedContent(
            targetState = indiceImagenActual,
            transitionSpec = { fadeIn(animationSpec = tween(2000)) togetherWith fadeOut(animationSpec = tween(2000)) },
            label = "Fondo"
        ) { targetIndex ->
            Image(
                painter = painterResource(id = imagenesFondo[targetIndex]),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.4f
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            DarkBackground.copy(alpha = 0.8f),
                            DarkBackground
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 40.dp)
                ) {
                    Text(
                        "ALAMUTT",
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Black,
                        color = AccentRed,
                        letterSpacing = 4.sp
                    )
                    Text(
                        "RUNNING CLUB",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = TextWhite,
                        letterSpacing = 8.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AccentRed.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .border(1.dp, AccentRed.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "\"${stringResource(fraseDelDia)}\"",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        textAlign = TextAlign.Center,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (state is AuthUiState.Error) {
                        Text(
                            text = state.message,
                            color = AccentRed,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    AnimatedVisibility(
                        visible = !mostrarFormularioLogin && !mostrarFormularioRegistro,
                        enter = fadeIn() + slideInVertically { it / 2 },
                        exit = fadeOut()
                    ) {
                        Column {
                            Button(
                                onClick = { mostrarFormularioRegistro = true },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(stringResource(R.string.join_club), fontSize = 16.sp, fontWeight = FontWeight.Black, color = TextWhite)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { mostrarFormularioLogin = true },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DarkerHeader),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(stringResource(R.string.login), fontSize = 16.sp, fontWeight = FontWeight.Black, color = TextWhite)
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                HorizontalDivider(modifier = Modifier.weight(1f), color = TextWhite.copy(alpha = 0.2f))
                                Text(
                                    stringResource(R.string.or_divider),
                                    color = TextWhite.copy(alpha = 0.4f),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )
                                HorizontalDivider(modifier = Modifier.weight(1f), color = TextWhite.copy(alpha = 0.2f))
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            OutlinedButton(
                                onClick = { onGoogleSignInClick() },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                border = BorderStroke(1.dp, TextWhite.copy(alpha = 0.2f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_google),
                                        contentDescription = "Google",
                                        tint = Color.Unspecified,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(stringResource(R.string.continue_google), fontSize = 14.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }

                    val fieldColors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = AccentRed,
                        unfocusedBorderColor = TextWhite.copy(alpha = 0.3f),
                        focusedLabelColor = AccentRed,
                        unfocusedLabelColor = TextWhite.copy(alpha = 0.5f),
                        cursorColor = AccentRed,
                        focusedContainerColor = DarkerHeader.copy(alpha = 0.9f),
                        unfocusedContainerColor = DarkerHeader.copy(alpha = 0.7f)
                    )

                    AnimatedVisibility(visible = mostrarFormularioLogin, enter = fadeIn(), exit = fadeOut()) {
                        Column {
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text(stringResource(R.string.email)) },
                                leadingIcon = { Icon(Icons.Default.Email, null, tint = TextWhite.copy(alpha = 0.5f)) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(4.dp),
                                colors = fieldColors
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text(stringResource(R.string.password)) },
                                leadingIcon = { Icon(Icons.Default.Lock, null, tint = TextWhite.copy(alpha = 0.5f)) },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(4.dp),
                                colors = fieldColors
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { viewModel.loginWithEmail(email, password, onLoginSuccess) },
                                enabled = state !is AuthUiState.Loading,
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                if (state is AuthUiState.Loading) {
                                    CircularProgressIndicator(color = TextWhite, modifier = Modifier.size(24.dp))
                                } else {
                                    Text(stringResource(R.string.enter_club), fontWeight = FontWeight.Black, color = TextWhite)
                                }
                            }
                            TextButton(
                                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp),
                                onClick = { mostrarFormularioLogin = false; viewModel.resetState() }
                            ) {
                                Text(stringResource(R.string.back), color = TextWhite.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    AnimatedVisibility(visible = mostrarFormularioRegistro, enter = fadeIn(), exit = fadeOut()) {
                        Column {
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text(stringResource(R.string.email)) },
                                leadingIcon = { Icon(Icons.Default.Email, null, tint = TextWhite.copy(alpha = 0.5f)) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(4.dp),
                                colors = fieldColors
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text(stringResource(R.string.password)) },
                                leadingIcon = { Icon(Icons.Default.Lock, null, tint = TextWhite.copy(alpha = 0.5f)) },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(4.dp),
                                colors = fieldColors
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = { Text(stringResource(R.string.confirm_password)) },
                                leadingIcon = { Icon(Icons.Default.Lock, null, tint = TextWhite.copy(alpha = 0.5f)) },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(4.dp),
                                colors = fieldColors
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { viewModel.registerWithEmail(email, password, confirmPassword, onLoginSuccess) },
                                enabled = state !is AuthUiState.Loading,
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                if (state is AuthUiState.Loading) {
                                    CircularProgressIndicator(color = TextWhite, modifier = Modifier.size(24.dp))
                                } else {
                                    Text(stringResource(R.string.register_me), fontWeight = FontWeight.Black, color = TextWhite)
                                }
                            }
                            TextButton(
                                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp),
                                onClick = { mostrarFormularioRegistro = false; viewModel.resetState() }
                            ) {
                                Text(stringResource(R.string.back), color = TextWhite.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
