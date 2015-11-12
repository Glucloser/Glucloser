package com.nlefler.glucloser

import android.app.Application
import android.content.Context
import android.os.Debug
import android.support.multidex.MultiDex
import android.util.Log
import com.nlefler.glucloser.components.datafactory.DaggerDataFactoryComponent
import com.parse.*
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
            ParseCrashReporting.enable(this)
        }

        Parse.initialize(this, this.getString(R.string.parse_app_id), this.getString(R.string.parse_client_key))
        ParseInstallation.getCurrentInstallation().saveInBackground()

        this.subscribeToPush()

        val realmConfig = RealmConfiguration.Builder(this)
        .name("myrealm.realm")
//        .encryptionKey(getKey())
        .schemaVersion(1)
//        .migration(new MyMigration())
        .build();

        Realm.setDefaultConfiguration(realmConfig);

        val dataFactory = DaggerDataFactoryComponent.create()
        var startupAction = dataFactory.startupAction()
        startupAction.run()
    }

    private fun subscribeToPush() {
        ParsePush.subscribeInBackground("", {e: ParseException? ->
            if (e == null) {
                Log.d(LOG_TAG, "successfully subscribed to the broadcast channel.")
            } else {
                Log.e(LOG_TAG, "failed to subscribe for push", e)
            }
        })

        ParsePush.subscribeInBackground("foursquareCheckin", {e: ParseException? ->
            if (e == null) {
                Log.d("com.parse.push", "successfully subscribed to the checkin channel.")
            } else {
                Log.e("com.parse.push", "failed to subscribe for push", e)
            }
        })

        ParsePush.subscribeInBackground("comcon", {e: ParseException? ->
            if (e == null) {
                Log.d("com.parse.push", "successfully subscribed to the comcon channel.")
            } else {
                Log.e("com.parse.push", "failed to subscribe for push", e)
            }
        })
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
