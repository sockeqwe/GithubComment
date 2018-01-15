package com.hannesdorfmann.githubcomment.input

import com.github.stkent.githubdiffparser.models.Diff
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
internal data class CodeLineComment(
        @Attribute val filePath: String,
        @Attribute val lineNumber: Long,
        @TextContent val commentText: String
) : Comment()


/**
 * A simple comment that shows up in the "conversation section" at the pull request on Github
 */
@Xml(name = "comment")
internal data class SimpleComment(
        @TextContent val commentText: String
) : Comment()


internal fun SimpleComment.toGithubComment() = GithubSimpleComment(
        text = commentText
)


internal fun CodeLineComment.toGithubComment(diffs: List<Diff>): GithubCodeLineComment {

    TODO()
}