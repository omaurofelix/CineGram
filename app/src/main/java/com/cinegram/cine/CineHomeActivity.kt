package com.cinegram.cine

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import com.cinegram.cine.profiles.ProfileGateScreen
import com.cinegram.cine.profiles.ProfileStore
import com.cinegram.design.CineTheme

class CineHomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ProfileStore.init(applicationContext)
        ProfileStore.setActive(null)

        SingletonImageLoader.setSafe { context ->
            ImageLoader.Builder(context)
                .components { add(OkHttpNetworkFetcherFactory()) }
                .crossfade(true)
                .build()
        }

        setContent {
            CineTheme {
                val profiles by ProfileStore.profiles.collectAsState()
                val activeId by ProfileStore.activeId.collectAsState()

                if (profiles.isEmpty() || activeId == null) {
                    ProfileGateScreen()
                } else {
                    HomeScreen()
                }
            }
        }
    }
}
