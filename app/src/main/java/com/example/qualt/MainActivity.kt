package com.example.qualt

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        val camara_btn: Button = findViewById(R.id.camera_btn)
        val logs_btn: Button = findViewById(R.id.logs_btn)

        camara_btn.setOnClickListener {
            val intent = Intent(this, MainActivity2::class.java)
            startActivity(intent)
            finish()
        }
        logs_btn.setOnClickListener {
            val intent = Intent(this, LogsActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}