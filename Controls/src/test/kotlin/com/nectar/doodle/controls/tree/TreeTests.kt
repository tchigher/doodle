package com.nectar.doodle.controls.tree

import com.nectar.doodle.JsName
import com.nectar.doodle.controls.theme.TreeUI.ItemUIGenerator
import com.nectar.doodle.controls.theme.TreeUI
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 3/26/18.
 */
@Ignore
class TreeTests {
    @Test @JsName("rootDefaulsToHidden")
    fun `root defaults to hidden`() {
        val tree = Tree<Int>(model())

        expect(false) { tree.rootVisible }
    }

    @Test @JsName("hasRightNumberOfRows")
    fun `has right number of rows`() {
        val model = SimpleModel(node(0) { node(1); node(2); node(3) })

        val ui          = mockk<TreeUI<Int>>         (relaxed = true)
        val uiGenerator = mockk<ItemUIGenerator<Int>>(relaxed = true)

        every { ui.uiGenerator } returns uiGenerator

        var tree = Tree(model).apply { renderer = ui }

        expect(3) { tree.numRows }

        verify { uiGenerator(tree, 1, Path(0), 0, false, false, false) }
        verify { uiGenerator(tree, 2, Path(1), 1, false, false, false) }
        verify { uiGenerator(tree, 3, Path(2), 2, false, false, false) }

        clearMocks(uiGenerator)

        tree = Tree(model).apply { renderer = ui; rootVisible = true }

        expect(4) { tree.numRows }

        verify { uiGenerator(tree, 0, Path( ), 0, false, false, false) }
        verify { uiGenerator(tree, 1, Path(0), 1, false, false, false) }
        verify { uiGenerator(tree, 2, Path(1), 2, false, false, false) }
        verify { uiGenerator(tree, 3, Path(2), 3, false, false, false) }
    }

    @Test @JsName("foo")
    fun `foo`() {
        val model = SimpleModel(node("root") {
            node("child1") {
                node("child1_1")
                node("child1_2") }
            node("child2") {
                node("child2_1")
            }
            node("child3")
        })

        val ui          = mockk<TreeUI<String>>         (relaxed = true)
        val uiGenerator = mockk<ItemUIGenerator<String>>(relaxed = true)

        every { ui.uiGenerator } returns uiGenerator

        val tree = Tree(model).apply { renderer = ui; expand(0) }

        expect(5) { tree.numRows }

        verify { uiGenerator(tree, "child1",   Path(0),     0, false, false, true ) }
        verify { uiGenerator(tree, "child1_1", Path(0) + 0, 1, false, false, false) }
        verify { uiGenerator(tree, "child1_2", Path(0) + 1, 2, false, false, false) }
        verify { uiGenerator(tree, "child2",   Path(1),     3, false, false, false) }
        verify { uiGenerator(tree, "child3",   Path(2),     4, false, false, false) }
    }

    @Test @JsName("getWorks")
    fun `get path`() {
        validateGetPath(node(11) { node(105); node(-24) { node(33) }; node(0) }, mapOf(
                Path<Int>( )     to  11,
                Path     (0)     to 105,
                Path     (1)     to -24,
                Path     (1) + 0 to  33,
                Path     (2)     to   0))

        validateGetPath(node(11) { node(105); node(-24) { node(33) }; node(0) }, mapOf(
                Path<Int>( )     to  11,
                Path     (0)     to 105,
                Path     (1)     to -24,
                Path     (1) + 0 to  33,
                Path     (2)     to   0)) { expandAll() }
    }

    @Test @JsName("getRow")
    fun `get row`() {
        validateGetRow(node(11) { node(105); node(-24) { node(33) }; node(0) }, listOf(105, -24, 0    ))
        validateGetRow(node(11) { node(105); node(-24) { node(33) }; node(0) }, listOf(105, -24, 33, 0)) {
            expandAll()
        }
        validateGetRow(node(11) { node(105); node(-24) { node(33) }; node(0) }, listOf(11, 105, -24, 33, 0)) {
            rootVisible = true
            expandAll()
        }
    }

    private fun <T> validateGetRow(root: TreeNode<T>, expected: List<T>, block: Tree<T>.() -> Unit = {}) {
        val tree = Tree(SimpleModel(root)).also{ block(it) }

        expected.forEachIndexed { index, value ->
            expect(value) { tree[index] }
        }
    }

    private fun <T> validateGetPath(root: TreeNode<T>, expected: Map<Path<Int>, T>, block: Tree<T>.() -> Unit = {}) {
        val tree = Tree(SimpleModel(root)).also{ block(it) }

        expected.forEach { (path, value) ->
            expect(value) { tree[path] }
        }
    }

    private fun <T> model(): Model<T> {
        val result = mockk<Model<T>>(relaxed = true)

        every { result.isEmpty() } returns true

        return result
    }
}