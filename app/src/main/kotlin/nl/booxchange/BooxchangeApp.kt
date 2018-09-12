package nl.booxchange

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.facebook.appevents.AppEventsLogger
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import java.lang.ref.WeakReference


/**
 * Created by Cristian Velinciuc on 3/10/18.
 */
class BooxchangeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseRemoteConfig.getInstance().apply {
            setDefaults(mapOf(
                    "KEY_SUBSCRIPTION_SYSTEM_ENABLED" to false
            ))
            fetch(60).addOnCompleteListener {
                FirebaseRemoteConfig.getInstance().activateFetched()
            }
        }

/*
    if (LeakCanary.isInAnalyzerProcess(this)) return
    LeakCanary.install(this)
*/

        _delegate = WeakReference(this)

        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        AppEventsLogger.activateApp(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getSystemService(NotificationManager::class.java).apply {
                createNotificationChannel(NotificationChannel("booxchange.channel.messaging", "Messages", NotificationManager.IMPORTANCE_HIGH))
                createNotificationChannel(NotificationChannel("booxchange.channel.requesting", "Requests", NotificationManager.IMPORTANCE_HIGH))
            }
        }
    }

    companion object {
        private var _delegate = WeakReference<BooxchangeApp>(null)
        val delegate: BooxchangeApp?
            get() = _delegate.get()

        var isInForeground = false
    }
}
