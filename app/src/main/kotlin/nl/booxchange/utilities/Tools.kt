package nl.booxchange.utilities

import android.support.v4.content.FileProvider
import nl.booxchange.BooxchangeApp
import nl.booxchange.extension.ARGB
import nl.booxchange.extension.color
import java.io.File

object Tools {
    fun getCacheUri(filename: String) =
        FileProvider.getUriForFile(BooxchangeApp.context, BooxchangeApp.context.packageName + ".file_provider", File.createTempFile(filename, "", File(BooxchangeApp.context.cacheDir.path)))

    fun interpolateColor(colorA: Int, colorB: Int, percentage: Float) =
        (colorA.ARGB to colorB.ARGB).let { (argbA, argbB) ->
            (0..3).map { interpolateValue(argbA[it].toFloat(), argbB[it].toFloat(), percentage).toInt() }.toIntArray().color
        }

    fun interpolateValue(valueA: Float, valueB: Float, percentage: Float) =
        valueA + (valueB - valueA) * percentage
}
