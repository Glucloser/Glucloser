package com.nlefler.glucloser.a.ui.main

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import com.nlefler.glucloser.a.GlucloserApplication
import com.nlefler.glucloser.a.R
import com.nlefler.glucloser.a.models.BolusEvent
import com.nlefler.glucloser.a.models.MealEntity
import com.nlefler.glucloser.a.models.SnackEntity
import io.requery.kotlin.desc
import rx.Observable

/**
 * Created by nathan on 8/6/16.
 */

class MainHistoryListFragment constructor(): Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rootView = inflater!!.inflate(R.layout.main_history_fragment, container, false)
        val listView: ListView = rootView.findViewById(R.id.main_list) as ListView

        val dataFactory = GlucloserApplication.sharedApplication?.rootComponent
        val dbManager = dataFactory?.dbFactory()
        val recentMeals: Observable<MealEntity> =
                dbManager?.data?.select(MealEntity::class)
                        ?.orderBy(MealEntity::eatenDate.desc())
                        ?.limit(30)?.get()?.toObservable()
                        ?: Observable.empty()
        val recentSnacks: Observable<SnackEntity> =
                dbManager?.data?.select(SnackEntity::class)
                        ?.orderBy(SnackEntity::eatenDate.desc())
                        ?.limit(30)?.get()?.toObservable()
                        ?: Observable.empty()

        val bolusEvents: Observable<BolusEvent> = Observable.merge(recentMeals, recentSnacks)
        val results: Observable<List<BolusEvent>> = bolusEvents.toSortedList { be1 , be2 ->
            if (!(be1 is BolusEvent) || !(be2 is BolusEvent)) {
                return@toSortedList 0
            }
            return@toSortedList be1.eatenDate.compareTo(be2.eatenDate)
        }
        listView.adapter = MainHistoryListAdapter(context, results)

////            val activity = getActivity();
//
//                    val intent = Intent(rootView.getContext(), LogBolusEventActivity::class.java)
//                    intent.putExtra(LogBolusEventActivity.BolusEventTypeKey, BolusEventType.BolusEventTypeMeal.name)
//
//                    activity.startActivityForResult(intent, LogBolusEventActivityIntentCode)

        return rootView
    }

    companion object {
        private val LOG_TAG = "PlaceholderFragment"
    }
}
