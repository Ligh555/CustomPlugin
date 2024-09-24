package com.ligh.plugin

import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

object ZipUtils {
    // 文件解压
    fun unZip(src: File, savePath: String) {
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