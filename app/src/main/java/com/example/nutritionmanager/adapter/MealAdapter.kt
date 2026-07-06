package com.example.nutritionmanager.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nutritionmanager.R
import com.example.nutritionmanager.model.Meal

class MealAdapter(
    private var meals: List<Meal>,
    private val onDeleteClick: (Meal) -> Unit
) : RecyclerView.Adapter<MealAdapter.MealViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_meal_history, parent, false)
        return MealViewHolder(view)
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        val meal = meals[position]
        holder.bind(meal)
        holder.itemView.setOnLongClickListener {
            onDeleteClick(meal)
            true
        }
    }

    override fun getItemCount() = meals.size

    fun updateMeals(newMeals: List<Meal>) {
        meals = newMeals
        notifyDataSetChanged()
    }

    class MealViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvMealName)
        private val tvCalories: TextView = itemView.findViewById(R.id.tvMealCalories)
        private val tvIngredients: TextView = itemView.findViewById(R.id.tvMealIngredients)
        private val tvDate: TextView = itemView.findViewById(R.id.tvMealDate)

        fun bind(meal: Meal) {
            tvName.text = meal.name
            tvCalories.text = "${meal.calories} kcal"
            tvIngredients.text = "Thành phần: ${meal.ingredients}"
            tvDate.text = meal.date
        }
    }
}