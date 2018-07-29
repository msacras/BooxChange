package nl.booxchange

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.os.Build
import com.facebook.appevents.AppEventsLogger
import nl.booxchange.api.APIClient
import nl.booxchange.utilities.UserData


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

    UserData
    APIClient

    delegate = this

    AppEventsLogger.activateApp(this)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel("booxchange.channel.messaging", "Messages", NotificationManager.IMPORTANCE_HIGH))
    }

    BooxchangeDatabase.instance = Room.databaseBuilder(this, BooxchangeDatabase::class.java, "booxchange_database").fallbackToDestructiveMigration().build()
//    Fabric.with(this, Crashlytics())
  }

  companion object {
    lateinit var delegate: BooxchangeApp
    var isInForeground = false
  }
}
