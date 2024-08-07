package com.pp.community.activity.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.kakao.sdk.user.UserApiClient
import com.pp.community.R
import com.pp.community.activity.CommunityPostDetailsActivity
import com.pp.community.activity.DiaryDetailsActivity
import com.pp.community.activity.UploadDiaryActivity
import com.pp.community.activity.main.route.MainNav
import com.pp.community.activity.main.ui.DiaryScreen
import com.pp.community.activity.main.ui.LoginScreen
import com.pp.community.activity.main.ui.SettingScreen
import com.pp.community.activity.notice.NoticeActivity
import com.pp.community.activity.profile.ProfileActivity
import com.pp.community.activity.terms.TermsOfUseActivity
import com.pp.community.base.BaseActivity
import com.pp.community.ui.CommonCompose
import com.pp.community.ui.theme.color_main
import com.pp.community.ui.theme.color_white
import com.pp.community.viewmodel.main.MainViewModel
import com.pp.domain.model.users.GetUserProfileResponse
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class MainActivity : BaseActivity<MainViewModel>() {
    private lateinit var navController: NavController
    private var shouldRerender by mutableStateOf(false)
    override val viewModel: MainViewModel by viewModels()
    override fun observerViewModel() {
        mViewModel.run {
            movePageEvent.onEach {
                when (it) {
                    "termsOfUse" -> {
                        val intent = Intent(this@MainActivity, TermsOfUseActivity::class.java)
                        intent.putExtra("idToken", getKakaoIdToken())
                        startActivity(intent)
                    }
                }
            }.launchIn(this@MainActivity.lifecycleScope)
        }
    }

    @Composable
    override fun ComposeUi() {
        val renderTrigger = remember { shouldRerender } // 상태 변수를 관찰하여 UI 재구성 트리거

        navController = rememberNavController()
        val appBarTitle = remember {
            mViewModel.appBarTitle
        }.value
        val isLogin = remember {
            mViewModel.isLogin
        }.value
        val communityPostList = remember {
            mViewModel.communityPostList
        }
        val profileInfo = remember{
            mViewModel.profileInfo
        }.value
        val postList by mViewModel.postList.observeAsState(emptyList())

        val packageInfo =
            applicationContext.packageManager.getPackageInfo(applicationContext.packageName, 0)
        var isRefreshing by remember { mutableStateOf(false) }

        val reportPostID = remember {
            mViewModel.reportPostId
        }
        LaunchedEffect(key1 = isLogin) {
            if (isLogin) {
                mViewModel.getPostList()
                mViewModel.getUserProfile()
            }
            mViewModel.fetchMyDiaryList()
        }

        LaunchedEffect(key1 = shouldRerender) {
            mViewModel.fetchMyDiaryList()
            if (isLogin) {
                mViewModel.getPostList()
                mViewModel.getUserProfile()
            }
        }

        Column(Modifier.fillMaxSize()) {
            CommonCompose.CommonAppBarUI(title = appBarTitle, isBackPressed = false) {}
            NavHost(
                modifier = Modifier.weight(1f),
                navController = navController as NavHostController,
                startDestination = MainNav.MyDiary.name
            ) {
                // 나의 일기
                composable(route = MainNav.MyDiary.name) {
                    mViewModel.setAppBarTitle(MainNav.MyDiary.name)
                    DiaryScreen(
                        communityPostList = postList,
                        onClickItemEvent = { postModel ->
                            moveDetailActivity(postModel.id) },
                        onClickUploadEvent = {
                            moveUploadActivity(MainNav.MyDiary.name)
                        },
                        loadEvent = { postList }
                    )
                }

                // 커뮤니티
                composable(route = MainNav.Community.name) {
                    mViewModel.setAppBarTitle(MainNav.Community.name)
                    when (isLogin) {
                        true -> {
                            SwipeRefresh(
                                state = rememberSwipeRefreshState(isRefreshing = isRefreshing),
                                onRefresh = {
                                    isRefreshing = true
                                    mViewModel.getPostList()
                                    isRefreshing = false
                                }
                            ){
                                DiaryScreen(
                                    communityPostList = communityPostList,
                                    onClickItemEvent = {
                                        moveCommunityPostDetailsActivity(it.id)
                                    },
                                    onClickUploadEvent = {
                                        moveUploadActivity(MainNav.Community.name)
                                    },
                                    loadEvent = { mViewModel.getPostList(false) }
                                )
                            }

                        }

                        false -> LoginScreen(

                        ) {
                            kakaoLogin()
                        }
                    }
                }
                // 설정
                composable(route = MainNav.Setting.name) {
                    mViewModel.setAppBarTitle(MainNav.Setting.name)
                    SettingScreen(
                        isLogin = isLogin,
                        profileInfo = profileInfo,
                        version = packageInfo.versionName ?: "Unknown"
                    ) {
                        when (it) {
                            "logout" -> mViewModel.logout()
                            "profile" -> moveProfileActivity(profileInfo)
                            "notice" -> moveNoticeActivity()
                            "terms1" -> moveToWebSite("https://pp-api.kro.kr/pp-policy/privacy-policy.html")
                            "terms2" -> moveToWebSite("https://pp-api.kro.kr/pp-policy/terms-and-condition.html")
                        }
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = color_white)
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MainNav.values().forEach { item ->
                    val selected = item.name == appBarTitle

                    Column(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .clickable { moveNavigate(item.name) },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = when (item.name) {
                                "MyDiary" -> painterResource(id = R.drawable.ic_navi_mydiary)
                                "Community" -> painterResource(id = R.drawable.ic_navi_community)
                                else -> painterResource(id = R.drawable.ic_navi_setting)
                            },
                            contentDescription = "${item.name} Icon",
                            tint = if (selected) color_main else Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = item.name,
                            fontWeight = FontWeight.Bold,
                            color = if (selected) color_main else Color.Gray,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

        }

    }

    override fun init() {
        initData()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onRestart() {
        super.onRestart()
        shouldRerender = !shouldRerender // 상태 값을 변경하여 Compose UI를 재렌더링
    }

    private fun initData() {
        mViewModel.getAccessToken()
    }

    private fun kakaoLogin() {
        UserApiClient.instance.loginWithKakaoAccount(this) { token, error ->
            if (error != null) {
                Log.e("EJ_LOG", "로그인 실패", error)
            } else if (token != null) {
                mViewModel.isUserRegistered(token.idToken ?: "")
            }
        }
    }

    private fun moveNavigate(destination: String) {
        if (::navController.isInitialized) {

            navController.navigate(destination){
                popUpTo(0)
            }
        }
    }

    private fun moveUploadActivity(type: String) {
        val intent = Intent(this@MainActivity, UploadDiaryActivity::class.java)
        intent.putExtra("type", type)
        startActivity(intent)
    }
    private fun moveProfileActivity(profileInfo: GetUserProfileResponse?) {
        val intent = Intent(this@MainActivity, ProfileActivity::class.java).apply {
            putExtra("profileInfo", profileInfo)
        }
        startActivity(intent)
    }
    private fun moveDetailActivity(postId: Int) {
        val intent = Intent(this@MainActivity, DiaryDetailsActivity::class.java)
        intent.putExtra("postId", postId)
        startActivity(intent)
    }
    private fun moveNoticeActivity(){
        val intent = Intent(this@MainActivity, NoticeActivity::class.java)
        startActivity(intent)
    }
    private fun moveToWebSite(url: String){
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }
    private fun moveCommunityPostDetailsActivity(postId: Int) {
        val intent = Intent(this@MainActivity, CommunityPostDetailsActivity::class.java)
        intent.putExtra("postId", postId)
//        startActivity(intent)
        startForResult.launch(intent)
    }
    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            data?.let {
                val resultValue = it.getIntExtra("result_key",-1)
                Log.d("MainActivity", "Result: $resultValue")
                if(resultValue != -1){
                    mViewModel.reportPostId = resultValue
                    mViewModel.getPostList()
                }
            }
        }
    }
}
