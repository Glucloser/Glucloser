package com.nlefler.glucloser.components.datafactory

import dagger.Module
import dagger.Provides
import io.realm.Realm
import javax.inject.Singleton

/**
 * Created by nathan on 10/20/15.
 */
@Module
public class DataFactoryModule {
    @Provides @Singleton public fun realm(): Realm {
        return Realm.getDefaultInstance()
    }
}
