package com.hannesdorfmann.githubcomment.http

/**
 * Represents a pull request.
 * Usually we are only interessted in the head
 */
data class PullRequest (
        val head : GitHead
)