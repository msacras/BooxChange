package nl.booxchange.utilities

import android.net.Uri
import androidx.core.content.FileProvider
import nl.booxchange.BooxchangeApp
import nl.booxchange.extension.ARGB
import nl.booxchange.extension.color
import org.joda.time.DateTime
import java.io.File

object Tools {
    fun getCacheUri(filename: String) =
        FileProvider.getUriForFile(BooxchangeApp.context, BooxchangeApp.context.packageName + ".file_provider", File.createTempFile(filename, ".jpg", BooxchangeApp.context.cacheDir))

    lateinit var lastCameraImageUri: Uri
    fun generateCameraImageId() {
        lastCameraImageUri = getCacheUri(DateTime.now().toString("yyyy-MM-dd'T'HH:mm:ss_"))
    }

    fun interpolateColor(colorA: Int, colorB: Int, percentage: Float) =
        (colorA.ARGB to colorB.ARGB).let { (argbA, argbB) ->
            (0..3).map { interpolateValue(argbA[it].toFloat(), argbB[it].toFloat(), percentage).toInt() }.toIntArray().color
        }

    fun interpolateValue(valueA: Float, valueB: Float, percentage: Float) =
        valueA + (valueB - valueA) * percentage
}
