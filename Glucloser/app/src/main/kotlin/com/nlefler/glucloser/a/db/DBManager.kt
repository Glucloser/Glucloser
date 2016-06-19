package com.nlefler.glucloser.a.db

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import bolts.Task
import bolts.TaskCompletionSource
import com.nlefler.glucloser.a.models.Models
import io.requery.Persistable
import io.requery.android.sqlite.DatabaseSource
import io.requery.rx.RxSupport
import io.requery.sql.EntityDataStore
import rx.schedulers.Schedulers
import javax.inject.Inject

/**
 * Created by nathan on 12/16/15.
 */
class DBManager @Inject constructor(val ctx: Context) {
    private val DB_VERSION = 0

    private val dbSource = DatabaseSource(ctx, Models.DEFAULT, DB_VERSION)
    private val entitySource = RxSupport.toReactiveStore(
            EntityDataStore<Persistable>(dbSource.configuration))

    private val scheduler = Schedulers.io()
    private var db: SQLiteDatabase? = null



    fun query(query: SQLStmts.Query, whereArgs: Array<String>?, handle: (Cursor?)->Unit): Task<Unit> {
        val task = TaskCompletionSource<Unit>()
        scheduler.createWorker().schedule {
            val cursor = db?.query(query.table, query.projection, query.whereClause, whereArgs, null, null, query.sortClause, null)
            handle(cursor)
            task.setResult(Unit)
        }
        return task.task
    }

    fun rawQuery(query: SQLStmts.RawQuery, whereArgs: Array<String>?, handle: (Cursor?)->Unit): Task<Unit> {
        val task = TaskCompletionSource<Unit>()
        scheduler.createWorker().schedule {
            val cursor = db?.rawQuery(query.query, whereArgs)
            handle(cursor)
            if (!(cursor?.isClosed ?: false)) {
                cursor?.close()
            }
            task.setResult(Unit)
        }
        return task.task
    }

    companion object {
        val LOG_TAG = "RealmManager"
    }
}
