package com.example.nutritionmanager.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.nutritionmanager.model.Meal
import java.text.SimpleDateFormat
import java.util.*

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "nutritrack.db"
        private const val DATABASE_VERSION = 4
        private const val TABLE_NAME = "meals"
        private const val COL_ID = "id"
        private const val COL_NAME = "name"
        private const val COL_CALORIES = "calories"
        private const val COL_INGREDIENTS = "ingredients"
        private const val COL_DATE = "date"
        private const val COL_TIMESTAMP = "timestamp"
        private const val COL_MEAL_TYPE = "meal_type"
        private const val COL_PROTEIN = "protein"
        private const val COL_FAT = "fat"
        private const val COL_CARBS = "carbs"
        private const val COL_FIBER = "fiber"
        private const val COL_USER_ID = "user_id"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_NAME (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_NAME TEXT NOT NULL,
                $COL_CALORIES INTEGER NOT NULL,
                $COL_INGREDIENTS TEXT,
                $COL_DATE TEXT NOT NULL,
                $COL_TIMESTAMP LONG NOT NULL,
                $COL_MEAL_TYPE TEXT DEFAULT 'lunch',
                $COL_PROTEIN INTEGER DEFAULT 0,
                $COL_FAT INTEGER DEFAULT 0,
                $COL_CARBS INTEGER DEFAULT 0,
                $COL_FIBER INTEGER DEFAULT 0,
                $COL_USER_ID INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent()
        db.execSQL(createTable)

        val createSettingsTable = """
            CREATE TABLE settings (
                key TEXT PRIMARY KEY,
                value TEXT
            )
        """.trimIndent()
        db.execSQL(createSettingsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        db.execSQL("DROP TABLE IF EXISTS settings")
        onCreate(db)
    }

    fun insertMeal(meal: Meal, userId: Int): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_NAME, meal.name)
            put(COL_CALORIES, meal.calories)
            put(COL_INGREDIENTS, meal.ingredients)
            put(COL_DATE, meal.date)
            put(COL_TIMESTAMP, meal.timestamp)
            put(COL_MEAL_TYPE, meal.mealType)
            put(COL_PROTEIN, meal.protein)
            put(COL_FAT, meal.fat)
            put(COL_CARBS, meal.carbs)
            put(COL_FIBER, meal.fiber)
            put(COL_USER_ID, userId)
        }
        return db.insert(TABLE_NAME, null, values)
    }

    fun updateMeal(meal: Meal, userId: Int): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_NAME, meal.name)
            put(COL_CALORIES, meal.calories)
            put(COL_INGREDIENTS, meal.ingredients)
            put(COL_DATE, meal.date)
            put(COL_MEAL_TYPE, meal.mealType)
            put(COL_PROTEIN, meal.protein)
            put(COL_FAT, meal.fat)
            put(COL_CARBS, meal.carbs)
            put(COL_FIBER, meal.fiber)
            put(COL_USER_ID, userId)
        }
        val result = db.update(TABLE_NAME, values, "$COL_ID = ? AND $COL_USER_ID = ?", arrayOf(meal.id.toString(), userId.toString()))
        return result > 0
    }

    fun getMealsByDate(date: String, userId: Int): List<Meal> {
        val meals = mutableListOf<Meal>()
        val db = readableDatabase
        val cursor = db.query(TABLE_NAME, null, "$COL_DATE = ? AND $COL_USER_ID = ?", arrayOf(date, userId.toString()), null, null, null)

        while (cursor.moveToNext()) {
            meals.add(extractMeal(cursor))
        }
        cursor.close()
        return meals
    }

    fun getAllMeals(userId: Int): List<Meal> {
        val meals = mutableListOf<Meal>()
        val db = readableDatabase
        val cursor = db.query(TABLE_NAME, null, "$COL_USER_ID = ?", arrayOf(userId.toString()), null, null, "$COL_TIMESTAMP DESC")

        while (cursor.moveToNext()) {
            meals.add(extractMeal(cursor))
        }
        cursor.close()
        return meals
    }

    fun getTotalCaloriesByDate(date: String, userId: Int): Int {
        var total = 0
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT SUM($COL_CALORIES) FROM $TABLE_NAME WHERE $COL_DATE = ? AND $COL_USER_ID = ?", arrayOf(date, userId.toString()))
        if (cursor.moveToFirst()) {
            total = cursor.getInt(0)
        }
        cursor.close()
        return total
    }

    fun getNutritionByDate(date: String, userId: Int): NutritionStats {
        var totalProtein = 0
        var totalFat = 0
        var totalCarbs = 0
        var totalFiber = 0

        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT SUM($COL_PROTEIN) as protein, SUM($COL_FAT) as fat, SUM($COL_CARBS) as carbs, SUM($COL_FIBER) as fiber FROM $TABLE_NAME WHERE $COL_DATE = ? AND $COL_USER_ID = ?",
            arrayOf(date, userId.toString())
        )

        if (cursor.moveToFirst()) {
            totalProtein = cursor.getInt(0)
            totalFat = cursor.getInt(1)
            totalCarbs = cursor.getInt(2)
            totalFiber = cursor.getInt(3)
        }
        cursor.close()

        return NutritionStats(totalProtein, totalFat, totalCarbs, totalFiber)
    }

    fun getMealsInDateRange(startDate: String, endDate: String, userId: Int): List<Meal> {
        val meals = mutableListOf<Meal>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_NAME WHERE $COL_DATE BETWEEN ? AND ? AND $COL_USER_ID = ? ORDER BY $COL_DATE ASC",
            arrayOf(startDate, endDate, userId.toString())
        )

        while (cursor.moveToNext()) {
            meals.add(extractMeal(cursor))
        }
        cursor.close()
        return meals
    }

    fun getLast7DaysAverage(userId: Int): Int {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dates = mutableListOf<String>()

        for (i in 0 until 7) {
            dates.add(dateFormat.format(calendar.time))
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        val db = readableDatabase
        val placeholders = dates.joinToString(",") { "?" }
        val params = dates.toTypedArray() + userId.toString()
        val cursor = db.rawQuery(
            "SELECT AVG($COL_CALORIES) FROM $TABLE_NAME WHERE $COL_DATE IN ($placeholders) AND $COL_USER_ID = ?",
            params
        )

        var avg = 0
        if (cursor.moveToFirst()) {
            avg = cursor.getInt(0)
        }
        cursor.close()
        return avg
    }

    fun deleteMeal(id: Int, userId: Int) {
        val db = writableDatabase
        db.delete(TABLE_NAME, "$COL_ID = ? AND $COL_USER_ID = ?", arrayOf(id.toString(), userId.toString()))
    }

    fun saveCalorieTarget(target: Int, userId: Int): Boolean {
        return try {
            val db = writableDatabase
            val values = ContentValues().apply {
                put("key", "calorie_target_$userId")
                put("value", target.toString())
            }
            val result = db.insertWithOnConflict("settings", null, values, SQLiteDatabase.CONFLICT_REPLACE)
            result != -1L
        } catch (e: Exception) {
            false
        }
    }

    fun getCalorieTarget(userId: Int): Int {
        val db = readableDatabase
        val cursor = db.query("settings", arrayOf("value"), "key = ?", arrayOf("calorie_target_$userId"), null, null, null)
        var target = 2000
        if (cursor.moveToFirst()) {
            target = cursor.getString(0).toIntOrNull() ?: 2000
        }
        cursor.close()
        return target
    }

    private fun extractMeal(cursor: android.database.Cursor): Meal {
        val id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID))
        val name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME))
        val calories = cursor.getInt(cursor.getColumnIndexOrThrow(COL_CALORIES))
        val ingredients = cursor.getString(cursor.getColumnIndexOrThrow(COL_INGREDIENTS))
        val date = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE))
        val timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COL_TIMESTAMP))
        val mealType = cursor.getString(cursor.getColumnIndexOrThrow(COL_MEAL_TYPE))
        val protein = cursor.getInt(cursor.getColumnIndexOrThrow(COL_PROTEIN))
        val fat = cursor.getInt(cursor.getColumnIndexOrThrow(COL_FAT))
        val carbs = cursor.getInt(cursor.getColumnIndexOrThrow(COL_CARBS))
        val fiber = cursor.getInt(cursor.getColumnIndexOrThrow(COL_FIBER))
        return Meal(id, name, calories, ingredients, date, timestamp, mealType, protein, fat, carbs, fiber)
    }
}

data class NutritionStats(
    val protein: Int,
    val fat: Int,
    val carbs: Int,
    val fiber: Int
)