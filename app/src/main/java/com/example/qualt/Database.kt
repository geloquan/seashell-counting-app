import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import android.content.Context

@Serializable
data class DatabaseData(
    var punaw: Int = 0,
    var greenshell: Int = 0,
    var tuway: Int = 0
) {
    fun total_shells(): Int {
        return punaw + greenshell + tuway
    }
}

class DatabaseManager(private val context: Context) {
    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
            prettyPrint = true
        }
        private const val FILENAME = "data.json"
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
        return json.decodeFromString<DatabaseData>(fileContent)
    }

    fun update(punaw: Int, greenshell: Int, tuway: Int) {
        val currentData = read()
        currentData.punaw += punaw
        currentData.greenshell += greenshell
        currentData.tuway += tuway

        val jsonContent = json.encodeToString(currentData)
        getDataFile().writeText(jsonContent)
    }
}