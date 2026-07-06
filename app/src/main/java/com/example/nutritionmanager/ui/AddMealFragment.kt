package com.example.nutritionmanager.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.example.nutritionmanager.R
import com.example.nutritionmanager.adapter.FoodSuggestionAdapter
import com.example.nutritionmanager.db.DatabaseHelper
import com.example.nutritionmanager.model.FoodDatabase
import com.example.nutritionmanager.model.Meal
import java.text.SimpleDateFormat
import java.util.*

class AddMealFragment : BaseFragment() {

    private lateinit var dbHelper: DatabaseHelper
    private var selectedDate: String = ""
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private val mealTypes = arrayOf("🍳 Bữa sáng", "🍱 Bữa trưa", "🍜 Bữa tối", "🍿 Bữa phụ")
    private val mealTypeMap = mapOf(
        "🍳 Bữa sáng" to "breakfast",
        "🍱 Bữa trưa" to "lunch",
        "🍜 Bữa tối" to "dinner",
        "🍿 Bữa phụ" to "snack"
    )

    private lateinit var foodSuggestionAdapter: FoodSuggestionAdapter
    private val allFoods = FoodDatabase.foodList

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_meal, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = DatabaseHelper(requireContext())

        selectedDate = dateFormat.format(Date())
        view.findViewById<TextView>(R.id.tvSelectedDate).text = "Ngày: $selectedDate"

        setupFoodSearch(view)
        setupSpinners(view)
        setupDatePicker(view)
        setupButtons(view)
    }

    private fun setupFoodSearch(view: View) {
        val etSearchFood = view.findViewById<TextInputEditText>(R.id.etSearchFood)
        val rvFoodSuggestions = view.findViewById<RecyclerView>(R.id.rvFoodSuggestions)

        foodSuggestionAdapter = FoodSuggestionAdapter(emptyList()) { food ->
            val etMealName = view.findViewById<TextInputEditText>(R.id.etMealName)
            val etCalories = view.findViewById<TextInputEditText>(R.id.etCalories)
            val etProtein = view.findViewById<TextInputEditText>(R.id.etProtein)
            val etFat = view.findViewById<TextInputEditText>(R.id.etFat)
            val etCarbs = view.findViewById<TextInputEditText>(R.id.etCarbs)
            val etFiber = view.findViewById<TextInputEditText>(R.id.etFiber)
            val etIngredients = view.findViewById<TextInputEditText>(R.id.etIngredients)

            etMealName.setText(food.name)
            etCalories.setText(food.calories.toString())
            etProtein.setText(food.protein.toString())
            etFat.setText(food.fat.toString())
            etCarbs.setText(food.carbs.toString())
            etFiber.setText(food.fiber.toString())
            etIngredients.setText(food.ingredients)

            rvFoodSuggestions.visibility = View.GONE
            etSearchFood.text?.clear()

            Toast.makeText(requireContext(), "Đã chọn: ${food.name}", Toast.LENGTH_SHORT).show()
        }

        rvFoodSuggestions.layoutManager = LinearLayoutManager(requireContext())
        rvFoodSuggestions.adapter = foodSuggestionAdapter

        etSearchFood.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val keyword = s.toString().trim()
                if (keyword.isNotEmpty()) {
                    val filtered = allFoods.filter {
                        it.name.contains(keyword, ignoreCase = true)
                    }
                    if (filtered.isNotEmpty()) {
                        foodSuggestionAdapter.updateFoods(filtered)
                        rvFoodSuggestions.visibility = View.VISIBLE
                    } else {
                        rvFoodSuggestions.visibility = View.GONE
                    }
                } else {
                    rvFoodSuggestions.visibility = View.GONE
                }
            }
        })
    }

    private fun setupSpinners(view: View) {
        val etMealType = view.findViewById<AutoCompleteTextView>(R.id.etMealType)
        val mealTypeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, mealTypes)
        etMealType.setAdapter(mealTypeAdapter)
        etMealType.setText("🍱 Bữa trưa", false)
    }

    private fun setupDatePicker(view: View) {
        val btnSelectDate = view.findViewById<MaterialButton>(R.id.btnSelectDate)
        val tvSelectedDate = view.findViewById<TextView>(R.id.tvSelectedDate)

        btnSelectDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    selectedDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
                    tvSelectedDate.text = "Ngày: $selectedDate"
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupButtons(view: View) {
        val btnSave = view.findViewById<MaterialButton>(R.id.btnSave)
        val btnCancel = view.findViewById<MaterialButton>(R.id.btnCancel)

        val etMealName = view.findViewById<TextInputEditText>(R.id.etMealName)
        val etCalories = view.findViewById<TextInputEditText>(R.id.etCalories)
        val etIngredients = view.findViewById<TextInputEditText>(R.id.etIngredients)
        val etMealType = view.findViewById<AutoCompleteTextView>(R.id.etMealType)
        val etProtein = view.findViewById<TextInputEditText>(R.id.etProtein)
        val etFat = view.findViewById<TextInputEditText>(R.id.etFat)
        val etCarbs = view.findViewById<TextInputEditText>(R.id.etCarbs)
        val etFiber = view.findViewById<TextInputEditText>(R.id.etFiber)

        btnSave.setOnClickListener {
            val name = etMealName.text.toString().trim()
            val caloriesStr = etCalories.text.toString().trim()
            val ingredients = etIngredients.text.toString().trim()
            val mealTypeDisplay = etMealType.text.toString()

            val protein = etProtein.text.toString().toIntOrNull() ?: 0
            val fat = etFat.text.toString().toIntOrNull() ?: 0
            val carbs = etCarbs.text.toString().toIntOrNull() ?: 0
            val fiber = etFiber.text.toString().toIntOrNull() ?: 0

            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập tên món ăn", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (caloriesStr.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập năng lượng", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val calories = caloriesStr.toIntOrNull()
            if (calories == null || calories <= 0) {
                Toast.makeText(requireContext(), "Năng lượng phải là số dương", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val meal = Meal(
                name = name,
                calories = calories,
                ingredients = ingredients.ifEmpty { "Không có ghi chú" },
                date = selectedDate,
                timestamp = System.currentTimeMillis(),
                mealType = mealTypeMap[mealTypeDisplay] ?: "lunch",
                protein = protein,
                fat = fat,
                carbs = carbs,
                fiber = fiber
            )

            val userId = getCurrentUserId()
            val result = dbHelper.insertMeal(meal, userId)

            if (result != -1L) {
                Toast.makeText(requireContext(), "✅ Đã thêm $name", Toast.LENGTH_SHORT).show()
                // Quay về Dashboard (màn hình chính)
                navigateToDashboard()
            } else {
                Toast.makeText(requireContext(), "❌ Lỗi khi thêm món ăn", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancel.setOnClickListener {
            navigateToDashboard()
        }
    }

    private fun navigateToDashboard() {
        // Xóa tất cả các fragment trong back stack và chuyển về Dashboard
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, DashboardFragment())
            .commit()
    }
}