package com.jeiel85.forestpetgarden

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, runs on an Android device/emulator.
 */
@RunWith(AndroidJUnit4::class)
class PackageNameTest {
    @Test
    fun packageName_isCorrect() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.jeiel85.forestpetgarden", appContext.packageName)
    }
}
