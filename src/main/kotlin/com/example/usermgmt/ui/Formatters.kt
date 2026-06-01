package com.example.usermgmt.ui

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Shared UI date/time formatting helpers. */
object Formatters {

    private val DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    private val DATE_TIME_SECONDS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    /** Formats an [Instant] in the system zone as `yyyy-MM-dd HH:mm`. */
    fun dateTime(instant: Instant): String =
        instant.atZone(ZoneId.systemDefault()).format(DATE_TIME)

    /** Formats an [Instant] in the system zone as `yyyy-MM-dd HH:mm:ss`. */
    fun dateTimeSeconds(instant: Instant): String =
        instant.atZone(ZoneId.systemDefault()).format(DATE_TIME_SECONDS)
}
