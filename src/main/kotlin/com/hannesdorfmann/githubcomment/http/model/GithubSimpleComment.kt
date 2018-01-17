package com.hannesdorfmann.githubcomment.http.model

import com.squareup.moshi.Json


/**
 * Used to create a simple comment on a pull request.
 */
data class GithubSimpleComment(

        /**
         * The text message (can contain markdown)
         */

        @Json(name = "body") val text : String
)