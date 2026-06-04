package com.aplicacionesmoviles.alamutt_running.features.onboarding

import android.graphics.Bitmap
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import com.aplicacionesmoviles.alamutt_running.data.cloudinary.CloudinaryRepository
import com.aplicacionesmoviles.alamutt_running.repository.UserRepository
import com.aplicacionesmoviles.alamutt_running.ui.theme.*
import com.canhub.cropper.CropImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@Composable
fun OnboardingScreen(
    uid: String,
    navController: NavController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userRepository = remember { UserRepository() }
    val cloudinaryRepository = remember { CloudinaryRepository() }

    var step by remember { mutableStateOf(1) }

    var name by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var weightKg by remember { mutableStateOf("") }
    var heightCm by remember { mutableStateOf("") }

    var photoUrl by remember { mutableStateOf<String?>(null) }
    var pendingCropUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var isSavingProfile by remember { mutableStateOf(false) }

    val cropImageView = remember { CropImageView(context) }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { pendingCropUri = it }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
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
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "CONFIGURA TU PERFIL",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = AccentRed,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "Paso $step de 3",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextWhite.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(TextWhite.copy(alpha = 0.1f), RoundedCornerShape(0.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(step / 3f)
                                .fillMaxHeight()
                                .background(AccentRed, RoundedCornerShape(0.dp))
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    when (step) {
                        1 -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "¡TE DAMOS LA BIENVENIDA AL CLUB! ¿CÓMO TE LLAMÁS?",
                                    color = TextWhite,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(bottom = 24.dp)
                                )
                                OutlinedTextField(
                                    value = name,
                                    onValueChange = { name = it },
                                    label = { Text("Nombre de usuario") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(4.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TextWhite,
                                        unfocusedTextColor = TextWhite,
                                        focusedBorderColor = AccentRed,
                                        unfocusedBorderColor = TextWhite.copy(alpha = 0.3f),
                                        focusedLabelColor = AccentRed,
                                        unfocusedLabelColor = TextWhite.copy(alpha = 0.5f),
                                        cursorColor = AccentRed,
                                        focusedContainerColor = DarkerHeader,
                                        unfocusedContainerColor = DarkerHeader
                                    )
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                OutlinedTextField(
                                    value = bio,
                                    onValueChange = { bio = it },
                                    label = { Text("Biografía (opcional)") },
                                    maxLines = 3,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(4.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TextWhite,
                                        unfocusedTextColor = TextWhite,
                                        focusedBorderColor = AccentRed,
                                        unfocusedBorderColor = TextWhite.copy(alpha = 0.3f),
                                        focusedLabelColor = AccentRed,
                                        unfocusedLabelColor = TextWhite.copy(alpha = 0.5f),
                                        cursorColor = AccentRed,
                                        focusedContainerColor = DarkerHeader,
                                        unfocusedContainerColor = DarkerHeader
                                    )
                                )
                            }
                        }
                        2 -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "INGRESA TUS DATOS FÍSICOS",
                                    color = TextWhite,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Text(
                                    "Para un cálculo de calorías más preciso",
                                    color = TextWhite.copy(alpha = 0.6f),
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(bottom = 24.dp)
                                )
                                OutlinedTextField(
                                    value = weightKg,
                                    onValueChange = {
                                        if (it.all { char -> char.isDigit() || char == '.' || char == ',' }) {
                                            weightKg = it.replace(",", ".")
                                        }
                                    },
                                    label = { Text("Peso (kg)") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(4.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TextWhite,
                                        unfocusedTextColor = TextWhite,
                                        focusedBorderColor = AccentRed,
                                        unfocusedBorderColor = TextWhite.copy(alpha = 0.3f),
                                        focusedLabelColor = AccentRed,
                                        unfocusedLabelColor = TextWhite.copy(alpha = 0.5f),
                                        cursorColor = AccentRed,
                                        focusedContainerColor = DarkerHeader,
                                        unfocusedContainerColor = DarkerHeader
                                    )
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                OutlinedTextField(
                                    value = heightCm,
                                    onValueChange = { 
                                        if (it.all { char -> char.isDigit() }) {
                                            heightCm = it
                                        }
                                    },
                                    label = { Text("Altura (cm)") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(4.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TextWhite,
                                        unfocusedTextColor = TextWhite,
                                        focusedBorderColor = AccentRed,
                                        unfocusedBorderColor = TextWhite.copy(alpha = 0.3f),
                                        focusedLabelColor = AccentRed,
                                        unfocusedLabelColor = TextWhite.copy(alpha = 0.5f),
                                        cursorColor = AccentRed,
                                        focusedContainerColor = DarkerHeader,
                                        unfocusedContainerColor = DarkerHeader
                                    )
                                )
                            }
                        }
                        3 -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (pendingCropUri != null) {
                                    Box(
                                        modifier = Modifier
                                            .size(250.dp)
                                            .clip(RectangleShape)
                                    ) {
                                        AndroidView(factory = {
                                            cropImageView.apply {
                                                layoutParams = FrameLayout.LayoutParams(
                                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                                    ViewGroup.LayoutParams.MATCH_PARENT
                                                )
                                                cropShape = CropImageView.CropShape.OVAL
                                                guidelines = CropImageView.Guidelines.ON
                                                setFixedAspectRatio(true)
                                                setAspectRatio(1, 1)
                                                setImageUriAsync(pendingCropUri)
                                            }
                                        })
                                    }
                                    Spacer(Modifier.height(16.dp))
                                    Row(horizontalArrangement = Arrangement.Center) {
                                        TextButton(onClick = { pendingCropUri = null }) {
                                            Text(
                                                "Cancelar",
                                                color = TextWhite,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(Modifier.width(16.dp))
                                        Button(
                                            onClick = {
                                                isUploading = true
                                                scope.launch(Dispatchers.Default) {
                                                    val bitmap = cropImageView.croppedImage
                                                    bitmap?.let { b ->
                                                        val file = File(
                                                            context.cacheDir,
                                                            "cp_${System.currentTimeMillis()}.jpg"
                                                        )
                                                        FileOutputStream(file).use { out ->
                                                            b.compress(
                                                                Bitmap.CompressFormat.JPEG,
                                                                80,
                                                                out
                                                            )
                                                        }
                                                        val uri = Uri.fromFile(file)
                                                        val url = cloudinaryRepository.uploadImage(
                                                            context,
                                                            uri
                                                        )
                                                        scope.launch(Dispatchers.Main) {
                                                            url?.let {
                                                                photoUrl = it
                                                            }
                                                            pendingCropUri = null
                                                            isUploading = false
                                                        }
                                                    } ?: run { isUploading = false }
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                "Recortar",
                                                color = TextWhite,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }
                                } else {
                                    Text(
                                        "AÑADE TU FOTO DE PERFIL",
                                        color = TextWhite,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(bottom = 24.dp)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(140.dp)
                                            .clip(CircleShape)
                                            .background(DarkerHeader)
                                            .clickable { imagePicker.launch(arrayOf("image/*")) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val optimizedUrl = photoUrl?.let {
                                            if (it.contains("cloudinary.com")) it.replace(
                                                "/upload/",
                                                "/upload/c_fill,w_300,h_300/"
                                            ) else it
                                        }
                                        if (optimizedUrl != null) {
                                            SubcomposeAsyncImage(
                                                model = optimizedUrl,
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop,
                                                loading = {
                                                    CircularProgressIndicator(color = AccentRed)
                                                }
                                            )
                                        } else {
                                            Icon(
                                                Icons.Default.CameraAlt,
                                                contentDescription = null,
                                                tint = TextWhite.copy(alpha = 0.6f),
                                                modifier = Modifier.size(40.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        "Toca para seleccionar",
                                        color = TextGray,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (step > 1 && pendingCropUri == null) {
                        Button(
                            onClick = { step-- },
                            modifier = Modifier.height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DarkerHeader,
                                contentColor = TextWhite
                            ),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("Atrás", fontWeight = FontWeight.Black)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    if (pendingCropUri == null) {
                        Button(
                            onClick = {
                                if (step == 1) {
                                    if (name.isBlank()) {
                                        Toast.makeText(
                                            context,
                                            "Por favor ingresa un nombre",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        step = 2
                                    }
                                } else if (step == 2) {
                                    val weight = weightKg.toDoubleOrNull() ?: 0.0
                                    val height = heightCm.toIntOrNull() ?: 0
                                    
                                    if (weight <= 0.0 || height <= 0) {
                                        Toast.makeText(
                                            context,
                                            "Por favor ingresa valores válidos (mayores a 0)",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        step = 3
                                    }
                                } else if (step == 3) {
                                    isSavingProfile = true

                                    if (!photoUrl.isNullOrEmpty()) {
                                        userRepository.updatePhoto(uid, photoUrl!!)
                                    }

                                    userRepository.updateUserData(
                                        uid = uid,
                                        name = name,
                                        bio = bio,
                                        weightKg = weightKg.toDoubleOrNull() ?: 0.0,
                                        heightCm = heightCm.toIntOrNull() ?: 0,
                                        onResult = { success ->
                                            isSavingProfile = false
                                            if (success) {
                                                navController.navigate("quick_start") {
                                                    popUpTo("onboarding") { inclusive = true }
                                                }
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "Error al guardar el perfil",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    )
                                }
                            },
                            modifier = Modifier.height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                            shape = RoundedCornerShape(4.dp),
                            enabled = !isSavingProfile
                        ) {
                            if (isSavingProfile) {
                                CircularProgressIndicator(
                                    color = TextWhite,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Text(
                                    text = if (step == 3) "Finalizar" else "Siguiente",
                                    color = TextWhite,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }

            if (isUploading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentRed)
                }
            }
        }
    }
}