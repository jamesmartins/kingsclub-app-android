package br.com.android.kingsclubapp.parse


import br.com.android.kingsclubapp.model.User
import br.com.android.kingsclubapp.model.UrlServer
import br.com.android.kingsclubapp.model.UserAuthData
import br.com.android.kingsclubapp.utils.PROJECT_ID
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONStringer

object Json {
    fun toRDUser(json: String): User {
        try {
            val obj = JSONObject(json)
            val user = User()
            user.RD_userId = obj.getString("RD_userId")
            user.RD_userCompany = obj.getString("RD_userCompany")
            user.RD_userMail = obj.getString("RD_userMail")
            user.RD_userName = obj.getString("RD_userName")
            user.RD_userpass = obj.getString("RD_userpass")
            user.RD_userType = (obj.getString("RD_userType").isNullOrBlank() ?: "").toString()
            user.RD_TokenCelular = (obj.getString("RD_TokenCelular").isNullOrBlank() ?: "").toString()
            user.RD_Versao = (obj.getString("RD_Versao").isNullOrBlank() ?: "").toString()
            user.jsonObject = obj
            return user
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
    }

    fun getRDLoggedUser(RD_userId : String, RD_userCompany: Int ): String{
        try {
            var json = JSONStringer()
                .`object`()
                    .key("RD_userId").value(RD_userId)
                    .key("RD_userCompany").value(RD_userCompany)
                .endObject()
                .toString()
        return json
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
    }

    fun getAuthUser(userCPF : String, passwd: String ): String{
        try {
            var json = JSONStringer()
                .`object`()
                .key("CPF").value(userCPF)
                .key("SENHA").value(passwd)
                .endObject()
                .toString()
            return json
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
    }

    fun toAuthUser(json: String): UserAuthData {
        try {
            val obj = JSONObject(json)
            val user = UserAuthData()
            user.cod_cliente = obj.getString("cod_cliente")
            user.auth = obj.getBoolean("auth")
            user.key = obj.getString("key")
            user.idU = obj.getString("idU")
            user.idL = obj.getString("idL")
            user.entidade = obj.getString("entidade")
            return user
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
    }

    fun getUserOneSignalData(user : User ): String{
        try {
            var json = JSONStringer()
                .`object`()
                    .key("RD_userId").value(user.RD_userId)
                    .key("RD_userCompany").value(PROJECT_ID)
                    .key("RD_userMail").value(user.RD_userMail)
                    .key("RD_userName").value(user.RD_userName)
                    .key("RD_userType").value(user.RD_userType)
                    .key("RD_TokenCelular").value(user.RD_TokenCelular)
                    .key("RD_User_Player_Id").value(user.RD_User_Player_Id)
                    .key("RD_Versao").value("Android")
                .endObject()
                .toString()
            return json
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
    }

    fun toJsonObject(json: String): JSONObject? {
        try {
            return JSONObject(json)
        } catch (e: JSONException) {
            e.printStackTrace()
            return null
        }
    }

    fun toUrlServer(json: String): UrlServer {
        try {
            val obj = JSONObject(json)
            val urlServer = UrlServer()
            urlServer.urlDefault = obj.getString("URL1")
            urlServer.url2 = obj.getString("URL2")
            urlServer.url3 = obj.getString("URL3")
            urlServer.url4 = obj.getString("URL4")
            return urlServer
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }

    }
}