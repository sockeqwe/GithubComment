package com.hannesdorfmann.githubcomment.input

import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.Xml

/**
 * List of comments
 */
@Xml
data class Comments(
        @Element val comments: List<Comment>
)