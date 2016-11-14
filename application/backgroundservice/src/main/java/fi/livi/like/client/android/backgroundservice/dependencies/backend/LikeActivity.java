package fi.livi.like.client.android.backgroundservice.dependencies.backend;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ 
    "type", 
    "confidence"
    })
public class LikeActivity {

    private Type type;
    private int confidence;

    public enum Type {
        IN_VEHICLE,
        ON_BICYCLE,
        RUNNING,
        WALKING;
    }
    
    public LikeActivity(
            @JsonProperty("type") Type type, 
            @JsonProperty("confidence") int confidence) {
        this.type = type;
        this.confidence = confidence;
    }

    @JsonProperty("type")
    public Type getType() {
        return type;
    }

    @JsonProperty("confidence")
    public int getConfidence() {
        return confidence;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + confidence;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        LikeActivity other = (LikeActivity) obj;
        if (confidence != other.confidence)
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Activity [type=" + type + ", confidence=" + confidence + "]";
    }
    
}
