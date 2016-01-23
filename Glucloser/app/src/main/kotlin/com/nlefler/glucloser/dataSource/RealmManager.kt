package com.nlefler.glucloser.dataSource

import android.util.Log
import bolts.Task
import bolts.TaskCompletionSource
import io.realm.Realm
import io.realm.RealmObject
import java.util.concurrent.Executors

/**
 * Created by nathan on 12/16/15.
 */
public class RealmManager {
    private val realmExecutor = Executors.newSingleThreadExecutor()
    private val taskExecutor = Executors.newCachedThreadPool()
    private var realm: Realm? = null

    init {
        realmExecutor.submit({
            realm = Realm.getDefaultInstance()
        })
    }

    public fun executeTransaction(tx: Tx) : Task<List<RealmObject?>> {
        val _task = TaskCompletionSource<List<RealmObject?>>()
        realmExecutor.submit({
            try {
                realm?.executeTransaction({ realm ->
                    val dependsOn = tx.dependsOn().map { obj -> obj ?: return@map obj; realm.copyToRealmOrUpdate(obj) }
                    val deadResults = tx.execute(dependsOn, realm).map { obj ->
                        obj ?: return@map obj;
                        if (obj.isValid) {
                            realm.copyFromRealm(obj)
                        } else {
                            obj
                        }
                    }
                    taskExecutor.submit {
                        _task.trySetResult(deadResults)
                    }
                })
            }
            catch (e: Exception) {
                Log.e(LOG_TAG, e.message)
                taskExecutor.submit {
                    _task.trySetError(e)
                }
            }
        })
        return _task.task
    }

    public interface Tx {
        fun dependsOn(): List<RealmObject?>
        fun execute(dependsOn: List<RealmObject?>, realm: Realm): List<RealmObject?>
    }

    companion object {
        val LOG_TAG = "RealmManager"
    }
}
