package nl.booxchange.screens.chat

import android.app.Activity
import androidx.lifecycle.LiveData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import android.provider.MediaStore
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage
import nl.booxchange.R
import nl.booxchange.extension.getColorCompat
import nl.booxchange.extension.takeNotBlank
import nl.booxchange.model.entities.ChatModel
import nl.booxchange.model.entities.ImageModel
import nl.booxchange.model.entities.MessageModel
import nl.booxchange.utilities.*
import nl.booxchange.utilities.database.ChatPagingDataSource
import nl.booxchange.utilities.database.LiveList
import nl.booxchange.utilities.recycler.ViewHolderConfig
import nl.booxchange.utilities.recycler.ViewHolderConfig.ViewType
import org.jetbrains.anko.dip
import org.jetbrains.anko.findOptional
import org.jetbrains.anko.toast
import org.joda.time.DateTime
import kotlin.random.Random

class ChatActivityViewModel: BaseViewModel() {
    val isSending = ObservableBoolean()

    val chatModel = ObservableField<ChatModel>()
    val messagesViewsConfigurations = listOf<ViewHolderConfig<MessageModel>>(
        ViewHolderConfig(R.layout.chat_item_message, ViewType.MESSAGE_TEXT, ::messagePositionBinding) { _, messageModel -> messageModel?.type == "TEXT" },
        ViewHolderConfig(R.layout.chat_item_request, ViewType.MESSAGE_REQUEST) { _, messageModel -> messageModel?.type == "REQUEST" },
        ViewHolderConfig(R.layout.chat_item_image, ViewType.MESSAGE_IMAGE, ::messagePositionBinding) { _, messageModel -> messageModel?.type == "IMAGE" },
        ViewHolderConfig(R.layout.chat_item_placeholder, ViewType.PLACEHOLDER) { _, _ -> true }
    )

    private fun messagePositionBinding(view: View, model: MessageModel?) = with (view) {
        val horizontal = dip(8)
        val ownership = dip(72)

        var left = horizontal
        var right = horizontal

        model ?: run {
            if (Random.nextBoolean()) {
                left += ownership
            } else {
                right += ownership
            }

            view.layoutParams = (view.layoutParams as ViewGroup.MarginLayoutParams).apply {
                marginStart = left
                marginEnd = right
            }

            return@with
        }

        val contentView = findOptional<View>(R.id.chat_item_content) ?: return@with

        contentView.background.setTint(context.getColorCompat(R.color.lightGray))

        when (model.type) {
            "TEXT" -> contentView.layoutParams = (contentView.layoutParams as FrameLayout.LayoutParams).apply {
                if (model.isOwnMessage) {
                    left += ownership
                    gravity = Gravity.END

                    (contentView as? AppCompatTextView)?.let { messageView ->
                        messageView.background.setTint(context.getColorCompat(R.color.themeGreen))
                        messageView.setTextColor(context.getColorCompat(R.color.white))
                    } ?: Unit
                } else {
                    right += ownership
                    gravity = Gravity.START

                    (contentView as? AppCompatTextView)?.let { messageView ->
                        messageView.background.setTint(context.getColorCompat(R.color.lightGray))
                        messageView.setTextColor(context.getColorCompat(R.color.jetGray))
                    } ?: Unit
                }
            }
            "IMAGE" -> contentView.layoutParams = (contentView.layoutParams as FrameLayout.LayoutParams).apply {
                if (model.isOwnMessage) {
                    left += ownership
                    gravity = Gravity.END
                } else {
                    right += ownership
                    gravity = Gravity.START
                }
            }
            "REQUEST" -> (findOptional<View>(R.id.chat_item_content) as? AppCompatTextView)?.movementMethod = LinkMovementMethod.getInstance()
        }

        contentView.layoutParams = (contentView.layoutParams as ViewGroup.MarginLayoutParams).apply {
            marginStart = left
            marginEnd = right
        }
    }

    private val dataSource = ChatPagingDataSource(FirebaseFirestore.getInstance().collection("null"), MessageModel.Companion::fromFirebaseEntry)
    private var databaseListenerReference = dataSource.baseQuery.addSnapshotListener { _, _ -> }
    private val messageListChangeListener = EventListener<QuerySnapshot> { snapshot, _ ->
        snapshot ?: return@EventListener
        if ((messagesList as LiveList).value.isEmpty()) return@EventListener

        snapshot.documentChanges.forEach { documentChange ->
            val document = documentChange.document

            when (documentChange.type) {
                DocumentChange.Type.ADDED -> {
                    println("Item ${document.id} added")

                    sendMessageReceivedStatus()
                    dataSource.loadAfter(messagesList.value.last()?.orderKey ?: return@forEach, messagesList::plusAssign)
                }
                DocumentChange.Type.MODIFIED -> {
                    println("Item ${document.id} changed")

                    messagesList[document.id] = MessageModel.fromFirebaseEntry(document)
                }
                DocumentChange.Type.REMOVED -> {
                    println("Item ${document.id} removed")

                    messagesList.value.find { it?.id == document.id }?.let(messagesList::minusAssign)
                }
            }
        }
    }

