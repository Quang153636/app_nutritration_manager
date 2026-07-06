package com.example.nutritionmanager.model

data class FoodItem(
    val id: Int,
    val name: String,
    val calories: Int,
    val protein: Int,
    val fat: Int,
    val carbs: Int,
    val fiber: Int,
    val ingredients: String
)

object FoodDatabase {
    val foodList = listOf(
        FoodItem(1, "Phở bò", 450, 25, 10, 55, 3, "Bánh phở, thịt bò, hành lá, rau thơm"),
        FoodItem(2, "Cơm tấm", 500, 20, 15, 70, 2, "Cơm, sườn, bì, chả, trứng, đồ chua"),
        FoodItem(3, "Bún chả", 420, 22, 12, 50, 4, "Bún, chả nướng, nem, rau sống"),
        FoodItem(4, "Cơm rang", 380, 12, 10, 55, 2, "Cơm, trứng, đậu Hà Lan, cà rốt, hành"),
        FoodItem(5, "Bánh mì thịt", 350, 15, 12, 40, 2, "Bánh mì, thịt nguội, pate, rau, đồ chua"),
        FoodItem(6, "Mì Quảng", 420, 18, 14, 52, 3, "Mì, tôm, thịt, đậu phộng, rau sống"),
        FoodItem(7, "Cháo sườn", 280, 15, 8, 35, 2, "Cháo, sườn non, hành lá, tiêu"),
        FoodItem(8, "Gà nướng", 320, 35, 18, 5, 1, "Ức gà, gia vị, dầu ăn"),
        FoodItem(9, "Cá hồi áp chảo", 380, 32, 22, 8, 2, "Cá hồi, muối tiêu, chanh"),
        FoodItem(10, "Salad rau trộn", 120, 5, 7, 12, 5, "Xà lách, cà chua, dưa chuột, sốt dầu giấm"),
        FoodItem(11, "Sinh tố bơ", 250, 3, 12, 35, 6, "Bơ, sữa đặc, đá, sữa tươi"),
        FoodItem(12, "Trứng ốp la", 180, 12, 14, 2, 0, "Trứng gà, bơ, muối tiêu"),
        FoodItem(13, "Bún bò Huế", 520, 28, 15, 60, 4, "Bún, bò, giò heo, huyết, rau muống"),
        FoodItem(14, "Xôi gà", 480, 22, 16, 58, 3, "Xôi, gà xé, hành phi, chả"),
        FoodItem(15, "Bánh cuốn", 320, 12, 8, 50, 3, "Bánh cuốn, chả, nước mắm, rau thơm")
    )
}