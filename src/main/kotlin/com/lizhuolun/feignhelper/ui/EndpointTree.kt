package com.lizhuolun.feignhelper.ui

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.Computable
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.SmartPointerManager
import com.intellij.icons.AllIcons
import com.intellij.ui.DoubleClickListener
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.ui.treeStructure.Tree
import com.lizhuolun.feignhelper.FeignHelperBundle
import com.lizhuolun.feignhelper.cache.BilateralMappingCacheService
import com.lizhuolun.feignhelper.core.EndpointKind
import com.lizhuolun.feignhelper.core.HttpMappingInfo
import java.awt.BorderLayout
import java.awt.Cursor
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.datatransfer.StringSelection
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.BorderFactory
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JPopupMenu
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

/**
 * 端点列表树组件，包含过滤输入框与树形展示。
 *
 * @param project 当前工程
 * @param kind 本树展示的端点类别，用于计算对端跳转
 * @author lizhuolun
 * @date 2026/6/12
 */
class EndpointTree(
    private val project: Project,
    private val kind: EndpointKind,
) : JPanel(BorderLayout()) {

    private val treeModel = EndpointTreeModel()
    private val tree = Tree(treeModel).apply {
        isRootVisible = false
        showsRootHandles = true
        emptyText.text = FeignHelperBundle.message("toolwindow.empty.text")
        cellRenderer = EndpointTreeCellRenderer()
        rowHeight = 24
    }
    private val filterField = JBTextField().apply {
        emptyText.text = FeignHelperBundle.message("toolwindow.filter.placeholder")
        border = BorderFactory.createEmptyBorder(2, 4, 2, 4)
    }

    private val clearLabel = JLabel("×").apply {
        foreground = JBColor.GRAY
        cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        isVisible = false
        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                filterField.text = ""
            }
        })
    }

    private val searchPanel = JPanel(BorderLayout(4, 0)).apply {
        border = BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, JBColor.GRAY.brighter()),
            BorderFactory.createEmptyBorder(6, 8, 6, 8),
        )
        add(JBLabel(AllIcons.Actions.Search), BorderLayout.WEST)
        add(filterField, BorderLayout.CENTER)
        add(clearLabel, BorderLayout.EAST)
    }

    /**
     * 当前完整端点数据，用于过滤时重建树。
     */
    private var allItems: List<EndpointTreeItem> = emptyList()

    /**
     * 上一次实际应用的过滤文本，避免 caret/focus 变化时重复触发过滤。
     */
    private var lastFilterText: String = ""

    /**
     * 当前过滤后的条目数量。
     */
    private var filteredCount: Int = 0

    /**
     * 数量变化回调，参数为 (总数, 过滤后数量)。
     */
    var onCountsChanged: ((total: Int, filtered: Int) -> Unit)? = null

    init {
        add(searchPanel, BorderLayout.NORTH)
        add(JBScrollPane(tree).apply {
            border = BorderFactory.createEmptyBorder()
        }, BorderLayout.CENTER)
        setupListeners()
    }

    /**
     * 刷新整棵树。
     *
     * 调用方应确保在 read action 内传入 endpoints，或保证其中的 PSI 属性已可安全访问。
     * 本方法会在 read action 内一次性提取展示所需字段并创建 SmartPointer，
     * 后续过滤与渲染不再触碰 PSI。
     *
     * @param endpoints 新的端点列表
     */
    fun refresh(endpoints: List<HttpMappingInfo>) {
        val smartManager = SmartPointerManager.getInstance(project)
        val items = ApplicationManager.getApplication().runReadAction(Computable {
            endpoints.mapNotNull { info ->
                if (!info.method.isValid) return@mapNotNull null
                val pointer = smartManager.createSmartPsiElementPointer(info.method)
                EndpointTreeItem.from(info, pointer)
            }
        })
        allItems = items
        applyFilter(filterField.text.trim())
    }

    private fun setupListeners() {
        filterField.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) = onFilterTextChanged()
            override fun removeUpdate(e: DocumentEvent?) = onFilterTextChanged()
            override fun changedUpdate(e: DocumentEvent?) = onFilterTextChanged()
        })

        tree.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                if (e.keyCode == KeyEvent.VK_ENTER) {
                    navigateSelected()
                    e.consume()
                }
            }
        })

        object : DoubleClickListener() {
            override fun onDoubleClick(event: MouseEvent): Boolean {
                navigateSelected()
                return true
            }
        }.installOn(tree)

        tree.componentPopupMenu = buildPopupMenu()
    }

    private fun onFilterTextChanged() {
        val text = filterField.text.trim()
        clearLabel.isVisible = text.isNotEmpty()
        if (text == lastFilterText) return
        lastFilterText = text
        applyFilter(text)
    }

    private fun applyFilter(text: String) {
        val filtered = if (text.isBlank()) {
            allItems
        } else {
            val lower = text.lowercase()
            allItems.filter {
                it.url.lowercase().contains(lower) ||
                    it.httpMethod.name.lowercase().contains(lower) ||
                    it.methodName.lowercase().contains(lower) ||
                    it.className.lowercase().contains(lower)
            }
        }
        filteredCount = filtered.size
        onCountsChanged?.invoke(allItems.size, filteredCount)
        treeModel.refresh(filtered)
        expandAll()
    }

    private fun expandAll() {
        for (i in 0 until tree.rowCount) {
            tree.expandRow(i)
        }
    }

    /**
     * 收起树中所有类分组节点。
     */
    fun collapseAll() {
        for (i in tree.rowCount - 1 downTo 0) {
            tree.collapseRow(i)
        }
    }

    /**
     * 控制搜索栏的显示/隐藏。
     *
     * @param visible true 显示，false 隐藏
     */
    fun setSearchVisible(visible: Boolean) {
        searchPanel.isVisible = visible
        revalidate()
        repaint()
    }

    /**
     * 获取当前 Tab 下完整的端点总数。
     *
     * @return 端点总数
     */
    fun getTotalCount(): Int = allItems.size

    /**
     * 获取当前过滤后显示的端点数量。
     *
     * @return 过滤后端点数量
     */
    fun getFilteredCount(): Int = filteredCount

    private fun navigateSelected() {
        val pointer = (tree.lastSelectedPathComponent as? EndpointNode)?.pointer ?: return
        ApplicationManager.getApplication().invokeLater {
            if (project.isDisposed) return@invokeLater
            val method = ApplicationManager.getApplication().runReadAction(Computable {
                pointer.element?.takeIf { it.isValid }
            }) ?: return@invokeLater
            navigateToMethod(method)
        }
    }

    private fun buildPopupMenu(): JPopupMenu {
        val menu = JPopupMenu()
        val navigateItem = javax.swing.JMenuItem(FeignHelperBundle.message("toolwindow.action.navigate.counterpart")).apply {
            addActionListener { navigateToCounterpart() }
        }
        val copyItem = javax.swing.JMenuItem(FeignHelperBundle.message("toolwindow.action.copy.url")).apply {
            addActionListener { copySelectedUrl() }
        }
        menu.add(navigateItem)
        menu.add(copyItem)
        return menu
    }

    private fun copySelectedUrl() {
        val node = tree.lastSelectedPathComponent as? EndpointNode ?: return
        val url = node.item?.url ?: return
        CopyPasteManager.getInstance().setContents(StringSelection(url))
    }

    private fun navigateToCounterpart() {
        val node = tree.lastSelectedPathComponent as? EndpointNode ?: return
        val pointer = node.pointer ?: return

        ApplicationManager.getApplication().executeOnPooledThread {
            val method = ApplicationManager.getApplication().runReadAction(Computable {
                pointer.element?.takeIf { it.isValid }
            }) ?: return@executeOnPooledThread

            val targets = when (kind) {
                EndpointKind.CONTROLLER -> BilateralMappingCacheService.of(project).findClientTargets(method)
                else -> BilateralMappingCacheService.of(project).findControllerTargets(method)
            }

            ApplicationManager.getApplication().invokeLater {
                if (project.isDisposed) return@invokeLater
                val valid = ApplicationManager.getApplication().runReadAction(Computable {
                    targets.mapNotNull { it.method }
                        .filter { it.isValid }
                        .filterIsInstance<NavigatablePsiElement>()
                })
                when (valid.size) {
                    0 -> {}
                    1 -> valid[0].navigate(true)
                    else -> JBPopupFactory.getInstance()
                        .createPopupChooserBuilder(valid)
                        .setTitle(FeignHelperBundle.message("toolwindow.action.navigate.counterpart"))
                        .setItemSelectedCallback { it?.navigate(true) }
                        .createPopup()
                        .showInFocusCenter()
                }
            }
        }
    }

    private fun navigateToMethod(method: PsiMethod) {
        ApplicationManager.getApplication().runReadAction(Computable {
            val file = method.containingFile?.virtualFile ?: return@Computable null
            val offset = method.textOffset
            OpenFileDescriptor(project, file, offset)
        })?.let {
            it.navigate(true)
        }
    }
}
