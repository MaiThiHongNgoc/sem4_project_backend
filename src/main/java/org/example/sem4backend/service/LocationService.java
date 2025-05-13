package org.example.sem4backend.service;

import org.example.sem4backend.entity.Location;

import java.util.List;
import java.util.UUID;

public interface LocationService {
    Location createLocation(Location location);
    Location updateLocation(UUID id, Location location);
    void softDeleteLocation(UUID id);
    List<Location> getAllActiveLocations();
    Location getLocationById(UUID id);
}
