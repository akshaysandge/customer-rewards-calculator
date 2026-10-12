package com.retailer.rewards.exception;

import com.retailer.rewards.dto.ApiErrorResponse;
import com.retailer.rewards.web.RequestCorrelationFilter;
import io.swagger.v3.oas.annotations.Hidden;
import java.time.Clock;
import java.time.Instant;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Formats container-level failures, including unknown paths, as the same JSON body
 * the exception handler uses. Handled controller exceptions do not reach this path.
 */
@Hidden
@RestController
public class ApiErrorController implements ErrorController {

    private static final Logger log = LoggerFactory.getLogger(ApiErrorController.class);

    private final Clock businessClock;

    public ApiErrorController(Clock businessClock) {
        this.businessClock = businessClock;
    }

    @RequestMapping("${server.error.path:/error}")
    public ResponseEntity<ApiErrorResponse> handleError(HttpServletRequest request) {
        HttpStatus status = resolveStatus(request);
        String path = resolvePath(request);
        String requestId = requestId(request);
        String message = messageFor(status);
        if (requestId != null) {
            MDC.put(RequestCorrelationFilter.REQUEST_ID_MDC_KEY, requestId);
        }
        try {
            logFailure(status, path, request);
        } finally {
            MDC.remove(RequestCorrelationFilter.REQUEST_ID_MDC_KEY);
        }

        ApiErrorResponse body = new ApiErrorResponse(
                Instant.now(businessClock),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                requestId);
        ResponseEntity.BodyBuilder response = ResponseEntity.status(status);
        if (requestId != null) {
            response.header(RequestCorrelationFilter.REQUEST_ID_HEADER, requestId);
        }
        return response.body(body);
    }

    private static void logFailure(HttpStatus status, String path, HttpServletRequest request) {
        Object failure = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        if (status.is5xxServerError()) {
            if (failure instanceof Throwable) {
                log.error("path={} status={}", path, Integer.valueOf(status.value()), (Throwable) failure);
            } else {
                log.error("path={} status={}", path, Integer.valueOf(status.value()));
            }
            return;
        }
        log.warn("path={} status={}", path, Integer.valueOf(status.value()));
    }

    private static HttpStatus resolveStatus(HttpServletRequest request) {
        Object statusCode = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (statusCode instanceof Integer) {
            HttpStatus resolved = HttpStatus.resolve(((Integer) statusCode).intValue());
            if (resolved != null) {
                return resolved;
            }
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private static String resolvePath(HttpServletRequest request) {
        Object originalPath = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        if (originalPath instanceof String) {
            return (String) originalPath;
        }
        return request.getRequestURI();
    }

    private static String requestId(HttpServletRequest request) {
        Object requestId = request.getAttribute(RequestCorrelationFilter.REQUEST_ID_ATTRIBUTE);
        if (requestId instanceof String) {
            return (String) requestId;
        }
        return null;
    }

    private static String messageFor(HttpStatus status) {
        if (status == HttpStatus.NOT_FOUND) {
            return "No endpoint matches this request.";
        }
        if (status == HttpStatus.METHOD_NOT_ALLOWED) {
            return "HTTP method is not supported for this endpoint.";
        }
        if (status.is4xxClientError()) {
            return "The request could not be processed.";
        }
        return "An unexpected error occurred.";
    }
}
