package com.aplicacionesmoviles.alamutt_running.features.profile

import android.graphics.Bitmap
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import com.aplicacionesmoviles.alamutt_running.data.cloudinary.CloudinaryRepository
import com.aplicacionesmoviles.alamutt_running.repository.UserRepository
import com.aplicacionesmoviles.alamutt_running.util.UnitConverter
import com.canhub.cropper.CropImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@Composable
fun ProfileScreen(
    uid: String,
    navController: NavController,
    viewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userRepository = remember { UserRepository() }
    val cloudinaryRepository = remember { CloudinaryRepository() }

    var isEditing by remember { mutableStateOf(false) }
    var pendingCropUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    val cropImageView = remember { CropImageView(context) }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            pendingCropUri = it
            isEditing = false
        }
    }

    LaunchedEffect(uid) {
        viewModel.loadUserData(uid)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(top = 40.dp, start = 8.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "Perfil",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
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
                    Spacer(Modifier.height(24.dp))
                    Row(horizontalArrangement = Arrangement.Center) {
                        TextButton(onClick = {
                            pendingCropUri = null
                            isEditing = false
                        }) {
                            Text("Cancelar", color = MaterialTheme.colorScheme.onSurface)
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
                                        val url = cloudinaryRepository.uploadImage(context, uri)
                                        scope.launch(Dispatchers.Main) {
                                            url?.let {
                                                userRepository.updatePhoto(uid, it)
                                                viewModel.photoUrl = it
                                            }
                                            pendingCropUri = null
                                            isUploading = false
                                        }
                                    } ?: run { isUploading = false }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Recortar y Guardar", color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                } else {
                    ProfileImage(
                        viewModel.photoUrl,
                        isEditing
                    ) { imagePicker.launch(arrayOf("image/*")) }
                    Spacer(Modifier.height(24.dp))
                    if (!isEditing) {
                        Text(
                            viewModel.name.ifBlank { "Nombre de usuario" },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "${viewModel.points} puntos",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD700)
                            )
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        Text(
                            viewModel.bio.ifBlank { "Sin biografía" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(Modifier.height(24.dp))
                        val weightLabel = UnitConverter.getWeightLabel(viewModel.unitSystem)
                        val heightLabel = UnitConverter.getHeightLabel(viewModel.unitSystem)

                        Text(
                            "Peso: ${viewModel.weightKg} $weightLabel",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Altura: ${viewModel.heightCm} $heightLabel",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(32.dp))
                        Button(
                            onClick = {
                                viewModel.editName = viewModel.name
                                viewModel.editBio = viewModel.bio
                                viewModel.editWeightKg = viewModel.weightKg
                                viewModel.editHeightCm = viewModel.heightCm
                                isEditing = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Text("Editar Perfil", color = MaterialTheme.colorScheme.onSurface)
                        }
                    } else {
                        ProfileFields(viewModel)
                        Spacer(Modifier.height(24.dp))
                        Row(horizontalArrangement = Arrangement.Center) {
                            TextButton(onClick = { isEditing = false }) {
                                Text(
                                    "Cancelar",
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(Modifier.width(16.dp))
                            Button(
                                onClick = { viewModel.saveChanges(uid) { isEditing = false } },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                if (viewModel.isSaving) CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                else Text("Guardar", color = MaterialTheme.colorScheme.onPrimary)
                            }
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
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun ProfileImage(url: String?, isEditing: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(enabled = isEditing, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        val optimizedUrl = url?.let {
            if (it.contains("cloudinary.com")) it.replace(
                "/upload/",
                "/upload/c_fill,w_300,h_300,q_auto:low/"
            ) else it
        }
        SubcomposeAsyncImage(
            model = optimizedUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            loading = {
                CircularProgressIndicator(
                    modifier = Modifier.size(30.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        )
        if (isEditing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}

@Composable
fun ProfileFields(viewModel: ProfileViewModel) {
    OutlinedTextField(
        value = viewModel.editName,
        onValueChange = { viewModel.editName = it },
        label = { Text("Nombre") },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(12.dp))
    OutlinedTextField(
        value = viewModel.editBio,
        onValueChange = { viewModel.editBio = it },
        label = { Text("Biografía") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 3
    )
    Spacer(Modifier.height(12.dp))
    val weightLabel = UnitConverter.getWeightLabel(viewModel.unitSystem)
    val heightLabel = UnitConverter.getHeightLabel(viewModel.unitSystem)

    OutlinedTextField(
        value = viewModel.editWeightKg.toString(),
        onValueChange = { viewModel.editWeightKg = it.toDoubleOrNull() ?: 0.0 },
        label = { Text("Peso ($weightLabel)") },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(12.dp))
    OutlinedTextField(
        value = viewModel.editHeightCm.toString(),
        onValueChange = { viewModel.editHeightCm = it.toIntOrNull() ?: 0 },
        label = { Text("Altura ($heightLabel)") },
        modifier = Modifier.fillMaxWidth()
    )
}