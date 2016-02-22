package com.nlefler.glucloser.components

import com.nlefler.glucloser.GlucloserApplication
import com.nlefler.glucloser.components.context.ContextComponent
import com.nlefler.glucloser.components.datafactory.DataFactoryComponent
import com.nlefler.glucloser.components.sync.SyncComponent
import com.nlefler.glucloser.components.user.AuthAndIdentityComponent
import dagger.Component
import javax.inject.Singleton

/**
 * Created by nathan on 2/21/16.
 */
@Singleton
@Component(modules = arrayOf(GlucloserApplication::class))
interface RootComponent {
    @Singleton
    fun contextComponent(): ContextComponent

    @Singleton
    fun dataFactoryComponent(): DataFactoryComponent

    @Singleton
    fun authAndIdentityComponent(): AuthAndIdentityComponent

    @Singleton
    fun syncComponent(): SyncComponent
}
