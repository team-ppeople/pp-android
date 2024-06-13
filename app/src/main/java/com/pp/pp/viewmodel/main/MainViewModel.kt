package com.pp.pp.viewmodel.main

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.pp.domain.model.post.GetPostsRequest
import com.pp.domain.model.post.PostModel
import com.pp.domain.model.token.OauthTokenRequest
import com.pp.domain.model.token.OauthTokenResponse
import com.pp.domain.model.token.RevokeTokenRequest
import com.pp.domain.usecase.datastore.DoLogoutUseCase
import com.pp.domain.usecase.datastore.GetAccessTokenUseCase
import com.pp.domain.usecase.datastore.SetAccessTokenUseCase
import com.pp.domain.usecase.posts.GetPostsUseCase
import com.pp.domain.usecase.token.OauthTokenUseCase
import com.pp.domain.usecase.token.RevokeTokenUseCase
import com.pp.domain.usecase.users.DeleteUserUseCase
import com.pp.domain.usecase.users.UserRegisteredUseCase
import com.pp.pp.activity.main.route.MainNav
import com.pp.pp.base.BaseViewModel
import com.pp.pp.widget.SingleFlowEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val isUserRegisteredUseCase: UserRegisteredUseCase,
    private val oauthTokenUseCase: OauthTokenUseCase,
    private val setAccessTokenUseCase: SetAccessTokenUseCase,
    private val getAccessTokenUseCase: GetAccessTokenUseCase,
    private val getPostsUseCase: GetPostsUseCase,
    private val revokeTokenUseCase: RevokeTokenUseCase,
    private val doLogoutUseCase: DoLogoutUseCase,
    private val deleteUserUseCase: DeleteUserUseCase
) : BaseViewModel() {
    // 공통
    var appBarTitle = mutableStateOf("")
        private set
    private val _movePageEvent = SingleFlowEvent<String>()
    val movePageEvent = _movePageEvent.flow

    // 로그인
    var isLogin = mutableStateOf(false)
        private set
    var isSelectedTerms1 = mutableStateOf(false)
        private set
    var isSelectedTerms2 = mutableStateOf(false)
        private set
    var isSelectedTermsAll = mutableStateOf(false)
        private set

    // 커뮤니티
    var communityPostList = mutableStateListOf<PostModel>()
        private set

    private var kakaoIdToken: String = ""

    // 탈퇴
    private val _deleteResult = SingleFlowEvent<Boolean>()
    val deleteResult = _deleteResult.flow

    /**
     * 로그인
     */
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
                when(it.data.isRegistered){
                    true -> getOauthToken()
                    false -> _movePageEvent.emit("termsOfUse")
                }
//                _movePageEvent.emit("termsOfUse")

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
                setAccessToken(it)
            }
        }
    }
    private fun setAccessToken(oauthTokenResponse: OauthTokenResponse) {
        viewModelScope.launch {
            setAccessTokenUseCase.invoke(oauthTokenResponse)
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
    suspend fun getAccessToken2(): String? {
        return withContext(Dispatchers.IO) {
            val token = getAccessTokenUseCase.invoke().first()
            Log.d("EJ_LOG", "getAccessToken : $token")
            token
        }
    }
    fun setKakaoIdToken(idToken: String){
        kakaoIdToken = idToken
    }
    fun getKakaoIdToken(): String{
        return kakaoIdToken
    }

    /**
     * 커뮤니티
     */
    fun getPostList() {
        val getPostsRequest = GetPostsRequest().apply {  }
        viewModelScope.launch {
            val response = getPostsUseCase.execute(this@MainViewModel, getPostsRequest)
            response?.posts?.let{
                communityPostList.clear()
                communityPostList.addAll(it)
            }
        }
    }
    /**
     * 로그아웃
     */
    fun logout(){
        viewModelScope.launch {
            val revokeTokenRequest = RevokeTokenRequest().apply {
                token = getAccessToken2()?:""
            }
            val response = revokeTokenUseCase.execute(this@MainViewModel,revokeTokenRequest)
            Log.d("EJ_LOG","revokeToken : $response")
            response?.let{
                doLogoutUseCase.invoke()
                isLogin.value = false
            }
        }
    }
    /**
     * 탈퇴하기
     */
    fun deleteUser(userId: String){
        viewModelScope.launch {
            val response = deleteUserUseCase.execute(this@MainViewModel,userId)
            response?.let{
                // TODO TEST
                _deleteResult.emit(true)
                doLogoutUseCase.invoke()
                isLogin.value = false

            }
        }

    }
}