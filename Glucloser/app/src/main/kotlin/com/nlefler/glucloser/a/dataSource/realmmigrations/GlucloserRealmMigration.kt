package com.nlefler.glucloser.a.dataSource.realmmigrations

import android.util.Log
import com.nlefler.glucloser.a.models.Place
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
    }
}