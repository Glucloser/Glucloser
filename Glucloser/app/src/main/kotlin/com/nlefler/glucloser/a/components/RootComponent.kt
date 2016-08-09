package com.nlefler.glucloser.a.components

import android.content.Context
import com.nlefler.glucloser.a.GlucloserApplication
import com.nlefler.glucloser.a.activities.LogBolusEventActivity
import com.nlefler.glucloser.a.activities.MainActivity
import com.nlefler.glucloser.a.activities.PlaceSelectionActivity
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
    fun inject(client: BolusPatternFactory)
    fun inject(client: BolusRateFactory)
    fun inject(client: FoodFactory)
    fun inject(client: MealFactory)
    fun inject(client: PlaceFactory)
    fun inject(client: PumpDataFactory)

    fun bloodSugarFactory(): BloodSugarFactory
    fun mealFactory(): MealFactory
    fun bolusPatternFactory(): BolusPatternFactory
    fun bolusRateFactory(): BolusRateFactory
    fun foodFactory(): FoodFactory
    fun placeFactory(): PlaceFactory
    fun pumpDataFactory(): PumpDataFactory

    fun dbFactory(): DBManager
    fun inject(client: DBManager)

    fun inject(client: MainActivity)
    fun inject(client: PlaceSelectionActivity)
    fun inject(client: LogBolusEventActivity)
    fun inject(client: PlaceSelectionViewHolder)
    fun inject(client: LoginActivityFragment)
    fun inject(client: CairoServices)
    fun inject(client: UserManager)

}
