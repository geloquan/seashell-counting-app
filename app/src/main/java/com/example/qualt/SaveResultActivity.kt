package com.example.qualt

import DatabaseManager
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SaveResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_save_result)

        val dbManager = DatabaseManager(this)
        val database = dbManager.read() // Read the database content

        val shellCountTextView: TextView = findViewById(R.id.total_shell_count)
        shellCountTextView.text = database.total_shells().toString()
    }
}