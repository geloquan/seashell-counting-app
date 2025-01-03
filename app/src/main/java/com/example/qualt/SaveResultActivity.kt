package com.example.qualt

import DatabaseManager
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.w3c.dom.Text
import java.lang.Integer.sum

class SaveResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_save_result)
        val punaw_count = intent?.getIntExtra("punaw_count", 0) ?: 0
        val greenshell_count = intent?.getIntExtra("greenshell_count", 0) ?: 0
        val tuway_count = intent?.getIntExtra("tuway_count", 0) ?: 0
        val notshell_count = intent?.getIntExtra("notshell_count", 0) ?: 0

        val punaw_conf = intent?.getFloatExtra("punaw_conf", 0f) ?: 0f
        val greenshell_conf = intent?.getFloatExtra("greenshell_conf", 0f) ?: 0f
        val tuway_conf = intent?.getFloatExtra("tuway_conf", 0f) ?: 0f
        val notshell_conf = intent?.getFloatExtra("notshell_conf", 0f) ?: 0f

        val dbManager = DatabaseManager(this)
        val database = dbManager.read()

        val before_punaw = database.total_punaw()
        val before_green = database.total_green()
        val before_tuway = database.total_tuway()
        val before_notshell = database.total_notshell()

        val before_punaw_conf = database.total_punaw_conf()
        val before_green_conf = database.total_green_conf()
        val before_tuway_conf = database.total_tuway_conf()
        val before_notshell_conf = database.total_notshell_conf()

        dbManager.update(
            punaw_count,
            greenshell_count,
            tuway_count,
            notshell_count,

            punaw_conf,
            greenshell_conf,
            tuway_conf,
            notshell_conf,
        )

        val total_punaw = sum(before_punaw, punaw_count)
        val total_green = sum(before_green, greenshell_count)
        val total_tuway = sum(before_tuway, tuway_count)
        val total_notshell = sum(before_notshell, notshell_count)

        val total_punaw_conf = if (before_punaw + punaw_count > 0) {
            (before_punaw.toFloat() * before_punaw_conf + punaw_count.toFloat() * punaw_conf) /
                    (before_punaw + punaw_count).toFloat()
            } else {
                0f
            }
        val total_green_conf = if (before_green + greenshell_count > 0) {
            (before_green.toFloat() * before_green_conf + greenshell_count.toFloat() * greenshell_conf) /
                    (before_green + greenshell_count).toFloat()
            } else {
                0f
            }
        val total_tuway_conf = if (before_tuway + tuway_count > 0) {
            (before_tuway.toFloat() * before_tuway_conf + tuway_count.toFloat() * tuway_conf) /
                    (before_tuway + tuway_count).toFloat()
            } else {
                0f
            }
        val total_notshell_conf = if (before_notshell + notshell_count > 0) {
            (before_notshell.toFloat() * before_notshell_conf + notshell_count.toFloat() * notshell_conf) /
                    (before_notshell + notshell_count).toFloat()
            } else {
                0f
            }

        val punaw_text: TextView = findViewById(R.id.punaw_count)
        punaw_text.text = "$before_punaw ${punaw_text.text} ${total_punaw} (+ ${punaw_count})"
        val green_text: TextView = findViewById(R.id.greenshell_count)
        green_text.text = "$before_green ${green_text.text} ${total_green} (+ ${greenshell_count})"
        val tuway_text: TextView = findViewById(R.id.tuway_count)
        tuway_text.text = "$before_tuway ${tuway_text.text} ${total_tuway} (+ ${tuway_count})"
        val notshell_text: TextView = findViewById(R.id.ns_count)
        notshell_text.text = "$before_notshell ${notshell_text.text} ${total_notshell} (+ ${notshell_count})"

        val punaw_conf_text: TextView = findViewById(R.id.punaw_conf)
        val punaw_diff = total_punaw_conf - before_punaw_conf
        val punaw_percentage = if (before_punaw_conf * 100 != 0.0f) (punaw_diff / before_punaw_conf) * 100 else total_punaw_conf * 100
        punaw_conf_text.text = "${String.format("%.2f", before_punaw_conf * 100)}% ${punaw_conf_text.text} ${String.format("%.2f", total_punaw_conf * 100)}% (${if (punaw_diff * 100 >= 0) "+" else "-"}${String.format("%.2f", punaw_percentage)}%)"

        val green_conf_text: TextView = findViewById(R.id.greenshell_conf)
        val green_diff = total_green_conf - before_green_conf
        val green_percentage = if (before_green_conf != 0.0f) (green_diff / before_green_conf) * 100 else total_green_conf * 100
        green_conf_text.text = "${String.format("%.2f", before_green_conf * 100)}% ${green_conf_text.text} ${String.format("%.2f", total_green_conf * 100)}% (${if (green_diff * 100 >= 0) "+" else "-"}${String.format("%.2f", green_percentage)}%)"

        val tuway_conf_text: TextView = findViewById(R.id.mudclam_conf)
        val tuway_diff = total_tuway_conf - before_tuway_conf
        val tuway_percentage = if (before_tuway_conf != 0.0f) (tuway_diff / before_tuway_conf) * 100 else total_tuway_conf * 100
        tuway_conf_text.text = "${String.format("%.2f", before_tuway_conf * 100)}% ${tuway_conf_text.text} ${String.format("%.2f", total_tuway_conf * 100)}% (${if (tuway_diff * 100 >= 0) "+" else "-"}${String.format("%.2f", tuway_percentage)}%)"

        val notshell_conf_text: TextView = findViewById(R.id.ns_conf)
        val notshell_diff = total_notshell_conf - before_notshell_conf
        val notshell_percentage = if (before_notshell_conf != 0.0f) (notshell_diff / before_notshell_conf) * 100 else total_notshell_conf * 100
        notshell_conf_text.text = "${String.format("%.2f", before_notshell_conf * 100)}% ${notshell_conf_text.text} ${String.format("%.2f", total_notshell_conf * 100)}% (${if (notshell_diff * 100 >= 0) "+" else "-"}${String.format("%.2f", notshell_percentage)}%)"

        val shellCountTextView: TextView = findViewById(R.id.total_shell_count)
        val total = total_punaw + total_green + total_tuway
        shellCountTextView.text = total.toString()

        val shellConfTextView: TextView = findViewById(R.id.total_shell_conf)

        val total_shell_conf = if (total_punaw + total_tuway + total_green > 0) {
            ((total_punaw_conf * total_punaw) +
                    (total_tuway_conf * total_tuway) +
                    (total_green_conf * total_green)) /
                    (total_punaw + total_tuway + total_green).toFloat()
        } else {
            0f
        }

        shellConfTextView.text = "${String.format("%.2f", total_shell_conf * 100)}%"

        val camara_btn: Button = findViewById(R.id.camera_btn)
        val logs_btn: Button = findViewById(R.id.logs_btn)
        val home_button: Button = findViewById(R.id.home_button)

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