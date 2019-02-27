package me.alvr.pressurizer.utils

/**
 * Used with for display message errors when a exception or 4xx error occurs.
 *
 * @property error Message to display
 * @constructor Creates an object with the message [error]
 */
data class StatusPageError(val error: String)