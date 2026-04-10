package com.focusguard.app.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.focusguard.app.data.database.converter.Converters
import com.focusguard.app.data.database.dao.AppRestrictionDao
import com.focusguard.app.data.database.entity.AppRestrictionEntity

@Database(
    entities = [AppRestrictionEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appRestrictionDao(): AppRestrictionDao

    companion object {
        const val DATABASE_NAME = "focus_guard.db"
    }
}
