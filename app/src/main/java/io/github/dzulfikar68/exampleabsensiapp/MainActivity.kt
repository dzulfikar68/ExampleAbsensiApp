package io.github.dzulfikar68.exampleabsensiapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("absensi")
        myRef.child("alamat").push().setValue(User("zzz", "yxz")).addOnSuccessListener {
            Toast.makeText(this, "OK", Toast.LENGTH_LONG).show()
        }
        myRef.child("alamat").addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "XXX", Toast.LENGTH_LONG).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                for (noteDataSnapshot in snapshot.children) {
                    /**
                     * Mapping data pada DataSnapshot ke dalam object Barang
                     * Dan juga menyimpan primary key pada object Barang
                     * untuk keperluan Edit dan Delete data
                     */
                    val user: User? = noteDataSnapshot.getValue(User::class.java)
                    user?.id = noteDataSnapshot.key
                    /**
                     * Menambahkan object Barang yang sudah dimapping
                     * ke dalam ArrayList
                     */
                    println("@XXX = " + user)
                }
            }

        })

        try {
            FirebaseMessaging.getInstance().subscribeToTopic("news")
            println("@ABSEN: Subcribed token")
        } catch (e: Exception) {
            println("@ABSEN: Failed Subcribed")
        }

        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener { instanceIdResult ->
            val deviceToken = instanceIdResult.token
            println("@ABSEN: Refreshed token $deviceToken")
        }
    }
}