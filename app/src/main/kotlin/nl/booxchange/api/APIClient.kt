package nl.booxchange.api

import android.util.Log
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.httpDelete
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.httpPut
import com.github.kittinunf.result.Result
import com.google.firebase.auth.FirebaseAuth
import nl.booxchange.extension.asJson
import nl.booxchange.extension.asObject
import nl.booxchange.extension.string
import nl.booxchange.model.*
import nl.booxchange.utilities.BaseActivity
import nl.booxchange.utilities.UserData
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONObject
import java.lang.ref.WeakReference
import java.util.*

/**
 * Created by Cristian Velinciuc on 3/9/18.
 */
object APIClient {
  private var isRequestingToken = false
    private var idToken: String? = null

  init {
      FuelManager.instance.basePath = /*"http://test.api.booxchange.website"*//*"http://192.168.88.128:8000"*/"http://192.168.0.104:8000"
    FuelManager.instance.baseHeaders = mapOf("Content-Type" to "application/json")
      FirebaseAuth.getInstance().addIdTokenListener { requestToken() }
  }

  private fun requestToken() {
    FirebaseAuth.getInstance().currentUser?.getIdToken(true)?.addOnCompleteListener { task ->
      if (task.isSuccessful) {
          idToken = task.result.token
        isRequestingToken = false

        while (pendingRequests.isNotEmpty()) {
          val pendingRequest = pendingRequests.pop()
          executeRequest(pendingRequest.first, pendingRequest.second)
        }
      } else {
        while (pendingRequests.isNotEmpty()) {
          val pendingRequest = pendingRequests.pop()
          pendingRequest.second(pendingRequest.first, Response.error(), Result.of {{ "" }})
        }
      }
    }
  }

  private val pendingRequests = Stack<Pair<Request, (Request, Response, Result<*, *>) -> Unit>>()

  private fun executeRequest(request: Request, callback: (Request, Response, Result<*, *>) -> Unit) {
      idToken?.let {
      doAsync {
          val request = request.header("User-Token" to (idToken ?: ""))

        Log.d("APIClient", "${request.method} ${request.path}")
        Log.d("APIClient", request.toString())

        val (_, response, result) = request.responseString()

        uiThread {
          Log.d("APIClient", response.toString())
          callback(request, response, result)
        }
      }
    } ?: run {
      pendingRequests.push(request to callback)
      if (!isRequestingToken) {
        isRequestingToken = true
        requestToken()
      }
    }
  }

  class RequestManager(activity: BaseActivity?) {
    private val activityReference: WeakReference<BaseActivity>? = activity?.let { WeakReference(activity) }
    private val requests: MutableMap<String, Request> = mutableMapOf()

    fun fetchAvailableBooks(queryKeyword: String, offerType: OfferType, startIndex: Int, callback: (BookListResponseModel?) -> Unit): RequestModel {
      val parameters = mapOf("query_keyword" to queryKeyword, "offer_type" to offerType, "start_index" to startIndex)
      val request = ALL_AVAILABLE_BOOKS.setParameters(parameters).httpGet().name { ALL_AVAILABLE_BOOKS }
      executeSafely(request) { callback(it?.asObject()) }
      return RequestModel(request)
    }

    fun fetchBooksByUserId(userId: String, callback: (BookListResponseModel?) -> Unit): RequestModel {
      val parameters = mapOf("user_id" to userId)
      val request = BOOKS_BY_USER_ID.setParameters(parameters).httpGet().name { BOOKS_BY_USER_ID }
      executeSafely(request) { callback(it?.asObject()) }
      return RequestModel(request)
    }

    fun bookAdd(bookModel: BookModel, callback: (BookResponseModel?) -> Unit): RequestModel {
      val request = BOOK_ADD.httpPost().body(bookModel.asJson()).name { BOOK_ADD }
      executeSafely(request) { callback(it?.asObject()) }
      return RequestModel(request)
    }

      fun bookGet(bookId: String, callback: (BookModel?) -> Unit): RequestModel {
      val parameters = mapOf("book_id" to bookId)
      val request = BOOK_GET.setParameters(parameters).httpGet().name { BOOK_GET }
          executeSafely(request) { callback(it?.let { JSONObject(it).opt("result")?.string?.asObject<BookModel>() }) }
      return RequestModel(request)
    }

    fun bookUpdate(bookModel: BookModel, callback: (BookResponseModel?) -> Unit): RequestModel {
      val request = BOOK_UPDATE.httpPut().body(bookModel.asJson()).name { BOOK_UPDATE }
      executeSafely(request) { callback(it?.asObject()) }
      return RequestModel(request)
    }

    fun bookDelete(bookId: String, callback: (ResponseModel?) -> Unit): RequestModel {
      val parameters = mapOf("book_id" to bookId)
      val request = BOOK_DELETE.setParameters(parameters).httpDelete().name { BOOK_DELETE }
      executeSafely(request) { callback(it?.asObject()) }
      return RequestModel(request)
    }

    fun userAdd(userModel: UserModel, callback: (UserResponseModel?) -> Unit): RequestModel {
      val request = USER_ADD.httpPost().body(userModel.asJson()).name { USER_ADD }
      executeSafely(request) { callback(it?.asObject()) }
      return RequestModel(request)
    }

