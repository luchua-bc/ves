// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.search.federation;

import com.yahoo.search.Query;
import com.yahoo.search.Result;
import com.yahoo.search.Searcher;
import com.yahoo.search.result.Hit;
import com.yahoo.search.searchchain.Execution;

/**
 * @author Tony Vaagenes
 */
class SetHitCountsSearcher extends Searcher {

    private final long totalHitCount;
    private final long deepHitCount;

    public SetHitCountsSearcher(long totalHitCount, long deepHitCount) {
        this.totalHitCount = totalHitCount;
        this.deepHitCount = deepHitCount;
    }

    @Override
    public Result search(Query query, Execution execution) {
        Result result = execution.search(query);
        result.hits().add(createLoggingHit());

        result.setTotalHitCount(totalHitCount);
        result.setDeepHitCount(deepHitCount);
        return result;
    }

    private Hit createLoggingHit() {
        Hit hit = new Hit("SetHitCountSearcher");
        hit.setMeta(true);
        hit.types().add("logging");
        return hit;
    }
}
