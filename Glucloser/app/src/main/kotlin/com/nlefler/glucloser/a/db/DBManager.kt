package com.nlefler.glucloser.a.db

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import bolts.Task
import bolts.TaskCompletionSource
import rx.schedulers.Schedulers
import javax.inject.Inject

/**
 * Created by nathan on 12/16/15.
 */
class DBManager @Inject constructor(val ctx: Context):
// TODO(nl) DB Version
        SQLiteOpenHelper(ctx, "GlucloserDB", null, 1) {

    private val scheduler = Schedulers.io()
    private var db: SQLiteDatabase? = null

    override fun onCreate(aDb: SQLiteDatabase) {
        db = aDb
    }

    override fun onUpgrade(aDB: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // TODL(nl) Upgrade DB
        onCreate(aDB)
    }

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
            task.setResult(Unit)
        }
        return task.task
    }

    companion object {
        val LOG_TAG = "RealmManager"
    }
}
