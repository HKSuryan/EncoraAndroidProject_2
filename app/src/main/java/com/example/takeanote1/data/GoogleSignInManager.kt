package com.example.takeanote1.data

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.example.takeanote1.R
import com.example.takeanote1.utils.Constants
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class GoogleSignInManager(activity: Activity) {

    private val googleSignInClient: GoogleSignInClient

    init {
        Log.d("GoogleSignInManager", "init: Initializing GoogleSignInOptions and Client")
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(activity.getString(R.string.default_web_client_id))
            .build()

        googleSignInClient = GoogleSignIn.getClient(activity, gso)
    }



    /** Launch Google Sign-In intent */
    fun signIn(): Intent {
        Log.d("GoogleSignInManager", "signIn: Creating sign-in intent")
        return googleSignInClient.signInIntent
    }

    /** Handle result from onActivityResult */
    fun handleSignInResult(
        data: Intent?,
        onSuccess: (GoogleSignInAccount) -> Unit,
        onError: (String) -> Unit
    ) {
        Log.d("GoogleSignInManager", "handleSignInResult: Processing sign-in result")
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            // Using getResult(ApiException::class.java) helps extract the status code
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                Log.d("GoogleSignInManager", "handleSignInResult: Success for ${account.email}")
                onSuccess(account)
            } else {
                Log.e("GoogleSignInManager", "handleSignInResult: Account is null")
                onError("Google account not found")
            }
        } catch (e: ApiException) {
            // Common codes: 
            // 10: DEVELOPER_ERROR (Usually SHA-1 mismatch or wrong package name)
            // 12500: SIGN_IN_FAILED
            // 7: NETWORK_ERROR
            Log.e("GoogleSignInManager", "handleSignInResult: ApiException code: ${e.statusCode}", e)
            onError("Sign-in failed (Code: ${e.statusCode})")
        } catch (e: Exception) {
            Log.e("GoogleSignInManager", "handleSignInResult: Unexpected error", e)
            onError(e.message ?: "Google sign-in failed")
        }
    }

    /** Optional: sign out */
    fun signOut(onComplete: () -> Unit = {}) {
        Log.d("GoogleSignInManager", "signOut: Signing out user")
        googleSignInClient.signOut().addOnCompleteListener {
            Log.d("GoogleSignInManager", "signOut: Sign-out complete")
            onComplete()
        }
    }

    companion object {
        const val RC_SIGN_IN = 1001
    }
}