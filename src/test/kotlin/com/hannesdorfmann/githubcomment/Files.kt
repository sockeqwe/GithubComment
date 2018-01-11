package com.hannesdorfmann.githubcomment

import java.io.File
import java.nio.file.Files

internal fun Any.getResourcePath(resourceRelativePath: String) = File(IntegrationTest::class.java.getClassLoader().getResource(resourceRelativePath).toURI())


internal fun readFile(file : File) = String(Files.readAllBytes(file.toPath()))