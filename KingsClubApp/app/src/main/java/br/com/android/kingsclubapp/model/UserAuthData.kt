package br.com.android.kingsclubapp.model

import org.json.JSONObject

class UserAuthData(
    var cod_cliente: String? = null,
    var auth: Boolean = false,
    var key: String? = null,
    var idU: String? = null,
    var idL: String? = null,
    var entidade: String? = null) {
}