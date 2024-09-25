package com.ligh.plugin


object FileUtil {
    const val PATH_NAME = "SystemTools"

    private lateinit var rootDir: String

    fun setRootDir(rootDir: String) {
        FileUtil.rootDir = rootDir
    }

    fun getRootDirPath(): String {
        return rootDir
    }

    fun getToolsDirPath(): String {
        return "$rootDir/$PATH_NAME/"
    }
}