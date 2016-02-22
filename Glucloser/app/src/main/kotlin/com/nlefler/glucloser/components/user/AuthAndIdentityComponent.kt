package com.nlefler.glucloser.components.user

import com.nlefler.glucloser.GlucloserApplication
import com.nlefler.glucloser.foursquare.FoursquareAuthManager
import com.nlefler.glucloser.user.UserManager
import dagger.Subcomponent
import javax.inject.Singleton

/**
 * Created by nathan on 2/17/16.
 */
@Subcomponent(modules = arrayOf(GlucloserApplication::class))
interface AuthAndIdentityComponent {
    fun userManager(): UserManager

    fun foursquareAuthManager(): FoursquareAuthManager
}
