package nl.booxchange.model

import android.content.Intent
import nl.booxchange.utilities.BaseFragment

data class StartActivity (val intent: Intent, val requestCode: Int?, val fromFragment: Class<*>)
