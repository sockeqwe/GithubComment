package com.hannesdorfmann.githubcomment.http.converter

import com.github.stkent.githubdiffparser.GitHubDiffParser
import com.github.stkent.githubdiffparser.models.Diff
import com.squareup.moshi.Types
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

/**
 * Simple body response converter
 */
class DiffResponseBodyConverter : Converter<ResponseBody, List<Diff>?> {

    override fun convert(value: ResponseBody?): List<Diff>? {
        if (value == null)
            return null

        val parser = GitHubDiffParser()
        return parser.parse(value.byteStream())
    }
}


/**
 * A simple converter Factory that is able to parse List<Diff> from response
 */
class DiffConverterFactory : Converter.Factory() {

    private val listDiffType = Types.newParameterizedType(List::class.java, Diff::class.java)

    override fun responseBodyConverter(type: Type?, annotations: Array<out Annotation>?, retrofit: Retrofit?): Converter<ResponseBody, *>? {
        return if (type == listDiffType) {
            DiffResponseBodyConverter()
        } else {
            null
        }
    }

}