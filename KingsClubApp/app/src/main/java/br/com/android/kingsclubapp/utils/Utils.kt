package br.com.android.kingsclubapp.utils

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.webkit.CookieManager

object Utils {
    private val SP_FILE_NAME = "br.com.android.kingsclubapp"
    val REQUEST_PERMISSION_CODE = 7

    fun isNetworkConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return (cm.activeNetworkInfo != null
                && cm.activeNetworkInfo!!.isAvailable
                && cm.activeNetworkInfo!!.isConnected)
    }

    fun getCookie(siteName: String, CookieName: String): String? {
        var CookieValue: String? = null

        val cookieManager = CookieManager.getInstance()
        val cookies = cookieManager.getCookie(siteName)
        val temp = cookies.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (ar1 in temp) {
            if (ar1.contains(CookieName)) {
                val temp1 = ar1.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                CookieValue = temp1[1]
                break
            }
        }
        return CookieValue
    }

    fun saveToPreference(context: Context, key: String, value: String) {
        val sharedPreferences = context.getSharedPreferences(SP_FILE_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun saveToPreference(context: Context, key: String, value: Boolean) {
        val sharedPreferences = context.getSharedPreferences(SP_FILE_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun readFromPreferences(context: Context, key: String, defaultValue: String): String? {
        val sharedPreferences = context.getSharedPreferences(SP_FILE_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(key, defaultValue)
    }

    fun readFromPreferences(context: Context, key: String, defaultValue: Boolean): Boolean? {
        val sharedPreferences = context.getSharedPreferences(SP_FILE_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(key, defaultValue)
    }
}