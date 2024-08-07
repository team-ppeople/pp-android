package com.pp.data.remote.api

import com.pp.data.model.ApiDataResponse
import com.pp.domain.model.comments.GetCommentsResponse
import com.pp.domain.model.comments.PostCommentRequest
import com.pp.domain.model.common.CommonResponse
import com.pp.domain.model.post.GetPostDetailsResponse
import com.pp.domain.model.post.GetPostsResponse
import com.pp.domain.model.post.GetPreSignedUrlRequest
import com.pp.domain.model.post.GetPreSignedUrlResponse
import com.pp.domain.model.post.UploadPostRequest
import com.pp.domain.model.users.GetUserProfileResponse
import com.pp.domain.model.users.UpdateUserProfileRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface PpAuthenticationApi {

    @GET("api/v1/posts")
    suspend fun getPosts(
        @Query("limit") limit: Int,
        @Query("lastId") lastId: Int?
    ): Response<ApiDataResponse<GetPostsResponse>>
    @POST("api/v1/presigned-urls/upload")
    suspend fun getPreSignedUrl(
        @Body getPreSignedUrlRequest: GetPreSignedUrlRequest
    ): Response<ApiDataResponse<GetPreSignedUrlResponse>>
    @POST("api/v1/posts")
    suspend fun uploadPost(
        @Body uploadPostRequest: UploadPostRequest
    ): Response<ApiDataResponse<CommonResponse?>>
    @GET("api/v1/posts/{postId}/comments")
    suspend fun getComments(
        @Path("postId") postId: Int,
        @Query("limit") limit: Int,
        @Query("lastId") lastId: Int?
    ): Response<ApiDataResponse<GetCommentsResponse>>
    @POST("api/v1/posts/{postId}/comments")
    suspend fun postComment(
        @Path("postId") postId: Int,
        @Body content: PostCommentRequest
    ): Response<ApiDataResponse<CommonResponse?>>
    @POST("api/v1/comments/{commentId}/report")
    suspend fun reportComment(
        @Path("commentId") commentId: Int
    ): Response<ApiDataResponse<CommonResponse?>>
    @GET("api/v1/users/{userId}/profiles")
    suspend fun getUserProfile(
        @Path("userId") userId: Int
    ): Response<ApiDataResponse<GetUserProfileResponse?>>
    @PATCH("api/v1/users/{userId}")
    suspend fun updateUserProfile(
        @Path("userId") userId: Int,
        @Body updateUserProfileRequest: UpdateUserProfileRequest
    ): Response<ApiDataResponse<CommonResponse?>>
    @GET("api/v1/posts/{postId}")
    suspend fun getPostDetails(
        @Path("postId") postId: Int,
    ): Response<ApiDataResponse<GetPostDetailsResponse>>
    @POST("api/v1/users/{userId}/block")
    suspend fun blockUser(
        @Path("userId") userId: Int
    ): Response<ApiDataResponse<CommonResponse?>>
    @POST("api/v1/posts/{postId}/report")
    suspend fun reportPost(
        @Path("postId") postId: Int
    ): Response<ApiDataResponse<CommonResponse?>>
    @POST("api/v1/posts/{postId}/thumbs-up")
    suspend fun thumbsUpPost(
        @Path("postId") postId: Int
    ): Response<ApiDataResponse<CommonResponse?>>
    @POST("api/v1/posts/{postId}/thumbs-sideways")
    suspend fun thumbsSideways(
        @Path("postId") postId: Int
    ): Response<ApiDataResponse<CommonResponse?>>
    @DELETE("api/v1/posts/{postId}")
    suspend fun deletePost(
        @Path("postId") postId: Int
    ): Response<ApiDataResponse<CommonResponse?>>
}