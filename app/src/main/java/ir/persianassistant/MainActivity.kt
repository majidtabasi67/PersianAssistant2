
package ir.persianassistant

import android.app.Activity
import android.os.Bundle
import android.graphics.Typeface
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private val prefs by lazy {
        getSharedPreferences("persianassistant", MODE_PRIVATE)
    }

    private lateinit var note: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pad = (20 * resources.displayMetrics.density).toInt()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad * 2, pad, pad)
            layoutDirection = ViewGroup.LAYOUT_DIRECTION_RTL
            setBackgroundColor(0xFFF4F7F5.toInt())
        }

        val heading = TextView(this).apply {
            text = "پرشیان"
            textSize = 30f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(0xFF234B40.toInt())
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            text = "دستیار شخصی شما — نسخه آغازین"
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, pad / 2, 0, pad)
        }

        note = EditText(this).apply {
            hint = "یادداشت خود را اینجا بنویسید..."
            minLines = 6
            gravity = Gravity.TOP or Gravity.RIGHT
            setText(prefs.getString("note", ""))
            textSize = 17f
            setPadding(pad / 2, pad / 2, pad / 2, pad / 2)
        }

        val save = Button(this).apply {
            text = "ذخیره یادداشت"

            setOnClickListener {
                prefs.edit()
                    .putString("note", note.text.toString())
                    .apply()

                Toast.makeText(
                    this@MainActivity,
                    "یادداشت روی گوشی ذخیره شد",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        val info = TextView(this).apply {
            text = "این نسخه پایه است. امکانات عکس، کارها، یادآوری و پشتیبان‌گیری در مراحل بعد اضافه می‌شوند."
            textSize = 14f
            setPadding(0, pad, 0, 0)
        }

        root.addView(heading)
        root.addView(subtitle)
        root.addView(
            note,
            LinearLayout.LayoutParams(-1, 0, 1f)
        )
        root.addView(save)
        root.addView(info)

        setContentView(root)
    }
}
