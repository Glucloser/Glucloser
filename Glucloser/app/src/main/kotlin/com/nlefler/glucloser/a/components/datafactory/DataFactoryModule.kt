package com.nlefler.glucloser.a.components.datafactory

import android.content.Context
import com.nlefler.glucloser.a.db.DBManager
import dagger.Module
import dagger.Provides

/**
 * Created by nathan on 10/20/15.
 */
@Module
class DataFactoryModule() {

    @Provides fun dbFactory(ctx: Context): DBManager {
        return DBManager(ctx)
    }

}
