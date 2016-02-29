//package com.nlefler.glucloser.components.sync
//
//import com.nlefler.ddpx.DDPx
//import com.nlefler.glucloser.GlucloserApplication
//import com.nlefler.glucloser.components.user.AuthAndIdentityComponent
//import com.nlefler.glucloser.dataSource.sync.DDPxSync
//import dagger.Subcomponent
//import javax.inject.Singleton
//
///**
// * Created by nathan on 2/17/16.
// */
//@Subcomponent(modules = arrayOf(GlucloserApplication::class))
//interface SyncComponent {
//    fun ddpx(): DDPx
//
//    fun serverSync(): DDPxSync
//
//    fun authAndIdentityComponent(): AuthAndIdentityComponent
//
//}