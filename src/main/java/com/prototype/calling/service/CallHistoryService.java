package com.prototype.calling.service;

import com.prototype.calling.model.CallRecord;
import com.prototype.calling.model.CallRecord.CallStatus;
import com.prototype.calling.model.CallRecordRepository;
import com.prototype.calling.model.SignalMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Manages call history — saves to H2 file database.
 * Data persists across server restarts.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CallHistoryService {

    private final CallRecordRepository repository;

    /**
     * Create a new call record when CALL_REQUEST signal fires.
     */
    public void createCallRecord(String callId, String caller, String callee, String callType) {
        // Avoid duplicate records for same callId
        if (repository.findByCallId(callId).isPresent()) return;

        CallRecord record = CallRecord.builder()
                .callId(callId)
                .caller(caller)
                .callee(callee)
                .callType(callType)
                .status(CallStatus.MISSED) // Default: missed until accepted
                .startTime(LocalDateTime.now())
                .build();

        repository.save(record);
        log.info("Call record created: {} → {} ({})", caller, callee, callType);
    }

    /**
     * Mark call as accepted — update start time to actual connect time.
     */
    public void markCallAccepted(String callId) {
        repository.findByCallId(callId).ifPresent(record -> {
            record.setStatus(CallStatus.COMPLETED);
            record.setStartTime(LocalDateTime.now()); // Real connect time
            repository.save(record);
        });
    }

    /**
     * Mark call as rejected.
     */
    public void markCallRejected(String callId) {
        repository.findByCallId(callId).ifPresent(record -> {
            record.setStatus(CallStatus.REJECTED);
            record.setEndTime(LocalDateTime.now());
            repository.save(record);
        });
    }

    /**
     * Mark call as ended — calculate duration.
     */
    public void markCallEnded(String callId) {
        repository.findByCallId(callId).ifPresent(record -> {
            LocalDateTime endTime = LocalDateTime.now();
            record.setEndTime(endTime);
            if (record.getStatus() == CallStatus.COMPLETED && record.getStartTime() != null) {
                long duration = ChronoUnit.SECONDS.between(record.getStartTime(), endTime);
                record.setDurationSeconds(duration);
            }
            repository.save(record);
        });
    }

    /**
     * Handle signal and update call record accordingly.
     */
    public void handleSignal(SignalMessage signal) {
        if (signal.getCallId() == null) return;

        switch (signal.getType()) {
            case "CALL_REQUEST" ->
                    createCallRecord(signal.getCallId(), signal.getFrom(), signal.getTo(), signal.getCallType());
            case "CALL_ACCEPT" ->
                    markCallAccepted(signal.getCallId());
            case "CALL_REJECT" ->
                    markCallRejected(signal.getCallId());
            case "CALL_END" ->
                    markCallEnded(signal.getCallId());
        }
    }

    /**
     * Get call history for a specific user (as caller or callee).
     */
    public List<CallRecord> getCallHistory(String username) {
        return repository.findByCallerOrCalleeOrderByStartTimeDesc(username, username);
    }

    /**
     * Get all call records.
     */
    public List<CallRecord> getAllCallHistory() {
        return repository.findAll();
    }
}