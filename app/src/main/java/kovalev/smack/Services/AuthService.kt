package kovalev.smack.Services

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kovalev.smack.Utilities.*
import org.json.JSONException
import org.json.JSONObject

object AuthService {

  var isLoggedIn = false
  var userEmail = ""
  var authToken = ""

  fun registerUser(context: Context, email: String, password: String, complete: (Boolean) -> Unit) {
    val jsonBody = JSONObject()
    jsonBody.put("email", email)
    jsonBody.put("password", password)
    val requestBody = jsonBody.toString()

    val registerRequest =
      object : StringRequest(Method.POST, URL_REGISTER, Response.Listener { _ ->
        complete(true)
      }, Response.ErrorListener { error ->
        Log.d("ERROR", "Could not register user: $error")
        complete(false)
      }) {
        override fun getBodyContentType(): String {
          return "application/json; charset=utf-8"
        }

        override fun getBody(): ByteArray {
          return requestBody.toByteArray()
        }
      }
    Volley.newRequestQueue(context).add(registerRequest)
  }

  fun loginUser(context: Context, email: String, password: String, complete: (Boolean) -> Unit) {
    val jsonBody = JSONObject()
    jsonBody.put("email", email)
    jsonBody.put("password", password)
    val requestBody = jsonBody.toString()

    val loginRequest =
      object : JsonObjectRequest(Method.POST, URL_LOGIN, null, Response.Listener { response ->
        try {
          userEmail = response.getString("user")
          authToken = response.getString("token")
          isLoggedIn = true
          complete(true)
        } catch (e: JSONException) {
          Log.d("JSON", "EXC:" + e.localizedMessage)
          complete(false)
        }
      }, Response.ErrorListener { error ->
        Log.d("ERROR", "Could not login user: $error")
        complete(false)
      }) {
        override fun getBodyContentType(): String {
          return "application/json; charset=utf-8"
        }

        override fun getBody(): ByteArray {
          return requestBody.toByteArray()
        }
      }
    Volley.newRequestQueue(context).add(loginRequest)
  }

  fun createUser(
    context: Context,
    name: String,
    email: String,
    avatarName: String,
    avatarColor: String,
    complete: (Boolean) -> Unit
  ) {
    val jsonBody = JSONObject()
    jsonBody.put("name", name)
    jsonBody.put("email", email)
    jsonBody.put("avatarName", avatarName)
    jsonBody.put("avatarColor", avatarColor)
    val requestBody = jsonBody.toString()

    val createRequest =
      object : JsonObjectRequest(Method.POST, URL_CREATE_USER, null, Response.Listener { response ->
        try {
          UserDataService.name = response.getString("name")
          UserDataService.email = response.getString("email")
          UserDataService.avatarColor = response.getString("avatarColor")
          UserDataService.avatarName = response.getString("avatarName")
          UserDataService.id = response.getString("_id")
          complete(true)
        } catch (e: JSONException) {
          Log.d("JSON", "EXC:" + e.localizedMessage)
          complete(false)
        }
      }, Response.ErrorListener { error ->
        Log.d("ERROR", "Could not add user: $error")
        complete(false)
      }) {
        override fun getBodyContentType(): String {
          return "application/json; charset=utf-8"
        }

        override fun getBody(): ByteArray {
          return requestBody.toByteArray()
        }

        override fun getHeaders(): MutableMap<String, String> {
          val headers = HashMap<String, String>()
          headers["Authorization"] = "Bearer $authToken"
          return headers
        }
      }
    Volley.newRequestQueue(context).add(createRequest)
  }

  fun findUserByEmail(context: Context, complete: (Boolean) -> Unit) {
    val findUserRequest =
      object : JsonObjectRequest(Method.GET, "$URL_GET_USER$userEmail", null, Response.Listener {
        try {
          UserDataService.name = it.getString("name")
          UserDataService.email = it.getString("email")
          UserDataService.avatarName = it.getString("avatarName")
          UserDataService.avatarColor = it.getString("avatarColor")
          UserDataService.id = it.getString("_id")

          val userDataChange = Intent(BROADCAST_USER_DATA_CHANGE)
          LocalBroadcastManager.getInstance(context).sendBroadcast(userDataChange)
          complete(true)
        } catch (e: JSONException) {
          Log.d("JSON", "EXC: " + e.localizedMessage)
        }
      }, Response.ErrorListener {
        Log.d("ERROR", "Could not find user.")
        complete(false)
      }) {
        override fun getBodyContentType(): String {
          return "application/json; charset=utf-8"
        }

        override fun getHeaders(): MutableMap<String, String> {
          val headers = HashMap<String, String>()
          headers["Authorization"] = "Bearer $authToken"
          return headers
        }
      }
    Volley.newRequestQueue(context).add(findUserRequest)
  }
}