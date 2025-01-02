import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import android.content.Context
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.Date
import java.util.Locale

@Serializable
data class Row(
    val id: Int,
    val punaw: Int,
    val greenshell: Int,
    val tuway: Int,
    val datetime: String
)

@Serializable
data class DatabaseData(
    var rows: MutableList<Row> = mutableListOf()
) {
    fun total_shells(): Int {
        return rows.sumOf { it.punaw + it.greenshell + it.tuway }
    }
    fun total_punaw(): Int {
        return rows.sumOf { it.punaw }
    }
    fun total_green(): Int {
        return rows.sumOf { it.greenshell }
    }
    fun total_tuway(): Int {
        return rows.sumOf { it.tuway }
    }
}

class DatabaseManager(private val context: Context) {
    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
            prettyPrint = true
        }
        private const val FILENAME = "data.json"

        private fun formatDate(): String {
            // Updated to use US date format with hours and minutes
            val dateFormat = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.US)
            return dateFormat.format(Date())
        }
    }

    fun resetAllData() {
        val initialData = DatabaseData()
        val initialContent = json.encodeToString(initialData)
        getDataFile().writeText(initialContent)
    }
    fun deleteRow(id: Int) {
        val currentData = read()

        // Remove the row with the matching id
        currentData.rows.removeIf { it.id == id }

        // Re-index the remaining rows to maintain continuous ID sequence
        currentData.rows.forEachIndexed { index, row ->
            currentData.rows[index] = row.copy(id = index + 1)
        }

        val jsonContent = json.encodeToString(currentData)
        getDataFile().writeText(jsonContent)
    }

    private fun getDataFile(): File {
        val directory = context.filesDir
        return File(directory, FILENAME)
    }

    fun read(): DatabaseData {
        val file = getDataFile()

        if (!file.exists()) {
            val initialData = DatabaseData()
            val initialContent = json.encodeToString(initialData)
            file.writeText(initialContent)
            return initialData
        }

        val fileContent = file.readText()
        return json.decodeFromString(fileContent)
    }

    fun update(punaw: Int, greenshell: Int, tuway: Int) {
        val currentData = read()

        val newRow = Row(
            id = currentData.rows.size + 1,
            punaw = punaw,
            greenshell = greenshell,
            tuway = tuway,
            datetime = formatDate()
        )

        currentData.rows.add(newRow)

        val jsonContent = json.encodeToString(currentData)
        getDataFile().writeText(jsonContent)
    }
}