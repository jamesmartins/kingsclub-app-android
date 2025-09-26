package br.com.android.kingsclubapp

import android.annotation.SuppressLint
import android.app.Application
import com.onesignal.Continue.none
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors


const val ONESIGNAL_APP_ID = "a580829b-437f-459b-b82b-e1f72ae17c11"

val TAG = InitApplication::class.java.simpleName

class InitApplication : Application() {
    companion object {
        private var mInstance: InitApplication? = null

        @Synchronized
        fun getInstance(): InitApplication? {
            return mInstance
        }
    }

    override fun onCreate() {
        super.onCreate()
        mInstance = this

        // Version 5.x
        OneSignal.Debug.logLevel = LogLevel.VERBOSE

        // Version 5.x
        OneSignal.initWithContext(this, ONESIGNAL_APP_ID)

        // requestPermission will show the native Android notification permission prompt.
        // NOTE: It's recommended to use a OneSignal In-App Message to prompt instead.
//        CoroutineScope(Dispatchers.IO).launch {
//            OneSignal.Notifications.requestPermission(true)
//        }
    }
}