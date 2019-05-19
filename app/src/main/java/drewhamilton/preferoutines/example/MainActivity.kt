package drewhamilton.preferoutines.example

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import kotlinx.android.synthetic.main.edit.editIntegerValue
import kotlinx.android.synthetic.main.edit.putButton


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyNoLimits()
        setContentView(R.layout.main)

        editIntegerValue.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                putButton.isEnabled = s?.isNotEmpty() ?: false
            }
        })
    }

    private fun applyNoLimits() {
        val window = window
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        )
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { _, insets ->
            val contentView = findViewById<View>(android.R.id.content)
            contentView.setPadding(
                insets.stableInsetLeft,
                insets.stableInsetTop,
                insets.stableInsetRight,
                insets.stableInsetBottom
            )
            insets
        }
    }
}
