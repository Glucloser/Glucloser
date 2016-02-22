package com.nlefler.glucloser.components.context

import android.content.Context
import com.nlefler.glucloser.GlucloserApplication
import dagger.Component
import dagger.Subcomponent
import javax.inject.Singleton

/**
 * Created by nathan on 12/13/15.
 */
@Subcomponent(modules = arrayOf(GlucloserApplication::class))
interface ContextComponent {

    fun appContext(): Context

}