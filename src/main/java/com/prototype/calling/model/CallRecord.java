package com.prototype.calling.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Call history record — stored in H2 file database.
 * Persists across server restarts.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "call_records")
public class CallRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String callId;       // UUID from frontend

    @Column(nullable = false)
    private String caller;

    @Column(nullable = false)
    private String callee;

    @Column(nullable = false)
    private String callType;     // "video" or "audio"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CallStatus status;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long durationSeconds;

    public enum CallStatus {
        COMPLETED,
        MISSED,
        REJECTED
    }
}