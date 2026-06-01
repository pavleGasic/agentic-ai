package com.master.incidentmanagementserver.repository;

import com.master.incidentmanagementserver.entity.Incident;
import com.master.incidentmanagementserver.entity.Message;
import com.master.incidentmanagementserver.entity.Visibility;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByIncidentOrderByCreatedAtAsc(Incident incident);
    List<Message> findByIncidentAndVisibilityOrderByCreatedAtAsc(Incident incident, Visibility visibility);
}
