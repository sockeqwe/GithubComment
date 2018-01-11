package com.hannesdorfmann.githubcomment

import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Path


interface Github {

    @POST("repos/{owner}/{repo}/issues/{issueNumber}/comments")
    fun postComment(@Path("owner") repoOwner: String, @Path("repo") repoName: String, @Path("issueNumber") issueNumber: Int) : Call<Any>
}