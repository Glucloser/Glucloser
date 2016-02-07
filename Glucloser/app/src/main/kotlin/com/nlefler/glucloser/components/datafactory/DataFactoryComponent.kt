package com.nlefler.glucloser.components.datafactory

import com.nlefler.ddpx.DDPx
import com.nlefler.glucloser.actions.LogBolusEventAction
import com.nlefler.glucloser.actions.StartupAction
import com.nlefler.glucloser.activities.LogBolusEventActivity
import com.nlefler.glucloser.dataSource.*
import com.nlefler.glucloser.dataSource.sync.DDPxSync
import com.nlefler.glucloser.ui.MealHistoryViewHolder
import com.nlefler.glucloser.ui.PlaceSelectionViewHolder
import dagger.Component
import io.realm.Realm
import javax.inject.Singleton

/**
 * Created by nathan on 10/20/15.
 */
@Singleton
@Component(modules = arrayOf(DataFactoryModule::class))
interface DataFactoryComponent {
    fun inject(client: BloodSugarFactory)
    fun inject(client: BolusEventFactory)
    fun inject(client: BolusPatternFactory)
    fun inject(client: BolusRateFactory)
    fun inject(client: FoodFactory)
    fun inject(client: MealFactory)
    fun inject(client: PlaceFactory)
    fun inject(client: SnackFactory)
    fun inject(client: LogBolusEventActivity)
    fun inject(client: LogBolusEventAction)
    fun inject(client: StartupAction)
    fun inject(client: MealHistoryViewHolder)
    fun inject(client: PlaceSelectionViewHolder)
    fun inject(client: DDPxSync)

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

    @Singleton
    fun serverSync(): DDPxSync

    @Singleton
    fun ddpx(): DDPx
}
