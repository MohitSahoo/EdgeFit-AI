package com.edgefit.coach.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesManager(private val context: Context) {

    companion object {
        private val SERVER_IP_KEY = stringPreferencesKey("server_ip")
        private val SERVER_PORT_KEY = stringPreferencesKey("server_port")

        // 10.0.2.2 is the special alias for the host loopback interface in Android Emulator
        const val DEFAULT_IP = "10.0.2.2"
        const val DEFAULT_PORT = "8000"
    }

    val serverIp: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SERVER_IP_KEY] ?: DEFAULT_IP
    }

    val serverPort: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SERVER_PORT_KEY] ?: DEFAULT_PORT
    }

    suspend fun saveServerConfig(ip: String, port: String) {
        context.dataStore.edit { preferences ->
            preferences[SERVER_IP_KEY] = ip
            preferences[SERVER_PORT_KEY] = port
        }
    }

    suspend fun getServerIp(): String {
        var ip = DEFAULT_IP
        context.dataStore.data.map { preferences ->
            ip = preferences[SERVER_IP_KEY] ?: DEFAULT_IP
        }
        return ip
    }

    suspend fun getServerPort(): String {
        var port = DEFAULT_PORT
        context.dataStore.data.map { preferences ->
            port = preferences[SERVER_PORT_KEY] ?: DEFAULT_PORT
        }
        return port
    }
}
