package com.example.byciclecomputer

import androidx.room.*

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userData: UserData)

    @Query("SELECT * FROM user_data WHERE id = 1")
    suspend fun getUserData(): UserData?
}
