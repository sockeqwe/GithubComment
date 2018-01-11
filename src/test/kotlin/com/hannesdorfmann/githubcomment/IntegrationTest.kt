package com.hannesdorfmann.githubcomment

import okhttp3.mockwebserver.MockWebServer
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldEqual
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

/**
 * The whole integration test suite
 */
object IntegrationTest : Spek({

    given("A commandline") {

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

        on("on posting a files content and valid github credentials") {
            val filePath = getResourcePath("SimpleFile.txt").absolutePath

            webServer respond200 "SuccessResponse.json"

            it("should print successful message on console") {


                start(
                        args = arrayOf(
                                OPTION_FILE_TO_POST, filePath,
                                OPTION_REPO_OWNER, "testRepoOwner",
                                OPTION_REPO_NAME, "testRepoName",
                                OPTION_PULL_REQUEST_ID, "123"
                        ),
                        errorStream = errorStream,
                        outputStream = outputStream,
                        githubUrl = webServer.url
                )


                "" shouldEqual outputStream.asString()
                errorStream.asString().shouldBeEmpty()
            }
        }
    }
})



