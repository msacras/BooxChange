package nl.booxchange.utilities

import android.content.Context
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.support.v4.content.FileProvider
import android.support.v7.widget.AppCompatButton
import android.view.WindowManager
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.github.kittinunf.fuel.core.FuelManager
import nl.booxchange.BooxchangeApp
import nl.booxchange.extension.ARGB
import nl.booxchange.extension.color
import nl.booxchange.utilities.Tools.safeContext
import java.io.File

/**
 * Created by Cristian Velinciuc on 3/10/18.
 */
object Tools {
    val safeContext
        get() = BooxchangeApp.delegate.applicationContext

    fun getCacheUri(filename: String): Uri {
        return FileProvider.getUriForFile(safeContext, safeContext.packageName + ".file_provider", File.createTempFile(filename, "", File(safeContext.cacheDir.path)))
    }

    fun interpolateColor(colorA: Int, colorB: Int, percentage: Float) =
        (colorA.ARGB to colorB.ARGB).let { (argbA, argbB) ->
            (0..3).map { interpolateValue(argbA[it].toFloat(), argbB[it].toFloat(), percentage).toInt() }.toIntArray().color
        }

    fun interpolateValue(valueA: Float, valueB: Float, percentage: Float) =
        valueA + (valueB - valueA) * percentage
}
