package com.example

import com.example.ui.updater.isNewerVersion
import org.junit.Assert.*
import org.junit.Test

class VersionCheckerTest {

    @Test
    fun testSemanticVersionComparison() {
        // Remote is newer patch
        assertTrue(isNewerVersion("1.0.1", "1.0.0"))

        // Remote is newer minor
        assertTrue(isNewerVersion("1.1.0", "1.0.0"))

        // Remote is newer major
        assertTrue(isNewerVersion("2.0.0", "1.9.9"))

        // Remote is equal
        assertFalse(isNewerVersion("1.0.0", "1.0.0"))

        // Remote is older patch
        assertFalse(isNewerVersion("1.0.0", "1.0.1"))

        // Remote is older minor
        assertFalse(isNewerVersion("1.0.5", "1.1.0"))

        // Remote has 'v' prefix
        assertTrue(isNewerVersion("v1.0.2", "1.0.1"))
    }
}
