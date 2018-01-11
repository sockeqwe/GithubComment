package com.hannesdorfmann.githubcomment

import okhttp3.mockwebserver.MockWebServer
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

/**
 * The whole integration test suite
 */
object NetworkTesting : Spek({

    given("a commandline") {

        lateinit var webServer: MockWebServer
        lateinit var outputStream: StringOutputStream
        lateinit var errorStream: StringOutputStream

        beforeEachTest {
            webServer = MockWebServer()
            webServer.start()
            outputStream = StringOutputStream()
            errorStream = StringOutputStream()
        }

        afterEachTest {
            webServer.shutdown()
        }

        val fileToPostAsComment = getResourcePath("SimpleFile.txt")


        on("posting a files content and valid github credentials") {


            it("should print successful message on console") {

                webServer respond200 "SuccessResponse.json"

                start(
                        args = arrayOf(
                                "-$OPTION_FILE_TO_POST", fileToPostAsComment.absolutePath,
                                "-$OPTION_REPO_OWNER", "testRepoOwner",
                                "-$OPTION_REPO_NAME", "testRepoName",
                                "-$OPTION_PULL_REQUEST_ID", "123"
                        ),
                        errorStream = errorStream,
                        outputStream = outputStream,
                        githubUrl = webServer.url
                )

                val expectedUrl = "${webServer.url}testRepoOwner/testRepoName/123"

                outputStream shouldEqualLine "Successfully posted comment to pull-request $expectedUrl"
                errorStream.shouldBeEmpty()
            }
        }

        on("posting file, but no network connection") {
            webServer.shutdown()
            it("should print error message on console") {
                start(
                        args = arrayOf(
                                "-$OPTION_FILE_TO_POST", fileToPostAsComment.absolutePath,
                                "-$OPTION_REPO_OWNER", "testRepoOwner",
                                "-$OPTION_REPO_NAME", "testRepoName",
                                "-$OPTION_PULL_REQUEST_ID", "123"
                        ),
                        errorStream = errorStream,
                        outputStream = outputStream,
                        githubUrl = webServer.url
                )


                outputStream.shouldBeEmpty()
                errorStream shouldEqualLine "An error while doing the http network call has been occurred. Scroll up for more information."
            }
        }
    }
})



