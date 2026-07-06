package com.example.nutritionmanager.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.nutritionmanager.model.User

class UserDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "nutritrack_users.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_USERS = "users"
        private const val COL_ID = "id"
        private const val COL_USERNAME = "username"
        private const val COL_EMAIL = "email"
        private const val COL_PASSWORD = "password"
        private const val COL_FULL_NAME = "full_name"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_USERS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USERNAME TEXT UNIQUE NOT NULL,
                $COL_EMAIL TEXT UNIQUE NOT NULL,
                $COL_PASSWORD TEXT NOT NULL,
                $COL_FULL_NAME TEXT
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    fun registerUser(user: User): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_USERNAME, user.username)
            put(COL_EMAIL, user.email)
            put(COL_PASSWORD, user.password)
            put(COL_FULL_NAME, user.fullName)
        }
        val result = db.insert(TABLE_USERS, null, values)
        return result != -1L
    }

    fun loginUser(username: String, password: String): User? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            null,
            "$COL_USERNAME = ? AND $COL_PASSWORD = ?",
            arrayOf(username, password),
            null, null, null
        )

        if (cursor.moveToFirst()) {
            val user = User(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                username = cursor.getString(cursor.getColumnIndexOrThrow(COL_USERNAME)),
                email = cursor.getString(cursor.getColumnIndexOrThrow(COL_EMAIL)),
                password = cursor.getString(cursor.getColumnIndexOrThrow(COL_PASSWORD)),
                fullName = cursor.getString(cursor.getColumnIndexOrThrow(COL_FULL_NAME))
            )
            cursor.close()
            return user
        }
        cursor.close()
        return null
    }

    fun isUsernameExists(username: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(TABLE_USERS, arrayOf(COL_ID), "$COL_USERNAME = ?", arrayOf(username), null, null, null)
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun isEmailExists(email: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(TABLE_USERS, arrayOf(COL_ID), "$COL_EMAIL = ?", arrayOf(email), null, null, null)
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }
}