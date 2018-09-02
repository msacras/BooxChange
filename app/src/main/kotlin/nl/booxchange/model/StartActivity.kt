package nl.booxchange.model

import android.content.Intent

data class StartActivity(val intent: Intent, val requestCode: Int?, val fromFragment: Class<*>)
