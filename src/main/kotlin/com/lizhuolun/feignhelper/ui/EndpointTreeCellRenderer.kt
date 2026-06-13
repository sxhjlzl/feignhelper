package com.lizhuolun.feignhelper.ui

import com.intellij.icons.AllIcons
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.JBColor
import com.intellij.ui.SimpleTextAttributes
import com.lizhuolun.feignhelper.core.HttpMethod
import java.awt.Color
import javax.swing.JTree

/**
 * 端点树自定义渲染器，为分组节点与端点节点提供美观的样式。
 *
 * - 类分组：类名加粗，使用类图标
 * - 端点叶子：HTTP 方法彩色徽章 + URL 加粗 + 方法名灰色
 *
 * @author lizhuolun
 * @date 2026/6/13
 */
class EndpointTreeCellRenderer : ColoredTreeCellRenderer() {

    override fun customizeCellRenderer(
        tree: JTree,
        value: Any?,
        selected: Boolean,
        expanded: Boolean,
        leaf: Boolean,
        row: Int,
        hasFocus: Boolean,
    ) {
        val node = value as? EndpointNode ?: return
        if (node.item == null) {
            renderGroupNode(node)
        } else {
            renderEndpointNode(node)
        }
    }

    private fun renderGroupNode(node: EndpointNode) {
        icon = AllIcons.Nodes.Class
        append(node.title, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
    }

    private fun renderEndpointNode(node: EndpointNode) {
        val item = node.item ?: return
        icon = AllIcons.Nodes.Method

        // HTTP 方法彩色加粗显示
        val methodColor = colorOf(item.httpMethod)
        append("${item.httpMethod.name}", SimpleTextAttributes(methodColor, null, null, SimpleTextAttributes.STYLE_BOLD))
        append("  ", SimpleTextAttributes.REGULAR_ATTRIBUTES)

        // URL 加粗显示
        append(item.url, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
        append("  →  ", SimpleTextAttributes.GRAYED_ATTRIBUTES)

        // 方法名灰色
        append(item.methodName, SimpleTextAttributes.GRAYED_ATTRIBUTES)
    }

    companion object {

        /**
         * 根据 HTTP 方法返回对应的展示颜色。
         *
         * @param method HTTP 方法
         * @return 展示颜色
         */
        fun colorOf(method: HttpMethod): Color = when (method) {
            HttpMethod.GET -> JBColor(Color(0x22C55E), Color(0x4ADE80))
            HttpMethod.POST -> JBColor(Color(0x3B82F6), Color(0x60A5FA))
            HttpMethod.PUT -> JBColor(Color(0xF59E0B), Color(0xFBBF24))
            HttpMethod.DELETE -> JBColor(Color(0xEF4444), Color(0xF87171))
            HttpMethod.PATCH -> JBColor(Color(0xA855F7), Color(0xC084FC))
            HttpMethod.HEAD -> JBColor(Color(0x06B6D4), Color(0x22D3EE))
            HttpMethod.OPTIONS -> JBColor(Color(0x14B8A6), Color(0x2DD4BF))
            HttpMethod.ANY -> JBColor(Color(0x6B7280), Color(0x9CA3AF))
        }
    }
}
