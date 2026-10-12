package com.retailer.rewards.exception;

import com.retailer.rewards.dto.ApiErrorResponse;
import com.retailer.rewards.web.RequestCorrelationFilter;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String UNEXPECTED_MESSAGE = "An unexpected error occurred.";

    private final Clock businessClock;

    public GlobalExceptionHandler(Clock businessClock) {
        this.businessClock = businessClock;
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(CustomerNotFoundException exception, HttpServletRequest request) {
        return respond(HttpStatus.NOT_FOUND, exception.getMessage(), request, false, null);
    }

    @ExceptionHandler(InvalidRewardQueryException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidQuery(InvalidRewardQueryException exception, HttpServletRequest request) {
        return respond(HttpStatus.BAD_REQUEST, exception.getMessage(), request, false, null);
    }

    @ExceptionHandler(DownstreamDataException.class)
    public ResponseEntity<ApiErrorResponse> handleDownstream(DownstreamDataException exception, HttpServletRequest request) {
        return respond(HttpStatus.BAD_GATEWAY, exception.getMessage(), request, true, exception);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request) {
        return respond(HttpStatus.BAD_REQUEST, typeMismatchMessage(exception), request, false, null);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingParameter(
            MissingServletRequestParameterException exception,
            HttpServletRequest request) {
        String message = "Parameter '" + exception.getParameterName() + "' is required.";
        return respond(HttpStatus.BAD_REQUEST, message, request, false, null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadable(HttpMessageNotReadableException exception, HttpServletRequest request) {
        return respond(HttpStatus.BAD_REQUEST, "Request body could not be read.", request, false, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException exception,
            HttpServletRequest request) {
        return respond(HttpStatus.METHOD_NOT_ALLOWED, exception.getMethod() + " is not supported for this endpoint.", request, false, null);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoHandler(NoHandlerFoundException exception, HttpServletRequest request) {
        String message = "No endpoint matches this request.";
        return respond(HttpStatus.NOT_FOUND, message, request, false, null);
    }

    @ExceptionHandler(AsyncRequestTimeoutException.class)
    public ResponseEntity<ApiErrorResponse> handleAsyncTimeout(AsyncRequestTimeoutException exception, HttpServletRequest request) {
        return respond(HttpStatus.SERVICE_UNAVAILABLE, "The request timed out.", request, true, exception);
    }

    @ExceptionHandler({CompletionException.class, ExecutionException.class})
    public ResponseEntity<ApiErrorResponse> handleAsyncFailure(Exception exception, HttpServletRequest request) {
        Throwable cause = unwrap(exception);
        if (cause instanceof CustomerNotFoundException) {
            return handleNotFound((CustomerNotFoundException) cause, request);
        }
        if (cause instanceof InvalidRewardQueryException) {
            return handleInvalidQuery((InvalidRewardQueryException) cause, request);
        }
        if (cause instanceof DownstreamDataException) {
            return handleDownstream((DownstreamDataException) cause, request);
        }
        if (cause instanceof Exception) {
            return handleUnexpected((Exception) cause, request);
        }
        return handleUnexpected(exception, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception, HttpServletRequest request) {
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, UNEXPECTED_MESSAGE, request, true, exception);
    }

    private ResponseEntity<ApiErrorResponse> respond(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            boolean serverError,
            Throwable throwable) {
        String requestId = requestId(request);
        if (requestId != null) {
            MDC.put(RequestCorrelationFilter.REQUEST_ID_MDC_KEY, requestId);
        }
        try {
            if (serverError) {
                log.error(message, throwable);
            } else {
                log.warn(message);
            }
            ApiErrorResponse body = new ApiErrorResponse(
                    Instant.now(businessClock),
                    status.value(),
                    status.getReasonPhrase(),
                    message,
                    request.getRequestURI(),
                    requestId);
            return ResponseEntity.status(status).body(body);
        } finally {
            MDC.remove(RequestCorrelationFilter.REQUEST_ID_MDC_KEY);
        }
    }

    private static String requestId(HttpServletRequest request) {
        Object requestId = request.getAttribute(RequestCorrelationFilter.REQUEST_ID_ATTRIBUTE);
        if (requestId instanceof String) {
            return (String) requestId;
        }
        return null;
    }

    private static String typeMismatchMessage(MethodArgumentTypeMismatchException exception) {
        Class<?> requiredType = exception.getRequiredType();
        String parameterName = exception.getName();
        if (requiredType == LocalDate.class) {
            return "Parameter '" + parameterName + "' must be an ISO-8601 date (yyyy-MM-dd).";
        }
        if (requiredType == Integer.class) {
            return "Parameter '" + parameterName + "' must be a whole number.";
        }
        if (requiredType == Boolean.class || requiredType == boolean.class) {
            return "Parameter '" + parameterName + "' must be true or false.";
        }
        return "Parameter '" + parameterName + "' has an invalid value.";
    }

    private static Throwable unwrap(Throwable throwable) {
        Throwable current = throwable;
        int depth = 0;
        while (current != null
                && depth < 10
                && (current instanceof CompletionException || current instanceof ExecutionException)
                && current.getCause() != null) {
            current = current.getCause();
            depth++;
        }
        return current == null ? throwable : current;
    }
}
