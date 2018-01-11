package com.hannesdorfmann.githubcomment

import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Options
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.io.OutputStream
import java.io.PrintWriter
import java.net.ConnectException

internal val OPTION_FILE_TO_POST = "file"
internal val OPTION_REPO_OWNER = "owner"
internal val OPTION_REPO_NAME = "repository"
internal val OPTION_PULL_REQUEST_ID = "id"

/**
 * The main method that is used to start the application.
 */
fun main(args: Array<String>) {
    start(args = args,
            outputStream = System.out,
            errorStream = System.err
    )
}


/**
 * Simple datastructure that represents what should be printed on the output
 */
internal sealed class Output {
    /**
     * This class represents the error case
     */
    internal data class Error(val errorMessage: String) : Output()

    /**
     * This class represents the successful case
     */
    internal data class Successful(val msg: String) : Output()
}

/**
 * Prints [Output] on the given streams
 */
private fun printOutput(output: Output, outputStream: OutputStream, errorStream: OutputStream) {
    val printer = PrintWriter(outputStream, true)
    val errorPrinter = PrintWriter(errorStream, true)
    when (output) {
        is Output.Error -> errorPrinter.println(output.errorMessage)
        is Output.Successful -> printer.println(output.msg)
    }
}

/**
 * This method only exists and has internal visibility for testing purpose
 */
internal fun start(args: Array<String>, githubUrl: String = "https://api.github.com", outputStream: OutputStream, errorStream: OutputStream) {

    val output = run(args, githubUrl)
    printOutput(
            output = output,
            outputStream = outputStream,
            errorStream = errorStream
    )
}


/**
 * Actually runs (executes) the programm. [start] is only visible for testing purpose
 */
private fun run(args: Array<String>, githubUrl: String): Output {

    val options = Options()
    options.addOption(OPTION_FILE_TO_POST, true, "Path to the file containing the text for the comment that should be posted to the given github issue")
    options.addOption(OPTION_REPO_OWNER, true, "The name of the github repository owner")
    options.addOption(OPTION_REPO_NAME, true, "The name of the github repository")
    options.addOption(OPTION_PULL_REQUEST_ID, true, "The id (number) of the github issue / pull request")

    val parser = DefaultParser()
    val cmd = parser.parse(options, args)
    val filePath = cmd.getOptionValue(OPTION_FILE_TO_POST) ?: return Output.Error("The path to the file which content should be posted is not set. Use $OPTION_FILE_TO_POST option")

    val githubRepoOwner = cmd.getOptionValue(OPTION_REPO_OWNER) ?: return Output.Error("Github Owner must be set. Use $OPTION_REPO_OWNER option")
    val githubRepoName = cmd.getOptionValue(OPTION_REPO_NAME) ?: return Output.Error("Github repository name must be set. Use $OPTION_REPO_NAME option")
    val githubPullRequestIdString = cmd.getOptionValue(OPTION_PULL_REQUEST_ID) ?: return Output.Error("The github id of the pull request / issue must be set. Use $OPTION_PULL_REQUEST_ID option")

    val githubPullRequestId = try {
        githubPullRequestIdString.toInt()
    } catch (e: NumberFormatException) {
        return Output.Error("Pull-request id is not a valid number")
    }

    val file = File(filePath)
    if (file.exists()) {

        return try {
            val github = createRetrofit(githubUrl)

            postComment(
                    githubUrl = githubUrl,
                    github = github,
                    githubRepoOwner = githubRepoOwner,
                    githubRepoName = githubRepoName,
                    githubPullRequestId = githubPullRequestId
            )
        } catch (e: ConnectException) {
            e.printStackTrace()
            Output.Error("Could not connect to $githubUrl. See stacktrace above")
        } catch (e: Exception) {
            e.printStackTrace()
            Output.Error("An unexpected Error has occurred. See stacktrace above")
        }
    } else
        return Output.Error("The passed file $filePath does not exist")


}

/**
 * Creates the retrofit interface to talk to github over http
 */
private fun createRetrofit(githubUrl: String): Github {
    val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

    return Retrofit.Builder()
            .baseUrl(githubUrl)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(Github::class.java)
}

/**
 * Executes the http request to post a comment to the github pull request
 */
private fun postComment(githubUrl: String, github: Github, githubRepoOwner: String, githubRepoName: String, githubPullRequestId: Int): Output {

    try {

        val response = github.postComment(
                repoOwner = githubRepoOwner,
                repoName = githubRepoName,
                issueNumber = githubPullRequestId)
                .execute()

        return if (response.isSuccessful)
            Output.Successful("Successfully posted comment to pull-request $githubUrl$githubRepoOwner/$githubRepoName/$githubPullRequestId")
        else
            Output.Error("The http request wasn't succsessful. HTTP response code is: ${response.code()}")

    } catch (t: Throwable) {
        t.printStackTrace()
        return Output.Error("An error while doing the http network call has been occurred. Scroll up for more information.")
    }

}