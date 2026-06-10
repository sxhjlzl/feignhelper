package com.lizhuolun.feignhelper.provider.linemarker

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor
import com.intellij.codeInsight.navigation.PsiTargetNavigator
import com.intellij.icons.AllIcons
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.lizhuolun.feignhelper.FeignHelperBundle
import com.lizhuolun.feignhelper.core.HttpMappingInfo
import com.lizhuolun.feignhelper.provider.UastElementUtils
import java.awt.datatransfer.StringSelection
import java.awt.event.MouseEvent
import javax.swing.Icon

/**
 * 行内导航图标公共基类，统一处理：
 * - 左键点击 -> 跳转到对端方法（多目标时显示选择 popup）
 * - 右键点击 -> 弹出"复制 URL"菜单，复制完整 URL 到剪贴板
 *
 * 注意：右键事件不会经过 navHandler，必须通过 GutterIconRenderer.getPopupMenuActions()
 * 暴露给 IDEA 的 gutter 上下文菜单系统。
 */
abstract class EndpointNavigationLineMarker : LineMarkerProviderDescriptor() {

    protected abstract val markerIcon: Icon
    protected abstract val titleKey: String
    protected abstract val accessibleKey: String

    /**
     * 当前 LineMarker 是否应该对该方法生效。
     */
    protected abstract fun isApplicable(method: PsiMethod): Boolean

    /**
     * 查找跳转目标。
     */
    protected abstract fun findTargets(project: Project, method: PsiMethod): List<HttpMappingInfo>

    /**
     * 计算当前方法自身的 URL（用于右键复制）。
     */
    protected abstract fun resolveSelfUrl(project: Project, method: PsiMethod): String?

    final override fun getName(): String = FeignHelperBundle.message(accessibleKey)

    final override fun getIcon(): Icon = markerIcon

    final override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        val project = element.project
        if (project.isDisposed) return null
        if (DumbService.isDumb(project)) return null

        val method = UastElementUtils.extractMethodForNameAnchor(element) ?: return null
        if (!isApplicable(method)) return null

        val targets = findTargets(project, method)
        val selfUrl = resolveSelfUrl(project, method)
        // 即使没有跳转目标，只要能解析出 URL 也展示图标，便于用户右键复制
        if (targets.isEmpty() && selfUrl.isNullOrEmpty()) return null

        val tooltipTitle = FeignHelperBundle.message(titleKey)
        val tooltipHint = FeignHelperBundle.message("linemarker.tooltip.hint")
        val tooltip = if (selfUrl.isNullOrEmpty()) {
            "$tooltipTitle<br/>$tooltipHint"
        } else {
            "$tooltipTitle<br/><code>$selfUrl</code><br/>$tooltipHint"
        }

        return EndpointLineMarkerInfo(
            element = element,
            icon = markerIcon,
            tooltipText = tooltip,
            accessibleName = FeignHelperBundle.message(accessibleKey),
            project = project,
            titleKey = titleKey,
            targets = targets,
            selfUrl = selfUrl,
        )
    }

    /**
     * 自定义 LineMarkerInfo：
     * - navHandler 处理左键跳转
     * - createGutterRenderer 返回的 renderer 暴露 popup actions，处理右键复制 URL
     */
    private class EndpointLineMarkerInfo(
        element: PsiElement,
        icon: Icon,
        tooltipText: String,
        private val accessibleName: String,
        private val project: Project,
        private val titleKey: String,
        private val targets: List<HttpMappingInfo>,
        private val selfUrl: String?,
    ) : LineMarkerInfo<PsiElement>(
        element,
        element.textRange,
        icon,
        { tooltipText },
        { event, _ -> navigateToTargets(event, project, targets, titleKey) },
        GutterIconRenderer.Alignment.RIGHT,
        { accessibleName },
    ) {

        override fun createGutterRenderer(): GutterIconRenderer {
            return object : LineMarkerGutterIconRenderer<PsiElement>(this) {
                override fun getPopupMenuActions(): ActionGroup? {
                    val url = selfUrl
                    if (url.isNullOrBlank()) return null
                    val group = DefaultActionGroup()
                    val label = FeignHelperBundle.message("popup.copy.url.action", url)
                    group.add(object : AnAction(label, null, AllIcons.Actions.Copy) {
                        override fun actionPerformed(e: AnActionEvent) {
                            copyUrlAndNotify(project, url)
                        }
                    })
                    return group
                }

                override fun equals(other: Any?): Boolean = super.equals(other)
                override fun hashCode(): Int = super.hashCode()
            }
        }
    }

    companion object {

        /**
         * 把 URL 写入系统剪贴板并发送一条气泡通知。
         */
        private fun copyUrlAndNotify(project: Project, url: String) {
            CopyPasteManager.getInstance().setContents(StringSelection(url))
            NotificationGroupManager.getInstance()
                .getNotificationGroup("FeignHelper")
                .createNotification(
                    FeignHelperBundle.message("notification.url.copied", url),
                    NotificationType.INFORMATION,
                )
                .notify(project)
        }

        /**
         * 跳转到对端目标方法。
         * - 单目标：直接 navigate
         * - 多目标：用 PsiTargetNavigator 展示选择 popup
         */
        private fun navigateToTargets(
            event: MouseEvent,
            project: Project,
            targets: List<HttpMappingInfo>,
            titleKey: String,
        ) {
            if (targets.isEmpty()) return
            val validTargets = targets
                .asSequence()
                .map { it.method }
                .filter { it.isValid }
                .filterIsInstance<NavigatablePsiElement>()
                .toList()
            if (validTargets.isEmpty()) return
            if (validTargets.size == 1) {
                validTargets[0].navigate(true)
                return
            }
            PsiTargetNavigator { validTargets }
                .navigate(event, FeignHelperBundle.message(titleKey), project)
        }
    }
}
