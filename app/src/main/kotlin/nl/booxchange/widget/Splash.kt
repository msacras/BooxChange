package nl.booxchange.widget

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import kotlinx.android.synthetic.main.activity_splash.*
import nl.booxchange.R
import nl.booxchange.screens.MainFragmentActivity
import java.io.IOException

class Splash : AppCompatActivity() {

    private val SPLASH_TIME = 2000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val anim = AnimationUtils.loadAnimation(this, R.anim.transition)
        icon.startAnimation(anim)
        text.startAnimation(anim)
/*        val intent = Intent(this, MainFragmentActivity::class.java)
        var time:Thread = object:Thread() {
            override fun run() {
                try
                {
                    Thread.sleep(2000)
                } catch (e: IOException) {
                    run({e.printStackTrace()})
                } finally {
                    run({startActivity(intent)
                    finish()})
                }
            }
        }
        time.start()*/

        Handler().postDelayed(object:Runnable {
            public override fun run() {
                //Do any action here. Now we are moving to next page
                val mySuperIntent = Intent(this@Splash, MainFragmentActivity::class.java)
                startActivity(mySuperIntent)
                /* This 'finish()' is for exiting the app when back button pressed
             * from Home page which is ActivityHome
             */
                finish()
            }
        }, SPLASH_TIME)

    }
}
