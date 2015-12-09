package com.nlefler.glucloser.dataSource.realmmigrations

import android.util.Log
import io.realm.DynamicRealm
import io.realm.FieldAttribute
import io.realm.Realm
import io.realm.RealmMigration

/**
 * Created by nathan on 12/6/15.
 */
class GlucloserRealmMigration : RealmMigration {
    override fun migrate(realm: DynamicRealm?, oldVersion: Long, newVersion: Long) {
        val schema = realm?.schema
        var version = oldVersion

        Log.i("MIGRATE", "Version " + oldVersion)

        if (version == 1L) {
            // Renames 'rate' field to 'carbsPerUnit'
            val rateSchema = schema?.get("BolusRate")
            rateSchema?.
                    addField("carbsPerUnit", Integer::class.java)?.
                    transform { obj -> obj.set("carbsPerUnit", obj.getInt("rate")) }?.
                    removeField("rate")
            version++
        }
        if (version == 2L) {
            schema?.get("BolusRate")?.setRequired("carbsPerUnit", false)
            version++
        }
    }
}