package com.example.nutritionmanager.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.example.nutritionmanager.R
import com.example.nutritionmanager.adapter.MealHistoryAdapter
import com.example.nutritionmanager.db.DatabaseHelper
import com.example.nutritionmanager.model.Meal
import java.text.SimpleDateFormat
import java.util.*

class HistoryFragment : BaseFragment() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var mealAdapter: MealHistoryAdapter
    private var allMeals = listOf<Meal>()
    private var currentFilter = "today" // today, week, month

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = DatabaseHelper(requireContext())

        setupRecyclerView(view)
        setupSearch(view)
        setupDateFilters(view)

        // Load mặc định là hôm nay
        loadTodayData(view)
        updateButtonStates(view, "today")
        updateTitle(view, "Hôm nay")
    }

    private fun setupRecyclerView(view: View) {
        mealAdapter = MealHistoryAdapter(emptyList())

        val rvHistory = view.findViewById<RecyclerView>(R.id.rvHistory)
        rvHistory.layoutManager = LinearLayoutManager(requireContext())
        rvHistory.adapter = mealAdapter
    }

    private fun setupDateFilters(view: View) {
        val btnToday = view.findViewById<MaterialButton>(R.id.btnToday)
        val btnWeek = view.findViewById<MaterialButton>(R.id.btnWeek)
        val btnMonth = view.findViewById<MaterialButton>(R.id.btnMonth)

        btnToday.setOnClickListener {
            currentFilter = "today"
            loadTodayData(view)
            updateButtonStates(view, "today")
            updateTitle(view, "Hôm nay")
        }

        btnWeek.setOnClickListener {
            currentFilter = "week"
            loadWeekData(view)
            updateButtonStates(view, "week")
            updateTitle(view, "Tuần này")
        }

        btnMonth.setOnClickListener {
            currentFilter = "month"
            loadMonthData(view)
            updateButtonStates(view, "month")
            updateTitle(view, "Tháng này")
        }
    }

    private fun updateTitle(view: View, period: String) {
        val tvTitle = view.findViewById<TextView>(R.id.tvNutritionTitle)
        tvTitle.text = "🥗 Thống kê nhóm dinh dưỡng $period"
    }

    private fun loadTodayData(view: View) {
        val userId = getCurrentUserId()
        val targetCalories = dbHelper.getCalorieTarget(userId)
        val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

        // Lấy danh sách món ăn
        allMeals = dbHelper.getMealsByDate(today, userId)
        applyFilters(view)

        // Cập nhật thống kê dinh dưỡng cho hôm nay
        updateNutritionStats(view, today)

        // Cập nhật tiến trình calo cho hôm nay
        updateCalorieProgress(view, today, targetCalories)

        // Cập nhật tổng calo hiển thị
        updateTotalCalories(view, allMeals)
    }

    private fun loadWeekData(view: View) {
        val userId = getCurrentUserId()
        val targetCalories = dbHelper.getCalorieTarget(userId)
        val calendar = Calendar.getInstance()
        val meals = mutableListOf<Meal>()
        val dates = mutableListOf<String>()

        // Lấy dữ liệu 7 ngày gần nhất
        for (i in 0 until 7) {
            val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)
            dates.add(date)
            meals.addAll(dbHelper.getMealsByDate(date, userId))
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
        allMeals = meals
        applyFilters(view)

        // Tính tổng dinh dưỡng cho cả tuần
        var totalProtein = 0
        var totalFat = 0
        var totalCarbs = 0
        var totalFiber = 0
        var totalCalories = 0

        for (date in dates) {
            val nutrition = dbHelper.getNutritionByDate(date, userId)
            totalProtein += nutrition.protein
            totalFat += nutrition.fat
            totalCarbs += nutrition.carbs
            totalFiber += nutrition.fiber
            totalCalories += dbHelper.getTotalCaloriesByDate(date, userId)
        }

        // Hiển thị thống kê tuần
        view.findViewById<TextView>(R.id.tvTotalProtein).text = "${totalProtein}g"
        view.findViewById<TextView>(R.id.tvTotalFat).text = "${totalFat}g"
        view.findViewById<TextView>(R.id.tvTotalCarbs).text = "${totalCarbs}g"
        view.findViewById<TextView>(R.id.tvTotalFiber).text = "${totalFiber}g"

        // Cập nhật tiến trình calo cho tuần (tổng calo cả tuần)
        val progress = (totalCalories * 100 / (targetCalories * 7)).coerceIn(0, 100)
        view.findViewById<ProgressBar>(R.id.progressCalorie).progress = progress
        view.findViewById<TextView>(R.id.tvCalorieProgress).text = "$totalCalories/${targetCalories * 7} kcal (tuần)"

        // Cập nhật tổng calo hiển thị
        updateTotalCalories(view, allMeals)
    }

    private fun loadMonthData(view: View) {
        val userId = getCurrentUserId()
        val targetCalories = dbHelper.getCalorieTarget(userId)
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        val meals = mutableListOf<Meal>()
        val dates = mutableListOf<String>()

        // Lấy tất cả các ngày trong tháng hiện tại
        val tempCalendar = Calendar.getInstance()
        tempCalendar.set(currentYear, currentMonth, 1)
        val lastDay = tempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        for (day in 1..lastDay) {
            val date = String.format("%02d/%02d/%04d", day, currentMonth + 1, currentYear)
            dates.add(date)
            meals.addAll(dbHelper.getMealsByDate(date, userId))
        }

        allMeals = meals
        applyFilters(view)

        // Tính tổng dinh dưỡng cho cả tháng
        var totalProtein = 0
        var totalFat = 0
        var totalCarbs = 0
        var totalFiber = 0
        var totalCalories = 0

        for (date in dates) {
            val nutrition = dbHelper.getNutritionByDate(date, userId)
            totalProtein += nutrition.protein
            totalFat += nutrition.fat
            totalCarbs += nutrition.carbs
            totalFiber += nutrition.fiber
            totalCalories += dbHelper.getTotalCaloriesByDate(date, userId)
        }

        // Hiển thị thống kê tháng
        view.findViewById<TextView>(R.id.tvTotalProtein).text = "${totalProtein}g"
        view.findViewById<TextView>(R.id.tvTotalFat).text = "${totalFat}g"
        view.findViewById<TextView>(R.id.tvTotalCarbs).text = "${totalCarbs}g"
        view.findViewById<TextView>(R.id.tvTotalFiber).text = "${totalFiber}g"

        // Cập nhật tiến trình calo cho tháng (tổng calo cả tháng)
        val daysInMonth = lastDay
        val progress = (totalCalories * 100 / (targetCalories * daysInMonth)).coerceIn(0, 100)
        view.findViewById<ProgressBar>(R.id.progressCalorie).progress = progress
        view.findViewById<TextView>(R.id.tvCalorieProgress).text = "$totalCalories/${targetCalories * daysInMonth} kcal (tháng)"

        // Cập nhật tổng calo hiển thị
        updateTotalCalories(view, allMeals)
    }

    private fun updateButtonStates(view: View, activeFilter: String) {
        val btnToday = view.findViewById<MaterialButton>(R.id.btnToday)
        val btnWeek = view.findViewById<MaterialButton>(R.id.btnWeek)
        val btnMonth = view.findViewById<MaterialButton>(R.id.btnMonth)

        // Reset all buttons
        btnToday.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        btnWeek.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        btnMonth.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        btnToday.setTextColor(android.graphics.Color.parseColor("#2E7D32"))
        btnWeek.setTextColor(android.graphics.Color.parseColor("#2E7D32"))
        btnMonth.setTextColor(android.graphics.Color.parseColor("#2E7D32"))

        // Highlight active button
        when (activeFilter) {
            "today" -> {
                btnToday.setBackgroundColor(android.graphics.Color.parseColor("#E8F5E9"))
                btnToday.setTextColor(android.graphics.Color.parseColor("#2E7D32"))
            }
            "week" -> {
                btnWeek.setBackgroundColor(android.graphics.Color.parseColor("#E8F5E9"))
                btnWeek.setTextColor(android.graphics.Color.parseColor("#2E7D32"))
            }
            "month" -> {
                btnMonth.setBackgroundColor(android.graphics.Color.parseColor("#E8F5E9"))
                btnMonth.setTextColor(android.graphics.Color.parseColor("#2E7D32"))
            }
        }
    }

    private fun updateNutritionStats(view: View, date: String) {
        val userId = getCurrentUserId()
        val nutrition = dbHelper.getNutritionByDate(date, userId)

        view.findViewById<TextView>(R.id.tvTotalProtein).text = "${nutrition.protein}g"
        view.findViewById<TextView>(R.id.tvTotalFat).text = "${nutrition.fat}g"
        view.findViewById<TextView>(R.id.tvTotalCarbs).text = "${nutrition.carbs}g"
        view.findViewById<TextView>(R.id.tvTotalFiber).text = "${nutrition.fiber}g"
    }

    private fun updateCalorieProgress(view: View, date: String, targetCalories: Int) {
        val userId = getCurrentUserId()
        val totalCalories = dbHelper.getTotalCaloriesByDate(date, userId)
        val progress = (totalCalories * 100 / targetCalories).coerceIn(0, 100)

        view.findViewById<ProgressBar>(R.id.progressCalorie).progress = progress
        view.findViewById<TextView>(R.id.tvCalorieProgress).text = "$totalCalories/$targetCalories kcal"
    }

    private fun applyFilters(view: View) {
        val etSearch = view.findViewById<TextInputEditText>(R.id.etSearch)
        val keyword = etSearch.text.toString()

        var filtered = allMeals
        if (keyword.isNotEmpty()) {
            filtered = filtered.filter {
                it.name.contains(keyword, ignoreCase = true) ||
                        it.ingredients.contains(keyword, ignoreCase = true)
            }
        }

        val sorted = filtered.sortedByDescending { it.timestamp }
        mealAdapter.updateMeals(sorted)
        updateTotalCalories(view, sorted)
    }

    private fun updateTotalCalories(view: View, meals: List<Meal>) {
        val total = meals.sumOf { it.calories }
        view.findViewById<TextView>(R.id.tvTotalCalories).text = "$total kcal"
    }

    private fun setupSearch(view: View) {
        val etSearch = view.findViewById<TextInputEditText>(R.id.etSearch)
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                applyFilters(view)
            }
        })
    }
}