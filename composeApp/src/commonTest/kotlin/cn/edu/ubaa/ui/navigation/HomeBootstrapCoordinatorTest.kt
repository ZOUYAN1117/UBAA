package cn.edu.ubaa.ui.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class HomeBootstrapCoordinatorTest {

  @Test
  fun restartRunsHomeBootstrapInStages() = runTest {
    val events = mutableListOf<String>()
    val coordinator = HomeBootstrapCoordinator(this)

    coordinator.restart(actionsFor(events) { testScheduler.currentTime })
    runCurrent()
    assertEquals(listOf("schedule:false@0"), events)
    assertTrue(coordinator.isRunning.value)

    advanceTimeBy(250)
    runCurrent()
    assertEquals(
        listOf(
            "schedule:false@0",
            "signin:false@250",
        ),
        events,
    )

    advanceTimeBy(1500)
    runCurrent()
    assertEquals("spoc:false@1750", events.last())

    advanceTimeBy(1000)
    runCurrent()
    assertEquals("bykc:false@2750", events.last())
    assertFalse(coordinator.isRunning.value)
  }

  @Test
  fun restartCancelsPendingStagesFromPreviousRun() = runTest {
    val events = mutableListOf<String>()
    val coordinator = HomeBootstrapCoordinator(this)

    coordinator.restart(actionsFor(events) { testScheduler.currentTime })
    runCurrent()
    advanceTimeBy(200)
    coordinator.restart(actionsFor(events) { testScheduler.currentTime }, forceRefresh = true)
    runCurrent()
    advanceUntilIdle()

    assertEquals(
        listOf(
            "schedule:false@0",
            "schedule:true@200",
            "signin:true@450",
            "spoc:true@1950",
            "bykc:true@2950",
        ),
        events,
    )
    assertFalse(coordinator.isRunning.value)
  }

  @Test
  fun cancelStopsPendingDelayedLoads() = runTest {
    val events = mutableListOf<String>()
    val coordinator = HomeBootstrapCoordinator(this)

    coordinator.restart(actionsFor(events) { testScheduler.currentTime })
    runCurrent()
    coordinator.cancel()
    advanceUntilIdle()

    assertEquals(listOf("schedule:false@0"), events)
    assertFalse(coordinator.isRunning.value)
  }

  private fun actionsFor(
      events: MutableList<String>,
      currentTime: () -> Long,
  ): HomeBootstrapActions {
    return HomeBootstrapActions(
        loadTodaySchedule = { force -> events += "schedule:$force@${currentTime()}" },
        loadSignin = { force -> events += "signin:$force@${currentTime()}" },
        loadSpoc = { force -> events += "spoc:$force@${currentTime()}" },
        loadBykc = { force -> events += "bykc:$force@${currentTime()}" },
    )
  }
}
