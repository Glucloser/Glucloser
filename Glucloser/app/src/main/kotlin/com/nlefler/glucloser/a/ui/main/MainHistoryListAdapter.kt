package com.nlefler.glucloser.a.ui.main

import android.content.Context
import android.database.DataSetObserver
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListAdapter
import com.nlefler.glucloser.a.R
import com.nlefler.glucloser.a.models.BolusEvent
import io.requery.query.Result
import rx.Observable
import java.util.*

/**
 * Created by nathan on 8/5/16.
 */

class MainHistoryListAdapter(val ctx: Context, val resultObservable: Observable<List<BolusEvent>>): ListAdapter {

    var results: List<BolusEvent>? = null
    var observers = ArrayList<DataSetObserver>()
    val layoutInflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    init {
        resultObservable.subscribe { list ->
            results = list
            observers.forEach { o -> o.onChanged() }
        }
    }
    override fun isEnabled(p0: Int): Boolean {
        // No seperators
        return true
    }

    override fun areAllItemsEnabled(): Boolean {
        return true
    }

    override fun getView(idx: Int, existingView: View?, parent: ViewGroup?): View? {
        var view = existingView
        if (view == null) {
            view = layoutInflater.inflate(R.id.main_history_list_item, parent, false)
        }

        return view
    }

    override fun getItem(idx: Int): Any? {
        return results?.get(idx)
    }

    override fun getCount(): Int {
        return results?.count() ?: 0
    }

    override fun isEmpty(): Boolean {
        return results?.isEmpty() ?: true
    }

    override fun registerDataSetObserver(ob: DataSetObserver?) {
        if (ob == null) {
            return
        }
        observers.add(ob)
    }

    override fun getItemViewType(idx: Int): Int {
        return R.id.main_history_list_item
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getItemId(idx: Int): Long {
        return results?.get(idx)?.primaryId?.hashCode()?.toLong() ?: 0
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun unregisterDataSetObserver(ob: DataSetObserver?) {
        if (ob == null) {
            return
        }
        observers.remove(ob)
    }
}

