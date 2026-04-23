package com.edgefit.coach.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edgefit.coach.data.model.DashboardData
import com.edgefit.coach.data.repository.DashboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val dashboardRepository: DashboardRepository
) : ViewModel() {

    private val _dashboardData = MutableStateFlow<DashboardData?>(null)
    val dashboardData: StateFlow<DashboardData?> = _dashboardData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _lastUpdated = MutableStateFlow<String?>(null)
    val lastUpdated: StateFlow<String?> = _lastUpdated.asStateFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            dashboardRepository.getDashboardData().fold(
                onSuccess = { data ->
                    _dashboardData.value = data
                    _lastUpdated.value = getCurrentTimestamp()
                    _isLoading.value = false
                },
                onFailure = { exception ->
                    _error.value = exception.message ?: "Failed to load dashboard data"
                    _isLoading.value = false
                }
            )
        }
    }

    fun refreshDashboard() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _error.value = null

            dashboardRepository.refreshDashboard().fold(
                onSuccess = { data ->
                    _dashboardData.value = data
                    _lastUpdated.value = getCurrentTimestamp()
                    _isRefreshing.value = false
                },
                onFailure = { exception ->
                    _error.value = exception.message ?: "Failed to refresh dashboard"
                    _isRefreshing.value = false
                }
            )
        }
    }

    fun clearError() {
        _error.value = null
    }

    private fun getCurrentTimestamp(): String {
        val formatter = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
        return formatter.format(java.util.Date())
    }
}
