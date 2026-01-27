package com.example.takeanote1.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.takeanote1.data.datastore.UserPreferences
import com.example.takeanote1.data.local.entity.UserEntity
import com.example.takeanote1.data.repository.NotesRepository
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val userRepository: NotesRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    fun signInWithGoogle(account: GoogleSignInAccount) {
        Log.d("AuthViewModel", "signInWithGoogle() called for user: ${account.email}")
        _uiState.value = AuthUiState.Loading

        viewModelScope.launch {
            try {
                val userId = account.id ?: throw Exception("No user ID")
                val userName = account.displayName ?: "Unknown"
                val userEmail = account.email ?: "Unknown"
                val profilePicture = account.photoUrl?.toString()

                Log.d("AuthViewModel", "signInWithGoogle: Saving user to Room database")
                userRepository.saveUser(
                    UserEntity(
                        id = userId,
                        name = userName,
                        email = userEmail,
                        profilePictureUrl = profilePicture
                    )
                )
                Log.d("AuthViewModel", "signInWithGoogle: User saved to Room successfully")

                Log.d("AuthViewModel", "signInWithGoogle: Saving userId to DataStore")
                userPreferences.saveUserId(userId)
                Log.d("AuthViewModel", "signInWithGoogle: userId saved to DataStore successfully")

                Log.d("AuthViewModel", "signInWithGoogle: Sign-in process completed successfully for $userId")
                _uiState.value = AuthUiState.Success
            } catch (e: Exception) {
                Log.e("AuthViewModel", "signInWithGoogle: Error during sign-in process", e)
                _uiState.value = AuthUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    class Factory(
        private val repository: NotesRepository,
        private val userPreferences: UserPreferences
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            Log.d("AuthViewModel.Factory", "create() called for ${modelClass.simpleName}")
            if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AuthViewModel(repository, userPreferences) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}