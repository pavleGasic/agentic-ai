package com.master.invoicemanagementserver.repository;

import com.master.invoicemanagementserver.entity.ProcessingLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ProcessingLogRepository extends JpaRepository<ProcessingLog, Long> {

  @Query("SELECT l FROM ProcessingLog l WHERE LOWER(l.businessContext) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY l.timestamp ASC")
  List<ProcessingLog> searchByBusinessContext(@Param("keyword") String keyword);

  @Query("""
          SELECT l FROM ProcessingLog l
          WHERE (:keyword IS NULL OR LOWER(l.businessContext) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:from IS NULL OR l.timestamp >= :from)
          AND (:to IS NULL OR l.timestamp <= :to)
          AND (:level IS NULL OR l.level = :level)
          ORDER BY l.timestamp ASC
          """)
  List<ProcessingLog> search(
          @Param("keyword") String keyword,
          @Param("from") LocalDateTime from,
          @Param("to") LocalDateTime to,
          @Param("level") String level
  );
}
