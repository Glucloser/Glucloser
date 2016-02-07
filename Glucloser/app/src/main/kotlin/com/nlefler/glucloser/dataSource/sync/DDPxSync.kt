package com.nlefler.glucloser.dataSource.sync

import android.util.Log
import bolts.Task
import com.nlefler.ddpx.DDPx
import com.nlefler.glucloser.dataSource.PlaceFactory
import com.nlefler.glucloser.models.Place
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by nathan on 1/31/16.
 */
@Singleton
class DDPxSync @Inject constructor(val ddpx: DDPx, val placeFactory: PlaceFactory) {

    init {
        ddpx.connect().continueWith { task ->
            if (task.isFaulted) {
                Log.e(LOG_TAG, task.error.message)
                return@continueWith;
            }
            setupSubs()
        }
    }

    public fun saveModel(collection: String, json: String): Task<Unit> {
        return ddpx.method("addModel", arrayOf(collection, json), null).continueWithTask { task ->
            if (task.isFaulted) {
                val error = Exception(task.error.message)
                return@continueWithTask Task.forError<Unit>(error)
            }
            return@continueWithTask Task.forResult(Unit)
        }
    }

    private fun setupSubs() {
        ddpx.sub(Place.ModelName, null).subscribe { change ->
            Log.v(LOG_TAG, "${change.collection} - ${change.type.name} - ${change.id}")

            // DDP returns modified fields only
            // So in event of 'added' or 'changed':
            // Find object for id
            // If there is none, create it and set fields (should have all fields in this case)
            // Else set fields from change event and save
        }
        // TODO(nl) Subscribe to other collections
    }

    companion object {
        private val LOG_TAG = "DDPxSync"
    }
}
