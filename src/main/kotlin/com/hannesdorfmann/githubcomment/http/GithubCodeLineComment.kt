package com.hannesdorfmann.githubcomment.http

import com.squareup.moshi.Json

/**
 * Used to create a  comment on a certain file on the given position (think "at a certain line").
 * This is basically what during a code review a person would do.
 */
data class GithubCodeLineComment(
        /**
         * The text message (can contain markdown)
         */
        @Json(name = "body") val text: String,

        /**
         * The SHA of the commit needing a comment. Not using the latest commit SHA may render your comment outdated
         */
        @Json(name = "commit_id") val commitSha: String,

        /**
         * The path to the file on which we would like to add a comment
         */
        val path: String,

        /**
         * The position (not line number) within the [path] in the context of this pull request.
         * Quite complicated. Take a look at github docs: https://developer.github.com/v3/pulls/comments/#create-a-comment
         */
        val position: Int
)