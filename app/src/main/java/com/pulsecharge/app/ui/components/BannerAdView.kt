package com.pulsecharge.app.ui.components

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.pulsecharge.app.BuildConfig
import com.pulsecharge.app.data.AdConfig

private const val TAG = "PulseChargeAds"

@Composable
fun BannerAdView(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isAdLoaded by remember { mutableStateOf(false) }

    val adView = remember {
        AdView(context).apply {
            setAdSize(AdSize.BANNER)
            adUnitId = AdConfig.getBannerAdUnitId()
            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    Log.d(TAG, "Ad loaded successfully: $adUnitId")
                    isAdLoaded = true
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(
                        TAG,
                        "Ad failed to load ($adUnitId): code=${error.code}, message=${error.message}, responseInfo=${error.responseInfo}"
                    )
                    isAdLoaded = false

                    // If in DEBUG mode and live ad unit fails (e.g. error code 3 NO_FILL by AdMob policy),
                    // fallback to Google's official sample ad unit so you can see live working ads on your device!
                    if (BuildConfig.DEBUG && adUnitId != AdConfig.TEST_BANNER_AD_UNIT_ID) {
                        Log.i(TAG, "Live ad returned code ${error.code} in debug. Falling back to Google test banner for device verification...")
                        adUnitId = AdConfig.TEST_BANNER_AD_UNIT_ID
                        loadAd(AdRequest.Builder().build())
                    }
                }
            }
            loadAd(AdRequest.Builder().build())
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            adView.destroy()
        }
    }

    // Completely remove the extra black card background!
    // If ad is not loaded, do not render any container or empty black card.
    if (isAdLoaded) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { adView }
            )
        }
    }
}
