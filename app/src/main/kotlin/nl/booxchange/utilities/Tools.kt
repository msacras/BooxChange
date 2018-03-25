package nl.booxchange.utilities

import android.content.Context
import android.graphics.Point
import android.net.Uri
import android.support.v4.content.FileProvider
import android.view.Display
import android.view.WindowManager
import android.widget.ImageView
import com.bumptech.glide.Glide
import nl.booxchange.BooxchangeApp
import nl.booxchange.R
import com.github.kittinunf.fuel.core.FuelManager
import java.io.File

/**
 * Created by Cristian Velinciuc on 3/10/18.
 */
object Tools {
  val safeContext
    get() = BooxchangeApp.delegate.applicationContext

  fun initializeImage(imageView: ImageView, url: String?) {
    val screenWidth = Point().apply { (safeContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getSize(this) }.x
    val serverImageUrl = FuelManager.instance.basePath + "/static/$url"
    val smallThumbnailUrl = "https://images1-focus-opensocial.googleusercontent.com/gadgets/proxy?url=$serverImageUrl&container=focus&resize_w=$screenWidth&refresh=604800"
    try { Glide.with(safeContext).load(smallThumbnailUrl).into(imageView) } catch (e: Exception) { /* Activity was destroyed before load could start */ }
  }

  fun getCacheFile(filename: String): Uri? {
    return FileProvider.getUriForFile(safeContext, safeContext.packageName + ".file_provider", File(safeContext.cacheDir.path + "/camera_output"))
  }
}