    val imageInput = ObservableField<ImageModel>()
    val messageInput = ObservableField<CharSequence>()
    val messagesList: LiveData<List<MessageModel?>> = LiveList()

    fun initializeWithConfig(chatId: String) {
        FirebaseFirestore.getInstance().collection("chats").document(chatId).get().addOnSuccessListener {
            bindChatModel(ChatModel.fromFirebaseEntry(it))
        }
    }

    private fun bindChatModel(chatModel: ChatModel) {
        this.chatModel.set(chatModel)

        databaseListenerReference.remove()
        dataSource.baseQuery = FirebaseFirestore.getInstance().collection("chats").document(chatModel.id).collection("messages")
        databaseListenerReference = dataSource.baseQuery.addSnapshotListener(messageListChangeListener)

        (messagesList as LiveList).initWithPlaceholders()
        dataSource.loadInitial(messagesList::postValue)
        sendMessageReceivedStatus()
    }

    fun fetchPreviousMessages() {
        val oldestLoadedMessageKey = messagesList.value.orEmpty().firstOrNull()?.orderKey ?: return
        dataSource.loadBefore(oldestLoadedMessageKey, (messagesList as LiveList)::prependItems)
    }

    fun View.send() {
        imageInput.get()?.let { sendImage(context) } ?: sendMessage(context)
    }

    private fun sendMessage(context: Context) {
        isSending.set(true)

        val message = mapOf(
            "senderID" to chatModel.get()?.getUserSelf()?.id,
            "content" to (messageInput.get()?.toString()?.takeNotBlank?.trim() ?: return),
            "type" to "TEXT",
            "timestamp" to DateTime.now().millis
        )

        dataSource.baseQuery.add(message)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    messageInput.set(SpannableString(""))
                } else {
                    context.toast("Failed to deliver your message")
                }
                isSending.set(false)
            }
    }

    private fun sendImage(context: Context) {
        isSending.set(true)

        FirebaseStorage.getInstance().getReference("images/messages/${chatModel.get()!!.id}/${DateTime.now().millis}").putFile(imageInput.get()?.path!!)
            .addOnSuccessListener {
                it.metadata!!.reference!!.downloadUrl
                    .addOnSuccessListener {
                        val message = mapOf(
                            "senderID" to chatModel.get()?.getUserSelf()?.id,
                            "content" to it.toString(),
                            "type" to "IMAGE",
                            "timestamp" to DateTime.now().millis
                        )

                        dataSource.baseQuery.add(message)
                            .addOnSuccessListener {
                                messageInput.set(SpannableString(""))
                                imageInput.set(null)
                                isSending.set(false)
                            }
                            .addOnFailureListener {
                                context.toast("Failed to deliver your message")
                                isSending.set(false)
                            }
                    }
                    .addOnFailureListener {
                        context.toast("Failed to deliver your message")
                        isSending.set(false)
                    }
            }
            .addOnFailureListener {
                context.toast("Failed to deliver your message")
                isSending.set(false)
            }
    }

    private fun sendMessageReceivedStatus() {
        FirebaseFunctions.getInstance("europe-west1").getHttpsCallable("setMessagesReceived").call(mapOf("chatId" to chatModel.get()?.id!!, "userId" to FirebaseAuth.getInstance().currentUser?.uid))
    }

    fun View.onAddPhotoFromCameraClick() {
        Tools.generateCameraImageId()

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val temporaryImageUri = Tools.getCacheUri(Tools.lastCameraImageId)

        context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).forEach { cameraAppPackage ->
            context.grantUriPermission(cameraAppPackage.activityInfo.packageName, temporaryImageUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, temporaryImageUri)
        (context as AppCompatActivity).startActivityForResult(intent, Constants.REQUEST_CAMERA)
    }

    fun View.onAddPhotoFromGalleryClick() {
        (context as AppCompatActivity).startActivityForResult(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), Constants.REQUEST_GALLERY)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val imageUri = when (requestCode) {
                Constants.REQUEST_CAMERA -> Tools.getCacheUri(Tools.lastCameraImageId)
                Constants.REQUEST_GALLERY -> data!!.data
                else -> return
            }

            imageInput.set(ImageModel(ImageModel.EditablePhotoType.LOCAL, imageUri))
        }
    }
}
