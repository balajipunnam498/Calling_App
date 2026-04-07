package com.prototype.calling.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Signaling message exchanged between peers via WebSocket.
 *
 * Message Types:
 *  REGISTER      - User comes online, announces their username
 *  CALL_REQUEST  - Caller initiates call to a specific user
 *  CALL_ACCEPT   - Callee accepts the incoming call
 *  CALL_REJECT   - Callee rejects the incoming call
 *  OFFER         - WebRTC SDP offer (sent by caller after CALL_ACCEPT)
 *  ANSWER        - WebRTC SDP answer (sent by callee in response to OFFER)
 *  ICE_CANDIDATE - ICE candidate for NAT traversal (sent by both peers)
 *  CALL_END      - Either party ends the active call
 *  USER_LIST     - Server broadcasts online user list
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignalMessage {

    private String type;        // Message type (see above)
    private String from;        // Sender's username
    private String to;          // Recipient's username
    private String callType;    // "video" or "audio"
    private String callId;      // Unique call identifier (UUID)
    private Object data;        // SDP offer/answer or ICE candidate payload
    private String message;     // Optional human-readable message
}