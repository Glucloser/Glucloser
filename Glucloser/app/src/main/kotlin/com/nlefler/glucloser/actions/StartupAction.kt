package com.nlefler.glucloser.actions

import bolts.Task
import bolts.TaskCompletionSource
import com.nlefler.glucloser.dataSource.BolusPatternFactory
import java.util.*
import javax.inject.Inject

/**
 * Created by nathan on 10/18/15.
 */
public class StartupAction @Inject constructor(val bolusPatternFactory: BolusPatternFactory) {

    public fun run(): Task<Void> {
        val taskSource = TaskCompletionSource<Void>()
        val tasks = ArrayList<Task<*>>()

        Task.whenAll(tasks).continueWith { taskSource.trySetResult(null)  }

        return taskSource.task
    }
}
