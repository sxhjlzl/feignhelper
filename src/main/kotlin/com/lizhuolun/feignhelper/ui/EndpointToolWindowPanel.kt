package com.lizhuolun.feignhelper.ui

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBTabbedPane
import com.lizhuolun.feignhelper.FeignHelperBundle
import com.lizhuolun.feignhelper.cache.BilateralMappingCacheService
import com.lizhuolun.feignhelper.core.EndpointKind
import com.lizhuolun.feignhelper.core.HttpMappingInfo
import com.lizhuolun.feignhelper.scanner.EndpointScanner
import com.lizhuolun.feignhelper.settings.FeignHelperSettings
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.BorderFactory

/**
 * FeignHelper 工具窗口主面板，包含 Controller 与 Feign 两个 Tab。
 *
 * 顶部提供刷新/收起工具栏，底部显示接口数量统计，主体为双 Tab 树形列表。
 *
 * @param project 当前工程
 * @author lizhuolun
 * @date 2026/6/12
 */
class EndpointToolWindowPanel(private val project: Project) : JBPanel<EndpointToolWindowPanel>(BorderLayout()) {

    private val controllerTree = EndpointTree(project, EndpointKind.CONTROLLER)
    private val feignTree = EndpointTree(project, EndpointKind.FEIGN)

    private val tabs = JBTabbedPane()
    private val statusLabel = JBLabel().apply {
        border = BorderFactory.createEmptyBorder(4, 8, 4, 8)
    }

    init {
        tabs.apply {
            addTab(controllerTitle(0), controllerTree)
            addTab(feignTitle(0), feignTree)
            addChangeListener { updateStatusLabel() }
        }
        add(tabs, BorderLayout.CENTER)

        val toolbar = ActionManager.getInstance().createActionToolbar(
            "FeignHelper.ToolWindow",
            DefaultActionGroup(RefreshAction(), CollapseAllAction()),
            true,
        )
        toolbar.targetComponent = this
        add(toolbar.component, BorderLayout.NORTH)

        val statusPanel = JBPanel<JBPanel<*>>(FlowLayout(FlowLayout.LEFT, 0, 0)).apply {
            border = BorderFactory.createMatteBorder(1, 0, 0, 0, com.intellij.ui.JBColor.GRAY.brighter())
            add(statusLabel)
        }
        add(statusPanel, BorderLayout.SOUTH)

        controllerTree.onCountsChanged = { total, filtered ->
            tabs.setTitleAt(0, controllerTitle(total))
            updateStatusLabel()
        }
        feignTree.onCountsChanged = { total, filtered ->
            tabs.setTitleAt(1, feignTitle(total))
            updateStatusLabel()
        }
    }

    /**
     * 从缓存刷新两侧列表；缓存为空时执行一次后台全量扫描。
     */
    fun refresh() {
        if (project.isDisposed) return
        val cache = BilateralMappingCacheService.of(project)
        var controllers = cache.getAllControllerMappings()
        var clients = cache.getAllClientMappings()

        if (controllers.isEmpty() || clients.isEmpty()) {
            val (scannedControllers, scannedClients) = scanInBackground()
            if (controllers.isEmpty()) controllers = scannedControllers
            if (clients.isEmpty()) clients = scannedClients
        }

        ApplicationManager.getApplication().invokeLater {
            if (project.isDisposed) return@invokeLater
            controllerTree.refresh(controllers)
            feignTree.refresh(clients)
        }
    }

    private fun controllerTitle(count: Int): String =
        "${FeignHelperBundle.message("toolwindow.tab.controller")} ($count)"

    private fun feignTitle(count: Int): String =
        "${FeignHelperBundle.message("toolwindow.tab.feign")} ($count)"

    private fun updateStatusLabel() {
        val controllerTotal = controllerTree.getTotalCount()
        val controllerFiltered = controllerTree.getFilteredCount()
        val feignTotal = feignTree.getTotalCount()
        val feignFiltered = feignTree.getFilteredCount()

        val text = when (tabs.selectedIndex) {
            0 -> formatCount(controllerFiltered, controllerTotal)
            1 -> formatCount(feignFiltered, feignTotal)
            else -> ""
        }
        statusLabel.text = text
    }

    private fun formatCount(filtered: Int, total: Int): String {
        return if (filtered == total) {
            FeignHelperBundle.message("toolwindow.status.total", total)
        } else {
            FeignHelperBundle.message("toolwindow.status.filtered", filtered, total)
        }
    }

    private fun scanInBackground(): Pair<List<HttpMappingInfo>, List<HttpMappingInfo>> {
        return try {
            ProgressManager.getInstance().runProcessWithProgressSynchronously<Pair<List<HttpMappingInfo>, List<HttpMappingInfo>>, RuntimeException>(
                {
                    val manualProfile = FeignHelperSettings.getInstance().state.manualActiveProfile
                    ApplicationManager.getApplication().runReadAction(Computable {
                        val controllers = EndpointScanner.scanControllerEndpoints(project, manualProfile)
                        val clients = EndpointScanner.scanClientEndpoints(project)
                        controllers to clients
                    })
                },
                FeignHelperBundle.message("progress.finding.targets"),
                true,
                project,
            )
        } catch (e: Exception) {
            emptyList<HttpMappingInfo>() to emptyList<HttpMappingInfo>()
        }
    }

    /**
     * 工具窗口顶部刷新按钮。
     */
    private inner class RefreshAction : AnAction(
        FeignHelperBundle.message("toolwindow.action.refresh"),
        null,
        AllIcons.Actions.Refresh,
    ) {
        override fun actionPerformed(e: AnActionEvent) {
            refresh()
        }
    }

    /**
     * 工具窗口顶部全部收起按钮。
     */
    private inner class CollapseAllAction : AnAction(
        FeignHelperBundle.message("toolwindow.action.collapse.all"),
        null,
        AllIcons.Actions.Collapseall,
    ) {
        override fun actionPerformed(e: AnActionEvent) {
            controllerTree.collapseAll()
            feignTree.collapseAll()
        }
    }
}
