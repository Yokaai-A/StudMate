package com.example.studmate.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

class Converters {
    @TypeConverter fun fromList(value: List<String>): String = value.joinToString(SEP)
    @TypeConverter fun toList(value: String): List<String> = if (value.isEmpty()) emptyList() else value.split(SEP)

    private companion object { const val SEP = "\u001F" }
}

@Database(
    entities = [
        UserEntity::class, ClassEntity::class, SessionEntity::class, EnrollmentEntity::class,
        AttendanceEntity::class, ReviewEntity::class, MaterialEntity::class, ForumPostEntity::class,
        AnnouncementEntity::class, PayoutEntity::class, OrgAdminEntity::class, NotificationEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun classDao(): ClassDao
    abstract fun enrollmentDao(): EnrollmentDao
    abstract fun communityDao(): CommunityDao
    abstract fun walletDao(): WalletDao
}
