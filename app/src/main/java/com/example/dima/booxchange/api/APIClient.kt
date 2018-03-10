package com.example.dima.booxchange.api

import android.util.Log
import com.example.dima.booxchange.extension.asObject
import com.example.dima.booxchange.model.*
import com.example.dima.booxchange.utilities.BaseActivity
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.httpDelete
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.httpPut
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.lang.ref.WeakReference

/**
 * Created by Cristian Velinciuc on 3/9/18.
 */
object APIClient {
  init {
    FuelManager.instance.basePath = "http://192.168.0.104:8000"//"http://api.booxchange.website/web"
  }

  private fun executeRequest(request: Request, callback: (String?) -> Unit) {
    doAsync {
      Log.d("APIClient", "${request.method} ${request.path}")
      Log.d("APIClient", request.toString())
      val (_, response, result) = request.responseString()
      Log.d("APIClient", response.toString())
      uiThread {
        callback(result.component1())
      }
    }
  }

  class RequestManager(activity: BaseActivity) {
    private val activity = WeakReference(activity)

    private fun safeCallback(callback: () -> Unit) {
      if (activity.get()?.isDestroyed == false) callback()
    }

    fun fetchAllAvailableBooks(callback: (BookListResponseModel?) -> Unit): RequestModel {
      val request = ALL_AVAILABLE_BOOKS.httpGet()
      executeRequest(request) { safeCallback { callback(it?.asObject()) }}
      return RequestModel(request)
    }

    fun fetchAllTradeBooks(callback: (BookListResponseModel?) -> Unit): RequestModel {
      val request = ALL_TRADE_BOOKS.httpGet()
      executeRequest(request) { safeCallback { callback(it?.asObject()) }}
      return RequestModel(request)
    }

    fun fetchAllPurchaseBook(callback: (BookListResponseModel?) -> Unit): RequestModel {
      val request = ALL_PURCHASE_BOOK.httpGet()
      executeRequest(request) { safeCallback { callback(it?.asObject()) }}
      return RequestModel(request)
    }

    fun fetchBooksByUserId(userId: Int, callback: (BookListResponseModel?) -> Unit): RequestModel {
      val request = BOOKS_BY_USER_ID.httpGet()
      executeRequest(request) { safeCallback { callback(it?.asObject()) }}
      return RequestModel(request)
    }

    fun bookAdd(bookModel: BookModel, callback: (ResponseModel?) -> Unit): RequestModel {
      val request = BOOK_ADD.httpPost()
      executeRequest(request) { safeCallback { callback(it?.asObject()) }}
      return RequestModel(request)
    }

    fun bookGet(bookId: Int, callback: (BookResponseModel?) -> Unit): RequestModel {
      val request = BOOK_GET.httpGet()
      executeRequest(request) { safeCallback { callback(it?.asObject()) }}
      return RequestModel(request)
    }

    fun bookUpdate(bookModel: BookModel, callback: (ResponseModel?) -> Unit): RequestModel {
      val request = BOOK_UPDATE.httpPut()
      executeRequest(request) { safeCallback { callback(it?.asObject()) }}
      return RequestModel(request)
    }

    fun bookDelete(bookId: Int, callback: (ResponseModel?) -> Unit): RequestModel {
      val request = BOOK_DELETE.httpDelete()
      executeRequest(request) { safeCallback { callback(it?.asObject()) }}
      return RequestModel(request)
    }
  }

  fun bindClass() {

  }

  private const val ALL_AVAILABLE_BOOKS = "/books/available"
  private const val ALL_TRADE_BOOKS = "/books/available/exchange"
  private const val ALL_PURCHASE_BOOK = "/books/available/purchase"
  private const val BOOKS_BY_USER_ID = "/books/library/1"
  private const val BOOK_ADD = "/book/add"
  private const val BOOK_GET = "/book/{book_id}"
  private const val BOOK_UPDATE = "/book/{book_id}"
  private const val BOOK_DELETE = "/book/{book_id}"
}
