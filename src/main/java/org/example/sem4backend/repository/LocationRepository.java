package org.example.sem4backend.repository;

import org.example.sem4backend.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LocationRepository extends JpaRepository<Location, UUID> {
    List<Location> findAllByStatus(Location.Status status);
}
