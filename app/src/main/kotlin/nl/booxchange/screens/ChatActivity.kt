package nl.booxchange.screens

import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.chat_item_message.view.*
import kotlinx.android.synthetic.main.chat_item_request.view.*
import nl.booxchange.R
import nl.booxchange.extension.*
import nl.booxchange.model.ChatModel
import nl.booxchange.model.MessageModel
import nl.booxchange.model.MessageType
import nl.booxchange.utilities.*
import nl.booxchange.widget.LAnimationDrawable
import org.jetbrains.anko.dip
import org.jetbrains.anko.toast
import org.joda.time.DateTime


/**
 * Created by Dima on 3/10/2018.
 */
class ChatActivity : BaseActivity() {
    lateinit var chatModel: ChatModel
    private val messagesAdapter = RecyclerViewAdapter()

    private val sendMessageProgressDrawable by lazy { LAnimationDrawable(thickness = dip(2).toFloat(), width = dip(30).toFloat(), primaryColor = getColorById(R.color.colorPrimary), secondaryColor = getColorById(R.color.whiteGray)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_chat)
        initializeSwipeRefreshLayout()
        initializeLayout()

        (intent.getSerializableExtra(Constants.EXTRA_PARAM_CHAT_MODEL) as? ChatModel?)?.let(this::chatModel::set)?.also { fetchMessages() } ?: fetchChatRoomInfo()
    }

    private fun initializeLayout() {
        messages_list.layoutManager = LinearLayoutManager(this)
        messages_list.adapter = messagesAdapter
        messages_list.addItemDecoration(RecyclerViewItemSpacer(0, dip(4)))

        messagesAdapter.addModelToViewBinding(R.layout.chat_item_request, MessageModel::class, { _, model -> model.type == MessageType.REQUEST.name }) { view, model ->
            view.request_message_content.text = MessageUtilities.formatRequest(model.content, chatModel.usersList)
            view.request_message_content.movementMethod = LinkMovementMethod.getInstance()
        }
        messagesAdapter.addModelToViewBinding(R.layout.chat_item_message, MessageModel::class) { view, model ->
            val isReceived = model.userId == UserData.Session.userModel?.id

            view.message_content_text.text = model.content
            view.message_time.text = DateTime.parse(model.createdAt).toString("HH:mm d MMM")

            if (isReceived) {
                view.author_photo.toGone()
                view.message_author.toGone()
                view.background.setColorFilter(getColorById(R.color.colorPrimary), PorterDuff.Mode.SRC_OUT)
            } else {
                val authorUserModel = chatModel.usersList.find { it.id == model.userId }
                if (chatModel.usersList.size > 2) {
                    view.message_author.toVisible()
                    view.message_author.text = authorUserModel?.formattedName ?: "Anonymous"
                } else {
                    view.message_author.toGone()
                }
                view.author_photo.toVisible()
                authorUserModel?.photo?.let { Tools.initializeImage(view.author_photo, it) }
                view.background.setColorFilter(getColorById(R.color.lightGray), PorterDuff.Mode.SRC_OUT)
            }
            view.layoutParams = (view.layoutParams as RecyclerView.LayoutParams).apply {
                leftMargin = dip(if (isReceived) 50 else 0)
                rightMargin = dip(if (isReceived) 0 else 50)
            }
        }

        message_input.background = sendMessageProgressDrawable
        message_input.setOnFocusChangeListener { _, hasFocus ->
            listOf(send_appointment, send_book, send_image).forEach { if (hasFocus) it.toGone() else it.toVisible() }
        }
        message_input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                send_message.setVisible(s?.isNotBlank() == true)
            }
        })

        send_message.setOnClickListener {
            sendMessage()
        }
    }

    private fun fetchMessages(index: Int = messagesAdapter.itemCount) {
        requestManager.fetchMessages(chatModel.id, index) { response ->
            response?.let { list ->
                messagesAdapter.prependItems(list)
                if (messagesAdapter.itemCount == list.size) {
                    messages_list.smoothScrollToPosition(messagesAdapter.itemCount - 1)
                }
            }
            swipe_refresh_layout.isRefreshing = false
        }
    }

    private fun sendMessage() {
        val message = MessageModel("", chatModel.id, UserData.Session.userModel?.id ?: "", message_input.text.string, MessageType.TEXT.name, DateTime.now().string)
        sendMessageProgressDrawable.isEnabled = true
        sendMessageProgressDrawable.start()
        requestManager.postMessage(message) { response ->
            response?.let { message ->
                messagesAdapter.appendItem(message)
                messages_list.smoothScrollToPosition(messagesAdapter.itemCount - 1)
                message_input.text.clear()
            } ?: toast("Failed to deliver message")
            sendMessageProgressDrawable.stop()
            sendMessageProgressDrawable.isEnabled = false
        }
    }

    fun receiveMessage(message: MessageModel) {
        runOnUiThread {
            messagesAdapter.appendItem(message)
            messages_list.smoothScrollToPosition(messagesAdapter.itemCount - 1)
        }
    }

    private fun fetchChatRoomInfo() {
        (intent.getSerializableExtra(Constants.EXTRA_PARAM_CHAT_ID) as? String?)?.let { chatId ->
            requestManager.fetchChatRoom(chatId) { response ->
                response?.let(this::chatModel::set)?.also { fetchMessages() } ?: retryView.show()
            }
        } ?: retryView.show()
    }

    private fun initializeSwipeRefreshLayout() {
        swipe_refresh_layout.canRefreshUp = true
        swipe_refresh_layout.canRefreshDown = false
        swipe_refresh_layout.setOnUpRefreshListener {
            fetchMessages(0)
        }
        swipe_refresh_layout.setOnDownRefreshListener {
            fetchMessages()
        }
    }
}
