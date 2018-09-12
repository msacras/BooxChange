package nl.booxchange.utilities.databinding

import android.databinding.BindingAdapter
import android.support.design.widget.TextInputLayout
import nl.booxchange.extension.takeNotBlank

@BindingAdapter("error")
fun TextInputLayout.setErrorText(errorText: String?) {
    error = errorText?.takeNotBlank
    isErrorEnabled = error != null
}
