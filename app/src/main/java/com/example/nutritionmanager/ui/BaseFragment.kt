package com.example.nutritionmanager.ui

import androidx.fragment.app.Fragment
import com.example.nutritionmanager.utils.SessionManager

abstract class BaseFragment : Fragment() {

    protected fun getCurrentUserId(): Int {
        return SessionManager(requireContext()).getUserId()
    }
}