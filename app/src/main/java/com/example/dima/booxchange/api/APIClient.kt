package com.example.dima.booxchange.api

import android.util.Log
import com.example.dima.booxchange.extension.asJson
import com.example.dima.booxchange.extension.asObject
import com.example.dima.booxchange.model.*
import com.example.dima.booxchange.utilities.BaseActivity
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.httpDelete
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.httpPut
import com.github.kittinunf.result.Result
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.lang.ref.WeakReference

/**
 * Created by Cristian Velinciuc on 3/9/18.
 */
object APIClient {
  init {
    FuelManager.instance.basePath = "http://api.booxchange.website/web"//"http://192.168.0.104:8000"
    FuelManager.instance.baseHeaders = mapOf("Content-Type" to "application/json")
  }

  private fun executeRequest(request: Request, callback: (Request, Response, Result<*, *>) -> Unit) {
    doAsync {
      Log.d("APIClient", "${request.method} ${request.path}")
      Log.d("APIClient", request.toString())
      val (_, response, result) = request.responseString()
      Log.d("APIClient", response.toString())
      uiThread {
        callback(request, response, result)
      }
    }
  }

  class RequestManager(activity: BaseActivity) {
    private val activity = WeakReference(activity)
    private val requests = mutableMapOf<String, Request>()

    fun fetchAvailableBooks(queryKeyword: String, offerType: OfferType, startIndex: Int, callback: (BookListResponseModel?) -> Unit): RequestModel {
      val parameters = mapOf("query_keyword" to queryKeyword, "offer_type" to offerType, "start_index" to startIndex)
      val request = ALL_AVAILABLE_BOOKS.setParameters(parameters).httpGet().name { ALL_AVAILABLE_BOOKS }
      executeSafely(request) { callback(it?.asObject()) }
      return RequestModel(request)
    }

    fun fetchBooksByUserId(userId: Int, callback: (BookListResponseModel?) -> Unit): RequestModel {
      val parameters = mapOf("userId" to userId)
      val request = BOOKS_BY_USER_ID.setParameters(parameters).httpGet().name { BOOKS_BY_USER_ID }
      executeSafely(request) { callback(it?.asObject()) }
      return RequestModel(request)
    }

    fun bookAdd(bookModel: BookModel, callback: (ResponseModel?) -> Unit): RequestModel {
      val request = BOOK_ADD.httpPost().body(bookModel.asJson()).name { BOOK_ADD }
      executeSafely(request) { callback(it?.asObject()) }
      return RequestModel(request)
    }

    fun bookGet(bookId: Int, callback: (BookResponseModel?) -> Unit): RequestModel {
      val parameters = mapOf("book_id" to bookId)
      val request = BOOK_GET.setParameters(parameters).httpGet().name { BOOK_GET }
      executeSafely(request) { callback(it?.asObject()) }
      return RequestModel(request)
    }

    fun bookUpdate(bookModel: BookModel, callback: (ResponseModel?) -> Unit): RequestModel {
      val request = BOOK_UPDATE.httpPut().body(bookModel.asJson()).name { BOOK_UPDATE }
      executeSafely(request) { callback(it?.asObject()) }
      return RequestModel(request)
    }

    fun bookDelete(bookId: Int, callback: (ResponseModel?) -> Unit): RequestModel {
      val parameters = mapOf("book_id" to bookId)
      val request = BOOK_DELETE.setParameters(parameters).httpDelete().name { BOOK_DELETE }
      executeSafely(request) { callback(it?.asObject()) }
      return RequestModel(request)
    }

    private fun executeSafely(request: Request, completion: (String?) -> Unit) {
      requests.put(request.name, request)?.cancel()
      executeRequest(request) { request1, response, result ->
        if (requests.get(request.name) == request1) {
          requests.remove(request.name)
          if (activity.get()?.isDestroyed == false) {
            completion(result.component1() as? String)
          }
        }
      }
    }
  }

  private fun String.setParameters(parametersMap: Map<String, Any>): String {
    return this.split('/').reduce { result, parameter -> result + "/" + (parametersMap[parameter.drop(1).dropLast(1)] ?: parameter) }.trim('/')
  }

  private const val ALL_AVAILABLE_BOOKS = "/books/available/{start_index}/{offer_type}/{query_keyword}"
  private const val BOOKS_BY_USER_ID = "/books/library/{user_id}"

  private const val BOOK_ADD = "/book/add"
  private const val BOOK_GET = "/book/{book_id}"
  private const val BOOK_UPDATE = "/book/update"
  private const val BOOK_DELETE = "/book/{book_id}"

  private const val USER_ADD = "/user/add"
  private const val USER_GET = "/user/{user_id}"
  private const val USER_UPDATE = "/user/update"
}
