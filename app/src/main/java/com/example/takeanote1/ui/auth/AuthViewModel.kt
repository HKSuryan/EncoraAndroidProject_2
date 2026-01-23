package com.example.takeanote1.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.takeanote1.data.datastore.UserPreferences
import com.example.takeanote1.data.local.entity.UserEntity
import com.example.takeanote1.data.repository.NotesRepository
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(
    private val repository: NotesRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    fun signInWithGoogle(account: GoogleSignInAccount) {
        Log.d("AuthViewModel", "signInWithGoogle started")
        _uiState.value = AuthUiState.Loading

        viewModelScope.launch {
            try {
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                val result = auth.signInWithCredential(credential).await()

                val firebaseUser = result.user
                    ?: throw Exception("Firebase user is null")

                val uid = firebaseUser.uid
                val name = firebaseUser.displayName ?: "Unknown"
                val email = firebaseUser.email ?: "Unknown"
                val photo = firebaseUser.photoUrl?.toString()

                Log.d("AuthViewModel", "Firebase login success: $uid")

                // Save user to Room
                repository.saveUser(
                    UserEntity(
                        id = uid,
                        name = name,
                        email = email,
                        profilePictureUrl = photo
                    )
                )

                // Save UID to DataStore
                userPreferences.saveUserId(uid)

                _uiState.value = AuthUiState.Success
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Google sign-in failed", e)
                _uiState.value = AuthUiState.Error(e.message ?: "Sign-in failed")
            }
        }
    }

    class Factory(
        private val repository: NotesRepository,
        private val userPreferences: UserPreferences
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AuthViewModel(repository, userPreferences) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
