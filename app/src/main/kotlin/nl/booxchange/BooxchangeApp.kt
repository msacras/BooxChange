package nl.booxchange

import android.app.Application
import nl.booxchange.api.APIClient
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import nl.booxchange.model.BookModel
import nl.booxchange.model.UserModel
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric

/**
 * Created by Cristian Velinciuc on 3/10/18.
 */
class BooxchangeApp: Application() {
  override fun onCreate() {
    super.onCreate()

/*
    if (LeakCanary.isInAnalyzerProcess(this)) return
    LeakCanary.install(this)
*/

    APIClient
    delegate = this

    FacebookSdk.sdkInitialize(applicationContext)
    AppEventsLogger.activateApp(this)
//    Fabric.with(this, Crashlytics())

  }

  companion object {
    lateinit var delegate: BooxchangeApp
  }
}
