package com.nlefler.glucloser

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Debug
import android.support.multidex.MultiDex
import com.nlefler.ddpx.DDPx
import com.nlefler.glucloser.components.DaggerRootComponent
import com.nlefler.glucloser.dataSource.realmmigrations.GlucloserRealmMigration
import com.nlefler.glucloser.dataSource.sync.DDPxSync
import com.nlefler.glucloser.foursquare.FoursquareAuthManager
import com.nlefler.glucloser.push.PushRegistrationIntentService
import com.nlefler.glucloser.user.UserManager
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
    lateinit var ddpx: DDPx

    val rootComponent = DaggerRootComponent.builder().glucloserApplication(this).build()

    private var ddpxSync: DDPxSync? = null
    private var foursquareAuthManager: FoursquareAuthManager? = null
    private var userManager: UserManager? = null

    override fun attachBaseContext(context: Context) {
        super.attachBaseContext(context)

        MultiDex.install(this)

        sharedApplication = this

        val realmConfig = RealmConfiguration.Builder(this)
                .name("myrealm.realm")
                .migration(GlucloserRealmMigration())
                .schemaVersion(4)
                .build();
        Realm.setDefaultConfiguration(realmConfig)

        ddpx = DDPx(getString(R.string.ddpx_server))
        ddpxSync = rootComponent.serverSync()
        userManager = UserManager(ddpxSync!!, this)
        foursquareAuthManager = rootComponent.foursquareAuthManager()
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

    // DataFactoryComponent
//    @Provides @Singleton
//    fun serverSync(): DDPxSync {
//        return ddpxSync!!
//    }

    // AuthAndIdentityComponent
    @Provides
    fun userManager(): UserManager {
        return userManager!!
    }

    @Provides
    fun ddpx(): DDPx {
        return ddpx
    }

    companion object {
        private val LOG_TAG = "GlucloserApplication"

        var sharedApplication: GlucloserApplication? = null
        private set
    }
}
