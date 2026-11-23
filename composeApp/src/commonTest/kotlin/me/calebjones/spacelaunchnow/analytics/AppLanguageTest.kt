package me.calebjones.spacelaunchnow.analytics

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import me.calebjones.spacelaunchnow.analytics.core.AnalyticsManagerImpl
import me.calebjones.spacelaunchnow.data.billing.MockBillingManager
import me.calebjones.spacelaunchnow.data.model.SubscriptionState
import me.calebjones.spacelaunchnow.util.TestSpaceLoggerInit
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AppLanguageTest {

    @BeforeTest
    fun setup() {
        TestSpaceLoggerInit.ensureInitialized()
    }

    @Test
    fun `supported languages resolve to their own tag`() {
        AppLanguage.SUPPORTED.forEach { assertEquals(it, AppLanguage.resolve(it)) }
    }

    @Test
    fun `regional device tags resolve to the shipped language`() {
        assertEquals("de", AppLanguage.resolve("de-AT"))
        assertEquals("es", AppLanguage.resolve("es-MX"))
        assertEquals("fr", AppLanguage.resolve("fr_CA"))
        assertEquals("en", AppLanguage.resolve("en-GB"))
        assertEquals("zh-CN", AppLanguage.resolve("zh-Hans-CN"))
        assertEquals("pt-BR", AppLanguage.resolve("pt-BR"))
    }

    @Test
    fun `unsupported languages and regions resolve to English`() {
        assertEquals("en", AppLanguage.resolve("ja-JP"))
        assertEquals("en", AppLanguage.resolve("pt-PT"))
        assertEquals("en", AppLanguage.resolve("zh-TW"))
        assertEquals("en", AppLanguage.resolve("zh-Hant"))
        assertEquals("en", AppLanguage.resolve(""))
    }

    private fun TestScope.analyticsWith(fake: FakeAnalyticsProvider) =
        AnalyticsManagerImpl(
            listOf(fake),
            scope = CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler))
        )

    @Test
    fun `syncer sets app_language at start and on change`() = runTest {
        val fake = FakeAnalyticsProvider()
        var tag = "de-DE"
        val syncer = FunnelUserPropertySyncer(analyticsWith(fake), MockBillingManager()) { tag }

        syncer.start(this, MutableStateFlow(SubscriptionState()))
        advanceUntilIdle()
        assertEquals("de", fake.userProperties["app_language"])

        tag = "pt-BR"
        syncer.syncAppLanguage()
        advanceUntilIdle()
        assertEquals("pt-BR", fake.userProperties["app_language"])

        coroutineContext.cancelChildren()
    }
}
