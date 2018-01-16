package com.hannesdorfmann.githubcomment.input

import com.hannesdorfmann.githubcomment.http.GithubCodeLineComment
import com.hannesdorfmann.githubcomment.http.GithubSimpleComment
import com.tickaroo.tikxml.annotation.Attribute
import com.tickaroo.tikxml.annotation.TextContent
import com.tickaroo.tikxml.annotation.Xml


/**
 * Super Type
 */
sealed class Comment

/**
 * A Comment on a given line in a given file.
 */
@Xml(name = "codelinecomment")
data class CodeLineComment(
        @Attribute val filePath: String,
        @Attribute val lineNumber: Int,
        @TextContent val commentText: String
) : Comment()


/**
 * A simple comment that shows up in the "conversation section" at the pull request on Github
 */
@Xml(name = "comment")
data class SimpleComment(
        @TextContent val commentText: String
) : Comment()


internal fun SimpleComment.toGithubComment() = GithubSimpleComment(
        text = commentText
)

internal fun CodeLineComment.toSimpleGithubComment() = GithubSimpleComment(
        text = "The following comment can't be posted directly at $filePath at line $lineNumber because this pull request hasn't changed this file. Message is:\n\n$commentText"
)


internal fun CodeLineComment.toGithubComment(commitSha: String, position: Int): GithubCodeLineComment {


    return GithubCodeLineComment(
            text = commentText,
            position = position,
            path = filePath,
            commitSha = commitSha
    )
}