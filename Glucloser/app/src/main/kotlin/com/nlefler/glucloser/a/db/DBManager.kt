package com.nlefler.glucloser.a.db

import android.content.Context
import com.nlefler.glucloser.a.models.Models
import io.requery.Persistable
import io.requery.android.sqlite.DatabaseSource
import io.requery.sql.*
import java.sql.Statement
import javax.inject.Inject
import javax.inject.Singleton
import javax.sql.DataSource

/**
 * Created by nathan on 12/16/15.
 */
@Singleton
class DBManager @Inject constructor(ctx: Context) {

    val dataSource: DatabaseSource = DatabaseSource(ctx, Models.DEFAULT, 1)
    val data: KotlinEntityDataStore<Persistable>
    init {
        dataSource.setLoggingEnabled(true)
        data = KotlinEntityDataStore<Persistable>(dataSource.configuration)
    }

    companion object {
        val LOG_TAG = "RealmManager"
    }

}
