package com.nlefler.glucloser.a.dataSource.sync.cairo

import com.nlefler.glucloser.a.dataSource.sync.cairo.services.CairoCollectionService
import com.nlefler.glucloser.a.dataSource.sync.cairo.services.CairoPumpService
import com.nlefler.glucloser.a.dataSource.sync.cairo.services.CairoUserService
import okhttp3.*
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Created by nathan on 4/10/16.
 */
class CairoServices {
    private var token: String? = null

    private val httpClient = OkHttpClient.Builder().addInterceptor { chain ->
        val request = chain.request()
        val authedRequest = Request.Builder()
                .headers(request.headers())
                .cacheControl(request.cacheControl())
                .method(request.method(), request.body())
                .url(request.url())
                .addHeader("Authorization", token)
                .build()

        val response = chain.proceed(authedRequest)
        token = response.header("Authorization")
        response
    }.build()
    private val retrofit = Retrofit.Builder()
            .client(httpClient)
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create())
            .baseUrl("https://cairo.glucloser.com/")
            .build()

    fun userService(): CairoUserService {
        return retrofit.create(CairoUserService::class.java)
    }

    fun collectionService(): CairoCollectionService {
        return retrofit.create(CairoCollectionService::class.java)
    }

    fun pumpService(): CairoPumpService {
        return retrofit.create(CairoPumpService::class.java)
    }
}
