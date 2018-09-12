package nl.booxchange.model.events

import android.content.Intent

data class StartActivity(val intent: Intent, val requestCode: Int?, val fromFragment: Class<*>)
