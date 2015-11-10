package com.nlefler.glucloser.models;

/**
 * Created by Nathan Lefler on 12/29/14.
 */
public class CheckInPushedData {
    private String alert;
    private CheckInData checkInData;

    static class CheckInData {
        private String venueId;
        private String venueName;
        private String venueLat;
        private String venueLon;

        public CheckInData() {

        }
    }

    public CheckInPushedData() {

    }

    public String getVenueId() {
        return checkInData.venueId;
    }

    public String getVenueName() {
        return checkInData.venueName;
    }

    public float getVenueLat() {
        if (checkInData.venueLat != null && !checkInData.venueLat.isEmpty()) {
            return Float.valueOf(checkInData.venueLat);
        }
        else {
            return 0;
        }
    }

    public float getVenueLon() {
        if (checkInData.venueLon != null && !checkInData.venueLon.isEmpty()) {
            return Float.valueOf(checkInData.venueLon);
        }
        else {
            return 0;
        }
    }

}
