package com.nlefler.glucloser.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.nlefler.glucloser.GlucloserApplication;
import com.nlefler.glucloser.dataSource.PlaceFactory;

/**
 * Created by Nathan Lefler on 12/24/14.
 */
public class MealParcelable implements Parcelable {
    private String mealId;
    private Place place;
    private int carbs;
    private float insulin;
    private boolean correction;

    public MealParcelable() {

    }

    public String getMealId() {
        return mealId;
    }

    public void setMealId(String mealId) {
        this.mealId = mealId;
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }

    public int getCarbs() {
        return carbs;
    }

    public void setCarbs(int carbs) {
        this.carbs = carbs;
    }

    public float getInsulin() {
        return insulin;
    }

    public void setInsulin(float insulin) {
        this.insulin = insulin;
    }

    public boolean getCorrection() {
        return this.correction;
    }

    public void setCorrection(boolean correction) {
        this.correction = correction;
    }

    /** Parcelable */
    protected MealParcelable(Parcel in) {
        mealId = in.readString();
        place = PlaceFactory.PlaceFromParcelable((PlaceParcelable) in.readValue(PlaceParcelable.class.getClassLoader()),
                GlucloserApplication.SharedApplication().getApplicationContext());
        carbs = in.readInt();
        insulin = in.readFloat();
        correction = in.readInt() != 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mealId);
        dest.writeParcelable(PlaceFactory.ParcelableFromPlace(place), flags);
        dest.writeInt(carbs);
        dest.writeFloat(insulin);
        dest.writeInt(correction ? 1 : 0);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<MealParcelable> CREATOR = new Parcelable.Creator<MealParcelable>() {
        @Override
        public MealParcelable createFromParcel(Parcel in) {
            return new MealParcelable(in);
        }

        @Override
        public MealParcelable[] newArray(int size) {
            return new MealParcelable[size];
        }
    };
}
