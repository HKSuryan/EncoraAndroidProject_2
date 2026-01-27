package com.example.takeanote1

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.takeanote1.navigation.AppNavGraph
import com.example.takeanote1.ui.theme.TakeANoteTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("FirebaseTest", "User = ${FirebaseAuth.getInstance().currentUser}")

        super.onCreate(savedInstanceState)
        setContent {
            TakeANoteTheme {
                AppNavGraph()
            }
        }
    }
}
