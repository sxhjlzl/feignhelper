package com.lizhuolun.feignhelper.core

import com.intellij.psi.PsiJavaFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class HttpMappingInfoTest : BasePlatformTestCase() {

    fun testQualifierDistinguishesOverloadedMethods() {
        val file = myFixture.configureByText(
            "OverloadedController.java",
            """
                package example;

                class OverloadedController {
                    void find(String value) {}
                    void find(long value) {}
                }
            """.trimIndent(),
        ) as PsiJavaFile
        val methods = file.classes.single().methods

        val qualifiers = methods.map(HttpMappingInfo::qualifierOf)

        assertSize(2, qualifiers.distinct())
        assertContainsElements(
            qualifiers,
            "example.OverloadedController#find(String)",
            "example.OverloadedController#find(long)",
        )
    }
}
