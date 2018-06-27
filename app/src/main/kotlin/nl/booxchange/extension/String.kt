package nl.booxchange.extension

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager

/**
 * Created by Cristian Velinciuc on 3/21/18.
 */

val String.withExitSymbol
    get() = "+$this"

val String.digitsOnly
    get() = replace("[^\\d]".toRegex(), "")

val String.takeNotBlank
    get() = this.takeIf { it.isNotBlank() }

val String.statisResourceUrl
    get() = FuelManager.instance.basePath + "/static/$this"
