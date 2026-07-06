package com.example.nutritionmanager.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nutritionmanager.R
import com.example.nutritionmanager.model.Meal

class DashboardMealAdapter(
    private var meals: List<Meal>,
    private val onDeleteClick: (Meal) -> Unit,
    private val onEditClick: (Meal) -> Unit
) : RecyclerView.Adapter<DashboardMealAdapter.DashboardMealViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DashboardMealViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_meal_dashboard, parent, false)
        return DashboardMealViewHolder(view)
    }

    override fun onBindViewHolder(holder: DashboardMealViewHolder, position: Int) {
        val meal = meals[position]
        holder.bind(meal)

        holder.btnEdit.setOnClickListener {
            onEditClick(meal)
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClick(meal)
        }
    }

    override fun getItemCount() = meals.size

    fun updateMeals(newMeals: List<Meal>) {
        meals = newMeals
        notifyDataSetChanged()
    }

    class DashboardMealViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        private val tvMealType: TextView = itemView.findViewById(R.id.tvMealType)
        private val tvMealName: TextView = itemView.findViewById(R.id.tvMealName)
        private val tvMealCalories: TextView = itemView.findViewById(R.id.tvMealCalories)
        private val tvMealIngredients: TextView = itemView.findViewById(R.id.tvMealIngredients)
        private val tvMealDate: TextView = itemView.findViewById(R.id.tvMealDate)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        fun bind(meal: Meal) {
            // Hiển thị loại bữa ăn với màu sắc
            val mealTypeText = when (meal.mealType) {
                "breakfast" -> "🍳 Bữa sáng"
                "lunch" -> "🍱 Bữa trưa"
                "dinner" -> "🍜 Bữa tối"
                "snack" -> "🍿 Bữa phụ"
                else -> "🍽 Bữa ăn"
            }

            tvMealType.text = mealTypeText
            tvMealName.text = meal.name
            tvMealCalories.text = "${meal.calories} kcal"
            tvMealIngredients.text = "Thành phần: ${meal.ingredients}"
            tvMealDate.text = meal.date

            // Đặt màu nền cho loại bữa ăn
            when (meal.mealType) {
                "breakfast" -> tvMealType.setBackgroundColor(android.graphics.Color.parseColor("#FFE0B2"))
                "lunch" -> tvMealType.setBackgroundColor(android.graphics.Color.parseColor("#C8E6C9"))
                "dinner" -> tvMealType.setBackgroundColor(android.graphics.Color.parseColor("#BBDEFB"))
                "snack" -> tvMealType.setBackgroundColor(android.graphics.Color.parseColor("#F8BBD9"))
                else -> tvMealType.setBackgroundColor(android.graphics.Color.parseColor("#E0E0E0"))
            }
        }
    }
}