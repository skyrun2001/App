package com.focusguard.app.domain.usecase

import com.focusguard.app.domain.model.AppRestriction
import com.focusguard.app.domain.repository.AppRestrictionRepository
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertFailsWith

class SaveRestrictionUseCaseTest {

    private val repository: AppRestrictionRepository = mockk()
    private val useCase = SaveRestrictionUseCase(repository)

    @Test
    fun `saves valid restriction to repository`() = runTest {
        val restriction = AppRestriction(
            packageName = "com.example.app",
            appName = "Test App",
            requiredStepCount = 1000,
            dailyTimeLimitMinutes = 30,
        )
        coJustRun { repository.save(restriction) }

        useCase(restriction)

        coVerify(exactly = 1) { repository.save(restriction) }
    }

    @Test
    fun `throws when packageName is blank`() = runTest {
        val invalid = AppRestriction(packageName = "", appName = "App")

        assertFailsWith<IllegalArgumentException> { useCase(invalid) }
    }

    @Test
    fun `throws when requiredStepCount is negative`() = runTest {
        val invalid = AppRestriction(
            packageName = "com.example.app",
            appName = "App",
            requiredStepCount = -1,
        )

        assertFailsWith<IllegalArgumentException> { useCase(invalid) }
    }

    @Test
    fun `throws when dailyTimeLimitMinutes is negative`() = runTest {
        val invalid = AppRestriction(
            packageName = "com.example.app",
            appName = "App",
            dailyTimeLimitMinutes = -5,
        )

        assertFailsWith<IllegalArgumentException> { useCase(invalid) }
    }
}
