package com.lizhuolun.feignhelper.listener

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiTreeChangeAdapter
import com.intellij.psi.PsiTreeChangeEvent
import com.lizhuolun.feignhelper.cache.BilateralMappingCacheService
import com.lizhuolun.feignhelper.cache.PsiClassCacheService
import com.lizhuolun.feignhelper.config.ApplicationConfigReader
import com.lizhuolun.feignhelper.core.EndpointKind
import com.lizhuolun.feignhelper.core.annotation.AnnotationParser
import com.lizhuolun.feignhelper.scanner.EndpointScanner
import com.lizhuolun.feignhelper.settings.FeignHelperSettings

/**
 * PSI 树变更监听器，维护 PsiClassCacheService 与 BilateralMappingCacheService 的实时性。
 *
 * 设计要点：
 * 1. 完整覆盖 childAdded / childReplaced / childRemoved / childrenChanged / childMoved / propertyChanged 六类事件
 * 2. 继承 PsiTreeChangeAdapter，只 override 实际关心的事件
 * 3. 在 Dumb 模式下直接 return，避免索引未就绪时触发异常
 *
 * @author lizhuolun
 * @date 2026/6/9
 */
class FeignHelperPsiListener : PsiTreeChangeAdapter() {

    override fun childAdded(event: PsiTreeChangeEvent) {
        handleUpsert(event.child)
    }

    override fun childReplaced(event: PsiTreeChangeEvent) {
        handleUpsert(event.newChild)
        handleUpsert(event.child)
    }

    override fun childrenChanged(event: PsiTreeChangeEvent) {
        handleUpsert(event.parent)
    }

    override fun childMoved(event: PsiTreeChangeEvent) {
        handleUpsert(event.child)
    }

    override fun propertyChanged(event: PsiTreeChangeEvent) {
        handleUpsert(event.element)
    }

    /**
     * 处理 childRemoved 事件，删除类或文件时同步从缓存中清掉对应的条目。
     *
     * @param event PSI 删除事件
     */
    override fun childRemoved(event: PsiTreeChangeEvent) {
        val removed = event.child ?: return
        val project = removed.project
        if (project.isDisposed) return
        if (DumbService.isDumb(project)) return
        if (scheduleConfigRefresh(removed)) return

        findPsiClass(event.parent ?: removed)?.let { parentClass ->
            handleUpsert(parentClass)
            return
        }

        val psiClass = removed as? PsiClass ?: findPsiClass(removed) ?: return
        val qualifiedName = psiClass.qualifiedName ?: return
        PsiClassCacheService.of(project).removeByQualifiedName(qualifiedName)
        BilateralMappingCacheService.of(project).removeByClassQualifiedName(qualifiedName)
    }

    /**
     * 通用 upsert 入口，仅当变更触及 Feign / HttpExchange / Controller 类时才刷新缓存。
     *
     * @param element 变更涉及的 PSI 元素
     */
    private fun handleUpsert(element: PsiElement?) {
        if (element == null) return
        val project = element.project
        if (project.isDisposed) return
        if (DumbService.isDumb(project)) return
        if (scheduleConfigRefresh(element)) return

        val psiClass = findPsiClass(element) ?: return
        val qualifiedName = psiClass.qualifiedName ?: return
        val classCache = PsiClassCacheService.of(project)
        val mappingCache = BilateralMappingCacheService.of(project)
        classCache.removeByQualifiedName(qualifiedName)
        mappingCache.removeByClassQualifiedName(qualifiedName)

        if (!AnnotationParser.isClientInterface(psiClass) &&
            !AnnotationParser.isControllerClass(psiClass)
        ) {
            return
        }

        classCache.upsert(psiClass)
        refreshMappings(project, psiClass)
    }

    /**
     * 类变更后重新解析其所有方法级映射，覆盖到缓存。
     *
     * @param project 当前工程
     * @param psiClass 发生变更的类
     */
    private fun refreshMappings(project: Project, psiClass: PsiClass) {
        val cache = BilateralMappingCacheService.of(project)
        val qualifiedName = psiClass.qualifiedName ?: return
        cache.removeByClassQualifiedName(qualifiedName)
        val kind = EndpointScanner.resolveKind(psiClass) ?: return
        val mappings = if (kind == EndpointKind.CONTROLLER) {
            val manualProfile = FeignHelperSettings.getInstance().state.manualActiveProfile
            EndpointScanner.extractControllerMappings(psiClass, manualProfile)
        } else {
            EndpointScanner.extractClientMappings(psiClass, kind)
        }
        for (info in mappings) cache.upsert(info)
    }

    /**
     * Spring 配置变化时异步刷新全部 Controller 映射。
     */
    private fun scheduleConfigRefresh(element: PsiElement): Boolean {
        val fileName = element.containingFile?.virtualFile?.name ?: return false
        if (!ApplicationConfigReader.isSpringConfigFile(fileName)) return false
        BilateralMappingCacheService.of(element.project).scheduleControllerRefresh()
        return true
    }

    /**
     * 向上查找最近的 PsiClass 容器，自身就是 PsiClass 时直接返回。
     *
     * @param element 起点元素
     * @return 最近的 PsiClass 祖先，找不到时返回 null
     */
    private fun findPsiClass(element: PsiElement): PsiClass? {
        var current: PsiElement? = element
        while (current != null) {
            if (current is PsiClass) return current
            current = current.parent
        }
        return null
    }
}
