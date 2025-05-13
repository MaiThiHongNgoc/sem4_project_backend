package org.example.sem4backend.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.sem4backend.entity.Location;
import org.example.sem4backend.repository.LocationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;

    @Override
    public Location createLocation(Location location) {
        return locationRepository.save(location);
    }

    @Override
    public Location updateLocation(UUID id, Location newData) {
        Location existing = locationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Location not found"));

        // Chặn sửa tọa độ nếu là vị trí cố định
        if (Boolean.TRUE.equals(existing.getIsFixedLocation())) {
            newData.setLatitude(existing.getLatitude());
            newData.setLongitude(existing.getLongitude());
        }

        existing.setName(newData.getName());
        existing.setAddress(newData.getAddress());
        existing.setStatus(newData.getStatus());

        return locationRepository.save(existing);
    }

    @Override
    public void softDeleteLocation(UUID id) {
        Location existing = locationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Location not found"));

        existing.setStatus(Location.Status.DELETED);
        locationRepository.save(existing);
    }

    @Override
    public List<Location> getAllActiveLocations() {
        return locationRepository.findAllByStatus(Location.Status.ACTIVE);
    }

    @Override
    public Location getLocationById(UUID id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Location not found"));
    }
}