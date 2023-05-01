// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.clientmetrics;

import com.yahoo.messagebus.Reply;

import java.util.HashMap;
import java.util.Map;

/**
* @author thomasg
*/
public class RouteMetricSet {

    private final String route;
    private final ProgressCallback callback;
    private final Map<Integer, MessageTypeMetricSet> typeMap = new HashMap<>();

    public interface ProgressCallback {
        void onProgress(RouteMetricSet route);
        void done(RouteMetricSet route);
    }

    public RouteMetricSet(String route, ProgressCallback callback) {
        this.route = route;
        this.callback = callback;
    }

    public Map<Integer, MessageTypeMetricSet> getMetrics() { return typeMap; }

    public void addReply(Reply r) {
        MessageTypeMetricSet type = typeMap.get(r.getMessage().getType());
        if (type == null) {
            String msgName = r.getMessage().getClass().getSimpleName().replace("Message", "");
            type = new MessageTypeMetricSet(msgName);
            typeMap.put(r.getMessage().getType(), type);
        }

        type.addReply(r);
        if (callback != null) {
            callback.onProgress(this);
        }
    }

    public void done() {
        if (callback != null) {
            callback.done(this);
        }
    }

    public String getRoute() {
        return route;
    }
}
