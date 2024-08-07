package com.pp.community.activity

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import coil.compose.rememberImagePainter
import com.pp.community.R
import com.pp.community.base.BaseActivity
import com.pp.community.ui.getRobotoFontFamily
import com.pp.community.ui.theme.color_ebebf4
import com.pp.community.utils.ConvertUtils.dateToString
import com.pp.community.viewmodel.DiaryDetailsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DiaryDetailsActivity : BaseActivity<DiaryDetailsViewModel>() {
    override val viewModel: DiaryDetailsViewModel by viewModels()

    override fun observerViewModel() {
        // ViewModel의 LiveData 또는 State를 관찰하여 UI를 업데이트합니다.
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
    @Composable
    override fun ComposeUi() {
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
        var showMenu by remember { mutableStateOf(false) }
        val postDetails by viewModel.postDetails.collectAsState()

        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(text = postDetails?.title ?: "") },
                    navigationIcon = {
                        IconButton(onClick = { finish() }) {
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowLeft,
                                contentDescription = "Localized description"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { showMenu = !showMenu }) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "Localized description"
                            )
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text(text = stringResource(id = R.string.btn_delete)) },
                                onClick = { if(postDetails?.id != null) {
                                    viewModel.deleteDiary(postDetails!!.id)
                                    showShortToast("삭제에 성공했습니다.")
                                    finish()
                                } }
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior,
                )
            },
            content = { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 16.dp, top = 66.dp, end = 16.dp, bottom = 16.dp),
                ) {
                    postDetails?.let { post ->
                        if (!post.images.isNullOrEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(361.dp)
                                    .background(
                                        color = Color.LightGray, // 기본 배경색 설정
                                        shape = RoundedCornerShape(10.dp),
                                    )
                            ) {
                                val pagerState =
                                    rememberPagerState(pageCount = { post.images?.size ?: 0 })

                                HorizontalPager(
                                    state = pagerState,
                                    modifier = Modifier.fillMaxSize()
                                ) { page ->
                                    Image(
                                        modifier = Modifier
                                            .height(361.dp)
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                color = Color.LightGray,
                                            ),
                                        painter = rememberImagePainter(post.images?.get(page)),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                    )
                                }

                                Row(
                                    Modifier
                                        .wrapContentHeight()
                                        .fillMaxWidth()
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 8.dp),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    repeat(pagerState.pageCount) { iteration ->
                                        val color =
                                            if (pagerState.currentPage == iteration) Color.DarkGray else Color.LightGray
                                        Box(
                                            modifier = Modifier
                                                .padding(2.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                                .size(8.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = post.title,
                            modifier = Modifier.padding(top = 25.dp),
                            fontSize = 18.sp,
                            fontFamily = getRobotoFontFamily()
                        )
                        Text(
                            text = dateToString(post.createDate).toString(),
                            modifier = Modifier.padding(top = 5.dp),
                            fontSize = 12.sp,
                            fontFamily = getRobotoFontFamily()
                        )
                        Text(
                            text = post.contents,
                            modifier = Modifier.padding(top = 20.dp),
                            fontSize = 12.sp,
                            fontFamily = getRobotoFontFamily()
                        )
                    }
                }
            }
        )
    }

    @Preview
    @Composable
    fun Preview() {
        ComposeUi()
    }

    override fun init() {
        val postId = intent.getIntExtra("postId", -1)
        if (postId != -1) {
            viewModel.loadDiaryDetails(postId)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
    }
}
