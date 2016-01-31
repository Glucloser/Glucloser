package com.nlefler.glucloser.dataSource

import android.util.Log
import bolts.Task
import bolts.TaskCompletionSource
import io.realm.Realm
import io.realm.RealmList
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
            try {
                realm = Realm.getDefaultInstance()
            }
            catch(e: Exception) {
                Log.e(LOG_TAG, e.message)
            }
        })
    }

    public fun defaultRealm(): Realm {
        return Realm.getDefaultInstance()
    }

    public fun <T: RealmObject?> executeTransaction(tx: Tx<T>) : Task<T> {
        val _task = TaskCompletionSource<T>()
        realmExecutor.submit({
            try {
                realm?.executeTransaction({ realm ->
                    val dependsOn = tx.dependsOn().map { obj -> obj ?: return@map obj; realm.copyToRealmOrUpdate(obj) }
                    val result = tx.execute(dependsOn, realm)
                    val deadResult = if (result != null) {
                        realm.copyFromRealm(result)
                    }
                    else {
                        null
                    }

                    taskExecutor.submit {
                        _task.trySetResult(deadResult)
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

    public fun executeTransaction(tx: TxMulti) : Task<List<RealmObject?>> {
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

    public fun <T: RealmObject?> executeTransaction(tx: TxList<T>) : Task<List<T>> {
        val _task = TaskCompletionSource<List<T>>()
        realmExecutor.submit({
            try {
                realm?.executeTransaction({ realm ->
                    val dependsOn = tx.dependsOn().map { obj -> obj ?: return@map obj; realm.copyToRealmOrUpdate(obj) }
                    val result = tx.execute(dependsOn, realm)
                    val deadResult = realm.copyFromRealm(result)

                    taskExecutor.submit {
                        _task.trySetResult(deadResult)
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

    public interface Tx<T: RealmObject?> {
        fun dependsOn(): List<RealmObject?>
        fun execute(dependsOn: List<RealmObject?>, realm: Realm): T
    }

    public interface TxMulti {
        fun dependsOn(): List<RealmObject?>
        fun execute(dependsOn: List<RealmObject?>, realm: Realm): List<RealmObject?>
    }

    public interface TxList<T: RealmObject?> {
        fun dependsOn(): List<RealmObject?>
        fun execute(dependsOn: List<RealmObject?>, realm: Realm): List<T>
    }

    companion object {
        val LOG_TAG = "RealmManager"
    }
}
