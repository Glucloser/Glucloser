package com.nlefler.glucloser.components.datafactory

import com.nlefler.glucloser.GlucloserApplication
import com.nlefler.glucloser.dataSource.realmmigrations.GlucloserRealmMigration
import dagger.Module
import dagger.Provides
import io.realm.Realm
import io.realm.RealmConfiguration
import javax.inject.Singleton

/**
 * Created by nathan on 10/20/15.
 */
@Module
public class DataFactoryModule {
    @Provides public fun realm(): Realm {
        return Realm.getDefaultInstance()
    }
}
