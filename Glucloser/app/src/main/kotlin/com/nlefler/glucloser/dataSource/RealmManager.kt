package com.nlefler.glucloser.dataSource

import bolts.Task
import bolts.TaskCompletionSource
import io.realm.Realm
import io.realm.RealmObject
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

    public fun executeTransaction(tx: Tx) : Task<List<RealmObject?>> {
        val _task = TaskCompletionSource<List<RealmObject?>>()
        executor.submit({
            realm?.executeTransaction({ realm ->
                val dependsOn = tx.dependsOn().map { obj -> obj ?: return@map obj; realm.copyToRealmOrUpdate(obj) }
                val deadResults = tx.execute(dependsOn, realm).map { obj -> obj ?: return@map obj; realm.copyFromRealm(obj )}
                _task.trySetResult(deadResults)
            })
        })
        return _task.task
    }

    public interface Tx {
        fun dependsOn(): List<RealmObject?>
        fun execute(dependsOn: List<RealmObject?>, realm: Realm): List<RealmObject?>
    }
}
