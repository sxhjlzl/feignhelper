package com.lizhuolun.feignhelper.provider.linemarker

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiMethod
import com.lizhuolun.feignhelper.cache.BilateralMappingCacheService
import com.lizhuolun.feignhelper.core.HttpMappingInfo
import com.lizhuolun.feignhelper.core.annotation.AnnotationParser
import com.lizhuolun.feignhelper.provider.RestIcons

/**
 * Controller 方法旁的导航 gutter。
 *
 * 左键：跳转到对端 FeignClient / HttpExchange 方法（多目标弹选择 popup）
 * 右键：复制当前 Controller URL 到剪贴板
 */
class ControllerToFeignLineMarker : EndpointNavigationLineMarker() {

    override val markerIcon = RestIcons.JUMP_CONTROLLER_TO_FEIGN
    override val titleKey = "linemarker.controller.to.feign.title"
    override val accessibleKey = "linemarker.controller.accessible"

    override fun isApplicable(method: PsiMethod): Boolean {
        val cls = method.containingClass ?: return false
        if (!AnnotationParser.isControllerClass(cls)) return false
        return AnnotationParser.findRestfulAnnotation(method) != null
    }

    override fun findTargets(project: Project, method: PsiMethod): List<HttpMappingInfo> =
        BilateralMappingCacheService.of(project)
            .findClientTargets(method)
            .filter { it.method.isValid }

    override fun resolveSelfUrl(project: Project, method: PsiMethod): String? =
        BilateralMappingCacheService.of(project).resolveMapping(method)?.url
}
