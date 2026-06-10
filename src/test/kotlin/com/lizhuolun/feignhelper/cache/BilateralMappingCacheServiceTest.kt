package com.lizhuolun.feignhelper.cache

import com.intellij.psi.PsiJavaFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class BilateralMappingCacheServiceTest : BasePlatformTestCase() {

    fun testFindControllerTargetsFallsBackToProjectScanWhenControllerCacheIsEmpty() {
        myFixture.addFileToProject(
            "org/springframework/web/bind/annotation/RestController.java",
            """
                package org.springframework.web.bind.annotation;

                public @interface RestController {}
            """.trimIndent(),
        )
        myFixture.addFileToProject(
            "org/springframework/web/bind/annotation/GetMapping.java",
            """
                package org.springframework.web.bind.annotation;

                public @interface GetMapping {
                    String[] value() default {};
                    String[] path() default {};
                }
            """.trimIndent(),
        )
        myFixture.addFileToProject(
            "org/springframework/cloud/openfeign/FeignClient.java",
            """
                package org.springframework.cloud.openfeign;

                public @interface FeignClient {
                    String name() default "";
                    String path() default "";
                }
            """.trimIndent(),
        )

        myFixture.configureByText(
            "DemoController.java",
            """
                package example;

                import org.springframework.web.bind.annotation.GetMapping;
                import org.springframework.web.bind.annotation.RestController;

                @RestController
                class DemoController {
                    @GetMapping("/api/hello")
                    String hello() {
                        return "ok";
                    }
                }
            """.trimIndent(),
        )
        val feignFile = myFixture.configureByText(
            "DemoClient.java",
            """
                package example;

                import org.springframework.cloud.openfeign.FeignClient;
                import org.springframework.web.bind.annotation.GetMapping;

                @FeignClient(name = "demo")
                interface DemoClient {
                    @GetMapping("/api/hello")
                    String hello();
                }
            """.trimIndent(),
        ) as PsiJavaFile

        val cache = BilateralMappingCacheService.of(project)
        cache.clear()

        val feignMethod = feignFile.classes.single().methods.single()
        val targets = cache.findControllerTargets(feignMethod)

        assertSize(1, targets)
        assertEquals("hello", targets.single().method.name)
    }
}
