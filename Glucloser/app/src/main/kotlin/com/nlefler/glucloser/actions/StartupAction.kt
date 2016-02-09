package com.nlefler.glucloser.actions

import android.util.Log
import bolts.Task
import bolts.TaskCompletionSource
import com.nlefler.glucloser.dataSource.BolusPatternFactory
import java.util.*
import javax.inject.Inject

/**
 * Created by nathan on 10/18/15.
 */
class StartupAction @Inject constructor() {

    fun run(): Task<Unit> {
        val taskSource = TaskCompletionSource<Unit>()
        val tasks = ArrayList<Task<*>>()

        Task.whenAll(tasks).continueWith { taskSource.trySetResult(Unit)  }

        return taskSource.task
    }
}
