package com.nlefler.glucloser.dataSource

import bolts.Continuation
import bolts.Task
import com.nlefler.glucloser.models.BolusRate
import com.parse.ParseObject
import com.parse.ParseQuery
import java.util.*

/**
 * Created by nathan on 9/19/15.
 */
public class BolusPatternFactory {

    public fun BolusPatternFromParseObject(parseObj: ParseObject): BolusPattern? {
        if (!parseObj.getClassName().equals(BolusPattern.ParseClassName)) {
            return null;
        }
        val pattern = BolusPattern()
        pattern.rateCount = parseObj.getInt(BolusPattern.ParseKeyRateCount)
        pattern.rates = parseObj.getList(BolusPattern.ParseKeyRates)

        return pattern
    }

    public fun CurrentBolusPattern(): Task<BolusPattern?> {
        val query = ParseQuery<ParseObject>(BolusPattern.ParseClassName)
        query.orderByDescending("updatedAt")
        query.setLimit(1)

        return query.getFirstInBackground().onSuccessTask({ task ->
            // Get all rates
            task.getResult().fetchIfNeededInBackground<ParseObject>()
        }).continueWith({ task ->
            BolusPatternFromParseObject(task.getResult())
        })
    }
}
