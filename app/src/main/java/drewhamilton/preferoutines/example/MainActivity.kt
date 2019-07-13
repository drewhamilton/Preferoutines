package drewhamilton.preferoutines.example

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import com.google.android.material.snackbar.Snackbar
import drewhamilton.preferoutines.awaitInt
import drewhamilton.preferoutines.awaitString
import drewhamilton.preferoutines.extras.awaitEdits
import drewhamilton.preferoutines.getIntFlow
import drewhamilton.preferoutines.getStringFlow
import kotlinx.android.synthetic.main.edit.editIntegerValue
import kotlinx.android.synthetic.main.edit.editStringValue
import kotlinx.android.synthetic.main.edit.putButton
import kotlinx.android.synthetic.main.edit.removeButton
import kotlinx.android.synthetic.main.observe.integerValue
import kotlinx.android.synthetic.main.observe.stringValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@UseExperimental(FlowPreview::class)
class MainActivity : AppCompatActivity() {

    private val activityJob = Job()
    private val activityScope = CoroutineScope(Dispatchers.Main + activityJob)

    private val preferences: SharedPreferences by lazy { getPreferences(Context.MODE_PRIVATE) }

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

        activityScope.launch {
            editStringValue.setText(preferences.awaitString(Keys.EXAMPLE_STRING, Defaults.STRING))
            editIntegerValue.setText(preferences.awaitInt(Keys.EXAMPLE_INT, Defaults.INT).toString())
        }

        activityScope.launch {
            preferences.getStringFlow(Keys.EXAMPLE_STRING, Defaults.STRING)
                .collect { value ->
                    stringValue.text = value
                }
        }
        activityScope.launch {
            preferences.getIntFlow(Keys.EXAMPLE_INT, Defaults.INT)
                .collect { value ->
                    integerValue.text = value.toString()
                }
        }

        putButton.setOnClickListener {
            activityScope.launch {
                val success = preferences.awaitEdits {
                    putString(Keys.EXAMPLE_STRING, editStringValue.textAsString.nullIfEmpty())
                    putInt(Keys.EXAMPLE_INT, editIntegerValue.textAsString.toInt())
                }
                if (!success) displayError()
            }
        }
        removeButton.setOnClickListener {
            activityScope.launch {
                val success = preferences.awaitEdits {
                    remove(Keys.EXAMPLE_STRING)
                    remove(Keys.EXAMPLE_INT)
                }
                if (success) {
                    editStringValue.text = null
                    editIntegerValue.setText(Defaults.INT.toString())
                } else displayError()
            }
        }
    }

    override fun onDestroy() {
        activityJob.cancel()
        super.onDestroy()
    }

    private val TextView.textAsString get() = text.toString()
    private fun String?.nullIfEmpty() = if (this == "") null else this

    private fun displayError() {
        Snackbar.make(putButton, "Error!", Snackbar.LENGTH_LONG)
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

    private object Keys {
        const val EXAMPLE_STRING = "Example string"
        const val EXAMPLE_INT = "Example int"
    }

    private object Defaults {
        const val STRING = ""
        const val INT = 0
    }
}
