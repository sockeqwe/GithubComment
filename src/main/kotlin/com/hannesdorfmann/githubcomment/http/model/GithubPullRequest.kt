package com.hannesdorfmann.githubcomment.http.model

/**
 * Represents a pull request.
 * Usually we are only interessted in the head
 */
data class GithubPullRequest(
        val head : GithubGitHead
)