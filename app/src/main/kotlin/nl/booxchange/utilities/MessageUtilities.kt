package nl.booxchange.utilities

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.*
import android.view.View
import com.vcristian.combus.post
import nl.booxchange.BooxchangeApp
import nl.booxchange.R
import nl.booxchange.extension.getColorCompat
import nl.booxchange.model.events.BookOpenedEvent
import nl.booxchange.model.entities.MessageModel
import nl.booxchange.screens.book.BookDetailsActivity
import org.jetbrains.anko.startActivity

object MessageUtilities {
/*
    fun getActionCommentary(message: MessageModel, talkersList: List<String>): String {
        val talkerName = when (message.id) {
//            UserData.Session.id -> "You"
            else -> ""//talkersList.find { it.id == message.id }?.getFormattedName() ?: "Anonymous"
        }
        val contentInfo = when (message.type) {
            MessageType.IMAGE -> "$talkerName sent an isImage"
            MessageType.TEXT -> "$talkerName: ${message.content}"
            MessageType.REQUEST -> formatRequest(message.content, talkersList)
        }

        return contentInfo.string
    }
*/

    fun getFormattedMessage(message: MessageModel, isOwnMessage: Boolean): Spannable? {
        return when (message.type) {
            "REQUEST" -> {
                if (isOwnMessage) {
                    SpannableString("You've sent a book request!").apply {
                        setSpan(StyleSpan(Typeface.BOLD), 0, length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
                    }
                } else {
                    formatRequest(message.content)
                }
            }
            "TEXT" -> SpannableString(message.content)
            "IMAGE" -> SpannableString(if (isOwnMessage) "You've sent an isImage" else "You've received an isImage").apply {
                setSpan(StyleSpan(Typeface.BOLD), 0, length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
            }
            else -> null
        }
    }

    fun formatRequest(content: String): Spannable {
        val spansList = ArrayList<SpanConfig>()
        val formattablePattern = "\\[\\[([^#]*)#([^#]*)#([^#]*)]]".toRegex()
        var formattedString = content

        formattablePattern.findAll(content).forEach {
            val (source, text, type, value) = it.groupValues

            when (type) {
                "USERID" -> {
                    val startPosition = formattedString.indexOf(source)
                    val endPosition = startPosition + text.length

                    formattedString = formattedString.replace(source, text)
                    spansList.add(SpanConfig(StyleSpan(Typeface.BOLD), startPosition..endPosition))
                    spansList.add(SpanConfig(ForegroundColorSpan(BooxchangeApp.context.getColorCompat(R.color.themeBlueDark)), startPosition..endPosition))
                }
                "BOOKID" -> {
                    val startPosition = formattedString.indexOf(source)
                    val endPosition = startPosition + text.length
                    formattedString = formattedString.replace(source, text)
                    spansList.add(SpanConfig(StyleSpan(Typeface.BOLD), startPosition..endPosition))
                    spansList.add(SpanConfig(UnderlineSpan(), startPosition..endPosition))
                    spansList.add(SpanConfig(ForegroundColorSpan(BooxchangeApp.context.getColorCompat(R.color.themeGreenDark)), startPosition..endPosition))
                    spansList.add(SpanConfig(object: ClickableSpan() {
                        override fun onClick(view: View) {
                            view.context.startActivity<BookDetailsActivity>(BookDetailsActivity.KEY_BOOK_ID to value)
                        }
                    }, startPosition..endPosition))
                }
            }
        }

        return SpannableString(formattedString).apply {
            spansList.forEach {
                setSpan(it.span, it.range.first, it.range.last, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }

    private data class SpanConfig(
        val span: CharacterStyle,
        val range: IntRange
    )
}
