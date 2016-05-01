package com.nlefler.glucloser.a.models

import java.util.*

/**
 * Created by Nathan Lefler on 5/16/15.
 */
data class Food(
        val primaryId: String = UUID.randomUUID().toString(),
        val carbs: Int = 0,
        val foodName: String = ""
    ) : DBTable {
    override val tableName = Food::class.simpleName!!
    override val primaryKeyField = Food::primaryId.name
}
