package com.example.nutritionmanager.model

data class Meal(
    val id: Int = 0,
    val name: String,
    val calories: Int,
    val ingredients: String,
    val date: String,
    val timestamp: Long = System.currentTimeMillis(),
    val mealType: String = "lunch", // breakfast, lunch, dinner, snack
    val protein: Int = 0,  // đạm (g)
    val fat: Int = 0,      // béo (g)
    val carbs: Int = 0,    // đường bột (g)
    val fiber: Int = 0     // chất xơ (g)
)