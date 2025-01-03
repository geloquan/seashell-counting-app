import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import android.content.Context
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.round
import kotlin.math.roundToInt

@Serializable
data class Row(
    val id: Int,
    val punaw: Int = 0,
    val greenshell: Int = 0,
    val tuway: Int = 0,
    val notshell: Int = 0,
    val punaw_conf: Float = 0f,
    val greenshell_conf: Float = 0f,
    val tuway_conf: Float = 0f,
    val notshell_conf: Float = 0f,
    val datetime: String = ""
)

@Serializable
data class DatabaseData(
    var rows: MutableList<Row> = mutableListOf()
) {
    fun total_shells(): Int = try {
        rows.sumOf { it.punaw + it.greenshell + it.tuway }
    } catch (e: Exception) {
        Log.e("DatabaseData", "Error calculating total shells", e)
        0
    }

    fun total_punaw(): Int = try {
        rows.sumOf { it.punaw }
    } catch (e: Exception) {
        Log.e("DatabaseData", "Error calculating total punaw", e)
        0
    }

    fun total_green(): Int = try {
        rows.sumOf { it.greenshell }
    } catch (e: Exception) {
        Log.e("DatabaseData", "Error calculating total green", e)
        0
    }

    fun total_tuway(): Int = try {
        rows.sumOf { it.tuway }
    } catch (e: Exception) {
        Log.e("DatabaseData", "Error calculating total tuway", e)
        0
    }

    fun total_notshell(): Int = try {
        rows.sumOf { it.notshell }
    } catch (e: Exception) {
        Log.e("DatabaseData", "Error calculating total notshell", e)
        0
    }

    private fun Float.to4Decimals(): Float {
        return (this * 10000).roundToInt() / 10000f
    }

    private fun calculateWeightedAverage(
        counts: List<Int>,
        confidences: List<Float>
    ): Float = try {
        val totalCount = counts.sum()
        if (totalCount > 0) {
            (counts.zip(confidences) { count, conf -> count * conf }.sum() / totalCount).to4Decimals()
        } else 0f
    } catch (e: Exception) {
        Log.e("DatabaseData", "Error calculating weighted average", e)
        0f
    }

    fun total_punaw_conf(): Float = calculateWeightedAverage(
        rows.map { it.punaw },
        rows.map { it.punaw_conf.to4Decimals() }
    )

    fun total_green_conf(): Float = calculateWeightedAverage(
        rows.map { it.greenshell },
        rows.map { it.greenshell_conf.to4Decimals() }
    )

    fun total_tuway_conf(): Float = calculateWeightedAverage(
        rows.map { it.tuway },
        rows.map { it.tuway_conf.to4Decimals() }
    )

    fun total_notshell_conf(): Float = calculateWeightedAverage(
        rows.map { it.notshell },
        rows.map { it.notshell_conf.to4Decimals() }
    )
}

class DatabaseManager(private val context: Context) {
    companion object {
        private const val TAG = "DatabaseManager"
        private const val FILENAME = "data.json"

        private val json = Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            isLenient = true
            encodeDefaults = true
        }

        private fun formatDate(): String = try {
            SimpleDateFormat("MMM d, yyyy h:mm a", Locale.US).format(Date())
        } catch (e: Exception) {
            Log.e(TAG, "Error formatting date", e)
            "Date Error"
        }

        private fun Float.to4Decimals(): Float {
            return (this * 10000).roundToInt() / 10000f
        }
    }

    private fun getDataFile(): File {
        return File(context.filesDir, FILENAME)
    }

    fun read(): DatabaseData {
        return try {
            val file = getDataFile()

            if (!file.exists()) {
                Log.d(TAG, "Data file doesn't exist, creating new one")
                return createNewDatabase()
            }

            val fileContent = file.readText()
            if (fileContent.isBlank()) {
                Log.d(TAG, "Data file is empty, creating new database")
                return createNewDatabase()
            }

            json.decodeFromString<DatabaseData>(fileContent).also {
                // Clean and round all confidence values to 4 decimals
                it.rows = it.rows.map { row ->
                    row.copy(
                        punaw_conf = row.punaw_conf.to4Decimals(),
                        greenshell_conf = row.greenshell_conf.to4Decimals(),
                        tuway_conf = row.tuway_conf.to4Decimals(),
                        notshell_conf = row.notshell_conf.to4Decimals()
                    )
                }.toMutableList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading database", e)
            createNewDatabase()
        }
    }

    private fun createNewDatabase(): DatabaseData {
        val initialData = DatabaseData()
        try {
            val initialContent = json.encodeToString(initialData)
            getDataFile().writeText(initialContent)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating new database", e)
        }
        return initialData
    }

    fun update(
        punaw: Int,
        greenshell: Int,
        tuway: Int,
        notshell: Int,
        punaw_conf: Float,
        greenshell_conf: Float,
        tuway_conf: Float,
        notshell_conf: Float
    ) {
        try {
            val currentData = read()
            val newId = (currentData.rows.maxOfOrNull { it.id } ?: 0) + 1

            val newRow = Row(
                id = newId,
                punaw = punaw.coerceAtLeast(0),
                greenshell = greenshell.coerceAtLeast(0),
                tuway = tuway.coerceAtLeast(0),
                notshell = notshell.coerceAtLeast(0),
                punaw_conf = punaw_conf.to4Decimals().coerceIn(0f, 100f),
                greenshell_conf = greenshell_conf.to4Decimals().coerceIn(0f, 100f),
                tuway_conf = tuway_conf.to4Decimals().coerceIn(0f, 100f),
                notshell_conf = notshell_conf.to4Decimals().coerceIn(0f, 100f),
                datetime = formatDate()
            )

            currentData.rows.add(newRow)
            saveDatabase(currentData)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating database", e)
            throw e
        }
    }

    fun resetAllData() {
        try {
            createNewDatabase()
        } catch (e: Exception) {
            Log.e(TAG, "Error resetting database", e)
            throw e
        }
    }

    fun deleteRow(id: Int) {
        try {
            val currentData = read()
            currentData.rows.removeIf { it.id == id }
            reindexRows(currentData)
            saveDatabase(currentData)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting row", e)
            throw e
        }
    }

    private fun reindexRows(data: DatabaseData) {
        data.rows.sortBy { it.id }
        data.rows.forEachIndexed { index, row ->
            data.rows[index] = row.copy(id = index + 1)
        }
    }

    private fun saveDatabase(data: DatabaseData) {
        try {
            val jsonContent = json.encodeToString(data)
            getDataFile().writeText(jsonContent)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving database", e)
            throw e
        }
    }
}