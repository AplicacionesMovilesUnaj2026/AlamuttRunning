package com.aplicacionesmoviles.alamutt_running.features.profile

import android.graphics.Bitmap
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import androidx.compose.ui.res.stringResource
import com.aplicacionesmoviles.alamutt_running.R
import com.aplicacionesmoviles.alamutt_running.core.data.cloudinary.CloudinaryRepository
import com.aplicacionesmoviles.alamutt_running.core.data.repository.UserRepository
import com.aplicacionesmoviles.alamutt_running.core.common.util.UnitConverter
import com.aplicacionesmoviles.alamutt_running.core.ui.theme.*
import com.aplicacionesmoviles.alamutt_running.features.auth.AuthViewModel
import com.canhub.cropper.CropImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@Composable
fun ProfileScreen(
    uid: String,
    navController: NavController,
    authViewModel: AuthViewModel,
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
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            pendingCropUri = it
            isEditing = false
        }
    }

    LaunchedEffect(uid) {
        viewModel.loadUserData(uid)
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkBackground)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkerHeader)
                        .padding(top = 40.dp, start = 8.dp, bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = TextWhite
                        )
                    }
                    Text(
                        text = stringResource(R.string.my_profile),
                        color = TextWhite,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
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
                                Text(stringResource(R.string.cancel), color = TextWhite)
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
                                                    authViewModel.userPhotoUrl = it
                                                }
                                                pendingCropUri = null
                                                isUploading = false
                                            }
                                        } ?: run { isUploading = false }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                            ) {
                                Text(stringResource(R.string.cut_and_save), color = TextWhite, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        ProfileImage(
                            viewModel.photoUrl,
                            isEditing
                        ) { imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                        Spacer(Modifier.height(24.dp))
                        if (!isEditing) {
                            Text(
                                viewModel.name.ifBlank { stringResource(R.string.default_username) },
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = TextWhite
                            )
                            Spacer(Modifier.height(8.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "${viewModel.points} ${stringResource(R.string.points).uppercase()}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFFFD700)
                                )
                            }
                            
                            Spacer(Modifier.height(16.dp))
                            Text(
                                viewModel.bio.ifBlank { stringResource(R.string.no_bio) },
                                fontSize = 14.sp,
                                color = TextWhite.copy(alpha = 0.6f)
                            )
                            Spacer(Modifier.height(32.dp))
                            
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = DarkerHeader),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    val weightLabel = if (viewModel.unitSystem == "Metric") stringResource(R.string.unit_kg) else stringResource(R.string.unit_lb)
                                    val heightLabel = if (viewModel.unitSystem == "Metric") stringResource(R.string.unit_cm) else stringResource(R.string.unit_in)

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(stringResource(R.string.weight), color = TextWhite.copy(alpha = 0.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text("${viewModel.weightKg} $weightLabel", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Black)
                                    }
                                    Spacer(Modifier.height(12.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(stringResource(R.string.height), color = TextWhite.copy(alpha = 0.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text("${viewModel.heightCm} $heightLabel", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }

                            Spacer(Modifier.height(32.dp))
                            Button(
                                onClick = {
                                    viewModel.editName = viewModel.name
                                    viewModel.editBio = viewModel.bio
                                    viewModel.editWeightKg = viewModel.weightKg.toString()
                                    viewModel.editHeightCm = viewModel.heightCm.toString()
                                    isEditing = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(stringResource(R.string.edit_profile), color = TextWhite, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            ProfileFields(viewModel)
                            Spacer(Modifier.height(24.dp))
                            Row(horizontalArrangement = Arrangement.Center) {
                                TextButton(onClick = { isEditing = false }) {
                                    Text(
                                        stringResource(R.string.cancel),
                                        color = TextWhite
                                    )
                                }
                                Spacer(Modifier.width(16.dp))
                                Button(
                                    onClick = {
                                        val weight = viewModel.editWeightKg.toDoubleOrNull() ?: 0.0
                                        val height = viewModel.editHeightCm.toIntOrNull() ?: 0
                                        if (weight <= 0.0 || height <= 0) {
                                            android.widget.Toast.makeText(context, context.getString(R.string.invalid_values_error), android.widget.Toast.LENGTH_SHORT).show()
                                        } else {
                                            viewModel.saveChanges(uid) { 
                                                authViewModel.userName = viewModel.name
                                                isEditing = false 
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    if (viewModel.isSaving) CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = TextWhite
                                    )
                                    else Text(stringResource(R.string.save_upper), color = TextWhite, fontWeight = FontWeight.Bold)
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
                    CircularProgressIndicator(color = AccentRed)
                }
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
            .background(DarkerHeader)
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
                    color = AccentRed
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileFields(viewModel: ProfileViewModel) {
    val fieldColors = OutlinedTextFieldDefaults.colors(
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

    OutlinedTextField(
        value = viewModel.editName,
        onValueChange = { viewModel.editName = it },
        label = { Text(stringResource(R.string.name)) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = fieldColors
    )
    Spacer(Modifier.height(12.dp))
    OutlinedTextField(
        value = viewModel.editBio,
        onValueChange = { viewModel.editBio = it },
        label = { Text(stringResource(R.string.bio)) },
        modifier = Modifier.fillMaxWidth(),
        minLines = 3,
        shape = RoundedCornerShape(12.dp),
        colors = fieldColors
    )
    Spacer(Modifier.height(12.dp))
    val weightLabel = if (viewModel.unitSystem == "Metric") stringResource(R.string.unit_kg) else stringResource(R.string.unit_lb)
    val heightLabel = if (viewModel.unitSystem == "Metric") stringResource(R.string.unit_cm) else stringResource(R.string.unit_in)

    OutlinedTextField(
        value = viewModel.editWeightKg,
        onValueChange = { 
            if (it.isEmpty() || it.matches(Regex("^\\d*[.,]?\\d*$"))) {
                viewModel.editWeightKg = it.replace(",", ".")
            }
        },
        label = { Text(stringResource(R.string.weight_with_unit, weightLabel)) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = fieldColors,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    )
    Spacer(Modifier.height(12.dp))
    OutlinedTextField(
        value = viewModel.editHeightCm,
        onValueChange = { 
            if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                viewModel.editHeightCm = it
            }
        },
        label = { Text(stringResource(R.string.height_with_unit, heightLabel)) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = fieldColors,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}
