package com.lizhuolun.feignhelper.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.ProjectManager
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import com.lizhuolun.feignhelper.FeignHelperBundle
import com.lizhuolun.feignhelper.cache.BilateralMappingCacheService
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * FeignHelper 设置页面，挂在 Settings -> Tools 下。
 *
 * @author lizhuolun
 * @date 2026/6/9
 */
class FeignHelperConfigurable : Configurable {

    private val profileTextField = JBTextField().apply {
        emptyText.text = FeignHelperBundle.message("settings.active.profile.placeholder")
    }

    override fun getDisplayName(): String = FeignHelperBundle.message("settings.title")

    override fun createComponent(): JComponent {
        reset()
        return FormBuilder.createFormBuilder()
            .addLabeledComponent(FeignHelperBundle.message("settings.active.profile.label"), profileTextField, 1, false)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    override fun isModified(): Boolean {
        val state = FeignHelperSettings.getInstance().state
        return profileTextField.text.trim() != state.manualActiveProfile
    }

    override fun apply() {
        val state = FeignHelperSettings.getInstance().state
        val newProfile = profileTextField.text.trim()
        if (state.manualActiveProfile == newProfile) return
        state.manualActiveProfile = newProfile
        ProjectManager.getInstance().openProjects.forEach { project ->
            BilateralMappingCacheService.of(project).scheduleControllerRefresh(delayMillis = 0)
        }
    }

    override fun reset() {
        val state = FeignHelperSettings.getInstance().state
        profileTextField.text = state.manualActiveProfile
    }
}
