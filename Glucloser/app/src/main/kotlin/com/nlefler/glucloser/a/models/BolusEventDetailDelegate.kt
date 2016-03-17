package com.nlefler.glucloser.a.models

import com.nlefler.glucloser.a.models.parcelable.BolusEventParcelable

/**
 * Created by Nathan Lefler on 12/24/14.
 */
public interface BolusEventDetailDelegate {
    public fun bolusEventDetailUpdated(bolusEventParcelable: BolusEventParcelable)
}
