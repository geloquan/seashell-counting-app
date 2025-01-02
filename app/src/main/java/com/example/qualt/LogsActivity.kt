package com.example.qualt

import DatabaseManager
import Row
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.setPadding
import org.w3c.dom.Text

class LogsActivity : AppCompatActivity() {
    private lateinit var databaseManager: DatabaseManager
    private lateinit var linearLayout: LinearLayout
    private lateinit var totalShellsTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logs)

        // Initialize DatabaseManager
        databaseManager = DatabaseManager(this)

        // Find views
        linearLayout = findViewById(R.id.logsContainer)
        totalShellsTextView = findViewById(R.id.totalShellsTextView)

        // Setup reset button
        findViewById<Button>(R.id.resetButton).setOnClickListener {
            showResetConfirmation()
        }
        findViewById<Button>(R.id.home_button).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        findViewById<Button>(R.id.camera_btn).setOnClickListener {
            val intent = Intent(this, MainActivity2::class.java)
            startActivity(intent)
            finish()
        }

        // Refresh the logs
        refreshLogs()
    }

    private fun refreshLogs() {
        // Clear existing views in the logs container
        linearLayout.removeAllViews()

        // Read the data
        val databaseData = databaseManager.read()

        // Update total shells at the top
        val totalShells = databaseData.total_shells()
        totalShellsTextView.text = "Total Shells: $totalShells"

        val total_classes: TextView = findViewById(R.id.total_classes)
        total_classes.text = buildSpannedString {
            append("Manila: ")
            bold { append("${databaseData.total_punaw()}") }
            append(" | Green: ")
            bold { append("${databaseData.total_green()}") }
            append(" | Mud: ")
            bold { append("${databaseData.total_tuway()}") }
        }
        databaseData.rows
            .sortedBy { row -> row.datetime }
            .forEach { row ->
                val rowLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(32)
                }

                val rowTextView = TextView(this).apply {
                    text = buildSpannedString {
                        append("Manila: ")
                        bold { append("${row.punaw}") }
                        append(", Green: ")
                        bold { append("${row.greenshell}") }
                        append(", Mud: ")
                        bold { append("${row.tuway}") }
                        append(" \nDate: ${row.datetime}")
                    }
                    textSize = 16f
                    setTextColor(ContextCompat.getColor(context, R.color.dark))
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                }

                val deleteButton = Button(this).apply {
                    setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.cross, // Your delete icon drawable resource
                        0, // No top drawable
                        0, // No right drawable
                        0  // No bottom drawable
                    )
                    background = null

                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    setOnClickListener {
                        showDeleteConfirmation(row)
                    }
                }

                // Add TextView and Delete button to the row layout
                rowLayout.addView(rowTextView)
                rowLayout.addView(deleteButton)

                // Add the row layout to the main linear layout
                linearLayout.addView(rowLayout)
            }
    }

    private fun showDeleteConfirmation(row: Row) {
        val totalRowShells = row.punaw + row.greenshell + row.tuway

        AlertDialog.Builder(this)
            .setTitle("Confirm Deletion")
            .setMessage(
                "Are you sure you want to delete this entry?\n\n" +
                        "Details:\n" +
                        "- Punaw: ${row.punaw}\n" +
                        "- Green Shell: ${row.greenshell}\n" +
                        "- Tuway: ${row.tuway}\n" +
                        "Total Shells in this Entry: $totalRowShells"
            )
            .setPositiveButton("Delete") { _, _ ->
                // Delete the specific row
                databaseManager.deleteRow(row.id)

                // Refresh the logs
                refreshLogs()
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showResetConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Reset All Data")
            .setMessage("Are you sure you want to delete ALL entries? This action cannot be undone.")
            .setPositiveButton("Reset") { _, _ ->
                // Reset all data
                databaseManager.resetAllData()

                // Refresh the logs
                refreshLogs()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}