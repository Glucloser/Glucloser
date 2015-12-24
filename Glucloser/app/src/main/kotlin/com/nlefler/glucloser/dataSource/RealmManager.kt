package com.nlefler.glucloser.dataSource

import bolts.Task
import bolts.TaskCompletionSource
import io.realm.Realm
import java.util.concurrent.Executors

/**
 * Created by nathan on 12/16/15.
 */
public class RealmManager {
    private val executor = Executors.newSingleThreadExecutor()
    private var realm: Realm? = null

    init {
        executor.submit({
            realm = Realm.getDefaultInstance()
        })
    }

    public fun <T> executeTransaction(tx: Realm.Transaction, task: Task<T>) : Task<T> {
        executor.submit({
            realm?.executeTransaction(tx)
        })
        return task
    }

}
