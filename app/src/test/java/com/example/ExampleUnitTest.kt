package com.example

import com.example.model.AuthState
import com.example.viewmodel.CyberViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExampleUnitTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testPasscodeEntryAndAuthentication() = runTest {
        val viewModel = CyberViewModel()
        assertEquals(AuthState.LOCKED, viewModel.authState.value)

        // Enter correct code 9955
        viewModel.onDigitEntered("9")
        viewModel.onDigitEntered("9")
        viewModel.onDigitEntered("5")
        viewModel.onDigitEntered("5")

        assertEquals(AuthState.AUTHENTICATING, viewModel.authState.value)
    }

    @Test
    fun testWrongPasscodeDenied() = runTest {
        val viewModel = CyberViewModel()
        viewModel.onDigitEntered("1")
        viewModel.onDigitEntered("2")
        viewModel.onDigitEntered("3")
        viewModel.onDigitEntered("4")

        assertEquals(AuthState.DENIED, viewModel.authState.value)
        assertEquals(1, viewModel.failedAttempts.value)
    }

    @Test
    fun testCustomCommandExecution() {
        val viewModel = CyberViewModel()
        val initialLineCount = viewModel.terminalLines.value.size

        viewModel.executeCustomCommand("scan 10.0.0.1")
        val updatedLines = viewModel.terminalLines.value

        assertNotEquals(initialLineCount, updatedLines.size)
    }
}
