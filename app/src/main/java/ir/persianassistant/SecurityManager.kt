
package ir.persianassistant

import android.content.Context

/**
 * مدیریت اجازه‌ها و تأییدهای امنیتی پرشیان
 */
class SecurityManager(context: Context) {

    private val preferences = context.getSharedPreferences(
        "persianassistant_security",
        Context.MODE_PRIVATE
    )

    // اجازه ارسال اطلاعات خصوصی به هوش آنلاین
    fun setOnlinePrivateDataPermission(
        allowed: Boolean
    ): Boolean {
        return preferences.edit()
            .putBoolean("online_private_data", allowed)
            .commit()
    }

    fun canSendPrivateDataOnline(): Boolean {
        return preferences.getBoolean(
            "online_private_data",
            false
        )
    }

    // بررسی نیاز به تأیید عملیات حساس
    fun requiresConfirmation(): Boolean {
        return true
    }

    // پاک کردن اجازه ارسال اطلاعات خصوصی
    fun revokeOnlinePermission(): Boolean {
        return preferences.edit()
            .putBoolean("online_private_data", false)
            .commit()
    }

    // بررسی وجود اجازه برای یک عملیات
    fun isActionAllowed(
        isPrivate: Boolean,
        isConfirmed: Boolean
    ): Boolean {
        if (isPrivate && !canSendPrivateDataOnline()) {
            return false
        }

        return isConfirmed
    }
}
