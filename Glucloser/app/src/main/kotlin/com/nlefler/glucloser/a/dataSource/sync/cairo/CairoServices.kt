package com.nlefler.glucloser.a.dataSource.sync.cairo

import com.nlefler.glucloser.a.dataSource.sync.cairo.services.CairoUserService
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Created by nathan on 4/10/16.
 */
class CairoServices {
    private val retrofit = Retrofit.Builder()
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create())
            .baseUrl("https://cairo.glucloser.com/")
            .build()

    fun userService(): CairoUserService {
        return retrofit.create(CairoUserService::class.java)
    }
}
