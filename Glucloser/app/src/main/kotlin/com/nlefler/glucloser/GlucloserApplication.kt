package com.nlefler.glucloser

import android.app.Application
import android.content.Context
import android.os.Debug
import android.support.multidex.MultiDex
import android.util.Log
import com.nlefler.glucloser.components.datafactory.DaggerDataFactoryComponent
import com.nlefler.glucloser.dataSource.realmmigrations.GlucloserRealmMigration
import io.realm.Realm
import io.realm.RealmConfiguration

/**
 * Created by Nathan Lefler on 12/12/14.
 */
public class GlucloserApplication : Application() {
    override fun attachBaseContext(context: Context) {
        super.attachBaseContext(context)

        MultiDex.install(this)

    }

    override fun onCreate() {
        super.onCreate()
//        LeakCanary.install(this);
        _sharedApplication = this

        if (!Debug.isDebuggerConnected()) {
            // Enable crash reporting
        }

        val realmConfig = RealmConfiguration.Builder(GlucloserApplication.SharedApplication())
                .name("myrealm.realm")
                .migration(GlucloserRealmMigration())
                .schemaVersion(4)
                .build();
        Realm.setDefaultConfiguration(realmConfig)


        this.subscribeToPush()

        val dataFactory = DaggerDataFactoryComponent.create()
        var startupAction = dataFactory.startupAction()
        startupAction.run()
    }

    private fun subscribeToPush() {
    }

    companion object {
        private val LOG_TAG = "GlucloserApplication"

        private var _sharedApplication: GlucloserApplication? = null

        @Synchronized
        public fun SharedApplication(): GlucloserApplication {
            return _sharedApplication!!
        }
    }
}
