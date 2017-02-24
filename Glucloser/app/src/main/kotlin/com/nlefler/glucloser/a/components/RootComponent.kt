package com.nlefler.glucloser.a.components

import android.content.Context
import com.nlefler.glucloser.a.GlucloserApplication
import com.nlefler.glucloser.a.activities.LogBolusEventActivity
import com.nlefler.glucloser.a.activities.MainActivity
import com.nlefler.glucloser.a.activities.PlaceSelectionActivity
import com.nlefler.glucloser.a.components.datafactory.DataFactoryModule
import com.nlefler.glucloser.a.dataSource.*
import com.nlefler.glucloser.a.dataSource.*
import com.nlefler.glucloser.a.dataSource.jsonAdapter.BolusPatternJsonAdapter
import com.nlefler.glucloser.a.dataSource.jsonAdapter.BolusRateJsonAdapter
import com.nlefler.glucloser.a.dataSource.sync.cairo.CairoServices
import com.nlefler.glucloser.a.dataSource.sync.cairo.services.CairoCollectionService
import com.nlefler.glucloser.a.dataSource.sync.cairo.services.CairoPumpService
import com.nlefler.glucloser.a.dataSource.sync.cairo.services.CairoUserService
import com.nlefler.glucloser.a.db.DBManager
import com.nlefler.glucloser.a.foursquare.FoursquareAuthManager
import com.nlefler.glucloser.a.models.json.BolusRateJson
import com.nlefler.glucloser.a.models.parcelable.BolusPatternParcelable
import com.nlefler.glucloser.a.models.parcelable.MealParcelable
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

    fun inject(client: BolusPatternJsonAdapter)
    fun bolusPatternJsonAdapter(): BolusPatternJsonAdapter

//    fun bolusRateJsonAdapter(): BolusRateJsonAdapter

    fun inject(client: BolusPatternParcelable)
    fun bolusPatternParcelable(): BolusPatternParcelable

    fun inject(client: MealParcelable)
    fun mealParcelable(): MealParcelable

    fun bloodSugarFactory(): BloodSugarFactory
    fun mealFactory(): MealFactory
    fun bolusPatternFactory(): BolusPatternFactory
    fun bolusRateFactory(): BolusRateFactory
    fun foodFactory(): FoodFactory
    fun placeFactory(): PlaceFactory

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
