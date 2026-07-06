package com.example.nutritionmanager.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.example.nutritionmanager.R
import com.example.nutritionmanager.adapter.DashboardMealAdapter
import com.example.nutritionmanager.db.DatabaseHelper
import com.example.nutritionmanager.model.Meal
import java.text.SimpleDateFormat
import java.util.*

class DashboardFragment : BaseFragment() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var mealAdapter: DashboardMealAdapter
    private var targetCalories = 2000

    private val tips = listOf(
        "💪 Ưu tiên bữa sáng giàu đạm, thêm rau xanh vào bữa trưa",
        "🥗 Uống đủ 2 lít nước mỗi ngày để tăng cường trao đổi chất",
        "🍎 Ăn trái cây thay vì đồ ngọt để giảm calo rỗng",
        "🏃‍♂️ Kết hợp vận động nhẹ sau bữa ăn 30 phút"
    )

    private val mealTypes = arrayOf("🍳 Bữa sáng", "🍱 Bữa trưa", "🍜 Bữa tối", "🍿 Bữa phụ")
    private val mealTypeMap = mapOf(
        "🍳 Bữa sáng" to "breakfast",
        "🍱 Bữa trưa" to "lunch",
        "🍜 Bữa tối" to "dinner",
        "🍿 Bữa phụ" to "snack"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = DatabaseHelper(requireContext())

        val userId = getCurrentUserId()
        targetCalories = dbHelper.getCalorieTarget(userId)

        setupViews(view)
        setupCalorieTarget(view)
        loadRandomTip(view)
        loadDashboardData(view)
    }

    private fun setupViews(view: View) {
        val btnAddMeal = view.findViewById<MaterialButton>(R.id.btnAddMeal)
        val btnViewHistory = view.findViewById<MaterialButton>(R.id.btnViewHistory)

        btnAddMeal.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, AddMealFragment())
                .addToBackStack(null)
                .commit()
        }

        btnViewHistory.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, HistoryFragment())
                .addToBackStack(null)
                .commit()
        }

        mealAdapter = DashboardMealAdapter(
            emptyList(),
            onDeleteClick = { meal ->
                showDeleteConfirmation(meal, view)
            },
            onEditClick = { meal ->
                showEditDialog(meal, view)
            }
        )

        val rvTodayMeals = view.findViewById<RecyclerView>(R.id.rvTodayMeals)
        rvTodayMeals.layoutManager = LinearLayoutManager(requireContext())
        rvTodayMeals.adapter = mealAdapter
        rvTodayMeals.isNestedScrollingEnabled = true
    }

    private fun setupCalorieTarget(view: View) {
        val btnSetTarget = view.findViewById<MaterialButton>(R.id.btnSetTarget)

        btnSetTarget.setOnClickListener {
            showSetTargetDialog()
        }
    }

    private fun showSetTargetDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_set_calorie_target, null)
        val etTarget = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etTargetCalories)
        val btnSave = dialogView.findViewById<MaterialButton>(R.id.btnSave)
        val btnCancel = dialogView.findViewById<MaterialButton>(R.id.btnCancel)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        // Hiển thị giá trị hiện tại
        etTarget.setText(targetCalories.toString())

        btnSave.setOnClickListener {
            val input = etTarget.text.toString().trim()

            // Kiểm tra không nhập dữ liệu
            if (input.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập mục tiêu calo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Kiểm tra ký tự không phải số
            val calories = input.toIntOrNull()
            if (calories == null) {
                Toast.makeText(requireContext(), "Mục tiêu calo phải là số hợp lệ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Kiểm tra giá trị calo không hợp lệ
            if (calories <= 0) {
                Toast.makeText(requireContext(), "Mục tiêu calo phải lớn hơn 0", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Lưu mục tiêu calo
            try {
                val userId = getCurrentUserId()
                val success = dbHelper.saveCalorieTarget(calories, userId)

                if (success) {
                    targetCalories = calories
                    Toast.makeText(requireContext(), "✅ Đã đặt mục tiêu: ${calories} kcal/ngày", Toast.LENGTH_SHORT).show()

                    // Cập nhật hiển thị mục tiêu mới
                    val tvCurrentTarget = requireView().findViewById<TextView>(R.id.tvCurrentTarget)
                    tvCurrentTarget.text = "$targetCalories kcal"

                    loadDashboardData(requireView())
                    dialog.dismiss()
                } else {
                    showSaveErrorDialog()
                }
            } catch (e: Exception) {
                showSaveErrorDialog()
            }
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showSaveErrorDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Lỗi lưu dữ liệu")
            .setMessage("Không thể lưu mục tiêu calo. Vui lòng thử lại.")
            .setPositiveButton("Thử lại") { _, _ ->
                showSetTargetDialog()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun loadRandomTip(view: View) {
        val tvTip = view.findViewById<TextView>(R.id.tvTip)
        val randomTip = tips.random()
        tvTip.text = randomTip
    }

    private fun loadDashboardData(view: View) {
        val userId = getCurrentUserId()
        val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val todayMeals = dbHelper.getMealsByDate(today, userId)
        val totalCalories = dbHelper.getTotalCaloriesByDate(today, userId)
        val avg7Days = dbHelper.getLast7DaysAverage(userId)

        val mealCount = todayMeals.size

        view.findViewById<TextView>(R.id.tvTodayCalories).text = "$totalCalories kcal"
        view.findViewById<TextView>(R.id.tvMealCount).text = mealCount.toString()
        view.findViewById<TextView>(R.id.tvAvg7Days).text = "$avg7Days kcal"
        view.findViewById<TextView>(R.id.tvCurrentTarget).text = "$targetCalories kcal"

        mealAdapter.updateMeals(todayMeals)
        updateWeeklyStats(view)
    }

    private fun updateWeeklyStats(view: View) {
        val userId = getCurrentUserId()
        val layout = view.findViewById<LinearLayout>(R.id.layoutWeeklyStats)
        layout.removeAllViews()

        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
        val fullDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        val days = mutableListOf<Pair<String, String>>()
        for (i in 0 until 7) {
            val displayDate = dateFormat.format(calendar.time)
            val fullDate = fullDateFormat.format(calendar.time)
            days.add(Pair(displayDate, fullDate))
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        days.reversed().forEachIndexed { index, (displayDate, fullDate) ->
            val calories = dbHelper.getTotalCaloriesByDate(fullDate, userId)

            val itemView = layoutInflater.inflate(R.layout.item_weekly_stat, null)
            val tvDate = itemView.findViewById<TextView>(R.id.tvStatDate)
            val tvCalories = itemView.findViewById<TextView>(R.id.tvStatCalories)

            tvDate.text = displayDate
            tvCalories.text = "$calories kcal"

            if (calories > 0) {
                tvCalories.setTextColor(android.graphics.Color.parseColor("#FF9800"))
            } else {
                tvCalories.setTextColor(android.graphics.Color.parseColor("#999999"))
            }

            itemView.setOnClickListener {
                showMealsByDate(fullDate, displayDate)
            }

            val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            itemView.layoutParams = params
            layout.addView(itemView)
        }
    }

    private fun showMealsByDate(fullDate: String, displayDate: String) {
        val userId = getCurrentUserId()
        val meals = dbHelper.getMealsByDate(fullDate, userId)
        val totalCalories = dbHelper.getTotalCaloriesByDate(fullDate, userId)

        val dialogView = layoutInflater.inflate(R.layout.dialog_day_meals, null)
        val tvDialogDate = dialogView.findViewById<TextView>(R.id.tvDialogDate)
        val rvDayMeals = dialogView.findViewById<RecyclerView>(R.id.rvDayMeals)
        val tvDialogTotalCalories = dialogView.findViewById<TextView>(R.id.tvDialogTotalCalories)

        tvDialogDate.text = "📅 Ngày: $displayDate"
        tvDialogTotalCalories.text = "🔥 Tổng calo: $totalCalories kcal | Số món: ${meals.size}"

        val adapter = com.example.nutritionmanager.adapter.MealHistoryAdapter(meals)
        rvDayMeals.layoutManager = LinearLayoutManager(requireContext())
        rvDayMeals.adapter = adapter

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Đóng") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showDeleteConfirmation(meal: Meal, view: View) {
        AlertDialog.Builder(requireContext())
            .setTitle("Xóa món ăn")
            .setMessage("Bạn có chắc muốn xóa '${meal.name}' không?")
            .setPositiveButton("Xóa") { _, _ ->
                val userId = getCurrentUserId()
                dbHelper.deleteMeal(meal.id, userId)
                loadDashboardData(view)
                Toast.makeText(requireContext(), "Đã xóa ${meal.name}", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun showEditDialog(meal: Meal, view: View) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_meal, null)

        val etName = dialogView.findViewById<EditText>(R.id.etEditName)
        val etCalories = dialogView.findViewById<EditText>(R.id.etEditCalories)
        val etIngredients = dialogView.findViewById<EditText>(R.id.etEditIngredients)
        val etMealType = dialogView.findViewById<AutoCompleteTextView>(R.id.etEditMealType)
        val etProtein = dialogView.findViewById<EditText>(R.id.etEditProtein)
        val etFat = dialogView.findViewById<EditText>(R.id.etEditFat)
        val etCarbs = dialogView.findViewById<EditText>(R.id.etEditCarbs)
        val etFiber = dialogView.findViewById<EditText>(R.id.etEditFiber)

        etName.setText(meal.name)
        etCalories.setText(meal.calories.toString())
        etIngredients.setText(meal.ingredients)
        etProtein.setText(meal.protein.toString())
        etFat.setText(meal.fat.toString())
        etCarbs.setText(meal.carbs.toString())
        etFiber.setText(meal.fiber.toString())

        val mealTypeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, mealTypes)
        etMealType.setAdapter(mealTypeAdapter)

        val currentMealTypeDisplay = when (meal.mealType) {
            "breakfast" -> "🍳 Bữa sáng"
            "lunch" -> "🍱 Bữa trưa"
            "dinner" -> "🍜 Bữa tối"
            "snack" -> "🍿 Bữa phụ"
            else -> "🍱 Bữa trưa"
        }
        etMealType.setText(currentMealTypeDisplay, false)

        AlertDialog.Builder(requireContext())
            .setTitle("✏️ Sửa món ăn")
            .setView(dialogView)
            .setPositiveButton("Lưu") { _, _ ->
                val newName = etName.text.toString().trim()
                val newCaloriesStr = etCalories.text.toString().trim()
                val newIngredients = etIngredients.text.toString().trim()
                val newMealTypeDisplay = etMealType.text.toString()

                val newProtein = etProtein.text.toString().toIntOrNull() ?: 0
                val newFat = etFat.text.toString().toIntOrNull() ?: 0
                val newCarbs = etCarbs.text.toString().toIntOrNull() ?: 0
                val newFiber = etFiber.text.toString().toIntOrNull() ?: 0

                val newMealType = mealTypeMap[newMealTypeDisplay] ?: "lunch"

                if (newName.isEmpty()) {
                    Toast.makeText(requireContext(), "Tên món ăn không được để trống", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val newCalories = newCaloriesStr.toIntOrNull()
                if (newCalories == null || newCalories <= 0) {
                    Toast.makeText(requireContext(), "Calories phải là số dương", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val updatedMeal = meal.copy(
                    name = newName,
                    calories = newCalories,
                    ingredients = newIngredients.ifEmpty { "Không có ghi chú" },
                    mealType = newMealType,
                    protein = newProtein,
                    fat = newFat,
                    carbs = newCarbs,
                    fiber = newFiber
                )

                val userId = getCurrentUserId()
                dbHelper.updateMeal(updatedMeal, userId)
                loadDashboardData(view)
                Toast.makeText(requireContext(), "✅ Đã cập nhật $newName", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
}