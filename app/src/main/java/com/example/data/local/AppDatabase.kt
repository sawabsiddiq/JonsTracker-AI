package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        JobApplication::class,
        JobEvent::class,
        ProcessedEmail::class,
        PendingAiExtraction::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun jobDao(): JobDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `pending_ai_extractions` (
                        `messageId` TEXT NOT NULL, 
                        `threadId` TEXT NOT NULL, 
                        `sender` TEXT NOT NULL, 
                        `subject` TEXT NOT NULL, 
                        `dateString` TEXT NOT NULL, 
                        `body` TEXT NOT NULL, 
                        `snippet` TEXT NOT NULL, 
                        `isJobRelated` INTEGER NOT NULL, 
                        `confidence` REAL NOT NULL, 
                        `eventType` TEXT NOT NULL, 
                        `companyName` TEXT, 
                        `jobTitle` TEXT, 
                        `applicationStatus` TEXT, 
                        `eventDate` TEXT, 
                        `deadline` TEXT, 
                        `recruiterName` TEXT, 
                        `recruiterEmail` TEXT, 
                        `source` TEXT NOT NULL DEFAULT 'Gmail', 
                        `summary` TEXT, 
                        `nextAction` TEXT, 
                        `followUpDate` TEXT, 
                        PRIMARY KEY(`messageId`)
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Drop the old version 2 pending_ai_extractions table if recreating it is cleaner
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `pending_ai_extractions_new` (
                        `messageId` TEXT NOT NULL, 
                        `threadId` TEXT NOT NULL, 
                        `sender` TEXT NOT NULL, 
                        `subject` TEXT NOT NULL, 
                        `dateString` TEXT NOT NULL, 
                        `bodyExcerpt` TEXT NOT NULL, 
                        `snippet` TEXT NOT NULL, 
                        `isJobRelated` INTEGER NOT NULL, 
                        `confidence` REAL NOT NULL, 
                        `eventType` TEXT NOT NULL, 
                        `companyName` TEXT, 
                        `jobTitle` TEXT, 
                        `applicationStatus` TEXT, 
                        `eventDate` TEXT, 
                        `deadline` TEXT, 
                        `recruiterName` TEXT, 
                        `recruiterEmail` TEXT, 
                        `source` TEXT NOT NULL DEFAULT 'Gmail', 
                        `summary` TEXT, 
                        `nextAction` TEXT, 
                        `followUpDate` TEXT, 
                        PRIMARY KEY(`messageId`)
                    )
                """.trimIndent())

                // Copy existing records, slicing old body column to max 1000 characters
                db.execSQL("""
                    INSERT INTO `pending_ai_extractions_new` (
                        messageId, threadId, sender, subject, dateString, bodyExcerpt, snippet, isJobRelated, confidence, eventType,
                        companyName, jobTitle, applicationStatus, eventDate, deadline, recruiterName, recruiterEmail, source, summary, nextAction, followUpDate
                    ) SELECT 
                        messageId, threadId, sender, subject, dateString, SUBSTR(body, 1, 1000), snippet, isJobRelated, confidence, eventType,
                        companyName, jobTitle, applicationStatus, eventDate, deadline, recruiterName, recruiterEmail, source, summary, nextAction, followUpDate
                    FROM `pending_ai_extractions`
                """.trimIndent())

                // Drop old table
                db.execSQL("DROP TABLE IF EXISTS `pending_ai_extractions`")

                // Rename new table to original
                db.execSQL("ALTER TABLE `pending_ai_extractions_new` RENAME TO `pending_ai_extractions`")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "job_hunt_tracker_db"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
