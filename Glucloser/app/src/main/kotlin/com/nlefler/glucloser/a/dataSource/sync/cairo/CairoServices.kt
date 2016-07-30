package com.nlefler.glucloser.a.dataSource.sync.cairo

import android.content.Context
import com.facebook.android.crypto.keychain.SharedPrefsBackedKeyChain
import com.facebook.crypto.Crypto
import com.facebook.crypto.util.SystemNativeCryptoLibrary
import com.nlefler.glucloser.a.db.DBManager
import com.nlefler.glucloser.a.dataSource.jsonAdapter.DateJsonAdapter
import com.nlefler.glucloser.a.dataSource.sync.cairo.services.CairoCollectionService
import com.nlefler.glucloser.a.dataSource.sync.cairo.services.CairoPumpService
import com.nlefler.glucloser.a.dataSource.sync.cairo.services.CairoUserService
import com.nlefler.glucloser.a.models.*
import com.nlefler.glucloser.a.util.EncryptedPrefsStorageHelper
import com.squareup.moshi.Moshi
import okhttp3.*
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Inject

/**
 * Created by nathan on 4/10/16.
 */
class CairoServices @Inject constructor(val ctx: Context, val dbManager: DBManager) {

    companion object {
        private val SHARED_PREFS_NAME = "com.nlefler.glucloser.a.cairoservices"
        private val SHARED_PREFS_TOKEN_KEY = "com.nlefler.glucloser.a.cairoservices.token"
        private val CONCEAL_ENTITY_NAME = "com.nlefler.glucloser.a.cairoservices.concealentity"
    }

    private val crypto = Crypto(SharedPrefsBackedKeyChain(ctx), SystemNativeCryptoLibrary())
    private val storageHelper = EncryptedPrefsStorageHelper(crypto, ctx, SHARED_PREFS_NAME, CONCEAL_ENTITY_NAME)

    private var token: String = storageHelper.fetch(SHARED_PREFS_TOKEN_KEY) ?: ""

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
        storageHelper.store(SHARED_PREFS_TOKEN_KEY, token)
        response
    }.build()

    private val moshi = Moshi.Builder()
            .add(DateJsonAdapter())
            .build()

    private val retrofit = Retrofit.Builder()
            .client(httpClient)
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl("https://cairo.glucloser.com/")
            .build()

    fun clearAuthentication() {
        storageHelper.store(SHARED_PREFS_TOKEN_KEY, "")
    }

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
