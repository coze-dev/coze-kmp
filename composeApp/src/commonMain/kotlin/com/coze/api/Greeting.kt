package com.coze.api

import kotlinx.datetime.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds


class Greeting {
    private val platform = getPlatform()
    private val api = ApiBase()

    fun daysPhrase(): String {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val closestNewYear = LocalDate(today.year + 1, 1, 1)
        val leftDays = today.daysUntil(closestNewYear)

        return "While waiting, let me tell you the TRUTH: it's $leftDays days from new year!"
    }

    fun greet(): Flow<String> = flow {
        emit(if (Random.nextBoolean()) "Hi!" else "Hello!")
        delay(1.seconds)
        emit("Visiting the chat v3 API! > ${platform.name}")
        delay(1.seconds)
        emit(daysPhrase())
        emit(api.launchPhrase())
    }

}
