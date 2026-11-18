
package ir.persianassistant

/**
 * موتور پاسخ‌گویی اولیه بدون اینترنت
 */
class OfflineEngine {

    fun respond(userText: String): String {
        val text = userText.trim()

        if (text.isEmpty()) {
            return "لطفاً چیزی بنویسید."
        }

        return when {
            text.contains("سلام") ->
                "سلام! من پرشیان هستم. چطور می‌توانم کمک کنم؟"

            text.contains("خوبی") ->
                "ممنونم! آماده‌ام به شما کمک کنم."

            text.contains("اسم") ->
                "نام من پرشیان است."

            text.contains("ساعت") ->
                "برای اعلام ساعت دقیق، باید ساعت گوشی بررسی شود."

            text.contains("کمک") ->
                "می‌توانم در مدیریت یادداشت‌ها، کارها و اطلاعات شخصی کمک کنم."

            else ->
                "این درخواست به دانش بیشتری نیاز دارد. با اتصال اینترنت می‌توانم از موتور هوشمند آنلاین کمک بگیرم."
        }
    }
}
