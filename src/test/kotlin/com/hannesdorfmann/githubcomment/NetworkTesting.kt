package com.hannesdorfmann.githubcomment

import com.hannesdorfmann.githubcomment.utils.*
import io.reactivex.schedulers.Schedulers
import okhttp3.mockwebserver.MockWebServer
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

/**
 * The whole integration test suite
 */
object NetworkTesting : Spek({

    given("a commandline that tries to post comments.xml file") {

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

        val fileToPostAsComment = getResourcePath("comments.xml")
        val accessToken = "MockedAccessToken"


        on("valid programm arguments") {


            it("should print successful message on console") {

                webServer respond200 "PullRequestInfoResponse.json"
                webServer respond201 "SimpleCommentSuccessfulResponse.json"
                webServer respond201 "SimpleCommentSuccessfulResponse.json"
                webServer respond201 "SimpleCommentSuccessfulResponse.json"

                start(
                        args = arrayOf(
                                "-$OPTION_FILE_TO_POST", fileToPostAsComment.absolutePath,
                                "-$OPTION_REPO_OWNER", "testRepoOwner",
                                "-$OPTION_REPO_NAME", "testRepoName",
                                "-$OPTION_PULL_REQUEST_ID", "1",
                                "-$OPTION_GIT_SHA_HEAD", "fakeSHA",
                                "-$OPTION_ACCESS_TOKEN", accessToken
                        ),
                        errorStream = errorStream,
                        outputStream = outputStream,
                        githubUrl = webServer.url,
                        httpScheduler = Schedulers.single()
                )

                val expectedUrl = "${webServer.url}testRepoOwner/testRepoName/123"

                errorStream.shouldBeEmpty()
                outputStream shouldEqualLine "Successfully posted comment to pull-request $expectedUrl"
            }
        }

        on("sha passed as programm argument is different from pull request sha") {

            webServer respond200 "PullRequestInfoResponse.json"
            start(
                    args = arrayOf(
                            "-$OPTION_FILE_TO_POST", fileToPostAsComment.absolutePath,
                            "-$OPTION_REPO_OWNER", "testRepoOwner",
                            "-$OPTION_REPO_NAME", "testRepoName",
                            "-$OPTION_PULL_REQUEST_ID", "1",
                            "-$OPTION_GIT_SHA_HEAD", "someOtherSHA",
                            "-$OPTION_ACCESS_TOKEN", accessToken
                    ),
                    errorStream = errorStream,
                    outputStream = outputStream,
                    githubUrl = webServer.url,
                    httpScheduler = Schedulers.single()
            )

            val expectedUrl = "${webServer.url}testRepoOwner/testRepoName/123"

            errorStream.shouldBeEmpty()
            outputStream shouldEqualLine "Skipping posting comments because the SHA of the head of this branch differs from the sha of the pull request. Usually this means that the pull request has been updated before this job (posting comments) has been started. Current SHA of this branch is someSHA but remote pull requests SHA is 6dcb09b5b57875f334f61aebed695e2e4193db5e"
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
                        githubUrl = webServer.url,
                        httpScheduler = Schedulers.single()
                )


                outputStream.shouldBeEmpty()
                errorStream shouldEqualLine "An error while doing the http network call has been occurred. Scroll up for more information."
            }
        }
    }
})



