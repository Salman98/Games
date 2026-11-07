package com.salman98.games.core

import android.app.Activity
import android.os.SystemClock
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

/**
 * All ad logic for a game: consent (EU/UK form), banner, rewarded and interstitial.
 * Ads are limited to G rating because children may play, even though the apps are 13+.
 */
class Ads(private val activity: Activity) {
    var ready by mutableStateOf(false); private set
    var rewardedReady by mutableStateOf(false); private set
    var privacyOptionsRequired by mutableStateOf(false); private set

    private val consent = UserMessagingPlatform.getConsentInformation(activity)
    private var rewarded: RewardedAd? = null
    private var interstitial: InterstitialAd? = null
    private var initStarted = false
    private var loadingRewarded = false
    private var loadingInterstitial = false
    private var levelsDone = 0
    private var lastInterstitialAt = 0L

    fun start() {
        consent.requestConsentInfoUpdate(activity, ConsentRequestParameters.Builder().build(), {
            UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) {
                privacyOptionsRequired = consent.privacyOptionsRequirementStatus ==
                    ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
                initIfAllowed()
            }
        }, { initIfAllowed() })
        initIfAllowed() // consent given in an earlier session lets ads start right away
    }

    fun showPrivacyOptions() = UserMessagingPlatform.showPrivacyOptionsForm(activity) {}

    private fun initIfAllowed() {
        if (!consent.canRequestAds() || initStarted) return
        initStarted = true
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder()
                .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_G)
                .build()
        )
        MobileAds.initialize(activity) {
            activity.runOnUiThread {
                ready = true
                loadRewarded()
                loadInterstitial()
            }
        }
    }

    private fun request() = AdRequest.Builder().build()

    private fun loadRewarded() {
        if (!ready || rewarded != null || loadingRewarded) return
        loadingRewarded = true
        RewardedAd.load(activity, activity.getString(R.string.ad_rewarded_id), request(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    loadingRewarded = false; rewarded = ad; rewardedReady = true
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    loadingRewarded = false; rewarded = null; rewardedReady = false
                }
            })
    }

    private fun loadInterstitial() {
        if (!ready || interstitial != null || loadingInterstitial) return
        loadingInterstitial = true
        InterstitialAd.load(activity, activity.getString(R.string.ad_interstitial_id), request(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    loadingInterstitial = false; interstitial = ad
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    loadingInterstitial = false; interstitial = null
                }
            })
    }

    private fun reloadWhenClosed(reload: () -> Unit) = object : FullScreenContentCallback() {
        override fun onAdDismissedFullScreenContent() = reload()
        override fun onAdFailedToShowFullScreenContent(error: AdError) = reload()
    }

    /** Opt-in video. [onReward] runs only if the player watched it. */
    fun showRewarded(onReward: () -> Unit) {
        val ad = rewarded ?: return loadRewarded()
        rewarded = null
        rewardedReady = false
        ad.fullScreenContentCallback = reloadWhenClosed(::loadRewarded)
        ad.show(activity) { onReward() }
    }

    /** Call when a level is won or a game ends. Shows an interstitial at most every 3rd time and 60s apart. */
    fun levelDone() {
        levelsDone++
        val now = SystemClock.elapsedRealtime()
        val ad = interstitial
        if (ad != null && levelsDone % 3 == 0 && now - lastInterstitialAt > 60_000) {
            interstitial = null
            lastInterstitialAt = now
            ad.fullScreenContentCallback = reloadWhenClosed(::loadInterstitial)
            ad.show(activity)
        }
        loadInterstitial() // retries after an earlier failed load
        loadRewarded()
    }

    @Composable
    fun Banner(modifier: Modifier = Modifier) {
        if (!ready) return
        val widthDp = LocalConfiguration.current.screenWidthDp
        AndroidView(
            modifier = modifier.fillMaxWidth(),
            factory = { ctx ->
                AdView(ctx).apply {
                    setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(ctx, widthDp))
                    adUnitId = ctx.getString(R.string.ad_banner_id)
                    loadAd(request())
                }
            },
            onRelease = { it.destroy() },
        )
    }
}
