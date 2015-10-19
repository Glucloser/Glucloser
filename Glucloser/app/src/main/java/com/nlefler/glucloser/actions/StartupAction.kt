package com.nlefler.glucloser.actions

import bolts.Task
import com.nlefler.glucloser.dataSource.BolusPatternFactory
import java.util.*

/**
 * Created by nathan on 10/18/15.
 */
public class StartupAction {
    public fun run(): Task<Void> {
        val task = Task.create<Void>()
        val tasks = ArrayList<Task<*>>()

        tasks.add(BolusPatternFactory.UpdateCurrentBolusPatternCache())

        Task.whenAll(tasks).continueWith { task.trySetResult(null)  }

        return task.getTask() as Task<Void>
    }
}
