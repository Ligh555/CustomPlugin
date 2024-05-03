package com.ligh

import android.databinding.tool.LayoutXmlProcessor
import java.io.File

class CustomLoomUp : LayoutXmlProcessor.OriginalFileLookup {
    override fun getOriginalFileFor(file: File): File {
        return file;
    }
}