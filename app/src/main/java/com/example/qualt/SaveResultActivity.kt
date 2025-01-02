package com.example.qualt

import DatabaseManager
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.w3c.dom.Text
import java.lang.Integer.sum

class SaveResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_save_result)
        val punaw_count = intent.getIntExtra("punaw_count",0)
        val greenshell_count = intent.getIntExtra("greenshell_count",0)
        val tuway_count = intent.getIntExtra("tuway_count",0)
        val notshell_count = intent.getIntExtra("notshell_count",0)

        val punaw_conf = intent.getFloatExtra("punaw_conf",0f)
        val greenshell_conf = intent.getFloatExtra("greenshell_conf",0f)
        val tuway_conf = intent.getFloatExtra("tuway_conf",0f)
        val notshell_conf = intent.getFloatExtra("notshell_conf",0f)

        val dbManager = DatabaseManager(this)
        val database = dbManager.read()
        val before_punaw = database.total_punaw()
        val before_green = database.total_green()
        val before_tuway = database.total_tuway()

        dbManager.update(punaw_count, greenshell_count, tuway_count) // Update the counts

        val total_punaw = sum(before_punaw, punaw_count)
        val total_green = sum(before_green, greenshell_count)
        val total_tuway = sum(before_tuway, tuway_count)

        val camara_btn: Button = findViewById(R.id.camera_btn)
        val logs_btn: Button = findViewById(R.id.logs_btn)
        val home_button: Button = findViewById(R.id.home_button)

        val punaw_text: TextView = findViewById(R.id.punaw_count)
        punaw_text.text = "$before_punaw ${punaw_text.text} ${total_punaw} (+ ${punaw_count})"
        val green_text: TextView = findViewById(R.id.greenshell_count)
        green_text.text = "$before_green ${green_text.text} ${total_green} (+ ${greenshell_count})"
        val tuway_text: TextView = findViewById(R.id.tuway_count)
        tuway_text.text = "$before_tuway ${tuway_text.text} ${total_tuway} (+ ${tuway_count})"

        val shellCountTextView: TextView = findViewById(R.id.total_shell_count)
        val total = total_punaw + total_green + total_tuway
        shellCountTextView.text = total.toString()

        camara_btn.setOnClickListener {
            val intent = Intent(this, MainActivity2::class.java)
            startActivity(intent)
            finish()
        }
        logs_btn.setOnClickListener {
            val intent = Intent(this, LogsActivity::class.java)
            startActivity(intent)
        }
        home_button.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}