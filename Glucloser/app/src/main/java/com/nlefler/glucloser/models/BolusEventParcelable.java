package com.nlefler.glucloser.models;

import android.os.Parcelable;

import java.util.Date;
import java.util.List;

/**
 * Created by Nathan Lefler on 5/8/15.
 */
public interface BolusEventParcelable {
    public BolusEventType getEventType();

    public Date getDate();
    public void setDate(Date date);

    public int getCarbs();
    public void setCarbs(int carbs);

    public float getInsulin();
    public void setInsulin(float insulin);

    public BloodSugarParcelable getBeforeSugarParcelable();
    public void setBeforeSugarParcelable(BloodSugarParcelable beforeSugarParcelable);

    public boolean isCorrection();
    public void setCorrection(boolean isCorrection);

    public List<FoodParcelable> getFoodParcelables();
    public void setFoodParcelables(List<FoodParcelable> foodParcelables);
}
