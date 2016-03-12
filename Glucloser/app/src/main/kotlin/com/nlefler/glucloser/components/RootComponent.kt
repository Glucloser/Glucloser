package com.nlefler.glucloser.components

import android.content.Context
import com.nlefler.ddpx.DDPx
import com.nlefler.glucloser.GlucloserApplication
import com.nlefler.glucloser.actions.LogBolusEventAction
import com.nlefler.glucloser.activities.LogBolusEventActivity
import com.nlefler.glucloser.activities.MainActivity
import com.nlefler.glucloser.components.datafactory.DataFactoryModule
import com.nlefler.glucloser.dataSource.*
import com.nlefler.glucloser.dataSource.sync.DDPxSync
import com.nlefler.glucloser.foursquare.FoursquareAuthManager
import com.nlefler.glucloser.ui.LoginActivityFragment
import com.nlefler.glucloser.ui.MealHistoryViewHolder
import com.nlefler.glucloser.ui.PlaceSelectionFragment
import com.nlefler.glucloser.ui.PlaceSelectionViewHolder
import com.nlefler.glucloser.user.UserManager
import dagger.Component
import javax.inject.Singleton

/**
 * Created by nathan on 2/21/16.
 */
@Singleton
@Component(modules = arrayOf(GlucloserApplication::class, DataFactoryModule::class))
interface RootComponent {

    fun appContext(): Context

    fun userManager(): UserManager

    fun foursquareAuthManager(): FoursquareAuthManager

    fun newDDPx(): (() -> DDPx)

    fun serverSync(): DDPxSync

    fun inject(client: BloodSugarFactory)
    fun inject(client: BolusEventFactory)
    fun inject(client: BolusPatternFactory)
    fun inject(client: BolusRateFactory)
    fun inject(client: FoodFactory)
    fun inject(client: MealFactory)
    fun inject(client: PlaceFactory)
    fun inject(client: SnackFactory)

    fun bloodSugarFactory(): BloodSugarFactory
    fun bolusEventFactory(): BolusEventFactory
    fun bolusPatternFactory(): BolusPatternFactory
    fun bolusRateFactory(): BolusRateFactory
    fun foodFactory(): FoodFactory
    fun mealFactory(): MealFactory
    fun placeFactory(): PlaceFactory
    fun snackFactory(): SnackFactory

    fun realmFactory(): RealmManager


    fun inject(client: MainActivity)
    fun inject(client: LogBolusEventActivity)
    fun inject(client: LogBolusEventAction)
    fun inject(client: MealHistoryViewHolder)
    fun inject(client: PlaceSelectionFragment)
    fun inject(client: PlaceSelectionViewHolder)
    fun inject(client: LoginActivityFragment)
    fun inject(client: MainActivity.HistoryListFragment)

}
