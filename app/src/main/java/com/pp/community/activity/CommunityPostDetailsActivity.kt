package com.pp.community.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.pp.community.R
import com.pp.community.activity.comment.CommentActivity
import com.pp.community.base.BaseActivity
import com.pp.community.ui.getRobotoFontFamily
import com.pp.community.ui.theme.color_F5004F
import com.pp.community.ui.theme.color_black
import com.pp.community.viewmodel.CommunityPostDetailsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class CommunityPostDetailsActivity : BaseActivity<CommunityPostDetailsViewModel>() {
    override val viewModel: CommunityPostDetailsViewModel by viewModels()

    override fun observerViewModel() {
        mViewModel.run {
            deletePostSuccessEvent.onEach {
                Toast.makeText(this@CommunityPostDetailsActivity, it, Toast.LENGTH_SHORT).show()
                finish()
            }.launchIn(lifecycleScope)
            reportPostSuccessEvent.onEach {
                Toast.makeText(this@CommunityPostDetailsActivity, it, Toast.LENGTH_SHORT).show()
                finishWithResult()
            }.launchIn(lifecycleScope)
            blockUserSuccessEvent.onEach {
                Toast.makeText(this@CommunityPostDetailsActivity, it, Toast.LENGTH_SHORT).show()
                finish()
            }.launchIn(lifecycleScope)
        }
    }

    @OptIn(
        ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
        ExperimentalGlideComposeApi::class
    )
    @Composable
    override fun ComposeUi() {
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
        var showMenu by remember { mutableStateOf(false) }
        val postDetails by viewModel.postDetails.collectAsState()

        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                CenterAlignedTopAppBar(
                    title = { },
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
                            when (mViewModel.getUserId() == postDetails?.createdUser?.id) {
                                true -> {
                                    DropdownMenuItem(
                                        text = { Text(text = stringResource(id = R.string.btn_delete)) },
                                        onClick = {
                                            mViewModel.deletePost()
                                        }
                                    )
                                }

                                false -> {
                                    DropdownMenuItem(
                                        text = { Text(text = stringResource(id = R.string.btn_report)) },
                                        onClick = {
                                            mViewModel.reportPost()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(text = "유저 차단") },
                                        onClick = {
                                            mViewModel.blockUser(postDetails?.createdUser?.id ?: -1)
                                        }
                                    )
                                }

                            }

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
                        if (post.postImageUrls.isNullOrEmpty().not()) {
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
                                    rememberPagerState(pageCount = {
                                        post.postImageUrls?.size ?: 0
                                    })

                                HorizontalPager(
                                    state = pagerState,
                                    modifier = Modifier.fillMaxSize()
                                ) { page ->
                                    GlideImage(
                                        modifier = Modifier
                                            .height(361.dp)
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                color = Color.LightGray,
                                            ),
                                        model = post.postImageUrls?.get(page),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop
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

                        Row(
                            modifier = Modifier.padding(top = 31.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            GlideImage(
                                modifier = Modifier
                                    .height(58.dp)
                                    .width(58.dp)
                                    .clip(RoundedCornerShape(30.dp))
                                    .background(
                                        color = Color.LightGray,
                                    ),
                                model = post.createdUser.profileImageUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop
                            )

                            Text(
                                text = post.createdUser.nickname,
                                modifier = Modifier.padding(start = 20.dp),
                                fontSize = 16.sp,
                                fontFamily = getRobotoFontFamily()
                            )
                        }

                        Text(
                            text = post.title,
                            modifier = Modifier.padding(top = 25.dp),
                            fontSize = 18.sp,
                            fontFamily = getRobotoFontFamily()
                        )
                        Text(
                            text = post.createdDate,
                            modifier = Modifier.padding(top = 5.dp),
                            fontSize = 12.sp,
                            fontFamily = getRobotoFontFamily()
                        )
                        Text(
                            text = post.content,
                            modifier = Modifier.padding(top = 20.dp),
                            fontSize = 12.sp,
                            fontFamily = getRobotoFontFamily()
                        )

                        Row(
                            modifier = Modifier.padding(top = 21.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "좋아요",
                                fontSize = 12.sp,
                                fontFamily = getRobotoFontFamily()
                            )
                            Row(
                                modifier = Modifier
                                    .clickable {
                                        if (post.userActionHistory.thumbsUpped) mViewModel.thumbsSidewaysPost(
                                            post.id
                                        ) else mViewModel.thumbsUpPost(post.id)
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_like),
                                    contentDescription = "Like Icon",
                                    modifier = Modifier.size(20.dp),
                                    tint = if (post.userActionHistory.thumbsUpped) color_F5004F else color_black
                                )
                                Text(
                                    text = post.thumbsUpCount.toString(),
                                    modifier = Modifier.padding(start = 2.dp),
                                    fontSize = 12.sp,
                                    fontFamily = getRobotoFontFamily()
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .clickable { moveCommentActivity(post.id) },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "댓글",
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(start = 11.dp),
                                    fontFamily = getRobotoFontFamily()
                                )
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_comment),
                                    contentDescription = "Comment Icon",
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = post.commentCount.toString(),
                                    modifier = Modifier.padding(start = 2.dp),
                                    fontSize = 12.sp,
                                    fontFamily = getRobotoFontFamily()
                                )
                            }
                        }
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
            viewModel.setPostId(postId)
            viewModel.getPostDetails()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
    }

    private fun moveCommentActivity(postId: Int) {
        Log.d("EJ_LOG", "moveCommentAcitivyt : $postId")
        val intent = Intent(this, CommentActivity::class.java)
        intent.putExtra("postId", postId)
        startActivity(intent)
    }
    private fun finishWithResult() {
        val data = Intent().apply {
            putExtra("result_key", mViewModel.getPostId()) // 전달할 데이터 설정
        }
        setResult(RESULT_OK, data)
        finish()
    }
}
