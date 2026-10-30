package com.pulsecharge.app.data

import com.pulsecharge.app.BuildConfig

object AdConfig {
    // Official AdMob IDs
    const val APP_ID = "ca-app-pub-3686694973388754~6598662892"
    const val PROD_BANNER_AD_UNIT_ID = "ca-app-pub-3686694973388754/3661508368"

    // Google's official sample test banner ad unit ID
    // Used in DEBUG builds to comply with AdMob policies and prevent account flagging
    const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"

    /**
     * Resolves the Banner Ad Unit ID.
     * Production ad unit ID is used so real ads are live in both debug and release builds.
     */
    fun getBannerAdUnitId(): String {
        return PROD_BANNER_AD_UNIT_ID
    }
}
