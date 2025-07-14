package com.example.trat.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.example.trat.data.converters.Converters
import com.example.trat.data.dao.ChatDao
import com.example.trat.data.dao.MessageDao
import com.example.trat.data.entities.Chat
import com.example.trat.data.entities.Message

@Database(
    entities = [Chat::class, Message::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TratDatabase : RoomDatabase() {
    
    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao
    
    companion object {
        @Volatile
        private var INSTANCE: TratDatabase? = null
        
        fun getDatabase(context: Context): TratDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TratDatabase::class.java,
                    "trat_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
} 