package org.example.sem4backend.service;



import org.springframework.security.core.context.SecurityContextHolder;
import org.example.sem4backend.entity.Location;
import org.example.sem4backend.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LocationService {

    @Autowired
    private LocationRepository locationRepository;

    public List<Location> getAllLocations() {
        return locationRepository.findByStatusNot(Location.Status.DELETED);
    }

    public Optional<Location> getLocationById(String id) {
        return locationRepository.findById(id).filter(loc -> loc.getStatus() != Location.Status.DELETED);
    }

    public Location createLocation(Location location) {
        location.setLocationId(null); // Let UUID be generated
        String userId = getCurrentUserId();
        location.setCreatedBy(userId);
        return locationRepository.save(location);
    }

    public Location updateLocation(String id, Location updated) {
        String userId = getCurrentUserId();
        return locationRepository.findById(id).map(existing -> {
            existing.setName(updated.getName());
            existing.setAddress(updated.getAddress());
            existing.setLatitude(updated.getLatitude());
            existing.setLongitude(updated.getLongitude());
            existing.setActive(updated.getActive());
            existing.setIsFixedLocation(updated.getIsFixedLocation());
            existing.setStatus(updated.getStatus());
            return locationRepository.save(existing);
        }).orElseThrow(() -> new RuntimeException("Location not found"));
    }

    public void softDeleteLocation(String id) {
        String userId = getCurrentUserId();
        locationRepository.findById(id).ifPresent(loc -> {
            loc.setStatus(Location.Status.DELETED);
            locationRepository.save(loc);
        });
    }

    private String getCurrentUserId() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}

