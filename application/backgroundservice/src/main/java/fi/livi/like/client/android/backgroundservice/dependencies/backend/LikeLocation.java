package fi.livi.like.client.android.backgroundservice.dependencies.backend;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ 
    "latitude", 
    "longitude",
    "heading",
    "speed",
    "timestamp"
    })
public class LikeLocation {

    private double latitude;
    private double longitude;
    private int heading;
    private int speed;
    
    @JsonCreator
    public LikeLocation(
            @JsonProperty("latitude") double latitude, 
            @JsonProperty("longitude") double longitude, 
            @JsonProperty("heading") int heading, 
            @JsonProperty("speed") int speed
            ) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.heading = heading;
        this.speed = speed;
    }

    @JsonProperty("latitude")
    public double getLatitude() {
        return latitude;
    }

    @JsonProperty("longitude")
    public double getLongitude() {
        return longitude;
    }

    @JsonProperty("heading")
    public int getHeading() {
        return heading;
    }

    @JsonProperty("speed")
    public int getSpeed() {
        return speed;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + heading;
        long temp;
        temp = Double.doubleToLongBits(latitude);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(longitude);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + speed;
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LikeLocation other = (LikeLocation) obj;
        if (heading != other.heading)
            return false;
        if (Double.doubleToLongBits(latitude) != Double
                .doubleToLongBits(other.latitude))
            return false;
        if (Double.doubleToLongBits(longitude) != Double
                .doubleToLongBits(other.longitude))
            return false;
        if (speed != other.speed)
            return false;
        return true;
    }
    @Override
    public String toString() {
        return "LikeLocation [latitude=" + latitude + ", longitude="
                + longitude + ", heading=" + heading + ", speed=" + speed + "]";
    }

}
