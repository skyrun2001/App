package com.focusguard.app.domain.usecase

import app.cash.turbine.test
import com.focusguard.app.domain.model.AppRestriction
import com.focusguard.app.domain.repository.AppRestrictionRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GetAllRestrictionsUseCaseTest {

    private val repository: AppRestrictionRepository = mockk()
    private val useCase = GetAllRestrictionsUseCase(repository)

    @Test
    fun `invoke returns flow from repository`() = runTest {
        val restrictions = listOf(
            AppRestriction(packageName = "com.example.a", appName = "App A"),
            AppRestriction(packageName = "com.example.b", appName = "App B"),
        )
        every { repository.observeAll() } returns flowOf(restrictions)

        useCase().test {
            assertThat(awaitItem()).isEqualTo(restrictions)
            awaitComplete()
        }
    }

    @Test
    fun `invoke emits empty list when no restrictions exist`() = runTest {
        every { repository.observeAll() } returns flowOf(emptyList())

        useCase().test {
            assertThat(awaitItem()).isEmpty()
            awaitComplete()
        }
    }
}
