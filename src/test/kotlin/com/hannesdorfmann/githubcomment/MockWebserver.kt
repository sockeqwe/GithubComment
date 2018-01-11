package com.hannesdorfmann.githubcomment

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.amshove.kluent.shouldEqual
import org.junit.Assert
import java.io.File


internal val MockWebServer.url: String
    get() = this.url("").toString()


/**
 * Reads the given file and enques a HTTP 200 with the file content as body
 */
internal infix fun MockWebServer.respond200(filePath: String) = enqueue(MockResponse().setBody(readFile(getResourcePath(filePath))))

internal infix fun MockWebServer.shouldReceivedRequestAt(url: String): HttpMethodAssertion {
    val request = takeRequest()
    val requestUrl = request.requestUrl.toString()

    requestUrl shouldEqual url


    return HttpMethodAssertion(request)
}

internal enum class Http(internal val methodAsString: String) {
    GET("GET"),
    POST("POST"),
    DELETE("DELETE"),
    PUT("PUT"),
    HEAD("HEAD"),
    CONNECT("CONNECT"),
    OPTIONS("OPTIONS"),
    TRACE("TRACE"),
    PATCH("PATCH");

    override fun toString(): String = methodAsString

    /*
    private object HELPER {
        fun fromString(str: String) = when (str) {
            GET.methodAsString -> GET
            POST.methodAsString -> POST
            DELETE.methodAsString -> DELETE
            PUT.methodAsString -> PUT
            HEAD.methodAsString -> HEAD
            CONNECT.methodAsString -> CONNECT
            OPTIONS.methodAsString -> OPTIONS
            TRACE.methodAsString -> TRACE
            PATCH.methodAsString -> PATCH
            else -> throw IllegalArgumentException("$str is not a valid HTTP Method name")
        }
    }
    */
}


internal data class HttpMethodAssertion(private val recordedRequest: RecordedRequest) {
    infix fun ofMethod(httpMethod: Http): HttpBodyAssertion {
        Assert.assertEquals(httpMethod.methodAsString, recordedRequest.method)
        return HttpBodyAssertion(recordedRequest)
    }
}

internal data class HttpBodyAssertion(
        private val recordedRequest: RecordedRequest
) {
    infix fun withBody(file: File) {
        val body = recordedRequest.body.toString()
        val expectedBody = readFile(file)
        Assert.assertEquals(expectedBody, body)
    }

    infix fun withBody(exoectedBody : String){
        val body = recordedRequest.body.toString()
        Assert.assertEquals(exoectedBody, body)
    }
}

