package com.nlefler.glucloser.model.place;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import android.location.Location;

import com.nlefler.glucloser.util.LocationUtil;
import com.nlefler.glucloser.util.database.DatabaseUtil;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import se.emilsjolander.sprinkles.Model;
import se.emilsjolander.sprinkles.annotations.AutoIncrement;
import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.Key;
import se.emilsjolander.sprinkles.annotations.Table;

@Table(Place.PLACE_DB_NAME)
public class Place extends Model implements Serializable {
	private static final long serialVersionUID = -7803318132865386549L;

	private static final String LOG_TAG = "Glucloser_Place";

    protected static final String PLACE_DB_NAME = "place";
    public static final String FOURSQUARE_ID_COLUMN_KEY = "foursquare_id";
	public static final String NAME_DB_COLUMN_KEY = "name";
	public static final String LOCATION_DB_COLUMN_KEY = "location";
	public static final String LATITUDE_DB_COLUMN_KEY = "location_latitude";
	public static final String LONGITUDE_DB_COLUMN_KEY = "location_longitude";
	public static final String READABLE_ADDRESS_COLUMN_KEY = "readable_address";
    public static final String LAST_VISITED_COLUMN_KEY = "last_visited";

    public static String getDatabaseTableName() {
        return PLACE_DB_NAME;
    }

    @Column(LATITUDE_DB_COLUMN_KEY)
	private double latitudeForSerializing;
    @Column(LONGITUDE_DB_COLUMN_KEY)
	private double longitudeForSerializing;

    @Key
    @AutoIncrement
    @Column(DatabaseUtil.ID_COLUMN_NAME)
    private int id;

    @Key
    @Column(DatabaseUtil.PARSE_ID_COLUMN_NAME)
	public String parseId;

    @Key
    @Column(DatabaseUtil.GLUCLOSER_ID_COLUMN_NAME)
    public String glucloserId;

    @Key
    @Column(FOURSQUARE_ID_COLUMN_KEY)
    public String foursquareId;

    @Key
    @Column(NAME_DB_COLUMN_KEY)
	public String name;

	public transient Location location;

    @Column(LAST_VISITED_COLUMN_KEY)
	public Date lastVisited;

    @Column(READABLE_ADDRESS_COLUMN_KEY)
	public String readableAddress;

    @Column(DatabaseUtil.CREATED_AT_COLUMN_NAME)
	public Date createdAt;
    @Column(DatabaseUtil.UPDATED_AT_COLUMN_NAME)
	public Date updatedAt;
    @Column(DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME)
	public boolean needsUpload;
    @Column(DatabaseUtil.DATA_VERSION_COLUMN_NAME)
	public int dataVersion;

	public Place() {
		this.parseId = UUID.randomUUID().toString();
        this.glucloserId = UUID.randomUUID().toString();
	}

    public Location getLocation() {
        Location l = new Location(LocationUtil.NO_PROVIDER);
        l.setLatitude(latitudeForSerializing);
        l.setLongitude(longitudeForSerializing);
        return l;
    }

	private ParseObject populateParseObject(ParseObject pobj) {
		pobj.put(NAME_DB_COLUMN_KEY, this.name);
		pobj.put(LOCATION_DB_COLUMN_KEY,
				LocationUtil.getParseGeoPointForLocation(this.getLocation()));
		pobj.put(DatabaseUtil.DATA_VERSION_COLUMN_NAME, dataVersion);

		if (readableAddress != null) {
			pobj.put(READABLE_ADDRESS_COLUMN_KEY, readableAddress);
		}

        if (glucloserId != null) {
            pobj.put(DatabaseUtil.GLUCLOSER_ID_COLUMN_NAME, glucloserId);
        }
        if (foursquareId != null) {
            pobj.put(FOURSQUARE_ID_COLUMN_KEY, foursquareId);
        }

		return pobj;
	}

	public ParseObject toParseObject() {
		ParseObject ret;
		try {
			ParseQuery query = new ParseQuery(PLACE_DB_NAME);
			ret = populateParseObject(query.get(parseId));
		} catch (ParseException e) {
			ret = populateParseObject(new ParseObject(PLACE_DB_NAME));
		}

		return ret;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Place)) return false;

        Place place = (Place) o;

        if (dataVersion != place.dataVersion) return false;
        if (foursquareId != null ? !foursquareId.equals(place.foursquareId) : place.foursquareId != null)
            return false;
        if (!glucloserId.equals(place.glucloserId)) return false;
        if (lastVisited != null ? !lastVisited.equals(place.lastVisited) : place.lastVisited != null)
            return false;
        if (location != null ? !location.equals(place.location) : place.location != null)
            return false;
        if (!name.equals(place.name)) return false;
        if (readableAddress != null ? !readableAddress.equals(place.readableAddress) : place.readableAddress != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = glucloserId.hashCode();
        result = 31 * result + (foursquareId != null ? foursquareId.hashCode() : 0);
        result = 31 * result + name.hashCode();
        result = 31 * result + (location != null ? location.hashCode() : 0);
        result = 31 * result + (lastVisited != null ? lastVisited.hashCode() : 0);
        result = 31 * result + (readableAddress != null ? readableAddress.hashCode() : 0);
        result = 31 * result + dataVersion;
        return result;
    }
}
