package nl.booxchange.model

import com.github.kittinunf.fuel.core.Request
import java.util.concurrent.Future

/**
 * Created by Velinciuc Cristian on 3/10/18.
 */
data class RequestModel(private val request: Request) {
  fun cancelRequest() {
    request.interrupt {  }
  }
}
