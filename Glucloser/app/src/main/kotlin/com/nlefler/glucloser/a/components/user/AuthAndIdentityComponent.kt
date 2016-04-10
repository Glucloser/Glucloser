//package com.nlefler.glucloser.components.user
//
//import com.nlefler.glucloser.GlucloserApplication
//import com.nlefler.glucloser.actions.LogBolusEventAction
//import com.nlefler.glucloser.activities.LogBolusEventActivity
//import com.nlefler.glucloser.activities.MainActivity
//import com.nlefler.glucloser.foursquare.FoursquareAuthManager
//import com.nlefler.glucloser.ui.MealHistoryViewHolder
//import com.nlefler.glucloser.ui.PlaceSelectionFragment
//import com.nlefler.glucloser.ui.PlaceSelectionViewHolder
//import com.nlefler.glucloser.user.UserManager
//import dagger.Subcomponent
//import javax.inject.Singleton
//
///**
// * Created by nathan on 2/17/16.
// */
//@Subcomponent(modules = arrayOf(GlucloserApplication::class))
//interface AuthAndIdentityComponent {
//    fun userManager(): UserManager
//
//    fun foursquareAuthManager(): FoursquareAuthManager
//
//    fun inject(client: MainActivity)
//    fun inject(client: LogBolusEventActivity)
//    fun inject(client: LogBolusEventAction)
//    fun inject(client: MealHistoryViewHolder)
//    fun inject(client: PlaceSelectionFragment)
//    fun inject(client: PlaceSelectionViewHolder)
//
//}
