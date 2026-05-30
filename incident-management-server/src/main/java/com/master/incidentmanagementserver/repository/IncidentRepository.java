package com.master.incidentmanagementserver.repository;

import com.master.incidentmanagementserver.entity.Incident;
import com.master.incidentmanagementserver.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident, Long> {
    List<Incident> findAllByOrderByCreatedAtDesc();
    List<Incident> findByCreatedByOrderByCreatedAtDesc(User createdBy);
}
