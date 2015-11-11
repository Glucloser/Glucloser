package com.nlefler.glucloser.actions

import bolts.Task
import com.nlefler.glucloser.dataSource.BolusPatternFactory
import java.util.*
import javax.inject.Inject

/**
 * Created by nathan on 10/18/15.
 */
public class StartupAction @Inject constructor(val bolusPatternFactory: BolusPatternFactory) {

    public fun run(): Task<Void> {
        val task = Task.create<Void>()
        val tasks = ArrayList<Task<*>>()

        tasks.add(bolusPatternFactory.updateCurrentBolusPatternCache())

        Task.whenAll(tasks).continueWith { task.trySetResult(null)  }

        return task.getTask() as Task<Void>
    }
}
