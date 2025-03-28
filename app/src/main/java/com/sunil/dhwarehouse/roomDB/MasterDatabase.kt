package com.sunil.dhwarehouse.roomDB

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [AccountMaster::class,ItemMaster::class,InvoiceMaster::class], version = 1, exportSchema = false)
abstract class MasterDatabase : RoomDatabase(){
    abstract fun accountDao() : AccountDao
    abstract fun itemDao() : ItemDao
    abstract fun invoiceDao():InvoiceDao

    companion object{

        @Volatile
        private var INSTANCE : MasterDatabase? = null

        fun getDatabase(context : Context) : MasterDatabase{
            return INSTANCE ?: synchronized(this) {
                val instance  = Room.databaseBuilder(
                    context.applicationContext,
                    MasterDatabase::class.java,
                    "master_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }

    }

}