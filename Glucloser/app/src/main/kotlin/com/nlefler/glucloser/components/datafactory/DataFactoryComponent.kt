package com.nlefler.glucloser.components.datafactory

import com.nlefler.ddpx.DDPx
import com.nlefler.glucloser.GlucloserApplication
import com.nlefler.glucloser.actions.LogBolusEventAction
import com.nlefler.glucloser.actions.StartupAction
import com.nlefler.glucloser.activities.LogBolusEventActivity
import com.nlefler.glucloser.activities.MainActivity
import com.nlefler.glucloser.dataSource.*
import com.nlefler.glucloser.dataSource.sync.DDPxSync
import com.nlefler.glucloser.ui.MealHistoryViewHolder
import com.nlefler.glucloser.ui.PlaceSelectionFragment
import com.nlefler.glucloser.ui.PlaceSelectionViewHolder
import dagger.Component
import dagger.Subcomponent
import io.realm.Realm
import javax.inject.Singleton

/**
 * Created by nathan on 10/20/15.
 */
@Subcomponent(modules = arrayOf(DataFactoryModule::class))
interface DataFactoryComponent {
    fun inject(client: BloodSugarFactory)
    fun inject(client: BolusEventFactory)
    fun inject(client: BolusPatternFactory)
    fun inject(client: BolusRateFactory)
    fun inject(client: FoodFactory)
    fun inject(client: MealFactory)
    fun inject(client: PlaceFactory)
    fun inject(client: SnackFactory)
    fun inject(client: MainActivity)
    fun inject(client: LogBolusEventActivity)
    fun inject(client: LogBolusEventAction)
    fun inject(client: StartupAction)
    fun inject(client: MealHistoryViewHolder)
    fun inject(client: PlaceSelectionFragment)
    fun inject(client: PlaceSelectionViewHolder)

    fun startupAction(): StartupAction
    fun bloodSugarFactory(): BloodSugarFactory
    fun bolusEventFactory(): BolusEventFactory
    fun bolusPatternFactory(): BolusPatternFactory
    fun bolusRateFactory(): BolusRateFactory
    fun foodFactory(): FoodFactory
    fun mealFactory(): MealFactory
    fun placeFactory(): PlaceFactory
    fun snackFactory(): SnackFactory

    fun realmFactory(): RealmManager

}
