package com.example.takeanote1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import com.example.takeanote1.navigation.AppNavGraph
import com.example.takeanote1.ui.theme.TakeANote1Theme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TakeANote1Theme {
                AppNavGraph()
            }
        }
    }
}

