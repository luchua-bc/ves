// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.prelude.searcher;

import com.yahoo.component.chain.dependencies.After;
import com.yahoo.component.chain.dependencies.Before;
import com.yahoo.component.chain.dependencies.Provides;
import com.yahoo.geo.DegreesParser;
import com.yahoo.geo.BoundingBoxParser;
import com.yahoo.yolean.Exceptions;
import com.yahoo.search.Query;
import com.yahoo.search.Result;
import com.yahoo.search.Searcher;
import com.yahoo.processing.request.CompoundName;
import com.yahoo.search.result.ErrorMessage;
import com.yahoo.search.searchchain.Execution;
import com.yahoo.search.searchchain.PhaseNames;
import com.yahoo.prelude.Location;

/**
 * A searcher converting human-readable position parameters
 * into internal format.
 * <br>
 * Reads the following query properties:
 * <ul>
 *  <li> pos.ll (geographical latitude and longitude)
 *  <li> pos.xy (alternate to pos.ll - direct x and y in internal units)
 *  <li> pos.radius (distance in one of:
 *        internal units (no suffix), meter (m), kilometer (km) or miles (mi)
 * </ul>
 *
 * @author arnej27959
 */
@After(PhaseNames.RAW_QUERY)
@Before(PhaseNames.TRANSFORMED_QUERY)
@Provides(PosSearcher.POSITION_PARSING)
public class PosSearcher extends Searcher {

    public static final String POSITION_PARSING = "PositionParsing";

    private static final CompoundName posBb = CompoundName.from("pos.bb");
    private static final CompoundName posLl = CompoundName.from("pos.ll");
    private static final CompoundName posXy = CompoundName.from("pos.xy");
    private static final CompoundName posAttributeName = CompoundName.from("pos.attribute");
    private static final CompoundName posRadius = CompoundName.from("pos.radius");

    // according to wikipedia:
    // Earth's equatorial radius = 6378137 meter - not used
    // meters per mile = 1609.344
    // 180 degrees equals one half diameter equals PI*r
    // Earth's polar radius = 6356752 meter

    public final static double km2deg = 1000.000 * 180.0 / (Math.PI * 6356752.0);
    public final static double mi2deg = 1609.344 * 180.0 / (Math.PI * 6356752.0);

    @Override
    public Result search(Query query, Execution execution) {
        String bb = query.properties().getString(posBb);
        String ll = query.properties().getString(posLl);
        String xy = query.properties().getString(posXy);

        if (ll == null && xy == null && bb == null) {
            return execution.search(query); // Nothing to do
        }
        if (query.getRanking().getLocation() != null) {
            // this searcher is a NOP if there is already a location
            // in the query
            query.trace("query already has a location set, not processing 'pos' params", false, 1);
            return execution.search(query);
        }

        Location loc = new Location();
        loc.setDimensions(2);
        String posAttribute = query.properties().getString(posAttributeName);
        loc.setAttribute(posAttribute);

        try {
            if (ll == null && xy == null) {
                parseBoundingBox(bb, loc);
            } else {
                if (ll != null && xy != null) {
                    throw new IllegalArgumentException("Cannot handle both lat/long and xy coords at the same time");
                }
                if (ll != null) {
                    handleGeoCircle(query, ll, loc);
                }
                if (xy != null) {
                    handleXyCircle(query, xy, loc);
                }
                if (bb != null) {
                    parseBoundingBox(bb, loc);
                }
            }
        }
        catch (IllegalArgumentException e) {
            return new Result(query, ErrorMessage.createInvalidQueryParameter("Error in pos parameters: " +
                                                                              Exceptions.toMessageString(e)));
        }
        // and finally:
        query.getRanking().setLocation(loc);
        return execution.search(query);
    }

    private void handleGeoCircle(Query query, String ll, Location target) {
        double ewCoord;
        double nsCoord;
        try {
            DegreesParser parsed = new DegreesParser(ll);
            ewCoord = parsed.longitude;
            nsCoord = parsed.latitude;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unable to parse lat/long string '" +ll + "'", e);
        }

        String radius = query.properties().getString(posRadius);
        double radiusdegrees;
        if (radius == null) {
            radiusdegrees = 50.0 * km2deg;
        } else if (radius.endsWith("km")) {
            double radiuskm = Double.parseDouble(radius.substring(0, radius.length()-2));
            radiusdegrees = radiuskm * km2deg;
        } else if (radius.endsWith("m")) {
            double radiusm = Double.parseDouble(radius.substring(0, radius.length()-1));
            radiusdegrees = radiusm * km2deg / 1000.0;
        } else if (radius.endsWith("mi")) {
            double radiusmiles = Double.parseDouble(radius.substring(0, radius.length()-2));
            radiusdegrees = radiusmiles * mi2deg;
        } else {
            radiusdegrees = Integer.parseInt(radius) * 0.000001;
        }
        target.setGeoCircle(nsCoord, ewCoord, radiusdegrees);
    }


    private void handleXyCircle(Query query, String xy, Location target) {
        int xcoord;
        int ycoord;
        // parse xy
        int semipos = xy.indexOf(';');
        if (semipos > 0 && semipos < xy.length()) {
            xcoord = Integer.parseInt(xy.substring(0, semipos));
            ycoord = Integer.parseInt(xy.substring(semipos+1, xy.length()));
        } else {
            throw new IllegalArgumentException("pos.xy must be in the format 'digits;digits' but was: '"+xy+"'");
        }

        String radius = query.properties().getString(posRadius);
        int radiusUnits;
        if (radius == null) {
            double radiuskm = 50.0;
            double radiusdegrees = radiuskm * km2deg;
            radiusUnits = (int)(radiusdegrees * 1000000);
        } else if (radius.endsWith("km")) {
            double radiuskm = Double.parseDouble(radius.substring(0, radius.length()-2));
            double radiusdegrees = radiuskm * km2deg;
            radiusUnits = (int)(radiusdegrees * 1000000);
        } else if (radius.endsWith("m")) {
            double radiusm = Double.parseDouble(radius.substring(0, radius.length()-1));
            double radiusdegrees = radiusm * km2deg / 1000.0;
            radiusUnits = (int)(radiusdegrees * 1000000);
        } else if (radius.endsWith("mi")) {
            double radiusmiles = Double.parseDouble(radius.substring(0, radius.length()-2));
            double radiusdegrees = radiusmiles * mi2deg;
            radiusUnits = (int)(radiusdegrees * 1000000);
        } else {
            radiusUnits = Integer.parseInt(radius);
        }
        target.setXyCircle(xcoord, ycoord, radiusUnits);
    }

    private static void parseBoundingBox(String bb, Location target) {
        BoundingBoxParser parser = new BoundingBoxParser(bb);
        target.setBoundingBox(parser.n, parser.s, parser.e, parser.w);
    }

}
