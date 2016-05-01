package com.nlefler.glucloser.a.components

import android.content.Context
import com.nlefler.glucloser.a.GlucloserApplication
import com.nlefler.glucloser.a.actions.LogBolusEventAction
import com.nlefler.glucloser.a.activities.LogBolusEventActivity
import com.nlefler.glucloser.a.activities.MainActivity
import com.nlefler.glucloser.a.components.datafactory.DataFactoryModule
import com.nlefler.glucloser.a.dataSource.*
import com.nlefler.glucloser.a.dataSource.*
import com.nlefler.glucloser.a.dataSource.sync.cairo.CairoServices
import com.nlefler.glucloser.a.dataSource.sync.cairo.services.CairoCollectionService
import com.nlefler.glucloser.a.dataSource.sync.cairo.services.CairoPumpService
import com.nlefler.glucloser.a.dataSource.sync.cairo.services.CairoUserService
import com.nlefler.glucloser.a.db.DBManager
import com.nlefler.glucloser.a.foursquare.FoursquareAuthManager
import com.nlefler.glucloser.a.ui.LoginActivityFragment
import com.nlefler.glucloser.a.ui.MealHistoryViewHolder
import com.nlefler.glucloser.a.ui.PlaceSelectionFragment
import com.nlefler.glucloser.a.ui.PlaceSelectionViewHolder
import com.nlefler.glucloser.a.user.UserManager
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

    fun cairoServices(): CairoServices

    fun inject(client: BloodSugarFactory)
    fun inject(client: BolusEventFactory)
    fun inject(client: BolusPatternFactory)
    fun inject(client: BolusRateFactory)
    fun inject(client: FoodFactory)
    fun inject(client: MealFactory)
    fun inject(client: PlaceFactory)
    fun inject(client: SnackFactory)
    fun inject(client: PumpDataFactory)

    fun bloodSugarFactory(): BloodSugarFactory
    fun bolusEventFactory(): BolusEventFactory
    fun bolusPatternFactory(): BolusPatternFactory
    fun bolusRateFactory(): BolusRateFactory
    fun foodFactory(): FoodFactory
    fun mealFactory(): MealFactory
    fun placeFactory(): PlaceFactory
    fun snackFactory(): SnackFactory
    fun pumpDataFactory(): PumpDataFactory

    fun realmFactory(): DBManager

    fun inject(client: MainActivity)
    fun inject(client: LogBolusEventActivity)
    fun inject(client: LogBolusEventAction)
    fun inject(client: MealHistoryViewHolder)
    fun inject(client: PlaceSelectionFragment)
    fun inject(client: PlaceSelectionViewHolder)
    fun inject(client: LoginActivityFragment)
    fun inject(client: MainActivity.HistoryListFragment)
    fun inject(client: CairoServices)
    fun inject(client: UserManager)

}
