package com.pp.pp.viewmodel.main

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.pp.domain.model.token.OauthTokenRequest
import com.pp.domain.usecase.datastore.GetAccessTokenUseCase
import com.pp.domain.usecase.datastore.SetAccessTokenUseCase
import com.pp.domain.usecase.token.OauthTokenUseCase
import com.pp.domain.usecase.users.UserRegisteredUseCase
import com.pp.pp.activity.main.route.MainNav
import com.pp.pp.base.BaseViewModel
import com.pp.pp.widget.SingleFlowEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val isUserRegisteredUseCase: UserRegisteredUseCase,
    private val oauthTokenUseCase: OauthTokenUseCase,
    private val setAccessTokenUseCase: SetAccessTokenUseCase,
    private val getAccessTokenUseCase: GetAccessTokenUseCase
) : BaseViewModel() {
    var appBarTitle = mutableStateOf("My Wallet")
        private set
    var isLogin = mutableStateOf(false)
        private set
    var isSelectedTerms1 = mutableStateOf(false)
        private set
    var isSelectedTerms2 = mutableStateOf(false)
        private set
    var isSelectedTermsAll = mutableStateOf(false)
        private set
    private val _movePageEvent = SingleFlowEvent<String>()
    val movePageEvent = _movePageEvent.flow
    private var kakaoIdToken: String = ""

    fun setAppBarTitle(title: String) {
        appBarTitle.value = title
    }

    fun isUserRegistered(idToken: String){
        viewModelScope.launch {
            val response = isUserRegisteredUseCase.execute(this@MainViewModel,"kakao",idToken)
            response?.let{
                Log.d("EJ_LOG","isUserReigstered : $it")
                setKakaoIdToken(idToken)
                // true -> oauthToken 호출
                // false -> 회원가입 페이지로 이동
//                when(it.data.isRegistered){
//                    true -> getOauthToken()
//                    false -> _movePageEvent.emit("termsOfUse")
//                }
                _movePageEvent.emit("termsOfUse")

            }
        }
    }

    fun getOauthToken() {
        val oauthTokenRequest = OauthTokenRequest().apply {
            client_id = "kauth.kakao.com"
            client_assertion = kakaoIdToken
            grant_type="client_credentials"
        }
        viewModelScope.launch {
            val response = oauthTokenUseCase.execute(this@MainViewModel, oauthTokenRequest)
            response?.let {
                setAccessToken(it.access_token)
            }
        }
    }
    private fun setAccessToken(accessToken: String) {
        viewModelScope.launch {
            setAccessTokenUseCase.invoke(accessToken)
        }
        getAccessToken()
    }

    fun getAccessToken(){
        viewModelScope.launch {
            getAccessTokenUseCase.invoke().collect{
                it?.let{
                    isLogin.value = true
                }?:{
                    isLogin.value = false
                }
                Log.d("EJ_LOG","getAccessToken : $it")
            }
        }
    }
    fun setKakaoIdToken(idToken: String){
        kakaoIdToken = idToken
    }
    fun getKakaoIdToken(): String{
        return kakaoIdToken
    }
}