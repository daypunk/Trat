package com.example.trat.di

import android.content.Context
import androidx.room.Room
import com.example.trat.data.dao.ChatDao
import com.example.trat.data.dao.MessageDao
import com.example.trat.data.database.TratDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TratDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            TratDatabase::class.java,
            "trat_database"
        ).build()
    }
    
    @Provides
    fun provideChatDao(database: TratDatabase): ChatDao {
        return database.chatDao()
    }
    
    @Provides
    fun provideMessageDao(database: TratDatabase): MessageDao {
        return database.messageDao()
    }
} 