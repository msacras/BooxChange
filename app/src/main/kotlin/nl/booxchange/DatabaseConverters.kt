package nl.booxchange

import android.arch.persistence.room.TypeConverter
import nl.booxchange.model.MessageModel
import nl.booxchange.model.MessageType
import nl.booxchange.model.OfferType
import nl.booxchange.model.SortingField
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

object DatabaseConverters {
    @JvmStatic
    @TypeConverter
    fun dateTimeToString(dateTime: DateTime): String {
        return dateTime.toString("yyyy-MM-dd'T'HH:mm:ssZ")
    }

    @JvmStatic
    @TypeConverter
    fun dateTimeFromString(dateTime: String): DateTime {
        return DateTime.parse(dateTime, DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ"))
    }

    @JvmStatic
    @TypeConverter
    fun stringCollectionToString(collection: List<String>): String {
        return collection.joinToString("\r\n")
    }

    @JvmStatic
    @TypeConverter
    fun stringToStringCollection(collection: String): List<String> {
        return collection.lines()
    }

    @JvmStatic
    @TypeConverter
    fun offerTypeToString(offerType: OfferType): String {
        return offerType.toString()
    }

    @JvmStatic
    @TypeConverter
    fun offerTypeFromString(offerType: String): OfferType {
        return OfferType.valueOf(offerType)
    }

    @JvmStatic
    @TypeConverter
    fun sortingTypeToString(sortingType: SortingField): String {
        return sortingType.toString()
    }

    @JvmStatic
    @TypeConverter
    fun sortingTypeFromString(sortingType: String): SortingField {
        return SortingField.valueOf(sortingType)
    }

    @JvmStatic
    @TypeConverter
    fun messageTypeToString(messageType: MessageType): String {
        return messageType.name
    }

    @JvmStatic
    @TypeConverter
    fun messageTypeFromString(messageType: String): MessageType {
        return MessageType.valueOf(messageType)
    }

    @JvmStatic
    @TypeConverter
    fun messageModelToId(messageModel: MessageModel): String {
        return messageModel.id
    }

    @JvmStatic
    @TypeConverter
    fun messageIdToModel(messageId: String): MessageModel {
        return BooxchangeDatabase.instance.messagesDao().getMessage(messageId)
    }
}
