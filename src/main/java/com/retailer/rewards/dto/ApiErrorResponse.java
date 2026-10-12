package com.retailer.rewards.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@JsonPropertyOrder({"timestamp", "status", "error", "message", "path", "requestId"})
@Schema(description = "Error returned for an invalid request or a failed data fetch.")
public class ApiErrorResponse {

    private final Instant timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private final String requestId;

    public ApiErrorResponse(
            Instant timestamp,
            int status,
            String error,
            String message,
            String path,
            String requestId) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.requestId = requestId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public String getRequestId() {
        return requestId;
    }
}
