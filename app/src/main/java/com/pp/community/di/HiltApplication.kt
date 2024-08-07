package com.pp.community.di

import android.app.Application
import com.kakao.sdk.common.KakaoSdk
import com.pp.community.BuildConfig
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HiltApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        KakaoSdk.init(this,BuildConfig.KAKAO_API_KEY)
    }
}