
package ir.persianassistant

/**
 * هسته مرکزی مغز پرشیان
 * مسئول تشخیص نوع درخواست و انتخاب مسیر پاسخ‌گویی
 */
class AssistantEngine {

    enum class RequestType {
        LOCAL,
        ONLINE,
        UNKNOWN
    }

    data class Decision(
        val type: RequestType,
        val message: String
    )

    fun analyze(userText: String): Decision {

        val text = userText.trim()
            .replace('ي', 'ی')
            .replace('ك', 'ک')

        if (text.isEmpty()) {
            return Decision(
                RequestType.UNKNOWN,
                "لطفاً درخواست خود را بنویسید."
            )
        }

        val localWords = listOf(
            "یادداشت",
            "ثبت کار:",
            "کار جدید:",
            "فهرست کارها",
            "کارهای من",
            "کارها",
            "پاک کردن یادداشت",
            "حذف یادداشت"
        )

        val isLocalRequest = localWords.any {
            text.contains(it)
        }

        return if (isLocalRequest) {
            Decision(
                RequestType.LOCAL,
                "این درخواست باید توسط امکانات داخلی پرشیان انجام شود."
            )
        } else {
            Decision(
                RequestType.ONLINE,
                "برای پاسخ هوشمند، درخواست به موتور آنلاین ارجاع داده می‌شود."
            )
        }
    }
}
