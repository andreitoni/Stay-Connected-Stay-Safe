package com.example.stayconnectedv3
/*
@author Andrei Toni Niculae
 */
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.stayconnectedv3.Notifications.NotificationData
import com.example.stayconnectedv3.Notifications.PushNotification
import com.example.stayconnectedv3.Notifications.RetrofitInstance
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val TOPIC = "/topics/myTopic2"
lateinit var address: String

class PanicActivity : AppCompatActivity() {
    val TAG = "PanicActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.panic_layout)
        val btnPanic = findViewById<Button>(R.id.panic_button)

        val userListGood = DashboardActivity.allUsersList.getUsers()
        address = ""
        for (user in userListGood) {
            if (user.firstName.equals(FirebaseAuth.getInstance().currentUser?.displayName)) {
                if (user.address != null) {
                    Log.d("nulllllllll", "" + user.address)
                    address = user.address
                } else {
                    Log.d("nulllllllll", "" + user.address)
                    address = "Unknown Location"
                }
            }
        }
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)


        btnPanic.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            user?.let {
                val name = user.displayName
                val title = "$name IS IN DANGER!!!"
                var message = "At: $address"
                if (message.equals("At: ") || message.equals("At:")) {
                    message = "At: Unknown Location"
                }

                PushNotification(
                        NotificationData(title, message),
                        TOPIC
                ).also {
                    try {
                        sendNotification(it)
                    } catch (e: StackOverflowError) {
                    }
                }
            }
        }


        //---------------------------END OF ONCREATE-------------------------------
    }

    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.postNotification(notification)
            if (response.isSuccessful) {
                Log.d(TAG, "Response: ${Gson().toJson(response)}")
            } else {
                Log.e(TAG, response.errorBody().toString())
            }
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }
    }

}