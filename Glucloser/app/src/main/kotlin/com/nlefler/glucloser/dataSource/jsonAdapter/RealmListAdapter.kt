package com.nlefler.glucloser.dataSource.jsonAdapter

import com.nlefler.glucloser.dataSource.RealmManager
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import io.realm.RealmList
import io.realm.RealmObject
import java.util.*

/**
 * Created by nathan on 2/8/16.
 */
class RealmListAdapter<T: RealmObject>(val realmManager: RealmManager) {
    @FromJson fun fromJson(list: List<T>): RealmList<T> {
        val realmList = RealmList<T>()
        realmList.addAll(list)

        return realmList
    }

    @ToJson fun toJson(realmList: RealmList<T>): List<T> {
        val list = ArrayList<T>()
        list.addAll(realmList)

        return list
    }
}
