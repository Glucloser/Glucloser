package com.nlefler.glucloser.components.datafactory

import com.nlefler.ddpx.DDPx
import com.nlefler.glucloser.GlucloserApplication
import com.nlefler.glucloser.R
import com.nlefler.glucloser.dataSource.PlaceFactory
import com.nlefler.glucloser.dataSource.RealmManager
import com.nlefler.glucloser.dataSource.realmmigrations.GlucloserRealmMigration
import com.nlefler.glucloser.dataSource.sync.DDPxSync
import dagger.Module
import dagger.Provides
import io.realm.Realm
import io.realm.RealmConfiguration
import javax.inject.Singleton

/**
 * Created by nathan on 10/20/15.
 */
@Module
public class DataFactoryModule(val placeFactory: PlaceFactory) {
    private lateinit var ddpxSync: DDPxSync

    init {
        val ddpx = DDPx(GlucloserApplication.SharedApplication().getString(R.string.ddpx_server))
        ddpxSync = DDPxSync(ddpx, placeFactory)
    }

    @Provides public fun realmFactory(): RealmManager {
        return RealmManager()
    }

    @Singleton @Provides public fun serverSync(): DDPxSync {
        return ddpxSync
    }
}
