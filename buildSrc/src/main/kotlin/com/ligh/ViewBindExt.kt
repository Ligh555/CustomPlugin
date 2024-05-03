package com.ligh

import android.databinding.tool.ext.XmlResourceReference
import android.databinding.tool.ext.parseLayoutClassName
import android.databinding.tool.ext.parseXmlResourceReference
import android.databinding.tool.processing.ViewBindingErrorMessages
import android.databinding.tool.store.ResourceBundle
import android.databinding.tool.writer.BaseLayoutModel
import android.databinding.tool.writer.ResourceReference
import android.databinding.tool.writer.ViewBinder
import android.databinding.tool.writer.ViewBinding
import com.ligh.dataBinding.ANDROID_VIEW
import com.squareup.javapoet.ClassName
import com.ligh.dataBinding.fieldType

fun BaseLayoutModel.toViewBinder(rPackage:String): ViewBinder {
    val rClassName = ClassName.get(rPackage, "R")

    fun ResourceBundle.BindingTargetBundle.toBinding(): ViewBinding {
        val idReference = id.parseXmlResourceReference().toResourceReference(rClassName, getRPackage)
        val (present, absent) = layoutConfigurationMembership(this)

        return ViewBinding(
            name = fieldName(this),
            type = parseLayoutClassName(fieldType, baseFileName),
            form = if (isBinder) ViewBinding.Form.Binder else ViewBinding.Form.View,
            id = idReference,
            presentConfigurations = present,
            absentConfigurations = absent
        )
    }
    validateExplicitViewBindingTypes()
    val bindings = sortedTargets
        .filter { it.id != null }
        .filter { it.viewName != "merge" } // <merge> can have ID but it's ignored at runtime.
        .map { it.toBinding() }
    val rootNode = parseRootNode(rClassName, bindings)
    return ViewBinder(
        generatedTypeName = ClassName.get(bindingClassPackage, bindingClassName),
        layoutReference = ResourceReference(rClassName, "layout", baseFileName),
        bindings = bindings,
        rootNode = rootNode
    )
}

private fun BaseLayoutModel.parseRootNode(
    rClassName: ClassName,
    bindings: List<ViewBinding>
): ViewBinder.RootNode {
    if (variations.any { it.isMerge }) {
        // If anyone is a <merge>, everyone must be a <merge>.
        check(variations.all { it.isMerge }) {
            val (present, absent) = variations.partition { it.isMerge }
            """|Configurations for $baseFileName.xml must agree on the use of a root <merge> tag.
               |
               |Present:
               |${present.joinToString("\n|") { " - ${it.directory}" }}
               |
               |Absent:
               |${absent.joinToString("\n|") { " - ${it.directory}" }}
               """.trimMargin()
        }
        return ViewBinder.RootNode.Merge
    }

    if (variations.any { it.rootNodeViewId != null }) {
        // If anyone has a root ID, everyone must agree on it.
        val uniqueIds = variations.mapTo(HashSet()) { it.rootNodeViewId }
        check(uniqueIds.size == 1) {
            buildString {
                append("Configurations for $baseFileName.xml must agree on the root element's ID.")
                uniqueIds.sortedWith(nullsFirst(naturalOrder())).forEach { id ->
                    append("\n\n${id ?: "Missing ID"}:\n")
                    val matching = variations.filter { it.rootNodeViewId == id }
                    append(matching.joinToString("\n") { " - ${it.directory}" })
                }
            }
        }
        // All variation's root nodes agree on the ID.
        val idName = uniqueIds.single()!!
        val id = idName.parseXmlResourceReference().toResourceReference(rClassName, getRPackage)

        // Check to make sure that the ID matches a binding. Ignored tags like <merge> or <fragment>
        // might have an ID but not have an actual binding. Only use ID if a match was found.
        val rootBinding = bindings.singleOrNull { it.id == id }
        if (rootBinding != null) {
            return ViewBinder.RootNode.Binding(rootBinding)
        }
    }

    val rootViewType = variations
        // Create a set of root node view types for all variations.
        .mapTo(LinkedHashSet()) { parseLayoutClassName(it.rootNodeViewType, baseFileName) }
        // If all of the variations agree on the type, use it.
        .singleOrNull()
    // Otherwise fall back to View.
        ?: ANDROID_VIEW
    return ViewBinder.RootNode.View(rootViewType)
}

private fun XmlResourceReference.toResourceReference(
    moduleRClass: ClassName,
    getRPackage: ((String, String) -> String)?
): ResourceReference {
    var rClassName = when (namespace) {
        "android" -> ANDROID_R
        null -> moduleRClass
        else -> throw IllegalArgumentException("Unknown namespace: $this")
    }
    if (getRPackage != null && rClassName != ANDROID_R) {
        rClassName = ClassName.get(getRPackage(type, name), "R")
    }

    return ResourceReference(rClassName, type, name)
}

private fun BaseLayoutModel.validateExplicitViewBindingTypes() {
    variations.forEach { layoutFileBundle ->
        layoutFileBundle.bindingTargetBundles.filter {
            it.viewBindingType != null
        }.forEach { bindingTarget ->
            // cannot set view binding type for included layouts
            check(bindingTarget.includedLayout == null) {
                ViewBindingErrorMessages.viewBindingTypeInIncludeTag(
                    layoutFileName = layoutFileBundle.fileName,
                    includeTagId = bindingTarget.id
                )
            }
            // if there is an explicit type and it does not match the view we've picked, throw an
            // error.
            check(
                bindingTarget.qualifiedViewBindingType == bindingTarget.fieldType

            ) {
                ViewBindingErrorMessages.inconsistentViewBindingType(
                    layoutFileName = layoutFileBundle.fileName,
                    bindingTargetId = bindingTarget.id,
                    bindingTypes = variations.mapNotNull {
                        val target = it.bindingTargetBundles.firstOrNull { otherTarget ->
                            otherTarget.id != null && otherTarget.isUsed
                        }
                        target?.viewBindingType ?: target?.viewName
                    }
                )
            }
        }
    }
}

val ANDROID_R = ClassName.get("android", "R")
