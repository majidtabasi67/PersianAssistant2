
package ir.persianassistant

/**
 * مدیریت ارتباط پرشیان با هوش مصنوعی آنلاین
 *
 * ارتباط واقعی در مرحله بعد از طریق سرور امن اضافه می‌شود.
 * کلید API نباید داخل برنامه اندروید قرار بگیرد.
 */

interface OnlineProvider {
    fun ask(prompt: String): String
}

class OnlineEngine(
    private val provider: OnlineProvider? = null
) {

    fun isAvailable(): Boolean {
        return provider != null
    }

    fun respond(userText: String): String {
        if (userText.isBlank()) {
            return "لطفاً درخواست خود را وارد کنید."
        }

        val onlineProvider = provider
            ?: return "ارتباط با هوش آنلاین هنوز راه‌اندازی نشده است."

        return try {
            onlineProvider.ask(userText)
        } catch (error: Exception) {
            "ارتباط با هوش آنلاین برقرار نشد. لطفاً دوباره تلاش کنید."
        }
    }
}
