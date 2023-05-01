// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.schema.processing;

import com.yahoo.config.application.api.DeployLogger;
import com.yahoo.schema.RankProfile;
import com.yahoo.schema.RankProfileRegistry;
import com.yahoo.schema.Schema;
import com.yahoo.searchlib.rankingexpression.parser.RankingExpressionParserConstants;
import com.yahoo.vespa.model.container.search.QueryProfiles;

import java.util.Arrays;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Issues a warning if some function has a reserved name. This is not necessarily
 * an error, as a rank profile function can shadow a built-in function.
 *
 * @author lesters
 */
public class ReservedFunctionNames extends Processor {

    private static final Set<String> reservedNames = getReservedNames();

    public ReservedFunctionNames(Schema schema, DeployLogger deployLogger, RankProfileRegistry rankProfileRegistry, QueryProfiles queryProfiles) {
        super(schema, deployLogger, rankProfileRegistry, queryProfiles);
    }

    @Override
    public void process(boolean validate, boolean documentsOnly) {
        if ( ! validate) return;
        if (documentsOnly) return;

        for (RankProfile rp : rankProfileRegistry.all()) {
            for (String functionName : rp.getFunctions().keySet()) {
                if (reservedNames.contains(functionName)) {
                    deployLogger.logApplicationPackage(Level.WARNING, "Function '" + functionName + "' " +
                                                                      "in rank profile '" + rp.name() + "' " +
                                                                      "has a reserved name. This might mean that the function shadows " +
                                                                      "the built-in function with the same name."
                    );
                }
            }
        }
    }

    private static Set<String> getReservedNames() {
        return Arrays.stream(RankingExpressionParserConstants.tokenImage)
                .map(token -> token.substring(1, token.length()-1)).collect(Collectors.toUnmodifiableSet());
    }

}
