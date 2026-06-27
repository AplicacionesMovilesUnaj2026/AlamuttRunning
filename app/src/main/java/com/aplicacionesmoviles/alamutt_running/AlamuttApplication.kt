package com.aplicacionesmoviles.alamutt_running

import android.app.Application
import android.util.Log
import com.aplicacionesmoviles.alamutt_running.core.data.local.RunCheckpointStore
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings

/**
 * Application entry point.
 *
 * Firestore offline persistence is enabled here — BEFORE any ViewModel or Repository
 * obtains a FirebaseFirestore instance. This is the only safe place because
 * firestoreSettings must be applied before the first getInstance() call.
 *
 * BOM >= 32.x: uses PersistentCacheSettings (the non-deprecated API).
 * Legacy isPersistenceEnabled is deprecated as of Firebase BoM 32.0.
 */
class AlamuttApplication : Application() {

    val runCheckpointStore: RunCheckpointStore by lazy {
        RunCheckpointStore(applicationContext)
    }

    override fun onCreate() {
        super.onCreate()
        enableFirestoreOfflinePersistence()
    }

    private fun enableFirestoreOfflinePersistence() {
        try {
            val settings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
                .build()
            FirebaseFirestore.getInstance().firestoreSettings = settings
        } catch (e: Exception) {
            // Firestore settings can only be changed before the first getInstance() touch.
            // A second attempt in the same process is silently rejected; log and continue.
            Log.w("AlamuttApplication", "Firestore persistence settings already applied: ${e.message}")
        }
    }
}
