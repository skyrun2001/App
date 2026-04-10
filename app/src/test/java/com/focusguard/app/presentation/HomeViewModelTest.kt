package com.focusguard.app.presentation

import android.content.Context
import android.provider.Settings
import app.cash.turbine.test
import com.focusguard.app.domain.model.AppRestriction
import com.focusguard.app.domain.repository.AppRestrictionRepository
import com.focusguard.app.domain.repository.StepCounterRepository
import com.focusguard.app.presentation.ui.home.HomeViewModel
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val context: Context = mockk(relaxed = true)
    private val stepCounterRepository: StepCounterRepository = mockk()
    private val restrictionRepository: AppRestrictionRepository = mockk()

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        // Accessibility: disabled by default
        every {
            context.contentResolver
        } returns mockk(relaxed = true)
        every {
            Settings.Secure.getString(any(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        } returns ""

        every { stepCounterRepository.observeStepsToday() } returns flowOf(0)
        every { restrictionRepository.observeAll() } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial loading state has isLoading true`() = runTest {
        viewModel = HomeViewModel(context, stepCounterRepository, restrictionRepository)

        viewModel.uiState.test {
            val initial = awaitItem()
            assertThat(initial.isLoading).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState reflects step count from repository`() = runTest {
        every { stepCounterRepository.observeStepsToday() } returns flowOf(3_500)
        every { restrictionRepository.observeAll() } returns flowOf(emptyList())

        viewModel = HomeViewModel(context, stepCounterRepository, restrictionRepository)

        viewModel.uiState.test {
            skipItems(1) // initial placeholder
            val loaded = awaitItem()
            assertThat(loaded.stepsToday).isEqualTo(3_500)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `activeRestrictionCount counts only enabled restrictions`() = runTest {
        val restrictions = listOf(
            AppRestriction(packageName = "a", appName = "A", isEnabled = true),
            AppRestriction(packageName = "b", appName = "B", isEnabled = false),
            AppRestriction(packageName = "c", appName = "C", isEnabled = true),
        )
        every { restrictionRepository.observeAll() } returns flowOf(restrictions)

        viewModel = HomeViewModel(context, stepCounterRepository, restrictionRepository)

        viewModel.uiState.test {
            skipItems(1)
            val loaded = awaitItem()
            assertThat(loaded.activeRestrictionCount).isEqualTo(2)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
