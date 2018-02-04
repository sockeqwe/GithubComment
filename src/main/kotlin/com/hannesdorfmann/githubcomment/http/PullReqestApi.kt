package com.hannesdorfmann.githubcomment.http

import com.github.stkent.githubdiffparser.models.Diff
import com.hannesdorfmann.githubcomment.Output
import com.hannesdorfmann.githubcomment.http.converter.DiffConverterFactory
import com.hannesdorfmann.githubcomment.http.model.GithubCodeLineComment
import com.hannesdorfmann.githubcomment.http.model.GithubPullRequest
import com.hannesdorfmann.githubcomment.http.model.GithubSimpleComment
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * A simple facade to make http calls to Github web API
 */
class PullReqestApi(
        private val githubBaseUrl: String,
        private val repoOwner: String,
        private val repoName: String,
        private val pullRequestId: Long,
        private val accessToken: String,
        private val exponentBackoffRetries: Long = 3,
        private val exponentialBackoffStartDelayMiliseconds: Long = 1000,
        private val scheduler: Scheduler
) {

    private val github: Github

    init {

        val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

        val okHttp = OkHttpClient.Builder()
                //.addInterceptor(HttpLoggingInterceptor().also { it.level = HttpLoggingInterceptor.Level.BODY })
                .build()

        github = Retrofit.Builder()
                .baseUrl(githubBaseUrl)
                .client(okHttp)
                .addConverterFactory(DiffConverterFactory())
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(Github::class.java)
    }

    private fun commentUrl() = githubBaseUrl + "repos/$repoOwner/$repoName/pulls/$pullRequestId"


    /**
     * Post a [GithubSimpleComment]
     */
    internal fun postSimpleComment(comment: GithubSimpleComment): Single<Output> =
            github.postSimpleComment(
                    accessToken = "token $accessToken",
                    repoOwner = repoOwner,
                    repoName = repoName,
                    pullRequestId = pullRequestId,
                    simpleComment = comment
            )
                    .subscribeOn(scheduler)
                    .exponetialBackoff(
                            retries = exponentBackoffRetries,
                            delay = exponentialBackoffStartDelayMiliseconds,
                            timeUnit = TimeUnit.MILLISECONDS
                    )
                    .map { Output.Successful("Successfully posted simple comment to ${commentUrl()}") as Output }
                    .onErrorReturn { Output.Error("An error has occurred while trying to post a simple comment to ${commentUrl()}") }


    /**
     * Post a [GithubCodeLineComment]
     */
    internal fun postCodeLineComment(comment: GithubCodeLineComment): Single<Output> =
            github.postCommentOnGivenFile(
                    accessToken = "token $accessToken",
                    repoOwner = repoOwner,
                    repoName = repoName,
                    pullRequestId = pullRequestId,
                    lineComment = comment
            )
                    .subscribeOn(scheduler)
                    .exponetialBackoff(
                            retries = exponentBackoffRetries,
                            delay = exponentialBackoffStartDelayMiliseconds,
                            timeUnit = TimeUnit.MILLISECONDS
                    )
                    .map { Output.Successful("Successfully posted simple comment to ${commentUrl()}") as Output }
                    .onErrorReturn { Output.Error("An error has occurred while trying to post a simple comment to ${commentUrl()}") }

    /**
     * Get the diff of the current pull request
     */
    internal fun getDiffOfTheCurrentPullRequest(): Observable<List<Diff>> =
            github.getPullRequestDiff(
                    accessToken = "token $accessToken",
                    repoOwner = repoOwner,
                    repoName = repoName,
                    pullRequestId = pullRequestId
            )
                    .subscribeOn(Schedulers.io())


    /**
     * Get the meta data for the pull request
     */
    internal fun getPullRequestInfo(): Single<GithubPullRequest> =
            github.getPullRequestDetails(
                    accessToken = "token $accessToken",
                    repoOwner = repoOwner,
                    repoName = repoName,
                    pullRequestId = pullRequestId
            )

    /**
     * Do the exponential backof
     */
    private fun <T> Single<T>.exponetialBackoff(retries: Long, delay: Long, timeUnit: TimeUnit) = retryWhen { errors ->
        errors.doOnNext { it.printStackTrace() }
                .scan(1) { counter, _ ->
                    counter + 1
                }.switchMap { retryCount ->
            Flowable.timer(delay.times(retryCount), timeUnit)
        }.take(retries)
        // TODO verify this is working as intended
    }

}
