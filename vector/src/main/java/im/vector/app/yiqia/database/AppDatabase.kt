package im.vector.app.yiqia.database

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import im.vector.app.yiqia.contact.data.ContactDaoV2
import im.vector.app.yiqia.contact.data.ContactsDisplayBeanV2
import im.vector.app.yiqia.contact.data.User
import im.vector.app.yiqia.contact.data.UserDao

/**
 * Created by chengww on 2020/11/3
 * @author chengww
 */
@Database(entities = [
    User::class,
    ContactsDisplayBeanV2::class
], version = 30)
abstract class AppDatabase : RoomDatabase() {
    //    abstract fun contactDao(): ContactDao
    abstract fun contactDaoV2(): ContactDaoV2

    abstract fun UserDao(): UserDao

    companion object {
        private var INSTANCE: AppDatabase? = null

        @JvmStatic
        fun getInstance(context: Context) = INSTANCE ?: Room.databaseBuilder(
                context.applicationContext, AppDatabase::class.java, "each_chat_db")
                .fallbackToDestructiveMigration()
                .addMigrations(MIGRATION_1_2)
                .build()
                .also { INSTANCE = it }

        @VisibleForTesting
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("alter table contacts ADD COLUMN lastSeenTs INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
