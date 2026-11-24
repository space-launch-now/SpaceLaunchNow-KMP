package me.calebjones.spacelaunchnow.offer

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory state for the current app launch. The win-back sheet waits for [checked] and
 * stays away when [shown] is true, so a user never gets both sheets in one session.
 */
object HolidayOfferSession {
    private val _checked = MutableStateFlow(false)
    private val _shown = MutableStateFlow(false)

    /** True once the offer host has decided whether it will show. */
    val checked: StateFlow<Boolean> = _checked.asStateFlow()

    /** True once the offer sheet appeared in this app launch. */
    val shown: StateFlow<Boolean> = _shown.asStateFlow()

    fun markChecked() { _checked.value = true }

    fun markShown() { _shown.value = true }
}
