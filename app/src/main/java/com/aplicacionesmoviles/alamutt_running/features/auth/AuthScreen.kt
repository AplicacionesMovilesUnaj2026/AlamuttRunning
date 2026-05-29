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
    import com.aplicacionesmoviles.alamutt_running.R
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
                "El dolor es temporal, el orgullo es para siempre.",
                "No importa qué tan lento vayas, sigues superando a todos los que están en el sillón.",
                "Tu único rival es la persona que fuiste ayer.",
                "Correr es el espacio donde el cuerpo y la mente se encuentran libres.",
                "Cada paso, por más corto que sea, te acerca más a tu meta diaria.",
                "La constancia vence a la genética. Sigue adelante hoy.",
                "Tus piernas no están cansadas, tu mente te está mintiendo."
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

        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(targetState = indiceImagenActual, transitionSpec = { fadeIn(animationSpec = tween(1500)) togetherWith fadeOut(animationSpec = tween(1500)) }, label = "Fondo") { targetIndex ->
                Image(painter = painterResource(id = imagenesFondo[targetIndex]), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            }
            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(Color(0xDD1A1A2E), Color(0xDD16213E), Color(0xEE0F3460))))) {
                Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceBetween) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("ALAMUTT", fontSize = 54.sp, fontWeight = FontWeight.Black, color = Color(0xFFE94560), letterSpacing = 4.sp)
                        Text("RUNNING CLUB", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 2.sp)
                    }

                    Box(modifier = Modifier.fillMaxWidth().background(Color(0x1FE94560), RoundedCornerShape(16.dp)).padding(16.dp), contentAlignment = Alignment.Center) {
                        Text(text = "\"$fraseDelDia\"", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color.LightGray, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }

                    if (state is AuthUiState.Error) {
                        Text(text = state.message, color = Color.Yellow, textAlign = TextAlign.Center, modifier = Modifier.padding(8.dp))
                    }

                    Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
                        AnimatedVisibility(visible = !mostrarFormularioLogin && !mostrarFormularioRegistro, enter = fadeIn(), exit = fadeOut()) {
                            Column {
                                Button(onClick = { mostrarFormularioRegistro = true }, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE94560)), shape = RoundedCornerShape(12.dp)) {
                                    Text("UNIRSE", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedButton(onClick = { mostrarFormularioLogin = true }, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White), shape = RoundedCornerShape(12.dp)) {
                                    Text("INICIAR SESIÓN", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color.Gray.copy(alpha = 0.5f))
                                    Text(" O ", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 8.dp))
                                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color.Gray.copy(alpha = 0.5f))
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                OutlinedButton(onClick = { onGoogleSignInClick() }, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White), shape = RoundedCornerShape(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                        Icon(painter = painterResource(id = R.drawable.ic_google), contentDescription = "Google", tint = Color.Unspecified, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text("Continuar con Google", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        AnimatedVisibility(visible = mostrarFormularioLogin, enter = fadeIn(), exit = fadeOut()) {
                            Column {
                                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email", color = Color.Gray) }, leadingIcon = { Icon(Icons.Default.Email, null, tint = Color.Gray) }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFFE94560), unfocusedBorderColor = Color.Gray))
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Contraseña", color = Color.Gray) }, leadingIcon = { Icon(Icons.Default.Lock, null, tint = Color.Gray) }, singleLine = true, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFFE94560), unfocusedBorderColor = Color.Gray))
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = { viewModel.loginWithEmail(email, password, onLoginSuccess) }, enabled = state !is AuthUiState.Loading, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE94560))) {
                                    if (state is AuthUiState.Loading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp)) else Text("ENTRAR", fontWeight = FontWeight.Bold)
                                }
                                TextButton(onClick = { mostrarFormularioLogin = false; viewModel.resetState() }) { Text("Volver", color = Color.LightGray) }
                            }
                        }

                        AnimatedVisibility(visible = mostrarFormularioRegistro, enter = fadeIn(), exit = fadeOut()) {
                            Column {
                                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email", color = Color.Gray) }, leadingIcon = { Icon(Icons.Default.Email, null, tint = Color.Gray) }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFFE94560), unfocusedBorderColor = Color.Gray))
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Contraseña", color = Color.Gray) }, leadingIcon = { Icon(Icons.Default.Lock, null, tint = Color.Gray) }, singleLine = true, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFFE94560), unfocusedBorderColor = Color.Gray))
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = { Text("Confirmar Contraseña", color = Color.Gray) }, leadingIcon = { Icon(Icons.Default.Lock, null, tint = Color.Gray) }, singleLine = true, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFFE94560), unfocusedBorderColor = Color.Gray))
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = { viewModel.registerWithEmail(email, password, confirmPassword, onLoginSuccess) }, enabled = state !is AuthUiState.Loading, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE94560))) {
                                    if (state is AuthUiState.Loading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp)) else Text("REGISTRARME", fontWeight = FontWeight.Bold)
                                }
                                TextButton(onClick = { mostrarFormularioRegistro = false; viewModel.resetState() }) { Text("Volver", color = Color.LightGray) }
                            }
                        }
                    }
                }
            }
        }
    }