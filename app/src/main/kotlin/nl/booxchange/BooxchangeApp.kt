package nl.booxchange

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.facebook.appevents.AppEventsLogger
import nl.booxchange.api.APIClient


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

    AppEventsLogger.activateApp(this)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val notificationManager = getSystemService(NotificationManager::class.java)
      notificationManager.createNotificationChannel(NotificationChannel("booxchange.channel.messaging", "Messages", NotificationManager.IMPORTANCE_HIGH))
    }
//    Fabric.with(this, Crashlytics())
  }

  companion object {
    lateinit var delegate: BooxchangeApp
    var isInForeground = false
  }
}
