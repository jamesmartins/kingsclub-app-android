package br.com.android.kingsclubapp.services

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class HttpClient {

    private var client: OkHttpClient = OkHttpClient()

    companion object {
        val getInstance: HttpClient by lazy { HolderLazy.INSTANCE }
        val JSON: MediaType = "application/json; charset=utf-8".toMediaTypeOrNull()!!
        var authorizationCode = "VVNOMFZHeldxMkVJR1JCWmZkNVpxMU1icHFGRzhROHlXUWZrTnowVEQ4Y0VqekFGWURIUEt3wqLCog=="
        var cookie = "PHPSESSID=im116o1bce4q3e3bgj3i6fngnj629k7f17csan7co29kke2jasj0"
    }

    private object HolderLazy {
        val INSTANCE = HttpClient()
    }

    @Throws(IOException::class)
    operator fun get(url: String): String {
        val request = Request.Builder()
            .url(url)
            .build()
        val response = client.newCall(request).execute()
        return response.body!!.string()
    }

    fun postAsync(url: String, json: String, callback: Callback): Call {
        val body = json.toRequestBody(JSON)
        val request = Request.Builder()
            .method("POST", body)
            .url(url)
            .post(body)
            .build()
        val call = client.newCall(request)
        call.enqueue(callback)
        return call
    }

    fun postAsync2(url: String, json: String, callback: Callback): Call {
        val body = json.toRequestBody(JSON)
        val request = Request.Builder()
            .method("POST", body)
            .addHeader("authorizationCode", authorizationCode)
            .url(url)
            .post(body)
            .build()
        val call = client.newCall(request)
        call.enqueue(callback)
        return call
    }

    @Throws(IOException::class)
    fun postWithHeaders(url: String, json: String, callback: Callback): Call {
        val client = client.newBuilder().build()
        val mediaType = "text/plain".toMediaTypeOrNull()
        val body = RequestBody.create(mediaType, "")
        val request = Request.Builder()
            .url(url)
            .method("POST", body)
            .addHeader("authorizationCode", authorizationCode)
            .addHeader("Cookie", cookie)
            .build()
        val call = client.newCall(request)
        call.enqueue(callback)
        return call
    }

    @Throws(IOException::class)
    fun postAsync3(url: String, json: String, code : String ,callback: Callback): Call {
        val client = OkHttpClient()
        val mediaType = "application/json".toMediaType()
        val body = json.toRequestBody(mediaType)
        val request = Request.Builder()
            .url("https://adm.bunkerapp.com.br/wsjson/authApp.do")
            .post(body)
            .addHeader("authorizationCode", code)
            .addHeader("Content-Type", "application/json")
//            .addHeader("Cookie", "PHPSESSID=im116o1bce4q3e3bgj3i6fngnj629k7f17csan7co29kke2jasj0")
            .build()
        val call = client.newCall(request)
        call.enqueue(callback)
        return call
    }

    @Throws(IOException::class)
    fun postAsync4(url: String, json: String, code : String, callback: Callback): Call {
        val client = OkHttpClient()
        val mediaType = "application/json".toMediaType()
        val body = json.toRequestBody(mediaType)
        val request = Request.Builder()
            .url("https://adm.bunkerapp.com.br/wsjson/authApp.do")
            .post(body)
            .addHeader("authorizationCode", code)
            .addHeader("Content-Type", "application/json")
            .addHeader("Cookie", "cMKjR0dLVXlpUTRzwqNOczh3c8KjSmZjRWNVSmE3eThNUG1ic8KjVjM1M2dyVGtKM0ZZbkpNcWd1NEpRa1RsaDhraWZkcMKjMURycU9xQXhaZnJkMG5FMGR0aDJKVXk0a0ZpaWxsZFZtYlZ5MURzwqNpb21vajZNRkhoUnVlY0lXZmdVMGk4VWVXWDlsSGRPUTBEcWd2V1h0N2Zidk5hb28yR0doelRDUVRDSWhqRkpJMkJuOFQwRUQwUjJ6MkxLMHfCosKi")
            .build()
        val call = client.newCall(request)
        call.enqueue(callback)
        return call
    }
}