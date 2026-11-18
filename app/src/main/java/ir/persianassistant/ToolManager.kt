
package ir.persianassistant

import android.content.Context
import java.util.UUID

/**
 * مدیریت ابزارهای شخصی پرشیان
 */
class ToolManager(context: Context) {

    private val memory = MemoryManager(context)

    private val preferences = context.getSharedPreferences(
        "persianassistant_tasks",
        Context.MODE_PRIVATE
    )

    data class Task(
        val id: String,
        val title: String,
        val completed: Boolean
    )

    // ذخیره یادداشت
    fun saveNote(text: String): Boolean {
        return memory.save("main_note", text)
    }

    // خواندن یادداشت
    fun readNote(): String {
        return memory.read("main_note") ?: ""
    }

    // افزودن کار جدید
    fun addTask(title: String): String? {
        if (title.isBlank()) return null

        val id = UUID.randomUUID().toString()

        val saved = preferences.edit()
            .putString("task_$id", title)
            .putBoolean("done_$id", false)
            .commit()

        return if (saved) id else null
    }

    // دریافت فهرست کارها
    fun getTasks(): List<Task> {
        return preferences.all.keys
            .filter { it.startsWith("task_") }
            .mapNotNull { key ->
                val id = key.removePrefix("task_")
                val title = preferences.getString(key, null)
                    ?: return@mapNotNull null

                Task(
                    id = id,
                    title = title,
                    completed = preferences.getBoolean(
                        "done_$id",
                        false
                    )
                )
            }
    }

    // تکمیل یک کار
    fun completeTask(id: String): Boolean {
        if (preferences.getString("task_$id", null) == null) {
            return false
        }

        return preferences.edit()
            .putBoolean("done_$id", true)
            .commit()
    }

    // حذف یک کار
    fun deleteTask(id: String): Boolean {
        return preferences.edit()
            .remove("task_$id")
            .remove("done_$id")
            .commit()
    }
}
