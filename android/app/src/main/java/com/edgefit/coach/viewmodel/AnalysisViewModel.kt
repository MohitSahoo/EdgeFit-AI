package com.edgefit.coach.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edgefit.coach.data.model.AnalysisResponse
import com.edgefit.coach.data.model.ReportFileResponse
import com.edgefit.coach.data.repository.AnalysisRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnalysisViewModel @Inject constructor(
    private val analysisRepository: AnalysisRepository
) : ViewModel() {

    private val _analysisResult = MutableStateFlow<AnalysisResponse?>(null)
    val analysisResult: StateFlow<AnalysisResponse?> = _analysisResult.asStateFlow()

    private val _reportFile = MutableStateFlow<ReportFileResponse?>(null)
    val reportFile: StateFlow<ReportFileResponse?> = _reportFile.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _isLoadingReport = MutableStateFlow(false)
    val isLoadingReport: StateFlow<Boolean> = _isLoadingReport.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun generateAnalysis() {
        viewModelScope.launch {
            _isGenerating.value = true
            _error.value = null

            analysisRepository.generateReport().fold(
                onSuccess = { response ->
                    _analysisResult.value = response
                    _isGenerating.value = false
                },
                onFailure = { exception ->
                    _error.value = exception.message ?: "Failed to generate analysis"
                    _isGenerating.value = false
                }
            )
        }
    }

    fun loadReportFile() {
        viewModelScope.launch {
            _isLoadingReport.value = true
            _error.value = null

            analysisRepository.getReportFile().fold(
                onSuccess = { response ->
                    _reportFile.value = response
                    _isLoadingReport.value = false
                },
                onFailure = { exception ->
                    _error.value = exception.message ?: "Failed to load report file"
                    _isLoadingReport.value = false
                }
            )
        }
    }

    fun clearError() {
        _error.value = null
    }
}
