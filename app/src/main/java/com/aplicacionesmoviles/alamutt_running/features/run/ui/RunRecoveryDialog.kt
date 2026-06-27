package com.aplicacionesmoviles.alamutt_running.features.run.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aplicacionesmoviles.alamutt_running.R
import com.aplicacionesmoviles.alamutt_running.core.domain.model.RunCheckpoint
import com.aplicacionesmoviles.alamutt_running.core.ui.theme.*
import java.util.Locale

/**
 * Dialog shown when an abandoned run checkpoint is found on launch.
 * Stateless composable — delegates save/discard decisions to the caller.
 */
@Composable
fun RunRecoveryDialog(
    checkpoint: RunCheckpoint,
    onSave: () -> Unit,
    onDiscard: () -> Unit
) {
    val distanceKm = String.format(Locale.US, "%.2f", checkpoint.distanceMeters / 1000.0)
    val minutes = checkpoint.durationSeconds / 60
    val seconds = checkpoint.durationSeconds % 60
    val duration = String.format(Locale.US, "%02d:%02d", minutes, seconds)

    AlertDialog(
        onDismissRequest = { /* non-dismissable — user must choose */ },
        containerColor = DarkerHeader,
        titleContentColor = AccentRed,
        textContentColor = TextWhite,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                text = stringResource(R.string.recovery_dialog_title),
                fontWeight = FontWeight.Black
            )
        },
        text = {
            Text(
                text = stringResource(R.string.recovery_dialog_message, distanceKm, duration)
            )
        },
        confirmButton = {
            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.recovery_save),
                    fontWeight = FontWeight.Black
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDiscard) {
                Text(
                    text = stringResource(R.string.recovery_discard),
                    color = TextGray,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}
