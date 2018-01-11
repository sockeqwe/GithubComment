package com.hannesdorfmann.githubcomment

import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldEqual
import java.io.File
import java.nio.file.Files

internal fun Any.getResourcePath(resourceRelativePath: String) = File(NetworkTesting::class.java.getClassLoader().getResource(resourceRelativePath).toURI())


internal fun readFile(file: File) = String(Files.readAllBytes(file.toPath()))


internal infix fun StringOutputStream.shouldEqualLine(expected: String) {
    asString() shouldEqual (expected + "\n")
}

internal fun StringOutputStream.shouldBeEmpty() {
    toString().shouldBeEmpty()
}