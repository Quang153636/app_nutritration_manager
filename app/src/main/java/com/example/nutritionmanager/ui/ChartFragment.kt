package com.example.nutritionmanager.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import com.example.nutritionmanager.R
import com.example.nutritionmanager.db.DatabaseHelper
import java.text.SimpleDateFormat
import java.util.*

class ChartFragment : BaseFragment() {

    private lateinit var dbHelper: DatabaseHelper
    private var targetCalories = 2000

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = DatabaseHelper(requireContext())

        val userId = getCurrentUserId()
        targetCalories = dbHelper.getCalorieTarget(userId)

        setupButtons(view)
        showWeekChart(view)
    }

    private fun setupButtons(view: View) {
        val btnWeek = view.findViewById<MaterialButton>(R.id.btnChartWeek)
        val btnMonth = view.findViewById<MaterialButton>(R.id.btnChartMonth)
        val btnYear = view.findViewById<MaterialButton>(R.id.btnChartYear)
        val btnCustom = view.findViewById<MaterialButton>(R.id.btnChartCustom)

        btnWeek.minWidth = 0
        btnMonth.minWidth = 0
        btnYear.minWidth = 0

        btnWeek.setOnClickListener {
            showWeekChart(view)
            updateButtonState(view, btnWeek, btnMonth, btnYear)
        }

        btnMonth.setOnClickListener {
            showMonthChart(view)
            updateButtonState(view, btnMonth, btnWeek, btnYear)
        }

        btnYear.setOnClickListener {
            showYearChart(view)
            updateButtonState(view, btnYear, btnWeek, btnMonth)
        }

        btnCustom.setOnClickListener {
            showDatePickerDialog(view)
            updateButtonState(view, btnCustom, btnWeek, btnMonth, btnYear)
        }
    }

    private fun updateButtonState(view: View, activeBtn: MaterialButton, vararg otherBtns: MaterialButton) {
        for (btn in otherBtns) {
            btn.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            btn.setTextColor(android.graphics.Color.parseColor("#2E7D32"))
            btn.strokeColor = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#2E7D32"))
        }

        activeBtn.setBackgroundColor(android.graphics.Color.parseColor("#2E7D32"))
        activeBtn.setTextColor(android.graphics.Color.WHITE)
        activeBtn.strokeColor = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#2E7D32"))
    }

    private fun showWeekChart(view: View) {
        val userId = getCurrentUserId()
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
        val fullDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val chartContainer = view.findViewById<LinearLayout>(R.id.chartContainer)
        chartContainer.removeAllViews()

        val weeklyData = mutableListOf<Pair<String, Int>>()

        for (i in 0 until 7) {
            val fullDate = fullDateFormat.format(calendar.time)
            val calories = dbHelper.getTotalCaloriesByDate(fullDate, userId)
            val shortDate = dateFormat.format(calendar.time)
            weeklyData.add(Pair(shortDate, calories))
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        weeklyData.reversed().forEach { pair ->
            val date = pair.first
            val calories = pair.second
            addBarToChart(chartContainer, date, calories, "#4CAF50")
        }

        updateStatistics(view, weeklyData.map { it.second })
        view.findViewById<TextView>(R.id.tvChartRange).text = "7 ngày gần nhất"
    }

    private fun showMonthChart(view: View) {
        val userId = getCurrentUserId()
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
        val fullDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val chartContainer = view.findViewById<LinearLayout>(R.id.chartContainer)
        chartContainer.removeAllViews()

        val monthData = mutableListOf<Pair<String, Int>>()

        // Lấy số ngày trong tháng
        val tempCalendar = Calendar.getInstance()
        tempCalendar.set(currentYear, currentMonth, 1)
        val lastDay = tempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        for (day in 1..lastDay) {
            val date = String.format("%02d/%02d/%04d", day, currentMonth + 1, currentYear)
            val calories = dbHelper.getTotalCaloriesByDate(date, userId)
            val shortDate = String.format("%02d/%02d", day, currentMonth + 1)
            monthData.add(Pair(shortDate, calories))
        }

        monthData.forEach { pair ->
            val date = pair.first
            val calories = pair.second
            addBarToChart(chartContainer, date, calories, "#FF9800")
        }

        updateStatistics(view, monthData.map { it.second })

        // Sửa lỗi: không thêm chữ "Tháng" dư thừa
        val monthNames = arrayOf("Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6",
            "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12")
        val monthName = monthNames[currentMonth]
        view.findViewById<TextView>(R.id.tvChartRange).text = "$monthName - $currentYear"
    }

    private fun showYearChart(view: View) {
        val userId = getCurrentUserId()
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val chartContainer = view.findViewById<LinearLayout>(R.id.chartContainer)
        chartContainer.removeAllViews()

        val monthlyTotals = mutableMapOf<Int, Int>()
        val monthlyCounts = mutableMapOf<Int, Int>()

        for (month in 0..11) {
            monthlyTotals[month] = 0
            monthlyCounts[month] = 0
        }

        val tempCalendar = Calendar.getInstance()
        tempCalendar.set(currentYear, 0, 1)

        while (tempCalendar.get(Calendar.YEAR) == currentYear) {
            val fullDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(tempCalendar.time)
            val calories = dbHelper.getTotalCaloriesByDate(fullDate, userId)
            val month = tempCalendar.get(Calendar.MONTH)
            monthlyTotals[month] = monthlyTotals[month]!! + calories
            monthlyCounts[month] = monthlyCounts[month]!! + 1
            tempCalendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        val monthNames = arrayOf("T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9", "T10", "T11", "T12")

        for (month in 0..11) {
            val avgCalories = if (monthlyCounts[month]!! > 0) monthlyTotals[month]!! / monthlyCounts[month]!! else 0
            addBarToChart(chartContainer, monthNames[month], avgCalories, "#2196F3")
        }

        val totalYearCalories = monthlyTotals.values.sum()
        val daysWithData = monthlyCounts.values.sum()
        val avgCaloriesPerDay = if (daysWithData > 0) totalYearCalories / daysWithData else 0

        view.findViewById<TextView>(R.id.tvChartTotalCalories).text = "$totalYearCalories kcal"
        view.findViewById<TextView>(R.id.tvChartDaysCount).text = daysWithData.toString()
        view.findViewById<TextView>(R.id.tvChartAvgCalories).text = "$avgCaloriesPerDay kcal"

        view.findViewById<TextView>(R.id.tvChartRange).text = "Năm $currentYear"
    }

    private fun showDatePickerDialog(view: View) {
        val calendar = Calendar.getInstance()

        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val startDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)

                DatePickerDialog(
                    requireContext(),
                    { _, year2, month2, dayOfMonth2 ->
                        val endDate = String.format("%02d/%02d/%04d", dayOfMonth2, month2 + 1, year2)
                        showCustomRangeChart(view, startDate, endDate)
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showCustomRangeChart(view: View, startDate: String, endDate: String) {
        val userId = getCurrentUserId()
        val chartContainer = view.findViewById<LinearLayout>(R.id.chartContainer)
        chartContainer.removeAllViews()

        val meals = dbHelper.getMealsInDateRange(startDate, endDate, userId)
        val dailyMap = mutableMapOf<String, Int>()

        for (meal in meals) {
            val date = meal.date
            val calories = meal.calories
            dailyMap[date] = dailyMap.getOrDefault(date, 0) + calories
        }

        val sortedDates = dailyMap.keys.sortedWith(compareBy {
            val parts = it.split("/")
            "${parts[2]}-${parts[1]}-${parts[0]}"
        })

        for (date in sortedDates) {
            val calories = dailyMap[date] ?: 0
            val shortDate = date.substring(0, 5)
            addBarToChart(chartContainer, shortDate, calories, "#9C27B0")
        }

        updateStatistics(view, dailyMap.values.toList())
        view.findViewById<TextView>(R.id.tvChartRange).text = "$startDate - $endDate"

        if (sortedDates.isEmpty()) {
            Toast.makeText(requireContext(), "Không có dữ liệu trong khoảng thời gian này", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addBarToChart(container: LinearLayout, date: String, calories: Int, color: String) {
        val maxHeight = 200
        val barHeight = if (targetCalories > 0) {
            (calories * maxHeight / targetCalories).coerceIn(10, maxHeight)
        } else {
            10
        }

        val barLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(90, maxHeight + 40)
            gravity = android.view.Gravity.BOTTOM
            setPadding(6, 0, 6, 0)
        }

        val barView = View(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, barHeight)
            setBackgroundColor(android.graphics.Color.parseColor(color))
        }

        val tvCalories = TextView(requireContext()).apply {
            text = if (calories > 0) "$calories" else "0"
            textSize = 10f
            gravity = android.view.Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }

        val tvDate = TextView(requireContext()).apply {
            text = date
            textSize = 10f
            gravity = android.view.Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }

        barLayout.addView(barView)
        barLayout.addView(tvCalories)
        barLayout.addView(tvDate)
        container.addView(barLayout)
    }

    private fun updateStatistics(view: View, caloriesList: List<Int>) {
        val totalCalories = caloriesList.sum()
        val daysCount = caloriesList.size
        val avgCalories = if (daysCount > 0) totalCalories / daysCount else 0

        view.findViewById<TextView>(R.id.tvChartTotalCalories).text = "$totalCalories kcal"
        view.findViewById<TextView>(R.id.tvChartDaysCount).text = daysCount.toString()
        view.findViewById<TextView>(R.id.tvChartAvgCalories).text = "$avgCalories kcal"
    }
}