package br.com.android.kingsclubapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.Toolbar
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import br.com.android.kingsclubapp.extensions.isValidCPF
import br.com.android.kingsclubapp.extensions.onlyNumbers2
import br.com.android.kingsclubapp.extensions.toBase64
import br.com.android.kingsclubapp.extensions.toast
import br.com.android.kingsclubapp.model.User
import br.com.android.kingsclubapp.model.UserAuthData
import br.com.android.kingsclubapp.parse.Json
import br.com.android.kingsclubapp.services.HttpClient
import br.com.android.kingsclubapp.utils.*
import br.com.android.kingsclubapp.WebViewMainActivity
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.onesignal.OneSignal
import okhttp3.Call
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class LoginActivity2 : AppCompatActivity(), TextWatcher {

    private val TAG = LoginActivity2::class.java.simpleName
    var btnLogin: Button? = null
    var editLogin: AppCompatEditText? = null
    var editPasswd: AppCompatEditText? = null
    var txtRememberPassword: TextView? = null
    var txtCreateLogin: TextView? = null
    var txtCheckLogin: SwitchMaterial? = null
    private var progressBar: ProgressBar? = null
    private var isConnected = false
    private lateinit var mToolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login3)

        mToolbar = findViewById(R.id.toolbar_login)
        this.setSupportActionBar(mToolbar)
        this.supportActionBar?.title = "Faça seu login"
        this.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        this.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)

        isConnected = Utils.isNetworkConnected(applicationContext)

        initViews()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActionModeStarted(mode: ActionMode?) {
        super.onActionModeStarted(mode)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    private fun verifyUserSavedPass(){
        if (hasDataUserSaved() && hasIduPassDataSaved()!!){
            var userIDUUrlpass = Utils.readFromPreferences(applicationContext, "userIDUPassSaved", " ")
            // Biometric request
            if (initBiometricV2()){
                promptInfo(completion = {
                    if (it){
                       if (userIDUUrlpass != null){
                           var loginCPF = Utils.readFromPreferences(applicationContext, "cpfSAVED"," ")
                           var loginPasswd = Utils.readFromPreferences(applicationContext, "passwdSAVED"," ")
                           var hasUserDataSaved = txtCheckLogin!!.isChecked
                            // open activity with webview + url authenticated user pass
                           doLogin(loginCPF.toString(), loginPasswd.toString(), hasUserDataSaved)


//                            startActivity(Intent(applicationContext, WebViewMainActivity::class.java)
//                                .putExtra("URL_LOAD_CONTENT", userIDUUrlpass.trim()))
//                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                        } else {
                            toast("Digite novamente os dados do login com manter os dados salvos!")
                        }
                    }
                })
            } /*else {
                // haven`t device credentials
                if (userIDUUrlpass != null){
                    startActivity(Intent(applicationContext, WebViewActivity::class.java).putExtra("URL_LOAD_CONTENT", userIDUUrlpass.trim()))
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }
            }*/
        }
    }

    override fun onResume() {
        super.onResume()
        progressBar.let {
            progressBar!!.progress = 0
            progressBar!!.visibility = View.GONE
        }
        // Execute Pre-saved authentication
        readFromAuthCookies()
    }

    fun initViews(){
        var btnManterDadosLogin = findViewById<SwitchMaterial>(R.id.txtCheckLogin)
        btnManterDadosLogin.isChecked = true
        editLogin = findViewById(R.id.edtLogin)
        editLogin!!.addTextChangedListener(this)
        editPasswd = findViewById(R.id.edtPasssword)
        btnLogin = findViewById(R.id.btnLogin)
        txtRememberPassword = findViewById(R.id.txtRememberPassword)
        txtCreateLogin = findViewById(R.id.txtCreateLogin)
        txtCheckLogin = findViewById(R.id.txtCheckLogin)
        progressBar = findViewById(R.id.progress)

        //actions
        txtRememberPassword!!.setOnClickListener {
            var mUrl = baseURL + mUrlRecuperacaoSenha
            startActivity(Intent(applicationContext, WebViewActivity::class.java)
                .putExtra("URL_LOAD_CONTENT", mUrl)
                .putExtra("URL_LOAD_TITLE","Recuperar Senha"))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        txtCreateLogin!!.setOnClickListener {
            var mUrl = baseURL + mUrlCadastro
            startActivity(Intent(applicationContext, WebViewActivity::class.java)
                .putExtra("URL_LOAD_CONTENT", mUrl)
                .putExtra("URL_LOAD_TITLE","Cadastro"))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        btnLogin!!.setOnClickListener {
            var login = editLogin!!.text.toString().onlyNumbers2()
            var passwd = editPasswd!!.text.toString().trim()
            var hasUserDataSaved = txtCheckLogin!!.isChecked
            // validate
            validate(login, passwd)
            //Do login
            doLogin(login, passwd, hasUserDataSaved)
        }
    }

    override fun onStart() {
        super.onStart()
    }

    private fun validate(login : String, passwd: String){
        if (login.isBlank() || login.isEmpty()){
            toast("CPF inválido!!")
            return
        }

        if (passwd.isBlank() || passwd.isEmpty()){
            toast("Campo de senha vazio ou inválida!!")
            return
        }

        if (!login.isValidCPF()) {
            toast("CPF inválido!!")
            return
        }
    }

    // Login process and send data to rededuque backend
    private fun doLogin(user : String, passwd: String, hasUserDataSaved: Boolean) {
        if (!isConnected) {
            toast("Falta de Conexão!", Toast.LENGTH_SHORT)
            return
        }
        progressBar!!.visibility = View.VISIBLE
        this@LoginActivity2.progressBar!!.progress = 0

        // saving CPF data
        if (hasUserDataSaved){
            saveDataUser(user, passwd)
        } else {
            saveDataUser("", "", false)
        }

        //Do authenticate
        var userAuthLogged: UserAuthData?
        var userRD: User?

        doAuthenticate(user, passwd,  completion = { success: Boolean, user: UserAuthData?, error : String  ->
            if (success){
                userAuthLogged = user
                // Get RedeDuque Login User token data
                if (!userAuthLogged!!.idL.isNullOrBlank() && !userAuthLogged!!.key.isNullOrBlank()){
                    //Save Auth Token Cookies
                    saveAuthLoggedUser(userAuthLogged!!.idL, userAuthLogged!!.key)
                }
                // Get RedeDuque Personal User Logged data
                var IDUkey = userAuthLogged!!.idU
                // Verifying on Rede Duque base if exist on RD and OneSignal
                if (!IDUkey.isNullOrBlank()) {
                    processRedeDuqueUrlKey(IDUkey, completion = { success: Boolean, user: User?->
                        if (success) {
                            userRD = user!!

                            // Get OneSignal data (2023)
                            // Methods getDeviceState() is deprecated
//                            var deviceState = OneSignal.getDeviceState()
//                           deviceState.let {
//                                userRD!!.RD_TokenCelular = deviceState?.pushToken
//                                userRD!!.RD_User_Player_Id = deviceState?.userId
//                            }

                            // New methods to get player Id and token data from OneSignal
                            // 08/2024
                            userRD!!.RD_TokenCelular = OneSignal.User.pushSubscription.token
                            userRD!!.RD_User_Player_Id = OneSignal.User.pushSubscription.id

                            //Save Auth Cookies Data
                            saveAuthCookies(userRD,IDUkey)

                            //Send OenSignal Data to RedeDuque
                            sendOneSignalDataToRedeDuque(userRD!!, completion = {
                                if (it) {
                                    Log.d(getString(R.string.Data_Sent_to_RedeDuque), "Dados OneSignal Enviados para Rede Duque!")
                                    var mUrl = mUrl_NOVO_MENU + "?key=" + userAuthLogged!!.key  + "&idU=" + userAuthLogged!!.idU + "&cds=0"

                                    // save url authenticated user pass
                                    saveIduPassData(mUrl, true)

                                    if (!hasSecurityAccessBiometric()!!){

                                        // Process the response
                                        runOnUiThread {
                                            val builder = AlertDialog.Builder(this)
                                            builder.setTitle("Atenção")
                                            builder.setMessage("Deseja ativar o acesso seguro por código de bloqueio ou leitura facial?")
                                                .setPositiveButton("Sim") { _, _ ->
                                                    saveSecurityAccessBiometric(true)
                                                    verifyUserSavedPass()
                                                    progressBar!!.setVisibility(View.GONE)
                                                    this@LoginActivity2.progressBar!!.progress = 100

                                                }
                                                .setNegativeButton("Não") { _, _ ->
                                                    saveSecurityAccessBiometric(false)

                                                    progressBar!!.setVisibility(View.GONE)
                                                    this@LoginActivity2.progressBar!!.progress = 100
                                                    // open activity with webview + url authenticated user pass
                                                    startActivity(Intent(applicationContext, WebViewMainActivity::class.java)
                                                        .putExtra("URL_LOAD_CONTENT", mUrl))
                                                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                                                }
                                            builder.show()
                                        }

                                    } else {
                                        // open activity with webview + url authenticated user pass
                                        startActivity(Intent(applicationContext, WebViewMainActivity::class.java)
                                            .putExtra("URL_LOAD_CONTENT", mUrl))
                                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                                    }
                                }
                            })
                        } else {
                            runOnUiThread {
                                progressBar!!.setVisibility(View.GONE)
                                this@LoginActivity2.progressBar!!.progress = 100
                                toast(error)
                            }
                        }
                    })
                }
            }
        })
    }

    private fun doAuthenticate(user: String, passwd: String, completion: (success: Boolean, user: UserAuthData?, error: String) -> Unit) {
        val postparams = Json.getAuthUser(user, passwd)

        HttpClient.getInstance.postAsync3(mUrlAuthApp, postparams, code = CODE_AUTHETICATION , callback =  object : okhttp3.Callback {

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    progressBar!!.setVisibility(View.GONE)
                    this@LoginActivity2.progressBar!!.progress = 100
                }
                Log.e(this::class.simpleName, "Error Comunication" + e.message)
                completion(false, null, "Error Comunication" + e.message)
            }

            override fun onResponse(call: Call, response: okhttp3.Response) {
                if (response.isSuccessful && response.code == 200) {
                    //get data user
                    var userData = response.peekBody(2048).string()
                    if (userData.isNotEmpty() && userData.isNotBlank()){
                        val obj = JSONObject(userData)
                        if (obj.has("cod_cliente")) {
                            var userLogged = Json.toAuthUser(userData)
                            //get data user from idU Key
                            Log.d(getString(R.string.Success_To_Login),"Login Realizado com Sucesso...")
                            completion(true, userLogged, "")

                        } else if (obj.has("errors")) {
                            val jsonError = JSONArray(obj).getJSONObject(0)
                            if (jsonError.has("message")){
                                runOnUiThread {
                                    progressBar!!.setVisibility(View.GONE)
                                    this@LoginActivity2.progressBar!!.progress = 100
                                }
                                Log.d(getString(R.string.Success_To_Login), jsonError.getString("message"))
                                completion(true, null, jsonError.getString("message"))
                            }
                        } else {
                            Log.d("Error_Message","Aconteceu algum problema de dados da RedeDuque...")
                            runOnUiThread {
                                progressBar!!.setVisibility(View.GONE)
                                this@LoginActivity2.progressBar!!.progress = 100
                            }
                            completion(false, UserAuthData(), "Aconteceu algum problema de dados da RedeDuque...")
                        }
                    } else {
                        Log.e(getString(R.string.Error_To_Login),"Aconteceu algum problema no Login...")
                        runOnUiThread {
                            progressBar!!.setVisibility(View.GONE)
                            this@LoginActivity2.progressBar!!.progress = 100
                        }
                        completion(false, null, "Aconteceu algum problema no Login...")
                    }
                } else {
                    Log.e(getString(R.string.Error_To_Login),"Aconteceu algum problema no Login...")
                    runOnUiThread {
                        progressBar!!.setVisibility(View.GONE)
                        this@LoginActivity2.progressBar!!.progress = 100
                        toast("Informaçoes de login ou senha inválidos")
                    }
                    completion(false, null, "Informaçoes de login ou senha inválidos!" )
                }
            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    /**
     * Shows a [Snackbar].
     *
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * @param actionStringId   The text of the action item.
     * @param listener         The listener associated with the Snackbar action.
     */
    private fun showSnackbar(mainTextStringId: Int, actionStringId: Int, listener: View.OnClickListener) {
        Snackbar.make(findViewById(android.R.id.content),
            getString(mainTextStringId),
            Snackbar.LENGTH_INDEFINITE).setAction(getString(actionStringId), listener).show()
    }

    private fun saveAuthCookies(user : User?, iduKey: String?) {
        if (user!!.RD_userMail != null && user!!.RD_userpass != null) {
            Utils.saveToPreference(applicationContext, "userId", user!!.RD_userId!!.trim { it <= ' ' })
            Utils.saveToPreference(applicationContext, "emailSAVED", user!!.RD_userMail!!.trim { it <= ' ' })
            Utils.saveToPreference(applicationContext, "passwdSAVED", user!!.RD_userpass!!.trim { it <= ' ' })
            Utils.saveToPreference(applicationContext, "playerIdSAVED", user!!.RD_User_Player_Id!!.trim { it <= ' ' })
            Utils.saveToPreference(applicationContext, "tokenPhoneSAVED", user!!.RD_TokenCelular!!.trim { it <= ' ' })
            Utils.saveToPreference(applicationContext, "IDUkeySAVED", iduKey!!.trim { it <= ' ' })
        }
    }

    private fun saveDataUser(CPF: String?, passwd : String?, loggedDataSAVED : Boolean = true) {
        if (CPF != null && passwd != null) {
            Utils.saveToPreference(applicationContext, "cpfSAVED", CPF.trim { it <= ' ' })
            Utils.saveToPreference(applicationContext, "passwdSAVED", passwd.trim { it <= ' ' })
            Utils.saveToPreference(applicationContext, "loggedDataSAVED", loggedDataSAVED)
        }
    }

    private fun hasDataUserSaved():Boolean{
        var loggedDataUser = Utils.readFromPreferences(applicationContext, "loggedDataSAVED",false)
        return loggedDataUser!!
    }

    private fun saveIduPassData(dataPath : String?, status : Boolean?){
        if (dataPath != null && status != null) {
            Utils.saveToPreference(applicationContext, "userIDUPassSaved", dataPath)
            Utils.saveToPreference(applicationContext, "userHasIDUPass", true)
        }
    }

    private fun saveSecurityAccessBiometric(accessSecurity : Boolean?){
        if (accessSecurity != null) {
            Utils.saveToPreference(applicationContext, "userAccessSecurity", accessSecurity)
        }
    }

    private fun hasSecurityAccessBiometric():Boolean?
        = Utils.readFromPreferences(applicationContext, "userAccessSecurity",false)


    private fun hasIduPassDataSaved(): Boolean? =
          Utils.readFromPreferences(applicationContext, "userHasIDUPass",false)

    private fun readFromAuthCookies() {
        if (hasDataUserSaved()) {
            var loginCPF = Utils.readFromPreferences(applicationContext, "cpfSAVED"," ")
            loginCPF = applyMask(loginCPF!!)
            editLogin!!.setText(loginCPF)
            var loginPasswd = Utils.readFromPreferences(applicationContext, "passwdSAVED"," ")
            editPasswd!!.setText(loginPasswd!!, TextView.BufferType.EDITABLE)

        }
        //Verify if exist biometric preferences saved
        if (hasSecurityAccessBiometric()!!){
            verifyUserSavedPass()
        }

    }

    private fun saveAuthLoggedUser(idlToken: String?, userKey: String?) {
        if (idlToken != null && userKey != null) {
            Utils.saveToPreference(applicationContext, "tokenSAVED", idlToken.trim { it <= ' ' })
            Utils.saveToPreference(applicationContext, "userKeySAVED", userKey.trim { it <= ' ' })
        }
    }

    private fun processRedeDuqueUrlKey(keyValue : String, companyId: Int = PROJECT_ID, completion: (success: Boolean, user: User?) -> Unit) {
        val postparams = Json.getRDLoggedUser(keyValue.toBase64(), companyId)

        HttpClient.getInstance.postAsync(url = mUrlUserSearchKeyData, json = postparams, callback = object : okhttp3.Callback {

            override fun onFailure(call: Call, e: IOException) {
                Log.e(this::class.simpleName, "Error Comunication in processRedeDuqueUrlKey" + e.message)
                runOnUiThread {
                    progressBar!!.setVisibility(View.GONE)
                    this@LoginActivity2.progressBar!!.progress = 100
                    toast("Erro de Comunicação com a Rededuque: " + e.message)
                }
                completion(false,  User())
            }

            override fun onResponse(call: Call, responseObj: okhttp3.Response) {
                if (responseObj.isSuccessful && responseObj.code == 200) {
                    //get data user from idU Key
                    var userResult = responseObj.peekBody(2048).string()
                    if (userResult.isNotEmpty() && userResult.isNotBlank()){
                        val obj = JSONObject(userResult)
                        if (obj.has("RD_userId")) {
                            var userLogged = Json.toRDUser(userResult)
                            Log.d("SUCESSO","CONSULTA DO CLIENTE REDEDUQUE FEITA COM SUCESSO...")
                            completion(true, userLogged)
                        } else {
                            Log.d("Error_Message","Aconteceu algum problema de dados da RedeDuque...")
                            completion(false, User())
                        }
                    } else {
                        Log.e("Error_Message","Aconteceu algum problema de dados da RedeDuque...")
                        runOnUiThread {
                            progressBar!!.setVisibility(View.GONE)
                            this@LoginActivity2.progressBar!!.progress = 100
                            toast("Aconteceu algum problema de dados da RedeDuque...")
                        }
                        completion(false, User())
                    }
                } else {
                    Log.e(getString(R.string.Error_With_RedeDuque),"Aconteceu algum problema na conexão...")
                    runOnUiThread {
                        progressBar!!.setVisibility(View.GONE)
                        this@LoginActivity2.progressBar!!.progress = 100
                        toast("Aconteceu algum problema na conexão...")
                    }
                    completion(false, User())
                }
            }
        })
    }

    private fun sendOneSignalDataToRedeDuque(userLogged : User, completion: (success: Boolean) -> Unit) {
        val postparams = Json.getUserOneSignalData(userLogged)

        HttpClient.getInstance.postAsync(url = mUrlUserPushDataInformation, json = postparams,  callback =  object : okhttp3.Callback {

            override fun onFailure(call: Call, e: IOException) {
                Log.e(this::class.simpleName, "Error Comunication sendOneSignalDataToRedeDuque:" + e.message)
                runOnUiThread {
                    progressBar!!.setVisibility(View.GONE)
                    this@LoginActivity2.progressBar!!.progress = 100
                    toast("Erro de Comunicação com a Rededuque..." + e.message)
                }
                completion(false)
            }

            override fun onResponse(call: Call, responseFromDuque: okhttp3.Response) {
                if (responseFromDuque.isSuccessful && responseFromDuque.code == 200) {
                    //get data user from idU Key
                    Log.d(getString(R.string.Success_To_RedeDuque),"Enviados dados OneSignal com sucesso...")
                    completion(true)
                } else {
                    Log.e(getString(R.string.Error_With_RedeDuque),"Aconteceu algum problema na conexão..." + call.execute().message.toString())
                    runOnUiThread {
                        progressBar!!.setVisibility(View.GONE)
                        this@LoginActivity2.progressBar!!.progress = 100
                        toast("Aconteceu algum problema na conexão...")
                    }
                    completion(false)
                }
            }
        })
    }

    private fun promptInfo(completion: (success: Boolean) -> Unit) {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Autenticação")
            .setSubtitle("Faça Login usando suas credenciais:")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()

        val executor = ContextCompat.getMainExecutor(this@LoginActivity2)

        val biometricPrompt = BiometricPrompt(this@LoginActivity2, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    // Autenticação biométrica bem-sucedida
                    Toast.makeText(applicationContext, "Autenticação com sucesso!", Toast.LENGTH_SHORT).show()
                    completion(true)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
//                    Toast.makeText(applicationContext, "Falha da autenticação!", Toast.LENGTH_SHORT).show()
                    completion(false)
                }

                override fun onAuthenticationFailed() {
//                    Toast.makeText(applicationContext, "Falha da autenticação!", Toast.LENGTH_SHORT).show()
                    completion(false)
                }
            })

        biometricPrompt.authenticate(promptInfo)
    }

    private fun initBiometricV2(): Boolean {
        val biometricManager = BiometricManager.from(this@LoginActivity2)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
            0 -> true
            else -> false
        }
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

    override fun afterTextChanged(txt: Editable?) {

    }

    private fun applyMask(text: String): String {
        val formattedText = StringBuilder()
        val cpfLength = 11

        for (i in text.indices) {
            if (i < cpfLength) {
                formattedText.append(text[i])
                if (i == 2 || i == 5) {
                    formattedText.append(".")
                } else if (i == 8) {
                    formattedText.append("-")
                }
            } else {
                formattedText.append(text[i])
                if (i == 1 || i == 4) {
                    formattedText.append(".")
                } else if (i == 7) {
                    formattedText.append("/")
                } else if (i == 11) {
                    formattedText.append("-")
                }
            }
        }

        return formattedText.toString()
    }

}

