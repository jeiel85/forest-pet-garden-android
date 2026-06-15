package com.jeiel85.forestpetgarden

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AppNameTest {

    @Test
    fun appName_isLocalizedTitle() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals("숲속 펫 정원", context.getString(R.string.app_name))
    }
}
