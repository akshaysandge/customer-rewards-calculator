package com.retailer.rewards.web;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

/**
 * Copies the caller's logging context onto pool threads so asynchronous data fetches
 * keep the same request id that the HTTP filter stored.
 */
public final class MdcTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        Map<String, String> callerContext = MDC.getCopyOfContextMap();
        return new Runnable() {
            @Override
            public void run() {
                Map<String, String> previousContext = MDC.getCopyOfContextMap();
                applyContext(callerContext);
                try {
                    runnable.run();
                } finally {
                    applyContext(previousContext);
                }
            }
        };
    }

    private static void applyContext(Map<String, String> contextMap) {
        if (contextMap == null || contextMap.isEmpty()) {
            MDC.clear();
            return;
        }
        MDC.setContextMap(new HashMap<String, String>(contextMap));
    }
}
