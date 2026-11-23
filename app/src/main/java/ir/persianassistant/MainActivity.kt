package ir.persianassistant

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var assistant: AssistantEngine
    private lateinit var offline: OfflineEngine
    private lateinit var online: OnlineEngine
    private lateinit var tools: ToolManager
    private lateinit var voice: VoiceManager
    private lateinit var response: TextView
    private lateinit var input: EditText
    private lateinit var note: EditText
    private lateinit var taskInput: EditText
    private lateinit var taskContainer: LinearLayout

    private val micPermission = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        assistant = AssistantEngine()
        offline = OfflineEngine()
        online = OnlineEngine()
        tools = ToolManager(this)

        val pad = (16 * resources.displayMetrics.density).toInt()
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
            layoutDirection = ViewGroup.LAYOUT_DIRECTION_RTL
            textDirection = View.TEXT_DIRECTION_RTL
            setBackgroundColor(0xFFF4F7F5.toInt())
        }

        val heading = TextView(this).apply {
            text = "پرشیان"
            textSize = 28f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(0xFF234B40.toInt())
            setPadding(0, 0, 0, pad)
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutDirection = ViewGroup.LAYOUT_DIRECTION_RTL
        }

        response = TextView(this).apply {
            text = "سلام! من پرشیان هستم. چطور می‌توانم کمک کنم؟"
            textSize = 17f
            setTextColor(0xFF202020.toInt())
            setPadding(0, pad, 0, pad)
        }
        input = EditText(this).apply {
            hint = "درخواست خود را بنویسید..."
            textSize = 16f
            minLines = 1
            maxLines = 4
            gravity = Gravity.RIGHT
        }
        val send = Button(this).apply {
            text = "ارسال درخواست"
            setOnClickListener { answer() }
        }
        val listen = Button(this).apply {
            text = "صحبت با پرشیان"
            setOnClickListener { startVoice() }
        }

        note = EditText(this).apply {
            hint = "یادداشت‌های شما..."
            minLines = 3
            gravity = Gravity.TOP or Gravity.RIGHT
            setText(tools.readNote())
        }
        val saveNote = Button(this).apply {
            text = "ذخیره یادداشت"
            setOnClickListener {
                showMessage(if (tools.saveNote(note.text.toString())) "یادداشت ذخیره شد" else "ذخیره یادداشت ناموفق بود")
            }
        }

        taskInput = EditText(this).apply {
            hint = "عنوان کار جدید..."
            gravity = Gravity.RIGHT
        }
        val addTask = Button(this).apply {
            text = "ثبت کار"
            setOnClickListener {
                val id = tools.addTask(taskInput.text.toString())
                if (id != null) {
                    taskInput.text.clear()
                    refreshTasks()
                    showMessage("کار ثبت شد")
                } else {
                    showMessage("لطفاً عنوان کار را وارد کنید")
                }
            }
        }

        val taskTitle = TextView(this).apply {
            text = "فهرست کارها"
            textSize = 19f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, pad, 0, pad / 2)
        }
        taskContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutDirection = ViewGroup.LAYOUT_DIRECTION_RTL
        }

        content.addView(response)
        content.addView(input)
        content.addView(send)
        content.addView(listen)
        content.addView(note)
        content.addView(saveNote)
        content.addView(taskInput)
        content.addView(addTask)
        content.addView(taskTitle)
        content.addView(taskContainer)

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            addView(content)
        }
        root.addView(heading)
        root.addView(scroll, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
        ))
        setContentView(root)

        voice = VoiceManager(this) { spokenText ->
            runOnUiThread {
                input.setText(spokenText)
                input.setSelection(input.text.length)
                answer()
            }
        }
        refreshTasks()
    }

    private fun answer() {
        val text = input.text.toString().trim()
        if (text.isEmpty()) {
            showMessage("لطفاً درخواست خود را بنویسید")
            return
        }
        val decision = assistant.analyze(text)
        val result = when (decision.type) {
            AssistantEngine.RequestType.LOCAL -> handleLocalRequest(text)
            AssistantEngine.RequestType.ONLINE -> online.respond(text)
            AssistantEngine.RequestType.UNKNOWN -> decision.message
        }
        response.text = result
        voice.speak(result)
    }


    /**
     * اجرای فرمان‌های پایه به‌صورت کاملاً محلی؛ اطلاعات از گوشی خارج نمی‌شوند.
     * نمونه: «یادداشت: خرید نان» یا «ثبت کار: تماس با مشتری»
     */
    private fun handleLocalRequest(rawText: String): String {
        val text = rawText.trim()
        val normalized = text
            .replace('ي', 'ی')
            .replace('ك', 'ک')

        return when {
            normalized.startsWith("یادداشت:") ||
                    normalized.startsWith("ذخیره یادداشت:") -> {
                val value = normalized.substringAfter(':').trim()
                if (value.isBlank()) {
                    "متن یادداشت خالی است. بعد از دونقطه، متن یادداشت را بنویسید."
                } else if (tools.saveNote(value)) {
                    note.setText(value)
                    "یادداشت شما روی همین گوشی ذخیره شد."
                } else {
                    "ذخیره یادداشت انجام نشد. دوباره تلاش کنید."
                }
            }

            normalized.contains("پاک کردن یادداشت") ||
                    normalized.contains("حذف یادداشت") -> {
                if (tools.saveNote("")) {
                    note.setText("")
                    "یادداشت پاک شد."
                } else {
                    "پاک کردن یادداشت ناموفق بود."
                }
            }

            normalized.startsWith("ثبت کار:") ||
                    normalized.startsWith("کار جدید:") -> {
                val title = normalized.substringAfter(':').trim()
                if (title.isBlank()) {
                    "عنوان کار خالی است. نمونه: ثبت کار: تماس با مشتری"
                } else if (tools.addTask(title) != null) {
                    refreshTasks()
                    "کار جدید ثبت شد: $title"
                } else {
                    "ثبت کار ناموفق بود."
                }
            }

            normalized.contains("فهرست کارها") ||
                    normalized.contains("کارهای من") ||
                    normalized == "کارها" -> {
                val tasks = tools.getTasks()
                if (tasks.isEmpty()) {
                    "هنوز کاری ثبت نکرده‌اید."
                } else {
                    tasks.joinToString("\n") { task ->
                        (if (task.completed) "✓ " else "○ ") + task.title
                    }
                }
            }

            normalized == "یادداشت" ||
                    normalized == "یادداشت‌ها" ||
                    normalized == "نمایش یادداشت" -> {
                tools.readNote().ifBlank { "هنوز یادداشتی ذخیره نشده است." }
            }

            else -> offline.respond(text)
        }
    }

    private fun startVoice() {
        if (!::voice.isInitialized) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), micPermission)
        } else {
            voice.startListening()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == micPermission) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                voice.startListening()
            } else {
                showMessage("اجازه استفاده از میکروفون داده نشد")
            }
        }
    }

    private fun refreshTasks() {
        taskContainer.removeAllViews()
        val tasks = tools.getTasks()
        if (tasks.isEmpty()) {
            taskContainer.addView(TextView(this).apply {
                text = "هنوز کاری ثبت نشده است."
                textSize = 15f
                setPadding(0, 8, 0, 8)
            })
            return
        }
        tasks.forEach { task ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutDirection = ViewGroup.LAYOUT_DIRECTION_RTL
            }
            val label = TextView(this).apply {
                text = (if (task.completed) "✓ " else "○ ") + task.title
                textSize = 15f
                setPadding(4, 8, 4, 8)
                if (task.completed) setTextColor(0xFF777777.toInt())
            }
            row.addView(label, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            if (!task.completed) {
                row.addView(Button(this).apply {
                    text = "انجام شد"
                    setOnClickListener {
                        if (tools.completeTask(task.id)) refreshTasks()
                    }
                })
            }
            row.addView(Button(this).apply {
                text = "حذف"
                setOnClickListener {
                    if (tools.deleteTask(task.id)) refreshTasks()
                    else showMessage("حذف کار ناموفق بود")
                }
            })
            taskContainer.addView(row)
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        if (::voice.isInitialized) voice.release()
        super.onDestroy()
    }
}
