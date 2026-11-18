
package ir.persianassistant

import android.app.Activity
import android.os.Bundle
import android.content.pm.PackageManager
import android.Manifest
import android.graphics.Typeface
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import android.text.InputType

class MainActivity : Activity() {

    private lateinit var assistant: AssistantEngine
    private lateinit var offline: OfflineEngine
    private lateinit var online: OnlineEngine
    private lateinit var memory: MemoryManager
    private lateinit var tools: ToolManager
    private lateinit var voice: VoiceManager

    private lateinit var response: TextView
    private lateinit var input: EditText
    private lateinit var note: EditText
    private lateinit var taskInput: EditText

    private val micPermission = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        assistant = AssistantEngine()
        offline = OfflineEngine()
        online = OnlineEngine()
        memory = MemoryManager(this)
        tools = ToolManager(this)

        voice = VoiceManager(this) { spokenText ->
            runOnUiThread {
                input.setText(spokenText)
                answer()
            }
        }

        val pad = (20 * resources.displayMetrics.density).toInt()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
            layoutDirection = ViewGroup.LAYOUT_DIRECTION_RTL
            setBackgroundColor(0xFFF4F7F5.toInt())
        }

        val heading = TextView(this).apply {
            text = "پرشیان"
            textSize = 30f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(0xFF234B40.toInt())
        }

        response = TextView(this).apply {
            text = "سلام! من پرشیان هستم. چطور می‌توانم کمک کنم؟"
            textSize = 17f
            setPadding(0, pad, 0, pad)
        }

        input = EditText(this).apply {
            hint = "درخواست خود را بنویسید..."
            textSize = 16f
        }

        val send = Button(this).apply {
            text = "ارسال"
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
            setText(memory.read("main_note") ?: "")
        }

        val saveNote = Button(this).apply {
            text = "ذخیره یادداشت"
            setOnClickListener {
                val saved = tools.saveNote(note.text.toString())
                showMessage(
                    if (saved) "یادداشت ذخیره شد"
                    else "ذخیره یادداشت ناموفق بود"
                )
            }
        }

        taskInput = EditText(this).apply {
            hint = "کار جدید..."
        }

        val addTask = Button(this).apply {
            text = "ثبت کار"
            setOnClickListener {
                val id = tools.addTask(taskInput.text.toString())
                if (id != null) {
                    taskInput.text.clear()
                    showTasks()
                    showMessage("کار ثبت شد")
                } else {
                    showMessage("عنوان کار را وارد کنید")
                }
            }
        }

        val tasksTitle = TextView(this).apply {
            text = "فهرست کارها"
            textSize = 19f
            typeface = Typeface.DEFAULT_BOLD
        }

        val taskList = TextView(this).apply {
            textSize = 15f
        }

        root.addView(heading)
        root.addView(response)
        root.addView(input)
        root.addView(send)
        root.addView(listen)
        root.addView(note)
        root.addView(saveNote)
        root.addView(taskInput)
        root.addView(addTask)
        root.addView(tasksTitle)
        root.addView(taskList)

        setContentView(root)

        showTasks()
    }

    private fun answer() {
        val text = input.text.toString().trim()

        if (text.isEmpty()) {
            showMessage("لطفاً درخواست خود را بنویسید")
            return
        }

        val decision = assistant.analyze(text)

        val result = when (decision.type) {
            AssistantEngine.RequestType.LOCAL ->
                offline.respond(text)

            AssistantEngine.RequestType.ONLINE ->
                online.respond(text)

            AssistantEngine.RequestType.UNKNOWN ->
                decision.message
        }

        response.text = result
        voice.speak(result)
    }

    private fun startVoice() {
        if (android.os.Build.VERSION.SDK_INT >= 23 &&
            checkSelfPermission(Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.RECORD_AUDIO),
                micPermission
            )
        } else {
            voice.startListening()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode, permissions, grantResults
        )

        if (requestCode == micPermission &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            voice.startListening()
        } else if (requestCode == micPermission) {
            showMessage("اجازه استفاده از میکروفون داده نشد")
        }
    }

    private fun showTasks() {
        val tasks = tools.getTasks()
        val message = if (tasks.isEmpty()) {
            "هنوز کاری ثبت نشده است."
        } else {
            tasks.joinToString("\n") {
                val status = if (it.completed) "✓" else "○"
                "$status ${it.title}"
            }
        }

        // فهرست کارها در مرحله بعد به نمایش تعاملی تبدیل می‌شود.
        response.text = response.text
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        voice.release()
        super.onDestroy()
    }
}
