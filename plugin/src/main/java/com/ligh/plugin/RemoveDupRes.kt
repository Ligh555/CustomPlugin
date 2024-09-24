package com.ligh.plugin

import com.ligh.plugin.utils.setString
import pink.madis.apk.arsc.ResourceFile
import pink.madis.apk.arsc.ResourceTableChunk
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException

object RemoveDupRes {

    fun optimize(resourcePath: String) {
        // 1. 遍历 res 目录下的图片，根据 md5 寻找重复图片，并记录在 map 中，map 的 key 为 md5，value 为图片数据
        val duplicatedResources: HashMap<String, ArrayList<DuplicatedEntry>> = findDuplicatedResources(resourcePath)
        val arscFile = File("$resourcePath/resources.arsc")

        if (arscFile.exists()) {
            var arscStream: FileInputStream? = null
            var resourceFile: ResourceFile?

            try {
                arscStream = FileInputStream(arscFile)

                // ResourceFile 是 android-chunk-utils 里面定义的数据结构
                resourceFile = ResourceFile.fromInputStream(arscStream)

                // 1. 调用 ResourceFile 的 getChunks 方法，将 arsc 流转换成 Chunk 对象树
                val chunks = resourceFile.chunks

                val toBeReplacedResourceMap = HashMap<String, String>(1024)

                // 2. 遍历 duplicatedResources 中记录的重复图片
                for ((_, duplicatedEntryList) in duplicatedResources) {
                    // 保留第一个资源，从索引1开始，其他资源删除掉
                    for (index in 1 until duplicatedEntryList.size) {
                        // 删除图片，并将删除的图片信息保存在 toBeReplacedResourceMap 中
                        removeZipEntry(apFile, duplicatedEntryList[index].name)
                        toBeReplacedResourceMap[duplicatedEntryList[index].name] = duplicatedEntryList[0].name
                    }
                }

                // 3. 更新 resources.arsc 中的数据
                for (chunk in chunks) {
                    if (chunk is ResourceTableChunk) {
                        val resourceTableChunk = chunk
                        // 3. 找到字符串常量池，调用 getStringPool 方法，StringPoolChunk 是 android-chunk-utils 中定义的
                        val stringPoolChunk = resourceTableChunk.stringPool
                        for (i in 0 until stringPoolChunk.stringCount) {
                            // 遍历字符串常量池的值，如果与 toBeReplacedResourceMap 中包含的值相等，则进行替换
                            val key = stringPoolChunk.getString(i)
                            if (toBeReplacedResourceMap.containsKey(key)) {
                                toBeReplacedResourceMap[key]?.let {
                                    stringPoolChunk.setString(i,
                                        it
                                    )
                                }
                            }
                        }
                    }
                }

            } catch (e: IOException) {
                e.printStackTrace() // 或者其他错误处理
            } catch (e: FileNotFoundException) {
                e.printStackTrace() // 或者其他错误处理
            } finally {
                // 确保流关闭
                arscStream?.close()
            }
        }
    }

    fun findDuplicatedResources(resourcePath: String): HashMap<String, ArrayList<DuplicatedEntry>> {
        // 返回重复图片的逻辑实现
        return HashMap()
    }

    fun removeZipEntry(apFile: File, entryName: String) {
        // 删除 zip 中的 entry 实现
    }

}