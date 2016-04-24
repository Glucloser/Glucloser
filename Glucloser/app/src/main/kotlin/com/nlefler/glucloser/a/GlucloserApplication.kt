package com.nlefler.glucloser.a

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Debug
import android.support.multidex.MultiDex
import com.nlefler.glucloser.a.R
import com.nlefler.glucloser.a.components.DaggerRootComponent
import com.nlefler.glucloser.a.dataSource.realmmigrations.GlucloserRealmMigration
import com.nlefler.glucloser.a.dataSource.sync.cairo.CairoServices
import com.nlefler.glucloser.a.dataSource.sync.cairo.services.CairoCollectionService
import com.nlefler.glucloser.a.dataSource.sync.cairo.services.CairoPumpService
import com.nlefler.glucloser.a.dataSource.sync.cairo.services.CairoUserService
import com.nlefler.glucloser.a.foursquare.FoursquareAuthManager
import com.nlefler.glucloser.a.push.PushRegistrationIntentService
import com.nlefler.glucloser.a.user.UserManager
import dagger.Module
import dagger.Provides
import io.realm.Realm
import io.realm.RealmConfiguration
import javax.inject.Singleton

/**
 * Created by Nathan Lefler on 12/12/14.
 */
@Module
class GlucloserApplication : Application() {
    val rootComponent = DaggerRootComponent.builder().glucloserApplication(this).build()

    override fun attachBaseContext(context: Context) {
        super.attachBaseContext(context)

        MultiDex.install(this)

        com.nlefler.glucloser.a.GlucloserApplication.Companion.sharedApplication = this

        val realmConfig = RealmConfiguration.Builder(this)
                .name("myrealm.realm")
                .migration(GlucloserRealmMigration())
                .schemaVersion(4)
                .build();
        Realm.setDefaultConfiguration(realmConfig)
    }

    override fun onCreate() {
        super.onCreate()
//        LeakCanary.install(this);

        if (!Debug.isDebuggerConnected()) {
            // Enable crash reporting
        }

        this.subscribeToPush()

    }

    private fun subscribeToPush() {
        startService(Intent(this, PushRegistrationIntentService::class.java))
    }

    // ContextComponent
    @Provides
    fun appContext(): Context {
        return this
    }

    companion object {
        private val LOG_TAG = "GlucloserApplication"

        var sharedApplication: com.nlefler.glucloser.a.GlucloserApplication? = null
        private set
    }
}
