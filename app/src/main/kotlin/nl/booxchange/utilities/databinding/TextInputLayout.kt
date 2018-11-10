package nl.booxchange.utilities.databinding

import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputLayout
import nl.booxchange.extension.takeNotBlank

@BindingAdapter("error")
fun TextInputLayout.setErrorText(errorText: String?) {
    error = errorText?.takeNotBlank
    isErrorEnabled = error != null
}
