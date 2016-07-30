package com.nlefler.glucloser.a.db

import android.content.Context
import com.nlefler.glucloser.a.models.Models
import io.requery.Persistable
import io.requery.android.sqlite.DatabaseSource
import io.requery.sql.KotlinEntityDataStore
import javax.inject.Inject

/**
 * Created by nathan on 12/16/15.
 */
class DBManager @Inject constructor(val ctx: Context) {

    val dataSource: DatabaseSource = DatabaseSource(ctx, Models.DEFAULT, 1)
    val data: KotlinEntityDataStore<Persistable> = KotlinEntityDataStore<Persistable>(dataSource.configuration)

    companion object {
        val LOG_TAG = "RealmManager"
    }
}
