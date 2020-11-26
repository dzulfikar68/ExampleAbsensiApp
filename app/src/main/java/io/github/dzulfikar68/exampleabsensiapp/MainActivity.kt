package io.github.dzulfikar68.exampleabsensiapp

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("absensix")

        myRef.child("users").addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "XXX", Toast.LENGTH_LONG).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val userList = mutableListOf<UserItem>()
                for (noteDataSnapshot in snapshot.children) {
                    val userItem: UserItem? = noteDataSnapshot.getValue(UserItem::class.java)
                    userItem?.let {
                        it.id = noteDataSnapshot.key
                        userList.add(it)
                        println("@XXX = " + userItem)
                    }
                }
                rvResult?.setListItem(userList.reversed())
            }
        })

        try {
            FirebaseMessaging.getInstance().subscribeToTopic("absensix")
            println("@ABSEN: Subcribed token")
        } catch (e: Exception) {
            println("@ABSEN: Failed Subcribed")
        }

        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener { instanceIdResult ->
            val deviceToken = instanceIdResult.token
            AbsensiFirebaseConstants.FIREBASE_DEVICE_TOKEN = deviceToken
            postNotification()
        }

        tvEmpty?.setOnClickListener {
            myRef.child("users").push().setValue(
                    UserItem(
                            id = null,
                            absen = "Check-In",
                            name = "Dzulfikar Fauzi khkjgjhjjgjhgjhgjgghjghjggjkuggkhkhkjkjljj",
                            role = "aaa",
                            date = dateString(),
                            note = "aaaassiii"
                    )
            ).addOnSuccessListener {
                Toast.makeText(this, "OK", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun openAbsenDialog() {
        val view = layoutInflater.inflate(R.layout.view_dialog, null, false)
        val noteEditText = view.findViewById<EditText>(R.id.noteEditText)
        val simpanButton = view.findViewById<Button>(R.id.simpanButton)
        val dialog = Dialog(view.context)
        dialog.setContentView(view)
        simpanButton?.setOnClickListener {
            val text = noteEditText?.text.toString().trim()
            if (text.isNotEmpty() || text == "null") {

            }
        }
        dialog.setOnDismissListener { }
        dialog.show()
    }

    private fun date() {
        val stringFormat = "HH:mm:ss\nEEEE, dd MMMM yyyy"
        val dateFormat = SimpleDateFormat(stringFormat, Locale.getDefault())
        val dateString = dateFormat.format(Date())
        val dateDate = try {
            dateFormat.parse(dateString)
        } catch (e: ParseException) {
            Date()
        }
    }

    private fun dateString(): String {
        val stringFormat = "HH:mm:ss\nEEEE, dd MMMM yyyy"
        val dateFormat = SimpleDateFormat(stringFormat, Locale.getDefault())
        val dateString = dateFormat.format(Date())
        return dateString
    }

    private fun runCountdown() {
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            @SuppressLint("SimpleDateFormat")
            override fun run() {
                val simpleDateFormat = SimpleDateFormat("HH:mm:ss\nEEEE, dd MMMM yyyy")
                val currentDateAndTime: String = simpleDateFormat.format(Date())
//                countdownTextView?.text = currentDateAndTime
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(runnable)
//        handler.removeCallbacks(runnable)
    }

    private fun postNotification() {
        val volleyQueue = Volley.newRequestQueue(this)

        val params = JSONObject().let {
            it.put("to", "${AbsensiFirebaseConstants.FIREBASE_DEVICE_TOKEN}/${getString(R.string.default_notification_channel_name)}")
            it.put("notification", JSONObject().apply {
                put("title", "Portugal vs. Denmark")
                put("body", "great match!")
                put("sound", "default")
                put("content_available", true)
                put("priority", "high")
            })
        }

        val request = object : JsonObjectRequest(
                Method.POST,
                AbsensiFirebaseConstants.FIREBASE_NOTIFICATION_URL,
                params,
                {
                    Toast.makeText(this, "OKEEEEE", Toast.LENGTH_LONG).show()
                }, {
            Toast.makeText(this, "ERRRNOO", Toast.LENGTH_LONG).show()
        }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                headers["Authorization"] = "key=${AbsensiFirebaseConstants.FIREBASE_SERVER_KEY}"
                return headers
            }
        }

        volleyQueue.add(request)
    }

    private fun RecyclerView.setListItem(items: List<UserItem>?) {
        layoutManager = LinearLayoutManager(context)
        hasFixedSize()
        adapter = ItemAdapter(items ?: emptyList())
    }
}