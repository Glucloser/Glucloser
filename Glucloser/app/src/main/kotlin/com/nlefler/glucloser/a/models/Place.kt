package com.nlefler.glucloser.a.models

import java.util.*

/**
 * Created by Nathan Lefler on 12/11/14.
 */
data class Place(
        val primaryId: String = UUID.randomUUID().toString(),
        val name: String = "",
        val foursquareId: String = UUID.randomUUID().toString(),
        val latitude: Float = 0f,
        val longitude: Float = 0f,
        val visitCount: Int = 0
    ) : DBTable, Syncable {
    override val tableName = Place::class.simpleName!!
    override val primaryKeyField = Place::primaryId.name
}
