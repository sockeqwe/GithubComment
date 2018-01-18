package com.hannesdorfmann.githubcomment.http

import com.github.stkent.githubdiffparser.models.Diff
import com.hannesdorfmann.githubcomment.http.model.GithubCodeLineComment
import com.hannesdorfmann.githubcomment.http.model.GithubPullRequest
import com.hannesdorfmann.githubcomment.http.model.GithubSimpleComment
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.http.*

/**
 * Github Web API
 */
interface Github {

    /**
     * Comment a simple Comment on a pull request
     */
    @POST("repos/{owner}/{repo}/issues/{pullRequestId}/comments")
    fun postSimpleComment(@Header("Authorization") accessToken: String, @Path("owner") repoOwner: String, @Path("repo") repoName: String, @Path("pullRequestId") pullRequestId: Long, @Body simpleComment: GithubSimpleComment): Single<Any>


    /**
     * Comment a comment on a given file at given line number (well, not exactly line number see [GithubCodeLineComment] docs, but you get the idea)
     */
    @POST("repos/{owner}/{repo}/pulls/{pullRequestId}/comments")
    fun postCommentOnGivenFile(@Header("Authorization") accessToken: String, @Path("owner") repoOwner: String, @Path("repo") repoName: String, @Path("pullRequestId") pullRequestId: Long, @Body lineComment: GithubCodeLineComment): Single<Any>

    /**
     * Load the details about a certain pull
     */
    @GET("repos/{owner}/{repo}/pulls/{pullRequestId}")
    fun getPullRequestDetails(@Header("Authorization") accessToken: String, @Path("owner") repoOwner: String, @Path("repo") repoName: String, @Path("pullRequestId") pullRequestId: Long): Single<GithubPullRequest>

    /**
     * Get the diffs of a pull request
     */
    @GET("repos/{owner}/{repo}/pulls/{pullRequestId}")
    @Headers(value = ["Accept: application/vnd.github.v3.diff"])
    fun getPullRequestDiff(@Header("Authorization") accessToken: String, @Path("owner") repoOwner: String, @Path("repo") repoName: String, @Path("pullRequestId") pullRequestId: Long): Observable<List<Diff>>


}