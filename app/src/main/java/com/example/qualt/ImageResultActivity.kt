package com.example.qualt

import DatabaseManager
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class ImageResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_image_result)
        val punaw_count = intent.getIntExtra("punaw_count",0)
        val greenshell_count = intent.getIntExtra("greenshell_count",0)
        val tuway_count = intent.getIntExtra("tuway_count",0)

        val punawCountTextView: TextView = findViewById(R.id.punaw_count)
        val greenshellCountTextView: TextView = findViewById(R.id.greenshell_count)
        val tuwayCountTextView: TextView = findViewById(R.id.tuway_count)

        val save_btn: Button = findViewById(R.id.save)

        punawCountTextView.text = punaw_count.toString()
        greenshellCountTextView.text = greenshell_count.toString()
        tuwayCountTextView.text = tuway_count.toString()

        save_btn.setOnClickListener {
            try {
                val dbManager = DatabaseManager(this)
                val database = dbManager.read() // Read the database content
                dbManager.update(punaw_count, greenshell_count, tuway_count) // Update the counts
                val intent = Intent(this, SaveResultActivity::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Error save: ${e}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}