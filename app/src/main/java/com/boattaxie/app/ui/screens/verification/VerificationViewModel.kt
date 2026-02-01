package com.boattaxie.app.ui.screens.verification

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boattaxie.app.data.model.*
import com.boattaxie.app.data.repository.AuthRepository
import com.boattaxie.app.data.repository.VerificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VerificationUiState(
    val isLoading: Boolean = false,
    val vehicleType: VehicleType = VehicleType.BOAT,
    val documents: Map<DocumentType, VerificationDocument?> = emptyMap(),
    val uploadingDocument: DocumentType? = null,
    val verificationStatus: VerificationStatus = VerificationStatus.NONE,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val canSubmit: Boolean = false,
    val emailIntent: Intent? = null
)

@HiltViewModel
class VerificationViewModel @Inject constructor(
    private val verificationRepository: VerificationRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(VerificationUiState())
    val uiState: StateFlow<VerificationUiState> = _uiState.asStateFlow()
    
    private var currentVehicleType: VehicleType = VehicleType.BOAT
    
    init {
        observeVerificationStatus()
    }
    
    fun setVehicleType(type: String) {
        currentVehicleType = if (type.lowercase() == "taxi") VehicleType.TAXI else VehicleType.BOAT
        _uiState.update { it.copy(vehicleType = currentVehicleType) }
        loadExistingDocuments()
    }
    
    private fun observeVerificationStatus() {
        viewModelScope.launch {
            verificationRepository.observeVerificationStatus().collect { status ->
                _uiState.update { it.copy(verificationStatus = status) }
            }
        }
    }
    
    private fun loadExistingDocuments() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val documents = verificationRepository.getUserDocuments()
            val documentsMap = documents.associateBy { it.documentType }
            
            val requiredDocs = verificationRepository.getRequiredDocuments(currentVehicleType)
            val canSubmit = requiredDocs.all { docType -> 
                documentsMap[docType] != null 
            }
            
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    documents = documentsMap,
                    canSubmit = canSubmit
                ) 
            }
        }
    }
    
    fun uploadDocument(documentType: DocumentType, imageUri: Uri) {
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    uploadingDocument = documentType,
                    errorMessage = null
                ) 
            }
            
            val result = verificationRepository.uploadDocument(
                imageUri = imageUri,
                documentType = documentType,
                vehicleType = currentVehicleType
            )
            
            result.fold(
                onSuccess = { document ->
                    val updatedDocs = _uiState.value.documents.toMutableMap()
                    updatedDocs[documentType] = document
                    
                    val requiredDocs = verificationRepository.getRequiredDocuments(currentVehicleType)
                    val canSubmit = requiredDocs.all { docType -> 
                        updatedDocs[docType] != null 
                    }
                    
                    _uiState.update { 
                        it.copy(
                            uploadingDocument = null,
                            documents = updatedDocs,
                            canSubmit = canSubmit,
                            successMessage = "Document uploaded successfully"
                        ) 
                    }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            uploadingDocument = null,
                            errorMessage = error.message ?: "Failed to upload document"
                        ) 
                    }
                }
            )
        }
    }
    
    fun deleteDocument(documentType: DocumentType) {
        val document = _uiState.value.documents[documentType] ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val result = verificationRepository.deleteDocument(document.id)
            
            result.fold(
                onSuccess = {
                    val updatedDocs = _uiState.value.documents.toMutableMap()
                    updatedDocs.remove(documentType)
                    
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            documents = updatedDocs,
                            canSubmit = false
                        ) 
                    }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to delete document"
                        ) 
                    }
                }
            )
        }
    }
    
    fun submitVerification() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            val documents = _uiState.value.documents.values.filterNotNull()
            
            // Get user name for email
            val userName = authRepository.getCurrentUser()?.fullName ?: "Unknown User"
            
            val result = verificationRepository.submitVerification(
                vehicleType = currentVehicleType,
                documents = documents
            )
            
            result.fold(
                onSuccess = {
                    // Create email intent with documents
                    val emailIntent = verificationRepository.sendVerificationEmail(
                        documents = documents,
                        vehicleType = currentVehicleType,
                        userName = userName
                    )
                    
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            verificationStatus = VerificationStatus.APPROVED, // Auto-approved for testing
                            successMessage = "Verification approved! Sending documents via email...",
                            emailIntent = emailIntent
                        ) 
                    }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to submit verification"
                        ) 
                    }
                }
            )
        }
    }
    
    fun clearEmailIntent() {
        _uiState.update { it.copy(emailIntent = null) }
    }
    
    fun getRequiredDocuments(): List<DocumentType> {
        return verificationRepository.getRequiredDocuments(currentVehicleType)
    }
    
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    fun clearSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }
}
