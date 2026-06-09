package com.aplicacionesmoviles.alamutt_running.core.data.cloudinary

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

class CloudinaryRepository {

    private val cloudName = "dkel7o2jd"
    private val uploadPreset = "alamutt_profiles"

    suspend fun uploadImage(
        context: Context,
        imageUri: Uri
    ): String? {

        val file = uriToFile(context, imageUri)

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                file.name,
                file.asRequestBody("image/*".toMediaTypeOrNull())
            )
            .addFormDataPart("upload_preset", uploadPreset)
            .build()

        val request = Request.Builder()
            .url("https://api.cloudinary.com/v1_1/$cloudName/image/upload")
            .post(requestBody)
            .build()

        val response = OkHttpClient().newCall(request).execute()

        if (!response.isSuccessful) return null

        val json = JSONObject(response.body?.string() ?: "")
        return json.getString("secure_url")
    }

    private fun uriToFile(context: Context, uri: Uri): File {

        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File.createTempFile("upload", ".jpg", context.cacheDir)

        val outputStream = FileOutputStream(file)

        inputStream?.copyTo(outputStream)

        inputStream?.close()
        outputStream.close()

        return file
    }
}