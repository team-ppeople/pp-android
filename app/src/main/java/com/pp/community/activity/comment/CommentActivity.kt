package com.pp.community.activity.comment

import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.pp.community.R
import com.pp.community.activity.comment.ui.CommentUI
import com.pp.community.base.BaseActivity
import com.pp.community.ui.CommonCompose
import com.pp.community.viewmodel.comment.CommentViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class CommentActivity : BaseActivity<CommentViewModel>() {
    override val viewModel: CommentViewModel by viewModels()

    override fun observerViewModel() {
        mViewModel.run {
            reportCommentSuccessEvent.onEach {
                Toast.makeText(
                    this@CommentActivity,
                    getString(R.string.toast_report_success),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    @Composable
    override fun ComposeUi() {
        val commentList = remember {
            viewModel.commentList
        }
        var inputComment by remember {
            mViewModel.inputComment
        }
        Column(Modifier.fillMaxSize()) {
            CommonCompose.CommonAppBarUI(
                title = stringResource(id = R.string.common_comment),
                isBackPressed = true
            ) {
                finish()
            }
            CommentUI(
                list = commentList,
                inputComment = inputComment,
                reportEvent = { mViewModel.reportComment(it) },
                inputCommentEvent = { inputComment = it },
                postCommentEvent = { mViewModel.postComment() },
                loadEvent = { mViewModel.getCommentList(false) }
            )
        }
    }

    override fun init() {
        val postId = intent.getIntExtra("postId", 0)
        Log.d("EJ_LOG", "postId : $postId")
        with(mViewModel) {
            setPostId(postId)
            getCommentList()
        }
    }

}