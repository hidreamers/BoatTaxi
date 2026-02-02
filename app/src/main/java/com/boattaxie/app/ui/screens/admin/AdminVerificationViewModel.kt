package com.boattaxie.app.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boattaxie.app.data.model.VerificationSubmission
import com.boattaxie.app.data.repository.VerificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminVerificationUiState(
    val isLoading: Boolean = false,
    val submissions: List<VerificationSubmission> = emptyList(),
    val pendingCount: Int = 0,
    val approvedCount: Int = 0,
    val rejectedCount: Int = 0,
    val message: String? = null,
    val error: String? = null
)

@HiltViewModel
class AdminVerificationViewModel @Inject constructor(
    private val verificationRepository: VerificationRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AdminVerificationUiState())
    val uiState: StateFlow<AdminVerificationUiState> = _uiState.asStateFlow()
    
    fun loadSubmissions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val submissions = verificationRepository.getAllSubmissions()
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        submissions = submissions,
                        pendingCount = submissions.count { s -> s.status == "pending" },
                        approvedCount = submissions.count { s -> s.status == "approved" },
                        rejectedCount = submissions.count { s -> s.status == "rejected" }
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load submissions"
                    )
                }
            }
        }
    }
    
    fun approveSubmission(submissionId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val result = verificationRepository.approveSubmission(submissionId)
            
            if (result.isSuccess) {
                _uiState.update { it.copy(message = "✅ Verification approved! User can now access free subscription.") }
                loadSubmissions() // Refresh list
            } else {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Failed to approve"
                    )
                }
            }
        }
    }
    
    fun rejectSubmission(submissionId: String, reason: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val result = verificationRepository.rejectSubmission(submissionId, reason)
            
            if (result.isSuccess) {
                _uiState.update { it.copy(message = "❌ Verification rejected.") }
                loadSubmissions() // Refresh list
            } else {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Failed to reject"
                    )
                }
            }
        }
    }
    
    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}
