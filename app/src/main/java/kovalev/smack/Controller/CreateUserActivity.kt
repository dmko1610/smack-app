package kovalev.smack.Controller

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_create_user.*
import kovalev.smack.R
import kovalev.smack.Services.AuthService
import java.util.*

class CreateUserActivity : AppCompatActivity() {
  var userAvatar = "profileDefault"
  var avatarColor = "[0.5, 0.5, 0.5, 1]"

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_create_user)
  }

  fun generateUserAvatar(view: View) {
    val random = Random()
    val color = random.nextInt(2)
    val avatar = random.nextInt(28)

    userAvatar = if (color == 0) "light$avatar" else "dark$avatar"

    val resourceId = resources.getIdentifier(userAvatar, "drawable", packageName)
    createAvatarImageView.setImageResource(resourceId)
  }

  fun createUserClicked(view: View) {
    AuthService.registerUser(this, "j@j.com", "123456") {complete ->
      if (complete) {

      }

    }
  }

  fun generateColorClicked(view: View) {
    val random = Random()
    val r = random.nextInt(255)
    val g = random.nextInt(255)
    val b = random.nextInt(255)

    createAvatarImageView.setBackgroundColor(Color.rgb(r, g, b))

    val savedR = r.toDouble() / 255
    val savedG = g.toDouble() / 255
    val savedB = b.toDouble() / 255

    avatarColor = "[$savedR, $savedG, $savedB, 1]"

  }
}