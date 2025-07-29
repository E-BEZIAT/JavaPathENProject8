package com.openclassrooms.tourguide.response;

import gpsUtil.location.Location;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Data
@Getter
@Setter
@ToString
public class AttractionDTO {
    private String attractionName;
    private Location attractionLocation;
    private Location userLocation;
    private double distance;
    private int rewardPoints;

    public AttractionDTO(
            String attractionName,
            Location attractionLocation,
            Location userLocation,
            double distance,
            int rewardPoints
    ) {
        this.attractionName = attractionName;
        this.attractionLocation = attractionLocation;
        this.userLocation = userLocation;
        this.distance = distance;
        this.rewardPoints = rewardPoints;
    }
}
