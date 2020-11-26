package io.github.dzulfikar68.exampleabsensiapp

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class MainActivity : AppCompatActivity() {

    private var database: FirebaseDatabase? = null
    private var reference: DatabaseReference? = null
    private val nameReference = "absensix"
    private val namePath = "users"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.title = "Absensi Online"

        database = FirebaseDatabase.getInstance()
        reference = database?.getReference(nameReference)

        pbLoading?.visibility = View.VISIBLE
        reference?.child(namePath)?.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                pbLoading?.visibility = View.GONE
                Toast.makeText(this@MainActivity, "Fetch Data Failed", Toast.LENGTH_LONG).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                pbLoading?.visibility = View.GONE
                val userList = mutableListOf<UserItem>()
                for (noteDataSnapshot in snapshot.children) {
                    val userItem: UserItem? = noteDataSnapshot.getValue(UserItem::class.java)
                    userItem?.let {
                        it.id = noteDataSnapshot.key
                        userList.add(it)
                    }
                }
                rvResult?.setListItem(userList.reversed())
            }
        })

        try {
            FirebaseMessaging.getInstance().subscribeToTopic(nameReference)
        } catch (e: Exception) {
            Toast.makeText(this@MainActivity, "Subscribe Topic Failed", Toast.LENGTH_LONG).show()
        }

        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener { instanceIdResult ->
            val deviceToken = instanceIdResult.token
            AbsensiFirebaseConstants.FIREBASE_DEVICE_TOKEN = deviceToken
        }.addOnFailureListener {
            Toast.makeText(this@MainActivity, "Get Device Token Failed", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.ic_add) {
            openAddAbsenDialog()
            true
        } else super.onOptionsItemSelected(item)
    }

    @SuppressLint("InflateParams")
    private fun openAddAbsenDialog() {
        val view = layoutInflater.inflate(R.layout.view_add_dialog, null, false)
        val etName = view.findViewById<EditText>(R.id.etName)
        val etRole = view.findViewById<EditText>(R.id.etRole)
        val rgAbsen = view.findViewById<RadioGroup>(R.id.rgAbsen)
        val etNote = view.findViewById<EditText>(R.id.etNote)
        val tvTime = view.findViewById<TextView>(R.id.tvTime)
        val btnSubmit = view.findViewById<Button>(R.id.btnSubmit)

        val dialog = Dialog(view.context)
        dialog.setContentView(view)
        runCountdown(tvTime)
        btnSubmit?.setOnClickListener {
            val name = etName.text.toString().trim()
            val role = etRole.text.toString().trim()
            val absen = rgAbsen.getAbsen()
            val date = getDate()
            val note = etNote.text.toString().trim()
            if (name.isNotEmpty() || role.isNotEmpty() || note.isNotEmpty()) {
                reference?.child(namePath)?.push()?.setValue(
                        UserItem(
                                id = null,
                                absen = absen,
                                name = name,
                                role = role,
                                date = date,
                                note = note
                        )
                )?.addOnSuccessListener {
                    dialog.dismiss()
                    postNotification()
                    Toast.makeText(this, "Submit Data Success", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "Please Fill The Form", Toast.LENGTH_LONG).show()
            }
        }
        dialog.setOnDismissListener {
            timeRunnable?.let {
                timeHandler?.removeCallbacks(it)
            }
        }
        dialog.show()
    }

    @SuppressLint("InflateParams")
    private fun openShowAbsenDialog(item: UserItem) {
        val view = layoutInflater.inflate(R.layout.view_show_dialog, null, false)
        val tvName = view.findViewById<TextView>(R.id.tvName)
        val tvRole = view.findViewById<TextView>(R.id.tvRole)
        val tvAbsen = view.findViewById<TextView>(R.id.tvAbsen)
        val tvDate = view.findViewById<TextView>(R.id.tvDate)
        val tvNote = view.findViewById<TextView>(R.id.tvNote)

        val dialog = Dialog(view.context)
        dialog.setContentView(view)
        tvName?.text = item.name
        tvRole?.text = item.role
        tvAbsen?.text = item.absen?.toUpperCase(Locale.ROOT)
        tvDate?.text = item.date
        tvNote?.text = item.note
        dialog.show()
    }

    private fun RadioGroup?.getAbsen(): String {
        return if (this?.checkedRadioButtonId == R.id.rbCheckOut) "Check-Out" else "Check-In"
    }

    private fun getDate(): String {
        val stringFormat = "HH:mm:ss\nEEEE, dd MMMM yyyy"
        val dateFormat = SimpleDateFormat(stringFormat, Locale.getDefault())
        return dateFormat.format(Date())
    }

    private var timeHandler: Handler? = null
    private var timeRunnable: Runnable? = null

    private fun runCountdown(textView: TextView?) {
        timeHandler = Handler(Looper.getMainLooper())
        timeRunnable = object : Runnable {
            @SuppressLint("SimpleDateFormat")
            override fun run() {
                val simpleDateFormat = SimpleDateFormat("HH:mm:ss\nEEEE, dd MMMM yyyy")
                val currentDateAndTime: String = simpleDateFormat.format(Date())
                textView?.text = currentDateAndTime
                timeHandler?.postDelayed(this, 1000)
            }
        }
        timeRunnable?.let { timeHandler?.post(it) }
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
                    Toast.makeText(this, "Push Notification Success", Toast.LENGTH_LONG).show()
                }, { Toast.makeText(this, "Push Notification Failed", Toast.LENGTH_LONG).show() }
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
        adapter = ItemAdapter(items ?: emptyList()) {
            openShowAbsenDialog(it)
        }
    }
}