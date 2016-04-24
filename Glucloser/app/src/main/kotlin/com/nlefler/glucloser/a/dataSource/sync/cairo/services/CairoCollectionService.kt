package com.nlefler.glucloser.a.dataSource.sync.cairo.services

import com.nlefler.glucloser.a.models.Meal
import com.nlefler.glucloser.a.models.Place
import com.nlefler.glucloser.a.models.Snack
import retrofit2.http.Body
import retrofit2.http.PUT
import rx.Observable

/**
 * Created by nathan on 4/10/16.
 */
interface CairoCollectionService {
   @PUT("collection/places/addPlace")
   fun addPlace(@Body body: Place): Observable<Unit>

   @PUT("collection/meals/addMeal")
   fun addMeal(@Body body: Meal): Observable<Unit>

   @PUT("collection/snacks/addSnack")
   fun addSnack(@Body body: Snack): Observable<Unit>
}
