package fi.livi.like.client.android.dependencies.backend;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ 
    "userId", 
    "journeyId",
    "subJourneyId",
    "timestamp",
    "likeLocation",
    "likeActivity"
    })
public class JourneyUpdate {

   private final String userId;
   private final long journeyId;
   private final long subJourneyId;
   private final Date timestamp;
   private final LikeLocation likeLocation;
   private final LikeActivity likeActivity;

   public JourneyUpdate(
           @JsonProperty("userId") String userId, 
           @JsonProperty("journeyId") long journeyId, 
           @JsonProperty("subJourneyId") long subJourneyId, 
           @JsonProperty("timestamp") Date timestamp,
           @JsonProperty("likeLocation") LikeLocation likeLocation, 
           @JsonProperty("likeActivity") LikeActivity likeActivity
           ) {
       this.userId = userId;
       this.journeyId = journeyId;
       this.subJourneyId = subJourneyId;
       this.timestamp = timestamp;
       this.likeLocation = likeLocation;
       this.likeActivity = likeActivity;
   }

   @JsonProperty("userId")
   public String getUserId() {
       return userId;
   }

   @JsonProperty("journeyId")
   public long getJourneyId() {
       return journeyId;
   }

   @JsonProperty("subJourneyId")
   public long getSubJourneyId() {
       return subJourneyId;
   }

   @JsonProperty("timestamp")
   public Date getTimestamp() {
       return timestamp;
   }

   @JsonProperty("likeLocation")
   public LikeLocation getLikeLocation() {
       return likeLocation;
   }

   @JsonProperty("likeActivity")
   public LikeActivity getLikeActivity() {
       return likeActivity;
   }

@Override
public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (journeyId ^ (journeyId >>> 32));
    result = prime * result
            + ((likeActivity == null) ? 0 : likeActivity.hashCode());
    result = prime * result
            + ((likeLocation == null) ? 0 : likeLocation.hashCode());
    result = prime * result + (int) (subJourneyId ^ (subJourneyId >>> 32));
    result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
    result = prime * result + ((userId == null) ? 0 : userId.hashCode());
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
    JourneyUpdate other = (JourneyUpdate) obj;
    if (journeyId != other.journeyId)
        return false;
    if (likeActivity == null) {
        if (other.likeActivity != null)
            return false;
    } else if (!likeActivity.equals(other.likeActivity))
        return false;
    if (likeLocation == null) {
        if (other.likeLocation != null)
            return false;
    } else if (!likeLocation.equals(other.likeLocation))
        return false;
    if (subJourneyId != other.subJourneyId)
        return false;
    if (timestamp == null) {
        if (other.timestamp != null)
            return false;
    } else if (!timestamp.equals(other.timestamp))
        return false;
    if (userId == null) {
        if (other.userId != null)
            return false;
    } else if (!userId.equals(other.userId))
        return false;
    return true;
}

@Override
public String toString() {
    return "JourneyUpdate [userId=" + userId + ", journeyId=" + journeyId
            + ", subJourneyId=" + subJourneyId + ", timestamp=" + timestamp
            + ", likeLocation=" + likeLocation + ", likeActivity="
            + likeActivity + "]";
}
}