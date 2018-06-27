package nl.booxchange

import android.app.Application
import android.content.Context
import android.graphics.Typeface
import android.util.Log
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import nl.booxchange.api.APIClient
import nl.booxchange.screens.MainFragmentActivity
import nl.booxchange.utilities.BaseActivity
import java.util.*


/**
 * Created by Cristian Velinciuc on 3/10/18.
 */
class BooxchangeApp: Application() {
    val activityStack = ArrayList<BaseActivity>()

  override fun onCreate() {
    super.onCreate()

/*
    if (LeakCanary.isInAnalyzerProcess(this)) return
    LeakCanary.install(this)
*/

    APIClient
    delegate = this

    AppEventsLogger.activateApp(this)
//    Fabric.with(this, Crashlytics())

//      overrideFont(this, "SERIF", "Lato-Regular")
  }

/*
    private fun overrideFont(context: Context, defaultFontNameToOverride: String, customFontFileNameInAssets: String) {
        try {
            val customFontTypeface = Typeface.createFromAsset(context.assets, "fonts/$customFontFileNameInAssets.ttf")
            val defaultFontTypefaceField = Typeface::class.java.getDeclaredField(defaultFontNameToOverride)
            defaultFontTypefaceField.isAccessible = true
            defaultFontTypefaceField.set(null, customFontTypeface)
        } catch (e: Exception) {
            Log.e(this::class.java.name, "Can not set custom font $customFontFileNameInAssets instead of $defaultFontNameToOverride")
        }
    }
*/

  companion object {
    lateinit var delegate: BooxchangeApp
    var mainActivityDelegate: MainFragmentActivity? = null
  }
}
