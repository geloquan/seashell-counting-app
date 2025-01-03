package com.example.qualt

import DatabaseManager
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class ImageResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_result)

        val punaw_count = intent.getIntExtra("punaw_count",0)
        val punaw_confidence = intent.getFloatExtra("punaw_conf",0f)
        val confidencePercent = (punaw_confidence * 100).toInt()
        findViewById<ProgressBar>(R.id.punaw_conf_bar).progress = confidencePercent
        findViewById<TextView>(R.id.punaw_conf).text = "${confidencePercent}%"

        val greenshell_count = intent.getIntExtra("greenshell_count",0)
        val greenshell_conf = intent.getFloatExtra("greenshell_conf", 0f)
        val greenshell_conf_percent = (greenshell_conf * 100).toInt()
        findViewById<ProgressBar>(R.id.greenshell_conf_bar).progress = greenshell_conf_percent
        findViewById<TextView>(R.id.greenshell_conf).text = "${greenshell_conf_percent}%"

        val tuway_count = intent.getIntExtra("tuway_count",0)
        val tuway_conf = intent.getFloatExtra("tuway_conf", 0f)
        val tuway_conf_percent = (tuway_conf * 100).toInt()
        findViewById<ProgressBar>(R.id.tuway_conf_bar).progress = tuway_conf_percent
        findViewById<TextView>(R.id.tuway_conf).text = "${tuway_conf_percent}%"

        val notshell_count = intent.getIntExtra("notshell_count",0)
        val notshell_conf = intent.getFloatExtra("notshell_conf", 0f)
        val notshell_conf_percent = (notshell_conf * 100).toInt()
        findViewById<ProgressBar>(R.id.notshell_conf_bar).progress = notshell_conf_percent
        findViewById<TextView>(R.id.notshell_conf).text = "${notshell_conf_percent}%"

        val punawCountTextView: TextView = findViewById(R.id.punaw_count)
        val greenshellCountTextView: TextView = findViewById(R.id.greenshell_count)
        val tuwayCountTextView: TextView = findViewById(R.id.tuway_count)
        val notShellTextView: TextView = findViewById(R.id.notshell_count)

        val save_btn: Button = findViewById(R.id.image_result_save_btn)
        val retry_btn: Button = findViewById(R.id.retry_btn)
        val home_btn: Button = findViewById(R.id.home_button)

        punawCountTextView.text = punaw_count.toString()
        greenshellCountTextView.text = greenshell_count.toString()
        tuwayCountTextView.text = tuway_count.toString()
        notShellTextView.text = notshell_count.toString()

        save_btn.setOnClickListener {
            try {
                val intent = Intent(this, SaveResultActivity::class.java)
                intent.putExtra("punaw_count", punaw_count)
                intent.putExtra("greenshell_count", greenshell_count)
                intent.putExtra("tuway_count", tuway_count)
                intent.putExtra("notshell_count", notshell_count)

                intent.putExtra("punaw_conf", punaw_confidence)
                intent.putExtra("greenshell_conf", greenshell_conf)
                intent.putExtra("tuway_conf", tuway_conf)
                intent.putExtra("notshell_conf", notshell_conf)

                startActivity(intent)
                finish()
            } catch (e: Exception) {
                Toast.makeText(this, "Error save: ${e}", Toast.LENGTH_SHORT).show()
            }
        }

        retry_btn.setOnClickListener {
            val intent = Intent(this, MainActivity2::class.java)
            startActivity(intent)
            finish()
        }
        home_btn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun updateProgressBar(progressBar: ProgressBar, percentage: Int, percentageText: TextView) {
        progressBar.progress = percentage
        percentageText.text = "$percentage%"

        val color = when (percentage) {
            in 0..10 -> Color.rgb(255, 0, 0) // Red
            in 11..25 -> Color.rgb(255, 69, 0) // Orange-Red
            in 26..40 -> Color.rgb(255, 140, 0) // Dark Orange
            in 41..55 -> Color.rgb(255, 215, 0) // Gold
            in 56..70 -> Color.rgb(173, 255, 47) // Green-Yellow
            in 71..85 -> Color.rgb(124, 252, 0) // Lawn Green
            in 86..100 -> Color.rgb(0, 255, 0) // Green
            else -> Color.rgb(255, 255, 255) // Default White (fallback)
        }

        progressBar.progressDrawable.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
    }
}