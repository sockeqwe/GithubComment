package com.hannesdorfmann.githubcomment

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer


internal val MockWebServer.url: String
    get() = this.url("").toString()


/**
 * Reads the given file and enques a HTTP 200 with the file content as body
 */
internal infix fun MockWebServer.respond200(filePath: String) = enqueue(MockResponse().setBody(readFile(getResourcePath(filePath))))