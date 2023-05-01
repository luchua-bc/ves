// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.search.grouping.vespa;

import com.yahoo.prelude.fastsearch.DocsumDefinitionSet;
import com.yahoo.prelude.fastsearch.FastHit;
import com.yahoo.prelude.fastsearch.GroupingListHit;
import com.yahoo.search.Query;
import com.yahoo.search.Searcher;
import com.yahoo.search.result.Hit;
import com.yahoo.search.result.Relevance;
import com.yahoo.searchlib.aggregation.FS4Hit;
import com.yahoo.searchlib.aggregation.VdsHit;

/**
 * Implementation of the {@link ResultBuilder.HitConverter} interface for {@link GroupingExecutor}.
 *
 * @author Simon Thoresen Hult
 */
class HitConverter implements ResultBuilder.HitConverter {

    private final Searcher searcher;
    private final Query query;

    /**
     * Creates a new instance of this class.
     *
     * @param searcher The searcher that owns this converter.
     * @param query    The query that returned the hits.
     */
    public HitConverter(Searcher searcher, Query query) {
        this.searcher = searcher;
        this.query = query;
    }

    @Override
    public com.yahoo.search.result.Hit toSearchHit(String summaryClass, com.yahoo.searchlib.aggregation.Hit hit) {
        if (hit instanceof FS4Hit) {
            return convertFs4Hit(summaryClass, (FS4Hit)hit);
        } else if (hit instanceof VdsHit) {
            return convertVdsHit(summaryClass, (VdsHit)hit);
        } else {
            throw new UnsupportedOperationException("Hit type '" + hit.getClass().getName() + "' not supported.");
        }
    }

    private Hit convertFs4Hit(String summaryClass, FS4Hit groupHit) {
        FastHit hit = new FastHit(groupHit.getGlobalId().getRawId(),
                                  new Relevance(groupHit.getRank()),
                                  groupHit.getPath(), groupHit.getDistributionKey());
        hit.setFillable();
        hit.setSearcherSpecificMetaData(searcher, summaryClass);

        GroupingListHit hitContext = (GroupingListHit)groupHit.getContext();
        if (hitContext == null)
            throw new NullPointerException("Hit has no context.");
        hit.setSource(hitContext.getSource());
        hit.setQuery(hitContext.getQuery());
        return hit;
    }

    private Hit convertVdsHit(String summaryClass, VdsHit grpHit) {
        FastHit ret = new FastHit();
        ret.setRelevance(grpHit.getRank());
        if (grpHit.getSummary().getData().length > 0) {
            GroupingListHit ctxHit = (GroupingListHit)grpHit.getContext();
            if (ctxHit == null) {
                throw new NullPointerException("Hit has no context.");
            }
            DocsumDefinitionSet defs = ctxHit.getDocsumDefinitionSet();
            defs.lazyDecode(summaryClass, grpHit.getSummary().getData(), ret);
            ret.setFilled(summaryClass);
            ret.setFilled(query.getPresentation().getSummary());
        }
        return ret;
    }

}
