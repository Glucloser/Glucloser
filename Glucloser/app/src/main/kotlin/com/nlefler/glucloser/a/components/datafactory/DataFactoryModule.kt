package com.nlefler.glucloser.a.components.datafactory

import com.nlefler.glucloser.a.GlucloserApplication
import com.nlefler.glucloser.a.R
import com.nlefler.glucloser.a.dataSource.PlaceFactory
import com.nlefler.glucloser.a.dataSource.RealmManager
import com.nlefler.glucloser.a.dataSource.realmmigrations.GlucloserRealmMigration
import dagger.Module
import dagger.Provides
import io.realm.Realm
import io.realm.RealmConfiguration
import javax.inject.Singleton

/**
 * Created by nathan on 10/20/15.
 */
@Module
class DataFactoryModule() {

    @Provides fun realmFactory(): RealmManager {
        return RealmManager()
    }

}
