package com.nlefler.glucloser.a

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Debug
import android.support.multidex.MultiDex
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.wearable.DataApi
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.nlefler.glucloser.a.R
import com.nlefler.glucloser.a.components.DaggerRootComponent
import com.nlefler.glucloser.a.dataSource.sync.cairo.CairoServices
import com.nlefler.glucloser.a.dataSource.sync.cairo.services.CairoCollectionService
import com.nlefler.glucloser.a.dataSource.sync.cairo.services.CairoPumpService
import com.nlefler.glucloser.a.dataSource.sync.cairo.services.CairoUserService
import com.nlefler.glucloser.a.foursquare.FoursquareAuthManager
import com.nlefler.glucloser.a.push.PushRegistrationIntentService
import com.nlefler.glucloser.a.user.UserManager
import dagger.Module
import dagger.Provides
import rx.Observable
import rx.Subscription
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Created by Nathan Lefler on 12/12/14.
 */
@Module
class GlucloserApplication : Application(),
        DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private val SENSOR_READING_KEY = "com.nlefler.glucloser.a.datakey.sensor_reading"

    private var googleApiClient: GoogleApiClient? = null
    private var sensorSub: Subscription? = null

    val rootComponent = DaggerRootComponent.builder().glucloserApplication(this).build()

    override fun attachBaseContext(context: Context) {
        super.attachBaseContext(context)

        MultiDex.install(this)

        com.nlefler.glucloser.a.GlucloserApplication.Companion.sharedApplication = this
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

    override fun onDataChanged(p0: DataEventBuffer?) {

    }

    override fun onConnected(p0: Bundle?) {
        sensorSub?.unsubscribe()
        sensorSub = Observable.interval(10, TimeUnit.MINUTES).doOnNext {
            val putDataMapReq = PutDataMapRequest.create("/sensor_reading")
            putDataMapReq.dataMap.putInt(SENSOR_READING_KEY, 999)
            val putDataReq = putDataMapReq.asPutDataRequest()
            val pendingResult = Wearable.DataApi.putDataItem(googleApiClient, putDataReq)
        }.subscribe()
    }

    override fun onConnectionSuspended(p0: Int) {
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        sensorSub?.unsubscribe()
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
