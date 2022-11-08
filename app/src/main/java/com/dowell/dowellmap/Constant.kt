package com.dowell.dowellmap

import androidx.datastore.preferences.core.preferencesKey

open class Constant {
    companion object {
        val USERPREF = "user_pref"
        val QR_ID = preferencesKey<String>("qrId")
    }
}
