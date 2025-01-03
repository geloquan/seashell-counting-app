package com.example.qualt

import DatabaseManager
import Row
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
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

    private fun combinePer() {

    }

    private fun refreshLogs() {
        // Clear existing views in the logs container
        linearLayout.removeAllViews()

        // Read the data
        val databaseData = databaseManager.read()

        // Update total shells at the top
        val totalShells = databaseData.total_shells()

        val total_shell_conf = if (databaseData.total_punaw() + databaseData.total_tuway() + databaseData.total_green() > 0) {
            ((databaseData.total_punaw_conf() * databaseData.total_punaw()) +
                    (databaseData.total_tuway_conf() * databaseData.total_tuway()) +
                    (databaseData.total_green_conf() * databaseData.total_green())) /
                    (databaseData.total_punaw() + databaseData.total_tuway() + databaseData.total_green()).toFloat()
        } else {
            0f
        }

        totalShellsTextView.text = "Total Shells: $totalShells | Confidence: ${String.format("%.2f", total_shell_conf * 100)}%"

        val total_classes: TextView = findViewById(R.id.total_classes)
        total_classes.text = buildSpannedString {
            append("Manila: ")
            bold { append("${databaseData.total_punaw()}") }
            append(" | Green: ")
            bold { append("${databaseData.total_green()}") }
            append(" | Mud: ")
            bold { append("${databaseData.total_tuway()}") }
            append(" | not shell: ")
            bold { append("${databaseData.total_notshell()}") }
        }
        databaseData.rows
            .sortedBy { row -> row.datetime }
            .asReversed()
            .forEach { row ->
                // Create main row container
                val rowLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(16)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }

                // Create table header
                val headerLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }

                // Add date and delete button to header
                val dateTextView = TextView(this).apply {
                    text = "Date: ${row.datetime}"
                    textSize = 14f
                    setTextColor(ContextCompat.getColor(context, R.color.dark))
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                }

                val deleteButton = Button(this).apply {
                    setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.cross,
                        0,
                        0,
                        0
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

                headerLayout.addView(dateTextView)
                headerLayout.addView(deleteButton)

                // Create table content
                val tableLayout = TableLayout(this).apply {
                    layoutParams = TableLayout.LayoutParams(
                        TableLayout.LayoutParams.MATCH_PARENT,
                        TableLayout.LayoutParams.WRAP_CONTENT
                    )
                    setStretchAllColumns(true)
                }

                // Add column headers
                val headerRow = TableRow(this).apply {
                    val headers = arrayOf("Type", "Manila", "Mud", "Green")
                    headers.forEach { header ->
                        addView(TextView(context).apply {
                            text = header
                            textSize = 16f
                            typeface = Typeface.DEFAULT_BOLD
                            gravity = Gravity.CENTER
                            setPadding(8, 8, 8, 8)
                        })
                    }
                }

                // Add count row
                val countRow = TableRow(this).apply {
                    addView(TextView(context).apply {
                        text = "Count"
                        gravity = Gravity.START
                        setPadding(8, 8, 8, 8)
                    })
                    val counts = arrayOf(row.punaw, row.tuway, row.greenshell)
                    counts.forEach { count ->
                        addView(TextView(context).apply {
                            text = count.toString()
                            gravity = Gravity.CENTER
                            setPadding(8, 8, 8, 8)
                        })
                    }
                }

                // Add confidence row
                val confRow = TableRow(this).apply {
                    addView(TextView(context).apply {
                        text = "Confidence"
                        gravity = Gravity.START
                        setPadding(8, 8, 8, 8)
                    })
                    val confs = arrayOf(row.punaw_conf, row.tuway_conf, row.greenshell_conf)
                    confs.forEach { conf ->
                        addView(TextView(context).apply {
                            text = "${String.format(" % .2f", conf * 100)}%"
                            gravity = Gravity.CENTER
                            setPadding(8, 8, 8, 8)
                        })
                    }
                }

                val line = LinearLayout(this).apply {
                    id = R.id.grrenshell_line
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        topMargin = 10
                    }
                    orientation = LinearLayout.HORIZONTAL

                    // Add the left spacing view (5%)
                    addView(View(context).apply {
                        layoutParams = LinearLayout.LayoutParams(0, 1).apply {
                            weight = 0.05f
                            background = null
                        }
                    })

                    // Add the main line (90%)
                    addView(View(context).apply {
                        layoutParams = LinearLayout.LayoutParams(0, 1).apply {
                            weight = 0.9f
                            setBackgroundColor(Color.BLACK)
                        }
                    })

                    // Add the right spacing view (5%)
                    addView(View(context).apply {
                        layoutParams = LinearLayout.LayoutParams(0, 1).apply {
                            weight = 0.05f
                            background = null
                        }
                    })
                }

                // Add all rows to table
                tableLayout.apply {
                    addView(headerRow)
                    addView(countRow)
                    addView(confRow)
                }

                // Add header and table to main layout
                rowLayout.apply {
                    addView(headerLayout)
                    addView(tableLayout)
                    addView(line)
                }

                // Add the complete row to the main linear layout
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
                        "- not shell: ${row.notshell}\n" +
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