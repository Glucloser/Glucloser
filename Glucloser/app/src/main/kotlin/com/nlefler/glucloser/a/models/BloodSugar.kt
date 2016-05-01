package com.nlefler.glucloser.a.models

import java.util.*

/**
 * Created by Nathan Lefler on 1/4/15.
 */
data class BloodSugar(
        val primaryId: String = UUID.randomUUID().toString(),
        val value: Int = 0,
        val recordedDate: Date = Date()
    ): DBTable {
    override val tableName = BloodSugar::class.simpleName!!
    override val primaryKeyField = BloodSugar::primaryId.name
}
