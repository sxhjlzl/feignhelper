package com.lizhuolun.feignhelper

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.PropertyKey

private const val BUNDLE = "messages.FeignHelperBundle"

/**
 * 国际化资源访问入口。
 *
 * messages/FeignHelperBundle.properties 为英文默认
 * messages/FeignHelperBundle_zh_CN.properties 为中文
 */
object FeignHelperBundle : DynamicBundle(BUNDLE) {

    @Nls
    fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): String =
        getMessage(key, *params)
}