    fun userGet(userId: String, callback: (UserResponseModel?) -> Unit): RequestModel {
      val parameters = mapOf("user_id" to userId)
      val request = USER_GET.setParameters(parameters).httpGet().name { USER_GET }
      executeSafely(request) { callback(it?.asObject()) }
      return RequestModel(request)
    }

    fun userUpdate(userModel: UserModel, callback: (UserResponseModel?) -> Unit): RequestModel {
      val request = USER_UPDATE.httpPut().body(userModel.asJson()).name { USER_UPDATE }
      executeSafely(request) { callback(it?.asObject()) }
      return RequestModel(request)
    }

    fun userDelete(userId: String, callback: (ResponseModel?) -> Unit): RequestModel {
      val parameters = mapOf("user_id" to userId)
      val request = USER_DELETE.setParameters(parameters).httpDelete().name { USER_DELETE }
      executeSafely(request) { callback(it?.asObject()) }
      return RequestModel(request)
    }

      fun updateInstanceId(callback: (ResponseModel?) -> Unit): RequestModel {
          val parameters = mapOf(
              "user_id" to UserData.Session.userId,
              "instance_id" to UserData.Session.instanceId
          )
          val request = USER_UPDATE_INSTANCE.setParameters(parameters).httpPut().name { USER_UPDATE_INSTANCE }
          executeSafely(request) { callback(it?.asObject()) }
          return RequestModel(request)
      }

      fun fetchChatRooms(callback: (List<ChatModel>?) -> Unit): RequestModel {
          val parameters = mapOf("user_id" to UserData.Session.userId)
          val request = CHAT_USER_ROOMS.setParameters(parameters).httpGet().name { CHAT_USER_ROOMS }
          executeSafely(request) { callback(it?.let { JSONObject(it).opt("result")?.string?.asObject<List<ChatModel>>() }) }
          return RequestModel(request)
      }

      fun findChatRoom(userIds: List<String>, chatTopic: String, callback: (ChatModel?) -> Unit): RequestModel {
          val body = mapOf(
              "chat_topic" to chatTopic,
              "users_list" to userIds
          )
          val request = CHAT_ROOM_FIND.httpPost().body(body.asJson()).name { CHAT_ROOM_FIND }
          executeSafely(request) { callback(it?.let { JSONObject(it).opt("result")?.string?.asObject<ChatModel>() }) }
          return RequestModel(request)
      }

      fun fetchChatRoom(chatId: String, callback: (ChatModel?) -> Unit): RequestModel {
          val parameters = mapOf("chat_id" to chatId)
          val request = CHAT_ROOM_JOIN.setParameters(parameters).httpGet().name { CHAT_ROOM_JOIN }
          executeSafely(request) { callback(it?.let { JSONObject(it).opt("result")?.string?.asObject<ChatModel>() }) }
          return RequestModel(request)
      }

      fun fetchMessages(chatId: String, fromIndex: Int, callback: (List<MessageModel>?) -> Unit): RequestModel {
          val parameters = mapOf(
              "chat_id" to chatId,
              "offset" to fromIndex
          )
          val request = MESSAGE_CHAT_LIST.setParameters(parameters).httpGet().name { MESSAGE_CHAT_LIST }
          executeSafely(request) { callback(it?.let { JSONObject(it).opt("result")?.string?.asObject<List<MessageModel>>() }) }
          return RequestModel(request)
      }

      fun postMessage(message: MessageModel, callback: (MessageModel?) -> Unit): RequestModel {
          val request = MESSAGE_POST.httpPost().body(message.asJson()).name { MESSAGE_POST }
          executeSafely(request) { callback(it?.let { JSONObject(it).opt("result")?.string?.asObject<MessageModel>() }) }
          return RequestModel(request)
      }

      fun markAsRead(chatId: String, callback: (ResponseModel?) -> Unit): RequestModel {
          val request = CHAT_MARK_READ.httpGet().name { CHAT_MARK_READ }
          executeSafely(request) { callback(it?.asObject()) }
          return RequestModel(request)
      }

    private fun executeSafely(request: Request, completion: (String?) -> Unit) {
      requests.put(request.name, request)?.cancel()
      executeRequest(request) { request1, _, result ->
        if (requests.get(request.name) == request1) {
          requests.remove(request.name)
          if (activityReference == null || activityReference.get()?.isDestroyed != true) {
            completion(result.component1() as? String)
          }
        }
      }
    }

    companion object {
      val instance = RequestManager(null)
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
  private const val USER_DELETE = "/user/{user_id}"
    private const val USER_UPDATE_INSTANCE = "/user/{user_id}/instances/{instance_id}"

    private const val CHAT_USER_ROOMS = "/chats/{user_id}"
    private const val CHAT_ROOM_FIND = "/chats/join/by_users"
    private const val CHAT_ROOM_JOIN = "/chats/join/by_chat/{chat_id}"
    private const val CHAT_MARK_READ = "/chats/read/{chat_id}/{user_id}"

    private const val MESSAGE_CHAT_LIST = "/messages/{chat_id}/{offset}"
    private const val MESSAGE_POST = "/messages/post"
}
