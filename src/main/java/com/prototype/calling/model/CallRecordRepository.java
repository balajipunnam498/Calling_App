package com.prototype.calling.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA repository for call history.
 * Spring auto-implements all queries at runtime.
 */
@Repository
public interface CallRecordRepository extends JpaRepository<CallRecord, Long> {

    // Find all calls where user was caller OR callee, newest first
    List<CallRecord> findByCallerOrCalleeOrderByStartTimeDesc(String caller, String callee);

    // Find by unique callId (UUID from frontend)
    Optional<CallRecord> findByCallId(String callId);
}