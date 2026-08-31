package moe.tlaster.mfm.parser

import moe.tlaster.mfm.parser.tree.FnNode
import moe.tlaster.mfm.parser.tree.Node
import moe.tlaster.mfm.parser.tree.TextNode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class MFMParserStressTest {
    @Test
    fun unmatchedClosingsStayOneTextRun() {
        val input = "]".repeat(128 * 1024)

        val result = MFMParser().parse(input)

        assertEquals(1, result.content.size)
        assertEquals(TextNode(input), result.content.single())
    }

    @Test
    fun deeplyNestedFunctionsDoNotUseTheCallStack() {
        val depth = 2_000
        val input = "\$[x ".repeat(depth) + "a" + "]".repeat(depth)

        val result = MFMParser().parse(input)

        var current: Node = result.content.single()
        repeat(depth) {
            current = assertIs<FnNode>(current).content.single()
        }
        assertEquals(TextNode("a"), current)
    }
}
