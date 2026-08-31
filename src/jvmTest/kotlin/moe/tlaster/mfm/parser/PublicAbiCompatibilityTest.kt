package moe.tlaster.mfm.parser

import kotlin.test.Test
import kotlin.test.assertEquals
import moe.tlaster.mfm.parser.tree.FnNode
import moe.tlaster.mfm.parser.tree.SearchNode

class PublicAbiCompatibilityTest {
    @Test
    fun legacyJvmSignaturesRemainAvailable() {
        SearchNode::class.java.getConstructor(String::class.java, String::class.java)
        SearchNode::class.java.getMethod("copy", String::class.java, String::class.java)
        MFMParser::class.java.getConstructor(Boolean::class.javaPrimitiveType)
        assertEquals(
            "java.util.HashMap<java.lang.String, java.lang.String>",
            FnNode::class.java.getMethod("getArgs").genericReturnType.typeName,
        )
    }
}
