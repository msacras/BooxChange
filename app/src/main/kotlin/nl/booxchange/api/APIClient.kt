package nl.booxchange.api

import android.util.Log
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.httpDelete
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.httpPut
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.JsonObject
import nl.booxchange.extension.asJson
import nl.booxchange.extension.asObject
import nl.booxchange.extension.parseJson
import nl.booxchange.model.*
import nl.booxchange.utilities.UserData
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.*

/**
 * Created by Cristian Velinciuc on 3/9/18.
 */
object APIClient {
    private var isRequestingToken = false
    private var idToken: String? = null

    init {
        FuelManager.instance.basePath = /*"https://api.booxchange.website"*//*"http://192.168.88.128:8000"*/"http://192.168.0.104:8000"
        FuelManager.instance.baseHeaders = mapOf("Content-Type" to "application/json")
        FirebaseAuth.getInstance().addIdTokenListener(::requestToken)
    }

    private fun requestToken(auth: FirebaseAuth) {
        auth.currentUser?.getIdToken(true)?.addOnCompleteListener { task ->
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
                    pendingRequest.second(null)
                }
            }
        }
    }

    private val pendingRequests = Stack<Pair<Request, (JsonObject?) -> Unit>>()
    private val requests = mutableMapOf<String, Request>()

    private fun executeRequest(request: Request, callback: (JsonObject?) -> Unit) {
        requests.put(request.name, request)?.cancel()

        idToken?.let { idToken ->
            doAsync {
                request.header("Id-Token" to idToken, "Instance-Id" to UserData.Session.instanceId, "User-Id" to UserData.Session.userId)

                Log.d("APIClient", "${request.method} ${request.path}")
                Log.d("APIClient", request.toString())

                val (_, response, result) = request.responseString()

                Log.d("APIClient", response.toString())

                if (requests[request.name] == request) {
                    requests -= request.name
                }

                uiThread {
                    callback(result.component1()?.parseJson?.asJsonObject)
                }
            }
        } ?: run {
            pendingRequests.push(request to callback)
            if (!isRequestingToken) {
                isRequestingToken = true
                requestToken(FirebaseAuth.getInstance())
            }
        }
    }

    object Book {
        fun fetchAvailableBooks(queryKeyword: String, offerType: OfferType, startIndex: Int, sortingField: SortingField, callback: (List<BookModel>?) -> Unit): RequestModel {
            val query = listOf("query_keyword" to queryKeyword, "offer_type" to offerType, "start_index" to startIndex, "sorting_field" to sortingField)
            val request = ALL_AVAILABLE_BOOKS.httpGet(query).name(::ALL_AVAILABLE_BOOKS)
            executeRequest(request) { callback(it?.get("result")?.asObject()) }
            return RequestModel(request)
        }

        fun fetchBooksByUserId(userId: String, callback: (List<BookModel>?) -> Unit): RequestModel {
            val parameters = mapOf("user_id" to userId)
            val request = BOOKS_BY_USER_ID.setParameters(parameters).httpGet().name(::BOOKS_BY_USER_ID)
            executeRequest(request) { callback(it?.get("result")?.asObject()) }
            return RequestModel(request)
        }

        fun bookAdd(bookModel: BookModel, callback: (BookModel?) -> Unit): RequestModel {
            val request = BOOK_ADD.httpPost().body(bookModel.asJson).name(::BOOK_ADD)
            executeRequest(request) { callback(it?.get("result")?.asObject()) }
            return RequestModel(request)
        }

        fun bookGet(bookId: String, callback: (BookModel?) -> Unit): RequestModel {
            val parameters = mapOf("book_id" to bookId)
            val request = BOOK_GET.setParameters(parameters).httpGet().name(::BOOK_GET)
            executeRequest(request) { callback(it?.get("result")?.asObject()) }
            return RequestModel(request)
        }

        fun bookUpdate(bookModel: BookModel, callback: (BookModel?) -> Unit): RequestModel {
            val request = BOOK_UPDATE.httpPut().body(bookModel.asJson).name(::BOOK_UPDATE)
            executeRequest(request) { callback(it?.get("result")?.asObject()) }
            return RequestModel(request)
        }

        fun bookDelete(bookId: String, callback: (ResponseModel?) -> Unit): RequestModel {
            val parameters = mapOf("book_id" to bookId)
            val request = BOOK_DELETE.setParameters(parameters).httpDelete().name(::BOOK_DELETE)
            executeRequest(request) { callback(it?.asObject()) }
            return RequestModel(request)
        }

        fun incrementViewsCount(bookId: String): RequestModel {
            val parameters = mapOf("book_id" to bookId)
            val request = BOOK_INCREMENT_VIEWS.setParameters(parameters).httpGet().name(::BOOK_INCREMENT_VIEWS)
            executeRequest(request) {}
            return RequestModel(request)
        }
    }

    object User {
        fun userAdd(userModel: UserModel, callback: (UserModel?) -> Unit): RequestModel {
            val request = USER_ADD.httpPost().body(userModel.asJson).name(::USER_ADD)
            executeRequest(request) { callback(it?.get("result")?.asObject()) }
            return RequestModel(request)
        }

        fun userGet(userId: String, callback: (UserModel?) -> Unit): RequestModel {
            val parameters = mapOf("user_id" to userId)
            val request = USER_GET.setParameters(parameters).httpGet().name(::USER_GET)
            executeRequest(request) { callback(it?.get("result")?.asObject()) }
            return RequestModel(request)
        }

        fun userUpdate(userModel: UserModel, callback: (UserModel?) -> Unit): RequestModel {
            val request = USER_UPDATE.httpPut().body(userModel.asJson).name(::USER_UPDATE)
            executeRequest(request) { callback(it?.get("result")?.asObject()) }
            return RequestModel(request)
        }

        fun userDelete(userId: String, callback: (ResponseModel?) -> Unit): RequestModel {
            val parameters = mapOf("user_id" to userId)
            val request = USER_DELETE.setParameters(parameters).httpDelete().name(::USER_DELETE)
            executeRequest(request) { callback(it?.asObject()) }
            return RequestModel(request)
        }

        fun updateInstanceId(callback: (ResponseModel?) -> Unit): RequestModel {
            val request = USER_UPDATE_INSTANCE.httpGet().name(::USER_UPDATE_INSTANCE)
            executeRequest(request) {}
            return RequestModel(request)
        }
    }

    object Chat {
        fun fetchChatRooms(type: String, callback: (List<ChatModel>?) -> Unit): RequestModel {
            val parameters = mapOf("user_id" to UserData.Session.userId, "type" to type)
            val request = CHAT_USER_ROOMS.setParameters(parameters).httpGet().name(::CHAT_USER_ROOMS)
            executeRequest(request) { callback(it?.get("result")?.asObject()) }
            return RequestModel(request)
        }

        fun findChatRoom(userIds: List<String>, chatTopic: String, callback: (ChatModel?) -> Unit): RequestModel {
            val body = mapOf("chat_topic" to chatTopic, "users_list" to userIds)
            val request = CHAT_ROOM_FIND.httpPost().body(body.asJson).name(::CHAT_ROOM_FIND)
            executeRequest(request) { callback(it?.get("result")?.asObject()) }
            return RequestModel(request)
        }

        fun fetchChatRoom(chatId: String, callback: (ChatModel?) -> Unit): RequestModel {
            val parameters = mapOf("chat_id" to chatId)
            val request = CHAT_ROOM_JOIN.setParameters(parameters).httpGet().name(::CHAT_ROOM_JOIN)
            executeRequest(request) { callback(it?.get("result")?.asObject()) }
            return RequestModel(request)
        }

        fun fetchMessagesBeforeId(chatId: String, messageId: String?, callback: (List<MessageModel>?) -> Unit): RequestModel {
            val parameters = mapOf("chat_id" to chatId, "message_id" to (messageId ?: "null"))
            val request = MESSAGE_CHAT_BEFORE.setParameters(parameters).httpGet().name(::MESSAGE_CHAT_BEFORE)
            executeRequest(request) { callback(it?.get("result")?.asObject()) }
            return RequestModel(request)
        }

        fun fetchMessagesAfterId(chatId: String, messageId: String?, callback: (List<MessageModel>?) -> Unit): RequestModel {
            val parameters = mapOf("chat_id" to chatId, "message_id" to (messageId ?: "null"))
            val request = MESSAGE_CHAT_AFTER.setParameters(parameters).httpGet().name(::MESSAGE_CHAT_AFTER)
            executeRequest(request) { callback(it?.get("result")?.asObject()) }
            return RequestModel(request)
        }

        fun postMessage(message: MessageModel, callback: (MessageModel?) -> Unit): RequestModel {
            val request = MESSAGE_POST.httpPost().body(message.asJson).name(::MESSAGE_POST)
            executeRequest(request) { callback(it?.get("result")?.asObject()) }
            return RequestModel(request)
        }

        fun postMessageReceived(chatId: String): RequestModel {
            val parameters = mapOf("chat_id" to chatId, "user_id" to UserData.Session.userId)
            val request = MESSAGE_RECEIVED.setParameters(parameters).httpGet().name(::MESSAGE_RECEIVED)
            executeRequest(request) {}
            return RequestModel(request)
        }

        fun postRequest(bookId: String, tradeType: String, exchangeBookId: String?, callback: (ResponseModel?) -> Unit): RequestModel {
            val body = mapOf("book_id" to bookId, "trade_type" to tradeType, "exchange_book_id" to exchangeBookId)
            val request = REQUEST_POST.httpPost().body(body.asJson).name(::REQUEST_POST)
            executeRequest(request) { callback(it?.asObject()) }
            return RequestModel(request)
        }

        fun markAsRead(chatId: String, callback: (ResponseModel?) -> Unit): RequestModel {
            val request = CHAT_MARK_READ.httpGet().name(::CHAT_MARK_READ)
            executeRequest(request) { callback(it?.get("result")?.asObject()) }
            return RequestModel(request)
        }
    }

    private fun String.setParameters(parametersMap: Map<String, Any?>): String {
        return split('/').reduce { result, parameter -> result + "/" + (parametersMap[parameter.drop(1).dropLast(1)] ?: parameter) }.trim('/')
    }

    private const val ALL_AVAILABLE_BOOKS = "/books/available"
    private const val BOOKS_BY_USER_ID = "/books/library/{user_id}"

    private const val BOOK_ADD = "/book/add"
    private const val BOOK_GET = "/book/{book_id}"
    private const val BOOK_UPDATE = "/book/update"
    private const val BOOK_DELETE = "/book/{book_id}"
    private const val BOOK_INCREMENT_VIEWS = "/book/views/{book_id}"

    private const val USER_ADD = "/user/add"
    private const val USER_GET = "/user/{user_id}"
    private const val USER_UPDATE = "/user/update"
    private const val USER_DELETE = "/user/{user_id}"
    private const val USER_UPDATE_INSTANCE = "/user/instances"

    private const val CHAT_USER_ROOMS = "/chats/{user_id}/{type}"
    private const val CHAT_ROOM_FIND = "/chats/join/by_users"
    private const val CHAT_ROOM_JOIN = "/chats/join/by_chat/{chat_id}"
    private const val CHAT_MARK_READ = "/chats/read/{chat_id}/{user_id}"

    private const val MESSAGE_CHAT_BEFORE = "/messages/{chat_id}/before/{message_id}"
    private const val MESSAGE_CHAT_AFTER = "/messages/{chat_id}/after/{message_id}"
    private const val MESSAGE_POST = "/messages/post"
    private const val MESSAGE_RECEIVED = "/messages/{chat_id}/received/{user_id}"

    private const val REQUEST_POST = "/messages/request"
}
