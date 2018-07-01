package nl.booxchange.screens.chat

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.provider.MediaStore
import android.text.SpannableString
import android.text.style.ImageSpan
import android.util.Base64
import android.view.View
import com.bumptech.glide.Glide
import com.vcristian.combus.expect
import com.vcristian.combus.post
import nl.booxchange.api.APIClient.Chat
import nl.booxchange.extension.takeNotBlank
import nl.booxchange.model.*
import nl.booxchange.utilities.BaseViewModel
import nl.booxchange.utilities.Constants
import nl.booxchange.utilities.Tools
import nl.booxchange.utilities.UserData
import org.jetbrains.anko.dip
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.joda.time.DateTime
import java.io.ByteArrayOutputStream


class ChatFragmentViewModel: BaseViewModel() {
    val isSending = ObservableBoolean()

    private val chatMessagesMap = mutableMapOf<ChatModel, MutableSet<MessageModel>>()
    val messagesList = ObservableMutableSet<MessageModel>()

    var chatModel: ChatModel? = null

    val messageInput = ObservableField<CharSequence>()

    init {
        expect(MessageReceivedEvent::class.java) { (messageModel) ->
            if (messageModel.chatId == chatModel?.id) {
                messagesList.add(messageModel)
            }
        }

        expect(ChatOpenedEvent::class.java) { event ->
            event.chatModel?.let(::bindChatModel) ?: fetchChat(event.chatId)
        }
    }

    private fun bindChatModel(chatModel: ChatModel) {
        messagesList.set(chatMessagesMap[chatModel] ?: mutableSetOf())
        chatMessagesMap[chatModel] = messagesList.get()
        this.chatModel = chatModel
        fetchLatestMessages()
    }

    private fun fetchChat(chatId: String) {
        chatMessagesMap.keys.find { it.id == chatId }?.let { bindChatModel(it) } ?: Chat.fetchChatRoom(chatId) { it?.let(::bindChatModel) }
    }

    fun fetchLatestMessages() {
        val messageId = messagesList.get().lastOrNull()?.id
        Chat.fetchMessagesAfterId(chatModel?.id ?: return, messageId) {
            it?.let(messagesList::addAll) ?: onLoadingFailed()
            onLoadingFinished()
        }
    }

    fun fetchOlderMessages() {
        val messageId = messagesList.get().firstOrNull()?.id
        Chat.fetchMessagesBeforeId(chatModel?.id ?: return, messageId) {
            it?.let(messagesList::addAll) ?: onLoadingFailed()
            onLoadingFinished()
        }
    }

    fun sendMessage() {
        val messageModel = sendableImage?.let { MessageModel("", chatModel?.id!!, UserData.Session.userId, "base64://" + Base64.encodeToString(it, Base64.DEFAULT), MessageType.IMAGE, DateTime.now()) }
            ?: MessageModel("", chatModel?.id!!, UserData.Session.userId, messageInput.get()?.toString()?.takeNotBlank ?: return, MessageType.TEXT, DateTime.now())
        isSending.set(true)
        Chat.postMessage(messageModel) {
            it?.let {
                messagesList.add(it)
                messageInput.set(SpannableString(""))
            }
            isSending.set(false)
        }
    }

    fun sendMessageReceivedStatus() {
        if (chatModel?.unreadCount/*?.get()*/ != 0) {
            chatModel?.unreadCount = 0//?.set(0)
            Chat.postMessageReceived(chatModel?.id ?: return)
        }
    }

    fun getAuthorByUserId(senderId: String): String? {
        return chatModel?.usersList?.find { it.id == senderId }?.getFormattedName()
    }

    fun getImageByUserId(senderId: String): List<String?> {
        return listOf(chatModel?.usersList?.find { it.id == senderId }?.photo)
    }

    fun isUserOwnedMessage(senderId: String): Boolean {
        return senderId == UserData.Session.userId
    }

    override fun onRefresh() {
        fetchLatestMessages()
    }

    fun onAddPhotoFromCameraClick(view: View) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val temporaryImageUri = Tools.getCacheUri("camera_output.jpeg")
        view.context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).forEach { cameraAppPackage ->
            view.context.grantUriPermission(cameraAppPackage.activityInfo.packageName, temporaryImageUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, temporaryImageUri)
        post(StartActivity(intent, Constants.REQUEST_CAMERA, ChatFragment::class.java))
    }

    fun onAddPhotoFromGalleryClick(view: View) {
        post(StartActivity(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), Constants.REQUEST_GALLERY, ChatFragment::class.java))
    }

    private var sendableImage: ByteArray? = null
        set(value) {
            field = value
            hasSendableImage.set(field != null)
        }
    val hasSendableImage = ObservableBoolean()

    fun removeSendableImage() {
        sendableImage = null
        messageInput.set("")
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val imageUri = when (requestCode) {
                Constants.REQUEST_CAMERA -> Tools.getCacheUri("camera_output.jpeg")
                Constants.REQUEST_GALLERY -> data!!.data
                else -> return
            }

            doAsync {
                val imageByteArray = ByteArrayOutputStream()
                val `200dp` = Tools.safeContext.dip(200)
                Tools.safeContext.contentResolver.openInputStream(imageUri).copyTo(imageByteArray)
                sendableImage = imageByteArray.toByteArray()
                val previewImage = Glide.with(Tools.safeContext).load(sendableImage).submit(`200dp`, `200dp`).get()
                uiThread { messageInput.set(SpannableString(" ").apply { setSpan(ImageSpan(/*Tools.safeContext.getDrawableCompat(R.drawable.ic_no_image)*/previewImage.apply { setBounds(0, 0, `200dp`, `200dp`) }/*previewImage*/)/*StyleSpan(Typeface.BOLD)*/, 0, 1, SpannableString.SPAN_INCLUSIVE_EXCLUSIVE) }) }
            }
        }
    }
}
