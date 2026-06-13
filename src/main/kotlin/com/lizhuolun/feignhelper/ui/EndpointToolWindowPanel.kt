package com.lizhuolun.feignhelper.ui

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable
import com.intellij.ui.components.JBTabbedPane
import com.lizhuolun.feignhelper.FeignHelperBundle
import com.lizhuolun.feignhelper.cache.BilateralMappingCacheService
import com.lizhuolun.feignhelper.core.EndpointKind
import com.lizhuolun.feignhelper.core.HttpMappingInfo
import com.lizhuolun.feignhelper.scanner.EndpointScanner
import com.lizhuolun.feignhelper.settings.FeignHelperSettings
import java.awt.BorderLayout
import javax.swing.JPanel

/**
 * FeignHelper 工具窗口主面板，包含 Controller 与 Feign 两个 Tab。
 *
 * @param project 当前工程
 * @author lizhuolun
 * @date 2026/6/12
 */
class EndpointToolWindowPanel(private val project: Project) : JPanel(BorderLayout()) {

    private val controllerTree = EndpointTree(project, EndpointKind.CONTROLLER)
    private val feignTree = EndpointTree(project, EndpointKind.FEIGN)

    init {
        val tabs = JBTabbedPane().apply {
            addTab(FeignHelperBundle.message("toolwindow.tab.controller"), controllerTree)
            addTab(FeignHelperBundle.message("toolwindow.tab.feign"), feignTree)
        }
        add(tabs, BorderLayout.CENTER)
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
}
