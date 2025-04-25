package es.vmy.musicapp.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.ByteArrayOutputStream

// Converters needed to store Bitmaps in the database
class Converters {

    @TypeConverter
    fun fromBitmap(bitmap: Bitmap?): ByteArray? {
        if (bitmap == null) {
            return null
        }
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }

    @TypeConverter
    fun toBitmap(byteArray: ByteArray?): Bitmap? {
        return byteArray?.let {
            BitmapFactory.decodeByteArray(it, 0, it.size)
        }
    }

    @TypeConverter
    fun fromLongList(list: List<Long>): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun toLongList(data: String): List<Long> {
        val type = object : TypeToken<List<Long>>() {}.type
        return Gson().fromJson(data, type)
    }
}