
package ir.persianassistant

import android.content.Context

/**
 * حافظه محلی پرشیان
 * ذخیره و بازیابی اطلاعات بدون نیاز به اینترنت
 */
class MemoryManager(context: Context) {

    private val preferences = context.getSharedPreferences(
        "persianassistant_memory",
        Context.MODE_PRIVATE
    )

    fun save(key: String, value: String): Boolean {
        if (key.isBlank()) return false

        return preferences.edit()
            .putString(key, value)
            .commit()
    }

    fun read(key: String): String? {
        return preferences.getString(key, null)
    }

    fun delete(key: String): Boolean {
        return preferences.edit()
            .remove(key)
            .commit()
    }

    fun getAllKeys(): Set<String> {
        return preferences.all.keys
    }

    fun clearAll(): Boolean {
        return preferences.edit()
            .clear()
            .commit()
    }
}
