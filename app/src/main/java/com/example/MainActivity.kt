package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.ui.MainScreen
import com.example.ui.TradingJournalViewModel

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: TradingJournalViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[TradingJournalViewModel::class.java]

        setContent {
            MainScreen(viewModel = viewModel)
        }
    }
}
