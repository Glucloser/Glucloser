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
        val whereClause: String
        val sortClause: String?
    }
    interface RawQuery {
        val query: String
    }
    companion object {
        private fun Array<String>.toWhereClause(): String {
            return this.joinToString(" = ? AND ") + " = ?"
        }
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
            override val whereClause = arrayOf(primaryId.name).toWhereClause()
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

    class BolusRate {
        class ForID: Query {
            val c = com.nlefler.glucloser.a.models.BolusRate::class
            val primaryID = com.nlefler.glucloser.a.models.BolusRate::primaryId
            val ordinal = com.nlefler.glucloser.a.models.BolusRate::ordinal
            val carbsPerUnit = com.nlefler.glucloser.a.models.BolusRate::carbsPerUnit
            val startTime = com.nlefler.glucloser.a.models.BolusRate::startTime
            override val table = c.simpleName!!
            override val projection = arrayOf(primaryID.name, ordinal.name, carbsPerUnit.name, startTime.name)
            override val whereClause = arrayOf(primaryID.name).toWhereClause()
            override val sortClause = null
            fun getID(cursor: Cursor): String {
                return cursor.getString(cursor.getColumnIndex(primaryID.name))
            }
            fun getOridnal(cursor: Cursor): Int {
                return cursor.getInt(cursor.getColumnIndex(ordinal.name))
            }
            fun getCarbsPerUnit(cursor: Cursor): Int {
                return cursor.getInt(cursor.getColumnIndex(carbsPerUnit.name))
            }
            fun getStartTime(cursor: Cursor): Int {
                return cursor.getInt(cursor.getColumnIndex(startTime.name))
            }
        }
    }

    class BolusPattern {
        class RatesForID: RawQuery {
            val patternTableName = com.nlefler.glucloser.a.models.BolusPattern::class.simpleName!!
            val primaryID = com.nlefler.glucloser.a.models.BolusPattern::primaryId
            val rates = com.nlefler.glucloser.a.models.BolusPattern::rates
            val ratesTableName = com.nlefler.glucloser.a.models.BolusRate::class.simpleName!!
            val ratesPrimaryID = com.nlefler.glucloser.a.models.BolusRate::primaryId
            val ordinal = com.nlefler.glucloser.a.models.BolusRate::ordinal
            val carbsPerUnit = com.nlefler.glucloser.a.models.BolusRate::carbsPerUnit
            val startTime = com.nlefler.glucloser.a.models.BolusRate::startTime
            override val query = """SELECT (${ratesPrimaryID.name}, ${ordinal.name}, ${carbsPerUnit.name}, ${startTime.name})
             FROM ${ratesTableName} WHERE ${ratesPrimaryID.name} IN (SELECT ${patternTableName}.${rates.name} FROM ${patternTableName} WHERE
             ${patternTableName}.${primaryID.name} = ?)
            """

            fun getRateID(cursor: Cursor): String {
                return cursor.getString(cursor.getColumnIndex(ratesPrimaryID.name))
            }
            fun getOridinal(cursor: Cursor): Int {
                return cursor.getInt(cursor.getColumnIndex(ordinal.name))
            }
            fun getCarbsPerUnit(cursor: Cursor): Int {
                return cursor.getInt(cursor.getColumnIndex(carbsPerUnit.name))
            }
            fun getStartTime(cursor: Cursor): Int {
                return cursor.getInt(cursor.getColumnIndex(startTime.name))
            }
        }
    }
}
