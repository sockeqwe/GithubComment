package com.hannesdorfmann.githubcomment.http

import com.github.stkent.githubdiffparser.GitHubDiffParser
import com.github.stkent.githubdiffparser.models.Diff
import com.hannesdorfmann.githubcomment.Output
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * A simple facade to make http calls to Github web API
 */
class GithubApi(
        private val githubBaseUrl: String,
        private val repoOwner: String,
        private val repoName: String,
        private val pullRequestId: Long
) {

    private val github: Github

    init {

        val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

        github = Retrofit.Builder()
                .baseUrl(githubBaseUrl)
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
                    repoOwner = repoOwner,
                    repoName = repoName,
                    pullRequestId = pullRequestId,
                    simpleComment = comment
            ).exponetialBackoff(
                    retries = 3,
                    delay = 500,
                    timeUnit = TimeUnit.MILLISECONDS
            )
                    .map { Output.Successful("Successfully posted simple comment to ${commentUrl()}") as Output }
                    .onErrorReturn { Output.Error("An error has occurred while trying to post a simple comment to ${commentUrl()}") }


    /**
     * Post a [GithubCodeLineComment]
     */
    internal fun postCodeLineComment(comment: GithubCodeLineComment): Single<Output> =
            github.postCommentOnGivenFile(
                    repoOwner = repoOwner,
                    repoName = repoName,
                    pullRequestId = pullRequestId,
                    lineComment = comment
            ).exponetialBackoff(
                    retries = 3,
                    delay = 500,
                    timeUnit = TimeUnit.MILLISECONDS
            )
                    .map { Output.Successful("Successfully posted simple comment to ${commentUrl()}") as Output }
                    .onErrorReturn { Output.Error("An error has occurred while trying to post a simple comment to ${commentUrl()}") }

    /**
     * Get the diff of the current pull request
     */
    internal fun getDiffOfTheCurrentPullRequest(): Observable<List<Diff>> =
            github.getPullRequestDiff(
                    repoOwner = repoOwner,
                    repoName = repoName,
                    pullRequestId = pullRequestId
            ).map { diffAsString ->
                val parser = GitHubDiffParser()
                parser.parse(diffAsString.byteInputStream())
            }


    /**
     * Get the meta data for the pull request
     */
    internal fun getPullRequestInfo(): Single<PullRequest> =
            github.getPullRequestDetails(
                    repoOwner = repoOwner,
                    repoName = repoName,
                    pullRequestId = pullRequestId
            )

    /**
     * Do the exponential backof
     */
    private fun <T> Single<T>.exponetialBackoff(retries: Int, delay: Long, timeUnit: TimeUnit) = retryWhen { errors ->
        errors.scan(1) { counter, exception ->
            counter + 1
        }.switchMap { retryCount ->
            Flowable.timer(delay.times(retryCount), timeUnit)
        }
        // TODO verify this is working as intended
    }

}
