package com.nlefler.glucloser.models

import com.nlefler.glucloser.models.parcelable.BolusEventParcelable

/**
 * Created by Nathan Lefler on 12/24/14.
 */
public interface BolusEventDetailDelegate {
    public fun bolusEventDetailUpdated(bolusEventParcelable: BolusEventParcelable)
}
