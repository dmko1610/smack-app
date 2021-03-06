package kovalev.smack.Services

import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import kovalev.smack.Controller.App
import kovalev.smack.Model.Channel
import kovalev.smack.Model.Message
import kovalev.smack.Utilities.URL_GET_CHANNELS
import kovalev.smack.Utilities.URL_GET_MESSAGES
import org.json.JSONException

object MessageService {
  val channels = ArrayList<Channel>()
  val messages = ArrayList<Message>()

  fun getChannels(complete: (Boolean) -> Unit) {
    val channelsRequest =
      object : JsonArrayRequest(Method.GET, URL_GET_CHANNELS, null, Response.Listener { response ->
        try {
          for (x in 0 until response.length()) {
            val channel = response.getJSONObject(x)
            val name = channel.getString("name")
            val channelDesc = channel.getString("description")
            val channelId = channel.getString("_id")

            val newChannel = Channel(name, channelDesc, channelId)
            this.channels.add(newChannel)
          }
          complete(true)
        } catch (e: JSONException) {
          Log.d("JSON", "EXC: " + e.localizedMessage)
          complete(false)
        }
      }, Response.ErrorListener {
        Log.d("ERROR", "Could not retrieve channels")
        complete(false)
      }) {
        override fun getBodyContentType(): String {
          return "application/json; charset=utf-8"
        }

        override fun getHeaders(): MutableMap<String, String> {
          val headers = HashMap<String, String>()
          headers["Authorization"] = "Bearer ${App.prefs.authToken}"
          return headers
        }
      }
    App.prefs.requestQueue.add(channelsRequest)
  }

  fun getMessages(channelId: String, complete: (Boolean) -> Unit) {
    val url = "$URL_GET_MESSAGES$channelId"

    val messagesRequest = object : JsonArrayRequest(Method.GET, url, null, Response.Listener {
      clearMessages()
      try {
        for (x in 0 until it.length()) {
          val message = it.getJSONObject(x)
          val messageBody = message.getString("messageBody")
          val channelId = message.getString("channelId")
          val id = message.getString("_id")
          val userName = message.getString("userName")
          val userAvatar = message.getString("userAvatar")
          val userAvatarColor = message.getString("userAvatarColor")
          val timeStamp = message.getString("timeStamp")

          val newMessage =
            Message(messageBody, userName, channelId, userAvatar, userAvatarColor, id, timeStamp)
          this.messages.add(newMessage)
        }
      } catch (e: JSONException) {
        Log.d("JSON", "EXC: " + e.localizedMessage)
        complete(false)
      }
    }, Response.ErrorListener {
      Log.d("ERROR", "Could not retrieve messages")
      complete(false)
    }) {
      override fun getBodyContentType(): String {
        return "application/json; charset=utf-8"
      }

      override fun getHeaders(): MutableMap<String, String> {
        val headers = HashMap<String, String>()
        headers["Authorization"] = "Bearer ${App.prefs.authToken}"
        return headers
      }
    }
    App.prefs.requestQueue.add(messagesRequest)
  }

  fun clearMessages() {
    messages.clear()
  }

  fun clearChannels() {
    channels.clear()
  }
}