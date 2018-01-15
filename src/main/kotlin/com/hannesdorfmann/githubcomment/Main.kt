package com.hannesdorfmann.githubcomment

import com.github.stkent.githubdiffparser.models.Diff
import com.github.stkent.githubdiffparser.models.Hunk
import com.hannesdorfmann.githubcomment.http.GithubApi
import com.hannesdorfmann.githubcomment.input.*
import com.tickaroo.tikxml.TikXml
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okio.Okio
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Options
import java.io.File
import java.io.OutputStream
import java.io.PrintWriter

internal val OPTION_FILE_TO_POST = "file"
internal val OPTION_REPO_OWNER = "owner"
internal val OPTION_REPO_NAME = "repository"
internal val OPTION_PULL_REQUEST_ID = "id"
internal val OPTION_GIT_SHA_HEAD = "sha"

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

    val outputs = run(args, githubUrl)

    outputs.forEach {
        printOutput(
                output = it,
                outputStream = outputStream,
                errorStream = errorStream
        )
    }
}


/**
 * Actually runs (executes) the programm. [start] is only visible for testing purpose
 */
private fun run(args: Array<String>, githubUrl: String): List<Output> {

    val options = Options()
    options.addOption(OPTION_FILE_TO_POST, true, "Path to the file containing the text for the comment that should be posted to the given github issue")
    options.addOption(OPTION_REPO_OWNER, true, "The name of the github repository owner")
    options.addOption(OPTION_REPO_NAME, true, "The name of the github repository")
    options.addOption(OPTION_PULL_REQUEST_ID, true, "The id (number) of the github issue / pull request")
    options.addOption(OPTION_GIT_SHA_HEAD, true, "The sha of the last git commit (HEAD). This is optional. If you don't specify the sha, no check with Github API will be done")

    val parser = DefaultParser()
    val cmd = parser.parse(options, args)
    val filePath = cmd.getOptionValue(OPTION_FILE_TO_POST) ?: return listOf(Output.Error("The path to the file which content should be posted is not set. Use $OPTION_FILE_TO_POST option"))

    val githubRepoOwner = cmd.getOptionValue(OPTION_REPO_OWNER) ?: return listOf(Output.Error("Github Owner must be set. Use $OPTION_REPO_OWNER option"))
    val githubRepoName = cmd.getOptionValue(OPTION_REPO_NAME) ?: return listOf(Output.Error("Github repository name must be set. Use $OPTION_REPO_NAME option"))
    val githubPullRequestIdString = cmd.getOptionValue(OPTION_PULL_REQUEST_ID) ?: return listOf(Output.Error("The github id of the pull request / issue must be set. Use $OPTION_PULL_REQUEST_ID option"))
    val sha = cmd.getOptionValue(OPTION_GIT_SHA_HEAD) ?: return listOf(Output.Error("SHA of head of this branch is not specified. Use $OPTION_GIT_SHA_HEAD option"))


    val githubPullRequestId = try {
        githubPullRequestIdString.toLong()
    } catch (e: NumberFormatException) {
        return listOf(Output.Error("Pull-request id is not a valid number"))

    }

    val file = File(filePath)
    if (!file.exists())
        return listOf(Output.Error("The passed file $filePath does not exist"))

    //
    // Read the xml file with all  comments that should be posted back to github
    //
    val comments: List<Comment> = try {
        readXmlFile(file)
    } catch (t: Throwable) {
        t.printStackTrace()
        return listOf(Output.Error("An error while reading ${file.absolutePath} has occurred. Scroll up for more information"))
    }


    //
    // Ready to start
    //
    val githubApi = GithubApi(
            repoName = githubRepoName,
            repoOwner = githubRepoOwner,
            pullRequestId = githubPullRequestId,
            githubBaseUrl = githubUrl
    )


    return githubApi.getPullRequestInfo()
            .flatMap { pullrequest ->
                if (pullrequest.head.sha != sha) {
                    Single.just(listOf(Output.Successful("Skipping posting comments because the SHA of the head of this " +
                            "branch differs from the sha of the pull request. Usually this means that the pull request " +
                            "has been updated before this job (posting comments) has been started. Current SHA of t" +
                            "his branch is $sha but remote pull requests SHA is ${pullrequest.head.sha}"))
                    )
                } else {

                    val diffRequest: Observable<List<Diff>> = githubApi.getDiffOfTheCurrentPullRequest().share()

                    val httpRequests: List<Single<Output>> = comments.map { comment: Comment ->
                        when (comment) {
                            is SimpleComment -> githubApi.postSimpleComment(comment.toGithubComment())
                            is CodeLineComment -> diffRequest.firstOrError().flatMap { diffs ->
                                val diffForFile = diffs.firstOrNull() { filePath == it.toFileName }
                                if (diffForFile != null) {

                                    val rawDiffLineNumber = diffForFile.getDiffLineNumberForToFileLocation(filePath, comment.lineNumber)
                                    if (rawDiffLineNumber != null) {
                                        val position = (rawDiffLineNumber
                                                - Diff.NUMBER_OF_LINES_PER_DELIMITER
                                                - diffForFile.headerLines.size
                                                - Hunk.NUMBER_OF_LINES_PER_DELIMITER);
                                        githubApi.postCodeLineComment(comment.toGithubComment(position = position, commitSha = sha))
                                    } else {
                                        // Could not be posted as CodeLine Comment (CodeReview comment) because the given file hasn't be changed AT THE GIVEN LINE by the pull request
                                        githubApi.postSimpleComment(comment.toSimpleGithubComment())
                                    }
                                } else {
                                    // Could not be posted as CodeLine Comment (CodeReview comment) because the given file hasn't be changed by the pull request
                                    githubApi.postSimpleComment(comment.toSimpleGithubComment())
                                }
                            }.onErrorReturn {
                                it.printStackTrace()
                                Output.Error("Could not load the diff for the  pull request from github web api. See stacktrace above.")
                            }
                        }.subscribeOn(Schedulers.io())
                    }

                    Single.concat(httpRequests).toList()
                }

            }.blockingGet()

}


/**
 * Reads the XML file with the comments to post back
 */
private fun readXmlFile(xmlFile: File): List<Comment> {
    val tikxml = TikXml.Builder()
            .build()

    val source = Okio.buffer(Okio.source(xmlFile.inputStream()))

    return tikxml.read(source, Comments::class.java).comments
}

