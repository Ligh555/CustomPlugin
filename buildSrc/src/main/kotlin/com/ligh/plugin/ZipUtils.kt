package com.ligh.plugin

import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object ZipUtils {

    fun unZipIt(zipFilePath: String, outputFolder: String) {
        val buffer = ByteArray(1024)
        val folder = File(outputFolder)
        if (!folder.exists()) {
            folder.mkdir()
        }
        try {
            //get the zip file content
            val zis = ZipInputStream(FileInputStream(zipFilePath))
            var ze = zis.nextEntry
            while (ze != null) {
                val fileName = ze.name
                val newFile = File(outputFolder + File.separator + fileName)
                println("file unzip : " + newFile.absoluteFile)
                //create all non exists folders
                //else you will hit FileNotFoundException for compressed folder
                //大部分网络上的源码，这里没有判断子目录
                if (ze.isDirectory) {
                    newFile.mkdirs()
                } else {
                    File(newFile.parent).mkdirs()
                    val fos = FileOutputStream(newFile)
                    var len: Int
                    while (zis.read(buffer).also { len = it } != -1) {
                        fos.write(buffer, 0, len)
                    }
                    fos.close()
                }
                ze = zis.nextEntry
            }
            zis.closeEntry()
            zis.close()
            println("Done")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // 文件解压
    fun unZip(src: File, savePath: String) {
        runCatching {
            var count: Int
            ZipFile(src).use { zipFile ->
                val entries = zipFile.entries()

                while (entries.hasMoreElements()) {
                    val buf = ByteArray(2048)
                    val entry = entries.nextElement() as ZipEntry
                    var filename = entry.name
                    filename = savePath + filename

                    val fileDir = File(filename.substring(0, filename.lastIndexOf('/')))
                    if (!fileDir.exists()) {
                        fileDir.mkdirs()
                    }

                    if (!filename.endsWith("/")) {
                        val file = File(filename)
                        file.createNewFile()

                        zipFile.getInputStream(entry).use { inputStream ->
                            FileOutputStream(file).use { fos ->
                                BufferedOutputStream(fos, 2048).use { bos ->
                                    while (inputStream.read(buf).also { count = it } > -1) {
                                        bos.write(buf, 0, count)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }.onFailure {
            println(it)
        }

    }

    //文件压缩
    fun zipFolder(srcPath: String, savePath: String) {
        val saveFile = File(savePath)
        if (saveFile.exists()) {
            saveFile.delete()
        }
        saveFile.createNewFile()

        ZipOutputStream(FileOutputStream(saveFile)).use { outStream ->
            val srcFile = File(srcPath)
            zipFile("${srcFile.absolutePath}${File.separator}", "", outStream)
        }
    }


    private fun zipFile(folderPath: String, fileString: String, out: ZipOutputStream) {
        val srcFile = File(folderPath + fileString)
        if (srcFile.isFile) {
            val zipEntry = ZipEntry(fileString)
            FileInputStream(srcFile).use { inputStream ->
                out.putNextEntry(zipEntry)
                val buf = ByteArray(2048)
                var len: Int
                while (inputStream.read(buf).also { len = it } != -1) {
                    out.write(buf, 0, len)
                }
                out.closeEntry()
            }
        } else {
            val fileList = srcFile.list()
            if (fileList.isNullOrEmpty()) {
                val zipEntry = ZipEntry("$fileString${File.separator}")
                out.putNextEntry(zipEntry)
                out.closeEntry()
            } else {
                for (file in fileList) {
                    zipFile(
                        folderPath,
                        if (fileString.isEmpty()) file else "$fileString${File.separator}$file",
                        out
                    )
                }
            }
        }
    }
}