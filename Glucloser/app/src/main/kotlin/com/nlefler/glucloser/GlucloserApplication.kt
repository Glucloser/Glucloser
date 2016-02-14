package com.nlefler.glucloser

import android.app.Application
import android.content.Context
import android.os.Debug
import android.support.multidex.MultiDex
import android.util.Log
import com.nlefler.ddpx.DDPx
import com.nlefler.glucloser.components.datafactory.DaggerDataFactoryComponent
import com.nlefler.glucloser.components.datafactory.DataFactoryModule
import com.nlefler.glucloser.dataSource.realmmigrations.GlucloserRealmMigration
import com.nlefler.glucloser.dataSource.sync.DDPxSync
import io.realm.Realm
import io.realm.RealmConfiguration

/**
 * Created by Nathan Lefler on 12/12/14.
 */
public class GlucloserApplication : Application() {
    lateinit var ddpx: DDPx

    val dataFactory = DaggerDataFactoryComponent.builder()
            .dataFactoryModule(DataFactoryModule())
            .build()

    private var ddpxSync: DDPxSync? = null

    override fun attachBaseContext(context: Context) {
        super.attachBaseContext(context)

        MultiDex.install(this)

        _sharedApplication = this
        ddpx = DDPx(GlucloserApplication.SharedApplication().getString(R.string.ddpx_server))
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

        var startupAction = dataFactory.startupAction()
        startupAction.run()

        val ddpx = DDPx(getString(R.string.ddpx_server))
        val placeFactory = dataFactory.placeFactory()
        ddpxSync = DDPxSync(ddpx, placeFactory)
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
