package nl.booxchange.screens.chat

import android.app.Activity
import android.arch.lifecycle.LiveData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.provider.MediaStore
import android.text.SpannableString
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.vcristian.combus.expect
import com.vcristian.combus.post
import nl.booxchange.R
import nl.booxchange.extension.takeNotBlank
import nl.booxchange.extension.single
import nl.booxchange.model.entities.ChatModel
import nl.booxchange.model.entities.ImageModel
import nl.booxchange.model.entities.MessageModel
import nl.booxchange.model.events.ChatOpenedEvent
import nl.booxchange.model.events.StartActivity
import nl.booxchange.utilities.*
import nl.booxchange.utilities.database.FirebasePagingDataSource
import nl.booxchange.utilities.database.ListLiveData
import nl.booxchange.utilities.recycler.ViewHolderConfig
import nl.booxchange.utilities.recycler.ViewHolderConfig.ViewType
import org.jetbrains.anko.toast
import org.joda.time.DateTime

class ChatFragmentViewModel: BaseViewModel() {
    val isSending = ObservableBoolean()

    val chatModel = ObservableField<ChatModel>()
    val messagesViewsConfigurations = listOf<ViewHolderConfig<MessageModel>>(
        ViewHolderConfig(R.layout.chat_item_message, ViewType.MESSAGE_TEXT) { _, messageModel -> messageModel?.type == "TEXT" },
        ViewHolderConfig(R.layout.chat_item_request, ViewType.MESSAGE_REQUEST) { _, messageModel -> messageModel?.type == "REQUEST" },
        ViewHolderConfig(R.layout.chat_item_image, ViewType.MESSAGE_IMAGE) { _, messageModel -> messageModel?.type == "IMAGE" },
        ViewHolderConfig(R.layout.chat_item_placeholder, ViewType.PLACEHOLDER) { _, _ -> true }
    )

    private var currentChatDatabaseReference = FirebaseDatabase.getInstance().getReference("messages/0")
    private val messageListChangeListener = object: ChildEventListener {
        override fun onCancelled(databaseError: DatabaseError) {
            databaseError.toException().printStackTrace()
        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, key: String?) {
            println("Item $key moved")
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, key: String?) {
            println("Item ${dataSnapshot.key} changed")

            if ((messagesList as ListLiveData).value.isNotEmpty()) {
                messagesList[dataSnapshot.key!!] = MessageModel.fromFirebaseEntry(dataSnapshot.key!! to dataSnapshot.value as Map<String, Any>)
            }
        }

        override fun onChildAdded(dataSnapshot: DataSnapshot, key: String?) {
            println("Item ${dataSnapshot.key} added")

            if ((messagesList as ListLiveData).value.isNotEmpty()) {
                dataSource.loadAfter(messagesList.value.last()?.id ?: return, messagesList::plusAssign)

                sendMessageReceivedStatus()
            }
        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {
            println("Item ${dataSnapshot.key} removed")

            if ((messagesList as ListLiveData).value.isNotEmpty()) {
                messagesList.value.find { it?.id == dataSnapshot.key }?.let(messagesList::minusAssign)
            }
        }
    }

    private val dataSource = FirebasePagingDataSource(currentChatDatabaseReference, MessageModel.Companion::fromFirebaseEntry)

    val imageInput = ObservableField<ImageModel>()
    val messageInput = ObservableField<CharSequence>()
    val messagesList: LiveData<List<MessageModel?>> = ListLiveData()

    init {
        expect(ChatOpenedEvent::class.java) { event ->
            event.chatModel?.let(::bindChatModel) ?: FirebaseDatabase.getInstance().getReference("chats/${event.chatId}").single {
                it?.let(ChatModel.Companion::fromFirebaseEntry)?.let(::bindChatModel)
            }
        }
    }

    private fun bindChatModel(chatModel: ChatModel) {
        this.chatModel.set(chatModel)
        currentChatDatabaseReference.removeEventListener(messageListChangeListener)
        currentChatDatabaseReference = FirebaseDatabase.getInstance().getReference("messages/${chatModel.id}")
        currentChatDatabaseReference.addChildEventListener(messageListChangeListener)
        dataSource.baseQuery = currentChatDatabaseReference
        (messagesList as ListLiveData).initWithPlaceholders()
        dataSource.loadInitial(messagesList::postValue)
        sendMessageReceivedStatus()
    }

    fun fetchPreviousMessages() {
        val oldestLoadedMessageKey = messagesList.value.orEmpty().firstOrNull()?.id ?: return
        dataSource.loadBefore(oldestLoadedMessageKey, (messagesList as ListLiveData)::prependItems)
    }

    fun View.send() {
        imageInput.get()?.let { sendImage(context) } ?: sendMessage(context)
    }

    private fun sendMessage(context: Context) {
        isSending.set(true)

        val message = mapOf(
            "user" to chatModel.get()!!.users["self"]!!.id,
            "content" to (messageInput.get()?.toString()?.takeNotBlank?.trim() ?: return),
            "type" to "TEXT"
        )

        currentChatDatabaseReference.child(DateTime.now().millis.toString()).setValue(message).addOnCompleteListener {
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
                val message = mapOf(
                    "user" to chatModel.get()!!.users["self"]!!.id,
                    "content" to it.metadata?.path,
                    "type" to "IMAGE"
                )

                currentChatDatabaseReference.child(DateTime.now().millis.toString()).setValue(message).addOnCompleteListener {
                    if (it.isSuccessful) {
                        messageInput.set(SpannableString(""))
                    }
                    imageInput.set(null)
                    isSending.set(false)
                }
            }
            .addOnFailureListener {
                context.toast("Failed to deliver your message")
                isSending.set(false)
            }
    }

    fun sendMessageReceivedStatus() {
        FirebaseDatabase.getInstance().getReference("chats/${chatModel.get()?.id}").child(FirebaseAuth.getInstance().currentUser?.uid!!).setValue(0)
    }

    fun View.onAddPhotoFromCameraClick() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val temporaryImageUri = Tools.getCacheUri("camera_output.jpeg")
        context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).forEach { cameraAppPackage ->
            context.grantUriPermission(cameraAppPackage.activityInfo.packageName, temporaryImageUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, temporaryImageUri)
        post(StartActivity(intent, Constants.REQUEST_CAMERA, ChatFragment::class.java))
    }

    fun View.onAddPhotoFromGalleryClick() {
        post(StartActivity(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), Constants.REQUEST_GALLERY, ChatFragment::class.java))
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val imageUri = when (requestCode) {
                Constants.REQUEST_CAMERA -> Tools.getCacheUri("camera_output.jpeg")
                Constants.REQUEST_GALLERY -> data!!.data
                else -> return
            }

            imageInput.set(ImageModel("", ImageModel.EditablePhotoType.LOCAL, imageUri))
        }
    }
}
