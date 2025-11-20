package com.example.tiffinapi.audit.repository;

import com.example.tiffinapi.audit.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByEntityTypeAndEntityIdOrderByTimestampDesc(String entityType, Long entityId);

    List<AuditLog> findByUserIdOrderByTimestampDesc(Long userId);

    Page<AuditLog> findByTimestampBetweenOrderByTimestampDesc(
            LocalDateTime start, LocalDateTime end, Pageable pageable);

    @Query("SELECT a FROM AuditLog a WHERE a.action = :action AND a.timestamp >= :since")
    List<AuditLog> findByActionSince(@Param("action") AuditLog.AuditAction action, 
                                     @Param("since") LocalDateTime since);

    @Query("SELECT a FROM AuditLog a WHERE a.entityType = :entityType AND a.timestamp >= :since")
    List<AuditLog> findByEntityTypeSince(@Param("entityType") String entityType, 
                                        @Param("since") LocalDateTime since);
}