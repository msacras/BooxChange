package nl.booxchange.utilities.databinding

import androidx.databinding.BindingAdapter
import androidx.appcompat.widget.AppCompatEditText
import android.text.Editable
import android.text.TextWatcher

@BindingAdapter("textFormat")
fun AppCompatEditText.setTextFormatting(template: String?) {
    template ?: return

    addTextChangedListener(object: TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable) {
            val input = s.toString().replace("[^0-9]+".toRegex(), "")

            var charIndex = 0
            val formatted = template.toMutableList().let { template ->
                template.indices.forEach { i ->
                    if (template[i] == '#' && charIndex < input.length) {
                        template[i] = input[charIndex++]
                    }
                }
                template.joinToString("").trim('-', '#')
            }

            if (s.toString() != formatted) {
                val cursorPosition = if (selectionStart == s.toString().length) formatted.length else selectionStart
                setText(formatted)
                setSelection(cursorPosition.coerceAtMost(formatted.length))
            }
        }
    })
}
