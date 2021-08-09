package kovalev.smack.Controller

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import io.socket.client.IO
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.nav_header_main.*
import kovalev.smack.Model.Channel
import kovalev.smack.R
import kovalev.smack.Services.AuthService
import kovalev.smack.Services.MessageService
import kovalev.smack.Services.UserDataService
import kovalev.smack.Utilities.BROADCAST_USER_DATA_CHANGE
import kovalev.smack.Utilities.SOCKET_URL
import kovalev.smack.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

  private val socket = IO.socket(SOCKET_URL)


  private lateinit var appBarConfiguration: AppBarConfiguration
  private lateinit var binding: ActivityMainBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    setSupportActionBar(binding.appBarMain.toolbar)

    val drawerLayout: DrawerLayout = binding.drawerLayout
    val navView: NavigationView = binding.navView
    val navController = findNavController(R.id.nav_host_fragment_content_main)
    // Passing each menu ID as a set of Ids because each
    // menu should be considered as top level destinations.
    appBarConfiguration = AppBarConfiguration(
      setOf(
        R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow
      ), drawerLayout
    )
    setupActionBarWithNavController(navController, appBarConfiguration)
    navView.setupWithNavController(navController)

    socket.connect()
    socket.on("channelCreated", onNewChannel)
  }

  override fun onResume() {
    LocalBroadcastManager.getInstance(this).registerReceiver(
      userDataChangeReceiver, IntentFilter(
        BROADCAST_USER_DATA_CHANGE
      )
    )
    super.onResume()
  }

  override fun onDestroy() {
    socket.close()
    LocalBroadcastManager.getInstance(this).unregisterReceiver(userDataChangeReceiver)
    super.onDestroy()
  }

  private val userDataChangeReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
      if (AuthService.isLoggedIn) {
        userNameNavHeader.text = UserDataService.name
        userEmailNavHeader.text = UserDataService.email
        val resourceId =
          resources.getIdentifier(UserDataService.avatarName, "drawable", packageName)
        userImageNavHeader.setImageResource(resourceId)
        userImageNavHeader.setBackgroundColor(UserDataService.returnAvatarColor(UserDataService.avatarColor))
        loginBtnNavHeader.text = "Logout"
      }
    }
  }

  override fun onSupportNavigateUp(): Boolean {
    val navController = findNavController(R.id.nav_host_fragment_content_main)
    return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
  }

  fun loginBtnNavClicked(view: View) {
    if (AuthService.isLoggedIn) {
      UserDataService.logout()
      userNameNavHeader.text = "Login"
      userEmailNavHeader.text = ""
      userImageNavHeader.setImageResource(R.drawable.profiledefault)
      userImageNavHeader.setBackgroundColor(Color.TRANSPARENT)
      loginBtnNavHeader.text = "Login"
    } else {
      val loginIntent = Intent(this, LoginActivity::class.java)
      startActivity(loginIntent)
    }
  }

  fun addChannelClicked(view: View) {
    if (AuthService.isLoggedIn) {
      val builder = AlertDialog.Builder(this)
      val dialogView = layoutInflater.inflate(R.layout.add_channel_dialog, null)

      builder.setView(dialogView)
        .setPositiveButton("Add") { dialogInterface, i ->
          val nameTextField = dialogView.findViewById<EditText>(R.id.addChannelNameTxt)
          val descTextField = dialogView.findViewById<EditText>(R.id.addChannelDescTxt)
          val channelName = nameTextField.text.toString()
          val channelDesc = descTextField.text.toString()

          // Create channel with the channel name and description
          socket.emit("newChannel", channelName, channelDesc)
        }
        .setNegativeButton("Cancel") { dialogInterface, i ->
        }
        .show()
    }
  }

  private val onNewChannel = Emitter.Listener { args ->
    runOnUiThread {
      val channelName = args[0] as String
      val channelDescription = args[1] as String
      val channelId = args[2] as String

      val newChannel = Channel(channelName, channelDescription, channelId)
      MessageService.channels.add(newChannel)
      println(newChannel.name)
      println(newChannel.description)
      println(newChannel.id)
    }
  }

  fun sendMsgBtnClicked(view: View) {
    hideKeyboard()
  }

  private fun hideKeyboard() {
    val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    if (inputManager.isAcceptingText) {
      inputManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }
  }
}