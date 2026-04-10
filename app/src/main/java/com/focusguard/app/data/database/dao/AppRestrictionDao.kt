package com.focusguard.app.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.focusguard.app.data.database.entity.AppRestrictionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppRestrictionDao {

    @Query("SELECT * FROM app_restrictions ORDER BY appName ASC")
    fun observeAll(): Flow<List<AppRestrictionEntity>>

    @Query("SELECT * FROM app_restrictions WHERE packageName = :packageName LIMIT 1")
    suspend fun findByPackageName(packageName: String): AppRestrictionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: AppRestrictionEntity)

    @Query("DELETE FROM app_restrictions WHERE packageName = :packageName")
    suspend fun deleteByPackageName(packageName: String)

    @Query(
        "UPDATE app_restrictions " +
            "SET usedMinutesToday = usedMinutesToday + :additionalMinutes " +
            "WHERE packageName = :packageName"
    )
    suspend fun addUsageMinutes(packageName: String, additionalMinutes: Int)

    @Query("UPDATE app_restrictions SET usedMinutesToday = 0")
    suspend fun resetAllDailyUsage()
}
