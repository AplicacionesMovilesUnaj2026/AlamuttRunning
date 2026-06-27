package com.aplicacionesmoviles.alamutt_running.core.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.aplicacionesmoviles.alamutt_running.core.domain.model.RunCheckpoint
import kotlinx.coroutines.flow.first

// Single DataStore instance per process — DataStore must not be instantiated more than once per file.
private val Context.runCheckpointDataStore: DataStore<Preferences> by preferencesDataStore(name = "run_checkpoint")

class RunCheckpointStore(private val context: Context) {

    private companion object {
        val KEY_USER_ID = stringPreferencesKey("run_userId")
        val KEY_DISTANCE = doublePreferencesKey("run_distance")
        val KEY_DURATION = longPreferencesKey("run_duration")
        val KEY_PACE = doublePreferencesKey("run_pace")
        val KEY_CALORIES = intPreferencesKey("run_calories")
        val KEY_STEPS = intPreferencesKey("run_steps")
        val KEY_UPDATED_AT = longPreferencesKey("run_updatedAt")
        val KEY_ACTIVE = booleanPreferencesKey("run_active")
    }

    /**
     * Writes all checkpoint fields atomically.
     * NFR-RP-1: DataStore writes are async and do not block the main thread.
     */
    suspend fun save(c: RunCheckpoint) {
        context.runCheckpointDataStore.edit { prefs ->
            prefs[KEY_USER_ID] = c.userId
            prefs[KEY_DISTANCE] = c.distanceMeters
            prefs[KEY_DURATION] = c.durationSeconds
            prefs[KEY_PACE] = c.pace
            prefs[KEY_CALORIES] = c.calories
            prefs[KEY_STEPS] = c.steps
            prefs[KEY_UPDATED_AT] = c.updatedAt
            prefs[KEY_ACTIVE] = true
        }
    }

    /**
     * Returns the stored checkpoint, or null if no active checkpoint exists.
     * NFR-RP-2: called before run screen renders; caller should await on a coroutine.
     */
    suspend fun read(): RunCheckpoint? {
        val prefs = context.runCheckpointDataStore.data.first()
        val active = prefs[KEY_ACTIVE] ?: false
        if (!active) return null
        return RunCheckpoint(
            userId = prefs[KEY_USER_ID] ?: return null,
            distanceMeters = prefs[KEY_DISTANCE] ?: 0.0,
            durationSeconds = prefs[KEY_DURATION] ?: 0L,
            pace = prefs[KEY_PACE] ?: 0.0,
            calories = prefs[KEY_CALORIES] ?: 0,
            steps = prefs[KEY_STEPS] ?: 0,
            updatedAt = prefs[KEY_UPDATED_AT] ?: 0L
        )
    }

    /**
     * Clears all checkpoint data. Called on successful run save or user discard.
     */
    suspend fun clear() {
        context.runCheckpointDataStore.edit { it.clear() }
    }
}
