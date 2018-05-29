package nl.booxchange.utilities

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.*
import android.view.View
import nl.booxchange.BooxchangeApp
import nl.booxchange.R
import nl.booxchange.extension.getColorById
import nl.booxchange.extension.string
import nl.booxchange.model.MessageModel
import nl.booxchange.model.MessageType
import nl.booxchange.model.UserModel
import nl.booxchange.screens.BookInfoActivity
import org.jetbrains.anko.startActivity

object MessageUtilities {
    fun getActionCommentary(message: MessageModel, talkersList: List<UserModel>): String {
        val talkerName = when (message.userId) {
            UserData.Session.userId -> "You"
            else -> talkersList.find { it.id == message.userId }?.formattedName ?: "Anonymous"
        }
        val contentInfo = when (MessageType.valueOf(message.type)) {
            MessageType.IMAGE -> "$talkerName sent an image"
            MessageType.TEXT -> "$talkerName: ${message.content}"
            MessageType.REQUEST -> formatRequest(message.content, talkersList)
        }

        return contentInfo.string
    }

    fun formatRequest(content: String, talkersList: List<UserModel>): Spannable {
        val spansList = ArrayList<SpanConfig>()
        val formattablePattern = "\\[\\[([^#]*)#([^#]*)#([^#]*)]]".toRegex()
        var formattedString = content

        formattablePattern.findAll(content).forEach {
            val (source, text, type, value) = it.groupValues

            when (type) {
                "USERMODELID" -> {
                    val talkerName = talkersList.find { it.id == value }?.formattedName ?: "Anonymous"
                    val startPosition = formattedString.indexOf(source)
                    val endPosition = startPosition + talkerName.length
                    formattedString = formattedString.replace(source, talkerName)
                    spansList.add(SpanConfig(StyleSpan(Typeface.BOLD), startPosition..endPosition))
                }
                "BOOKMODELID" -> {
                    val startPosition = formattedString.indexOf(source)
                    val endPosition = startPosition + text.length
                    formattedString = formattedString.replace(source, text)
                    spansList.add(SpanConfig(object : ClickableSpan() {
                        override fun onClick(widget: View?) {
                            BooxchangeApp.delegate.baseContext.startActivity<BookInfoActivity>(Constants.EXTRA_PARAM_BOOK_ID to value)
                        }
                    }, startPosition..endPosition))
                    spansList.add(SpanConfig(UnderlineSpan(), startPosition..endPosition))
                    spansList.add(SpanConfig(ForegroundColorSpan(BooxchangeApp.delegate.baseContext.getColorById(R.color.jetGray)), startPosition..endPosition))
                }
            }
        }

        return SpannableString(formattedString).apply {
            spansList.forEach {
                this.setSpan(it.span, it.range.first, it.range.last, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }

    private data class SpanConfig(
        val span: CharacterStyle,
        val range: IntRange
    )
}
