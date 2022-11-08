package com.dowell.dowellmap.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.clear
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.createDataStore
import com.dowell.dowellmap.Constant.Companion.QR_ID
import com.dowell.dowellmap.Constant.Companion.USERPREF
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserDatastore @Inject constructor(context: Context) {

    private val applicationContext = context.applicationContext
    private val dataStore: DataStore<Preferences> = applicationContext.createDataStore(
        name = USERPREF
    )

    suspend fun setQrId(qrId: String) {
        dataStore.edit { preferences ->
            preferences[QR_ID] = qrId

        }
    }

    fun getQrId() = dataStore.data.map { preferences -> preferences[QR_ID] ?: "" }


    //clear datastore
    suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

}