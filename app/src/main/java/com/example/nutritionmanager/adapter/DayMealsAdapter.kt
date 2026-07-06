package com.example.nutritionmanager.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nutritionmanager.R
import com.example.nutritionmanager.model.Meal

class DayMealsAdapter(
    private var meals: List<Meal>
) : RecyclerView.Adapter<DayMealsAdapter.DayMealViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayMealViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_day_meal, parent, false)
        return DayMealViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayMealViewHolder, position: Int) {
        val meal = meals[position]
        holder.bind(meal)
    }

    override fun getItemCount() = meals.size

    fun updateMeals(newMeals: List<Meal>) {
        meals = newMeals
        notifyDataSetChanged()
    }

    class DayMealViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        private val tvMealName: TextView = itemView.findViewById(R.id.tvDayMealName)
        private val tvMealCalories: TextView = itemView.findViewById(R.id.tvDayMealCalories)
        private val tvMealIngredients: TextView = itemView.findViewById(R.id.tvDayMealIngredients)

        fun bind(meal: Meal) {
            tvMealName.text = meal.name
            tvMealCalories.text = "${meal.calories} kcal"
            tvMealIngredients.text = "🍽 ${meal.ingredients}"
        }
    }
}