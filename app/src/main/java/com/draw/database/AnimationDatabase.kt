package com.draw.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.draw.model.Animation

@Database(entities = [Animation::class], version = 2)
abstract class AnimationDatabase : RoomDatabase() {
    abstract fun animationDao(): Animation_Dao

    companion object {
        @Volatile

        private var INSTANCE: AnimationDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `animation_showed_ads` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `keyListAnim` INTEGER NOT NULL, `keyPosition` INTEGER NOT NULL)")
            }
        }

        fun getDatabase(context: Context): AnimationDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AnimationDatabase::class.java,
                    "animation_database"
                ).addMigrations(MIGRATION_1_2) // thêm migration vào đây
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

