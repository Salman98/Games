package com.techeliteclub.game.core

/** Pure streak rules (no Android), so they are easy to test. Days are local calendar day numbers. */
object StreakMath {
    /** Streak after playing on [today]. Same day: unchanged; next day: +1; a gap: back to 1. */
    fun next(lastDay: Long, streak: Int, today: Long): Int = when (lastDay) {
        today -> streak
        today - 1 -> streak + 1
        else -> 1
    }

    /** Streak to show on [today]: still alive if the player played today or yesterday. */
    fun shown(lastDay: Long, streak: Int, today: Long): Int = if (lastDay == today || lastDay == today - 1) streak else 0

    /** Whether [day] is one of the streak's days (the run that ended on [lastDay]). */
    fun inStreak(day: Long, lastDay: Long, streak: Int): Boolean = day in (lastDay - streak + 1)..lastDay

    /** Monday of the week containing [day] (day 0 = Thursday 1 Jan 1970). */
    fun weekStart(day: Long): Long = day - Math.floorMod(day + 3, 7L)
}
