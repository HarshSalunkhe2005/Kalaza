package com.kalazacare.app

import android.app.Application
import android.content.Intent
import java.io.PrintWriter
import java.io.StringWriter

/**
 * Installs a process-wide uncaught-exception handler so a crash always ends in our own
 * full-detail screen ([CrashActivity]) instead of whatever truncated system dialog the
 * OEM's Android build shows by default.
 */
object CrashHandler {
    fun install(app: Application) {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val writer = StringWriter()
                throwable.printStackTrace(PrintWriter(writer))
                val intent = Intent(app, CrashActivity::class.java).apply {
                    putExtra(CrashActivity.EXTRA_STACK_TRACE, writer.toString())
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                app.startActivity(intent)
            } catch (_: Throwable) {
                // If we can't even show our own crash screen, fall back to the system's.
            }
            previous?.uncaughtException(thread, throwable)
            android.os.Process.killProcess(android.os.Process.myPid())
            kotlin.system.exitProcess(1)
        }
    }
}
