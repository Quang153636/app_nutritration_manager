package com.example.nutritionmanager.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nutritionmanager.R
import com.example.nutritionmanager.model.FoodItem

class FoodSuggestionAdapter(
    private var foods: List<FoodItem>,
    private val onItemClick: (FoodItem) -> Unit
) : RecyclerView.Adapter<FoodSuggestionAdapter.FoodViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_food_suggestion, parent, false)
        return FoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val food = foods[position]
        holder.bind(food)
        holder.itemView.setOnClickListener {
            onItemClick(food)
        }
    }

    override fun getItemCount() = foods.size

    fun updateFoods(newFoods: List<FoodItem>) {
        foods = newFoods
        notifyDataSetChanged()
    }

    class FoodViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        private val tvFoodName: TextView = itemView.findViewById(R.id.tvFoodName)
        private val tvFoodCalories: TextView = itemView.findViewById(R.id.tvFoodCalories)
        private val tvFoodProtein: TextView = itemView.findViewById(R.id.tvFoodProtein)
        private val tvFoodCarbs: TextView = itemView.findViewById(R.id.tvFoodCarbs)

        fun bind(food: FoodItem) {
            tvFoodName.text = food.name
            tvFoodCalories.text = "${food.calories} kcal"
            tvFoodProtein.text = "🥩 ${food.protein}g"
            tvFoodCarbs.text = "🍚 ${food.carbs}g"
        }
    }
}