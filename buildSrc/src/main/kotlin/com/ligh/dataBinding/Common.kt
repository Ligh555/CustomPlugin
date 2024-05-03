package com.ligh.dataBinding

import android.databinding.tool.store.ResourceBundle.BindingTargetBundle
import com.squareup.javapoet.ClassName

internal val ANDROID_VIEW: ClassName = ClassName.get("android.view", "View")
internal val ANDROID_LAYOUT_INFLATER: ClassName = ClassName.get("android.view", "LayoutInflater")
internal val ANDROID_VIEW_GROUP: ClassName = ClassName.get("android.view", "ViewGroup")

internal val BindingTargetBundle.fieldType: String get() = interfaceType ?: fullClassName

internal fun renderConfigurationJavadoc(present: List<String>, absent: List<String>): String {
    return """
        |This binding is not available in all configurations.
        |<p>
        |Present:
        |<ul>
        |${present.joinToString("\n|") { "  <li>$it/</li>" }}
        |</ul>
        |
        |Absent:
        |<ul>
        |${absent.joinToString("\n|") { "  <li>$it/</li>" }}
        |</ul>
        |""".trimMargin() // Trailing newline for JavaPoet.
}
