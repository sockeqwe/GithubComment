package com.hannesdorfmann.githubcomment.input;

import com.tickaroo.tikxml.annotation.Element;
import com.tickaroo.tikxml.annotation.ElementNameMatcher;
import com.tickaroo.tikxml.annotation.Xml;

import java.util.List;
import java.util.Objects;

/**
 * TODO konvert this to kotlin
 */
@Xml
public final class Comments {
    private final List<Comment> comments;

    public Comments(
            @Element(typesByElement = {
                    @ElementNameMatcher(type = SimpleComment.class),
                    @ElementNameMatcher(type = CodeLineComment.class)
            })
                    List<Comment> comments) {
        this.comments = comments;
    }


    public List<Comment> getComments() {
        return comments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Comments)) return false;
        Comments comments1 = (Comments) o;
        return Objects.equals(comments, comments1.comments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(comments);
    }
}
