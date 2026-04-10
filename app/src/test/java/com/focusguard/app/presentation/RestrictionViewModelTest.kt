package com.focusguard.app.presentation

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.SavedStateHandle
import com.focusguard.app.domain.model.AppRestriction
import com.focusguard.app.domain.model.TimeWindow
import com.focusguard.app.domain.repository.AppRestrictionRepository
import com.focusguard.app.domain.usecase.DeleteRestrictionUseCase
import com.focusguard.app.domain.usecase.SaveRestrictionUseCase
import com.focusguard.app.presentation.ui.restriction.RestrictionViewModel
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RestrictionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val testPackage = "com.example.testapp"
    private val savedStateHandle = SavedStateHandle(mapOf("packageName" to testPackage))

    private val context: Context = mockk(relaxed = true)
    private val repository: AppRestrictionRepository = mockk()
    private val saveUseCase: SaveRestrictionUseCase = mockk()
    private val deleteUseCase: DeleteRestrictionUseCase = mockk()

    private lateinit var viewModel: RestrictionViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        val packageManager: PackageManager = mockk(relaxed = true)
        every { context.packageManager } returns packageManager
        every { packageManager.getApplicationLabel(any<ApplicationInfo>()) } returns "Test App"

        coEvery { repository.findByPackageName(testPackage) } returns null
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = RestrictionViewModel(
        savedStateHandle = savedStateHandle,
        context = context,
        restrictionRepository = repository,
        saveRestrictionUseCase = saveUseCase,
        deleteRestrictionUseCase = deleteUseCase,
    )

    @Test
    fun `initial state has isLoading true`() {
        viewModel = createViewModel()
        assertThat(viewModel.uiState.value.isLoading).isTrue()
    }

    @Test
    fun `loads existing restriction from repository`() = runTest {
        val existing = AppRestriction(
            packageName = testPackage,
            appName = "Test App",
            isEnabled = true,
            requiredStepCount = 3_000,
            dailyTimeLimitMinutes = 45,
            allowedTimeWindows = listOf(
                TimeWindow(startHour = 9, startMinute = 0, endHour = 17, endMinute = 0)
            ),
        )
        coEvery { repository.findByPackageName(testPackage) } returns existing

        viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.isEnabled).isTrue()
        assertThat(state.requiredStepCount).isEqualTo("3000")
        assertThat(state.dailyTimeLimitMinutes).isEqualTo("45")
        assertThat(state.allowedTimeWindows).hasSize(1)
    }

    @Test
    fun `onRequiredStepsChanged filters non-digit characters`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onRequiredStepsChanged("5abc000")

        assertThat(viewModel.uiState.value.requiredStepCount).isEqualTo("5000")
    }

    @Test
    fun `onDailyLimitChanged filters non-digit characters`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onDailyLimitChanged("30min")

        assertThat(viewModel.uiState.value.dailyTimeLimitMinutes).isEqualTo("30")
    }

    @Test
    fun `addTimeWindow appends new window to list`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.addTimeWindow(startHour = 8, startMinute = 0, endHour = 18, endMinute = 0)

        assertThat(viewModel.uiState.value.allowedTimeWindows).hasSize(1)
        assertThat(viewModel.uiState.value.allowedTimeWindows.first().startHour).isEqualTo(8)
    }

    @Test
    fun `removeTimeWindow removes specific window`() = runTest {
        val window = TimeWindow(id = 1L, startHour = 9, startMinute = 0, endHour = 17, endMinute = 0)
        val existing = AppRestriction(
            packageName = testPackage,
            appName = "Test App",
            allowedTimeWindows = listOf(window),
        )
        coEvery { repository.findByPackageName(testPackage) } returns existing

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.removeTimeWindow(window)

        assertThat(viewModel.uiState.value.allowedTimeWindows).isEmpty()
    }

    @Test
    fun `save calls saveUseCase and sets isSaved`() = runTest {
        coJustRun { saveUseCase(any()) }

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.save()
        advanceUntilIdle()

        coVerify(exactly = 1) { saveUseCase(any()) }
        assertThat(viewModel.uiState.value.isSaved).isTrue()
    }

    @Test
    fun `deleteRestriction calls deleteUseCase and sets isDeleted`() = runTest {
        coJustRun { deleteUseCase(testPackage) }

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.deleteRestriction()
        advanceUntilIdle()

        coVerify(exactly = 1) { deleteUseCase(testPackage) }
        assertThat(viewModel.uiState.value.isDeleted).isTrue()
    }
}
