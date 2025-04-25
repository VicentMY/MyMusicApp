package es.vmy.musicapp.classes

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import es.vmy.musicapp.dao.PlaylistDAO
import es.vmy.musicapp.dao.SongDAO
import es.vmy.musicapp.utils.Converters
import es.vmy.musicapp.utils.DATABASE_NAME

@Database(
    entities = [Song::class, Playlist::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class AppDB: RoomDatabase() {

    abstract fun SongDAO(): SongDAO
    abstract fun PlaylistDAO(): PlaylistDAO

    companion object {

        private var instance: AppDB? = null

        fun getInstance(context: Context): AppDB {
            synchronized(this) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context,
                        AppDB::class.java,
                        DATABASE_NAME
                    ).build()
                }
            }
            return instance!!
        }
    }
}