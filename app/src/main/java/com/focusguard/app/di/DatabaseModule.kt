package com.focusguard.app.di

import android.content.Context
import androidx.room.Room
import com.focusguard.app.data.database.AppDatabase
import com.focusguard.app.data.database.dao.AppRestrictionDao
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideAppRestrictionDao(database: AppDatabase): AppRestrictionDao =
        database.appRestrictionDao()
}
