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
        val whereClause: String?
        val sortClause: String?
        val orderBy: String?
        val limit: Int?
    }
    interface RawQuery {
        val query: String
    }
    companion object {
        private fun Array<String>.toWhereClause(): String {
            return this.joinToString(" = ? AND ") + " = ?"
        }
        private fun Query.toRawQuery(whereArgs: Array<String>): RawQuery {
            var rawQ = "SELECT ${this.projection} FROM ${this.table}"
            if (this.whereClause != null) {
                val where = whereArgs.forEach { arg -> this.whereClause?.replaceFirst("?", arg) }
                rawQ += " WHERE ${where} "
            }
            if (this.sortClause != null) {
                rawQ += " SORT BY ${this.sortClause}"

            }
            if (this.orderBy != null) {
                rawQ += " ORDER BY ${this.orderBy}"
            }
            if (this.limit != null) {
                rawQ += "LIMIT ${this.limit}"
            }
            return object: RawQuery {
                override val query = rawQ
            }
        }
    }

    class Create {

    }

    open class BloodSugar {
        val c = com.nlefler.glucloser.a.models.BloodSugar::class
        val primaryId = com.nlefler.glucloser.a.models.BloodSugar::primaryId
        val value = com.nlefler.glucloser.a.models.BloodSugar::value
        val recDate = com.nlefler.glucloser.a.models.BloodSugar::recordedDate

        class Base: BloodSugar(), Query {
            override val table = c.simpleName!!
            override val projection = arrayOf(primaryId.name, value.name, recDate.name)
            override val whereClause = arrayOf(primaryId.name).toWhereClause()
            override val sortClause = null
            override val orderBy = null
            override val limit = null

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

        fun forID(): Base {
            return Base()
        }
    }

    class BolusRate {
        val c = com.nlefler.glucloser.a.models.BolusRate::class
        val primaryID = com.nlefler.glucloser.a.models.BolusRate::primaryId
        val ordinal = com.nlefler.glucloser.a.models.BolusRate::ordinal
        val carbsPerUnit = com.nlefler.glucloser.a.models.BolusRate::carbsPerUnit
        val startTime = com.nlefler.glucloser.a.models.BolusRate::startTime

        fun getID(cursor: Cursor): String {
            return cursor.getString(cursor.getColumnIndex(primaryID.name))
        }
        fun getOrdinal(cursor: Cursor): Int {
            return cursor.getInt(cursor.getColumnIndex(ordinal.name))
        }
        fun getCarbsPerUnit(cursor: Cursor): Int {
            return cursor.getInt(cursor.getColumnIndex(carbsPerUnit.name))
        }
        fun getStartTime(cursor: Cursor): Int {
            return cursor.getInt(cursor.getColumnIndex(startTime.name))
        }

        fun forID(id: String): Query {
            return object: Query {
                override val table = c.simpleName!!
                override val projection = arrayOf(primaryID.name, ordinal.name, carbsPerUnit.name, startTime.name)
                override val whereClause = arrayOf(primaryID.name).toWhereClause()
                override val sortClause = null
                override val orderBy = null
                override val limit = null
            }
        }
    }

    class BolusPattern {
        val patternTableName = com.nlefler.glucloser.a.models.BolusPattern::class.simpleName!!
        val primaryID = com.nlefler.glucloser.a.models.BolusPattern::primaryId
        val rates = com.nlefler.glucloser.a.models.BolusPattern::rates
        val ratesTableName = com.nlefler.glucloser.a.models.BolusRate::class.simpleName!!
        val ratesPrimaryID = com.nlefler.glucloser.a.models.BolusRate::primaryId
        val ordinal = com.nlefler.glucloser.a.models.BolusRate::ordinal
        val carbsPerUnit = com.nlefler.glucloser.a.models.BolusRate::carbsPerUnit
        val startTime = com.nlefler.glucloser.a.models.BolusRate::startTime

        fun ratesForID(id: String): RawQuery {
            return object: RawQuery {
                override val query = "SELECT (${ratesPrimaryID.name}, ${ordinal.name}, ${carbsPerUnit.name}, ${startTime.name})" +
                        " FROM ${ratesTableName} WHERE ${ratesPrimaryID.name} IN (SELECT ${patternTableName}.${rates.name} FROM ${patternTableName} WHERE" +
                        " ${patternTableName}.${primaryID.name} = ${id})"

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

    class Food {
        class ForID: Query {
            val primaryID = com.nlefler.glucloser.a.models.Food::primaryId
            val carbs = com.nlefler.glucloser.a.models.Food::carbs
            val foodName = com.nlefler.glucloser.a.models.Food::foodName
            override val table = com.nlefler.glucloser.a.models.Food::class.simpleName!!
            override val projection = arrayOf(primaryID.name, carbs.name, foodName.name)
            override val whereClause = arrayOf(primaryID.name).toWhereClause()
            override val sortClause = null
            override val orderBy = null
            override val limit = null
            fun getPrimaryID(cursor: Cursor): String {
                return cursor.getString(cursor.getColumnIndex(primaryID.name))
            }
            fun getCarbs(cursor: Cursor): Int {
                return cursor.getInt(cursor.getColumnIndex(carbs.name))
            }
            fun getFoodName(cursor: Cursor): String {
                return cursor.getString(cursor.getColumnIndex(foodName.name))
            }
        }
    }

    class Place {
        open class Base: Query {
            protected val tableName = com.nlefler.glucloser.a.models.Place::class.simpleName!!
            protected val primaryID = com.nlefler.glucloser.a.models.Place::primaryId
            protected val foursquareID = com.nlefler.glucloser.a.models.Place::foursquareId
            protected val name = com.nlefler.glucloser.a.models.Place::name
            protected  val latitude = com.nlefler.glucloser.a.models.Place::latitude
            protected val longitude = com.nlefler.glucloser.a.models.Place::longitude
            protected val visitCount = com.nlefler.glucloser.a.models.Place::visitCount
            override val table = tableName
            override val projection = arrayOf(primaryID.name, foursquareID.name, name.name, latitude.name,
                    longitude.name, visitCount.name)
            override val whereClause: String? = null
            override val sortClause = null
            override val orderBy: String? = null
            override val limit: Int? = null
            fun getID(cursor: Cursor): String {
                return cursor.getString(cursor.getColumnIndex(primaryID.name))
            }
            fun getFoursquareID(cursor: Cursor): String {
                return cursor.getString(cursor.getColumnIndex(foursquareID.name))
            }
            fun getName(cursor: Cursor): String {
                return cursor.getString(cursor.getColumnIndex(name.name))
            }
            fun getLatitude(cursor: Cursor): Float {
                return cursor.getFloat(cursor.getColumnIndex(latitude.name))
            }
            fun getLongitude(cursor: Cursor): Float {
                return cursor.getFloat(cursor.getColumnIndex(longitude.name))
            }
            fun getVisitCount(cursor: Cursor): Int {
                return cursor.getInt(cursor.getColumnIndex(visitCount.name))
            }
        }
        class ForID: Base() {
            override val whereClause = arrayOf(primaryID.name).toWhereClause()
        }
        class ForFoursquareID: Base() {
            override val whereClause = arrayOf(foursquareID.name).toWhereClause()
        }
        class MostUsed(val count: Int): Base() {
            override val orderBy = "${visitCount.name} DESC"
            override val limit = count
        }
    }

    open class Meal {
        protected val tableName = com.nlefler.glucloser.a.models.Meal::class.simpleName!!
        protected val primaryID = com.nlefler.glucloser.a.models.Meal::primaryId
        protected val date = com.nlefler.glucloser.a.models.Meal::date
        protected val bolusPattern = com.nlefler.glucloser.a.models.Meal::bolusPattern
        protected val carbs = com.nlefler.glucloser.a.models.Meal::carbs
        protected val insulin = com.nlefler.glucloser.a.models.Meal::insulin
        protected val beforeSugar = com.nlefler.glucloser.a.models.Meal::beforeSugar
        protected val isCorrection = com.nlefler.glucloser.a.models.Meal::isCorrection
        protected val foods = com.nlefler.glucloser.a.models.Meal::foods
        protected val place = com.nlefler.glucloser.a.models.Meal::place

        protected val bolusPatternID = "bolusPatternID"
        protected val beforeSugarID = "beforeSugarID"
        protected val foodIDs = "foodIDs"
        protected val placeID = "placeID"

        open class Base: Meal(), Query {
            override val table = tableName
            override val projection = arrayOf(primaryID.name, date.name, bolusPatternID,
                    carbs.name, insulin.name, beforeSugarID, isCorrection.name, foodIDs, placeID)
            override val whereClause: String? = null
            override val sortClause: String? = null
            override val orderBy: String? = null
            override val limit: Int? = null
        }
        fun forID(): Query {
            return object: Base() {
                override val whereClause = "${primaryID.name} = ?"
            }
        }
        fun PlaceForMeal(mealID: String): RawQuery {
            val placeForID = SQLStmts.Place.ForID()
            val placeIDSelectionClause = "(SELECT ${placeID} FROM ${tableName} WHERE ${primaryID} = ${mealID})"
            val whereClause = placeForID.whereClause.replace("?", placeIDSelectionClause)
            return object: RawQuery {
                override val query = "SELECT ${placeForID.projection.joinToString(", ")} FROM ${placeForID.table}" +
                        " WHERE ${whereClause}"
            }
        }
        fun FoodsForMeal(mealID: String): RawQuery {
            val foodForID = SQLStmts.Food.ForID()
            val foodIDsSelectionClause = "(SELECT ${foodIDs} FROM ${tableName} WHERE ${primaryID} = ${mealID})"
            return object: RawQuery {
                override val query = "SELECT ${foodForID.projection.joinToString(", ")} FROM ${foodForID.table}" +
                        " WHERE ${foodForID.primaryID} IN ${foodIDsSelectionClause})"
            }
        }
        fun BolusPatternForMeal(mealID: String): RawQuery {
            return BolusPattern().ratesForID("(SELECT ${bolusPatternID} FROM ${tableName} WHERE ${primaryID.name} = ${mealID})")
        }
        fun BeforeSugarForMeal(mealID: String): RawQuery {
            return BloodSugar().forID().toRawQuery(arrayOf("(SELECT ${beforeSugarID} FROM ${tableName} WHERE ${primaryID.name} = ${mealID})"))
        }
    }
}
