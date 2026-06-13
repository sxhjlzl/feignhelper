package com.lizhuolun.feignhelper.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

/**
 * FeignHelper 工具窗口工厂。
 *
 * @author lizhuolun
 * @date 2026/6/12
 */
class EndpointToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = EndpointToolWindowPanel(project)
        EndpointToolWindowService.of(project).register(panel)

        val content = ContentFactory.getInstance().createContent(panel, "", false)
        content.isCloseable = false
        toolWindow.contentManager.addContent(content)

        // 面板创建后立即加载一次数据
        panel.refresh()
    }
}
