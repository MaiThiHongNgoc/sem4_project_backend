package org.example.sem4backend.repository;

import org.example.sem4backend.entity.Department;
import org.example.sem4backend.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PositionRepository extends JpaRepository<Position, String> {
    @Query(value = "SELECT * FROM positions", nativeQuery = true)
    List<Position> findAllPositionNative();
}