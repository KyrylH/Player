package com.example.player.util

class DurationCalcUtil {
    companion object {
        fun calcDuration(millis: Long): String {
            val seconds = ((millis / 1000) % 60)
            val minutes = ((millis / (1000 * 60)) % 60)
            val hours = ((millis / (1000 * 60 * 60)) % 24)
            val secondsString = if (seconds.toString().length == 1) "0$seconds" else seconds
            val minutesString = if (minutes.toString().length == 1) "0$minutes" else minutes
            val hoursString = if (hours.toString().length == 1) "0$hours" else hours
            return if (hours > 0) {
                "$hoursString:$minutesString:$secondsString"
            } else {
                "$minutesString:$secondsString"
            }
        }
    }
}