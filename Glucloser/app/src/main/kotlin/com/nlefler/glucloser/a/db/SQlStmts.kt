package com.nlefler.glucloser.a.db

import android.database.Cursor
import java.util.*


/**
 * Created by nathan on 4/30/16.
 */
class SQLStmts {
    interface Query {
        val table: String
        val projection: Array<String>
        val whereCols: Array<String>
        val sortClause: String?
    }

    class Create {

    }

    class BloodSugar {
        class ForID: Query {
            val c = com.nlefler.glucloser.a.models.BloodSugar::class
            val primaryId = com.nlefler.glucloser.a.models.BloodSugar::primaryId
            val value = com.nlefler.glucloser.a.models.BloodSugar::value
            val recDate = com.nlefler.glucloser.a.models.BloodSugar::recordedDate
            override val table = c.simpleName!!
            override val projection = arrayOf(primaryId.name, value.name, recDate.name)
            override val whereCols = arrayOf(primaryId.name)
            override val sortClause = null

            fun getID(cursor: Cursor): String {
                return cursor.getString(cursor.getColumnIndex(primaryId.name))
            }
            fun getValue(cursor: Cursor): Int {
                return cursor.getInt(cursor.getColumnIndex(value.name))
            }
            fun getDate(cursor: Cursor): Date {
                return Date(cursor.getLong(cursor.getColumnIndex(recDate.name)))
            }
        }
    }
}
