package com.cinegram.cine

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.cinegram.design.CineTheme

/**
 * Abre a tela inicial do CineGram. Fica num Activity separado do principal
 * enquanto a interface nova é construída, para não quebrar o app que já funciona.
 */
class CineHomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CineTheme {
                HomeScreen()
            }
        }
    }
}
