package nl.booxchange

import android.app.Application
import nl.booxchange.api.APIClient
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

/**
 * Created by Cristian Velinciuc on 3/10/18.
 */
class BooxchangeApp: Application() {
  override fun onCreate() {
    super.onCreate()

    APIClient
    delegate = this

    FacebookSdk.sdkInitialize(getApplicationContext());
    AppEventsLogger.activateApp(this);
  }

  companion object {
    lateinit var delegate: BooxchangeApp
  }
}
