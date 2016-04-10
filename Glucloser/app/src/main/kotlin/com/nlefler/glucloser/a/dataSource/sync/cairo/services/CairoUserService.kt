package com.nlefler.glucloser.a.dataSource.sync.cairo.services

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import rx.Observable

/**
 * Created by nathan on 4/10/16.
 */
interface CairoUserService {
    interface WithSessionBody {
        val sessionID: String
    }

    interface CreateOrLoginBody: WithSessionBody {
        val email: String
    }
    @POST("/user/createOrLogin")
    fun createOrLogin(@Body body: CreateOrLoginBody): Observable<Unit>

    interface SaveFoursquareIDBody: WithSessionBody {
        val token: String
    }
    @PUT("/user/saveFoursquareID")
    fun saveFoursquareID(@Body body: SaveFoursquareIDBody): Observable<Unit>

    interface SavePushTokenBody: WithSessionBody {
        val token: String
    }
    @PUT("/user/savePushToken")
    fun savePushToken(@Body body: SavePushTokenBody): Observable<Unit>
}