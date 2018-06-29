/**
 * $Id$
 *
 * This file is part of Hibernate Spatial, an extension to the 
 * hibernate ORM solution for geographic data. 
 *
 * Copyright © 2007 Geovise BVBA
 * Copyright © 2007 K.U. Leuven LRD, Spatial Applications Division, Belgium
 *
 * This work was partially supported by the European Commission, 
 * under the 6th Framework Programme, contract IST-2-004688-STP.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, visit: http://www.hibernatespatial.org/
 */
package org.hibernatespatial.postgis;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.hibernatespatial.AbstractDBGeometryType;
import org.hibernatespatial.HBSpatialExtension;
import org.hibernatespatial.mgeom.MCoordinate;
import org.hibernatespatial.mgeom.MGeometry;
import org.hibernatespatial.mgeom.MLineString;
import org.postgis.*;
import org.postgresql.util.PGobject;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Specific <code>GeometryType</code> for Postgis geometry type
 *
 * @author Karel Maesen
 */
public class PGGeometryUserType extends AbstractDBGeometryType {

    private static final int[] geometryTypes = new int[]{Types.STRUCT};

    private static Pattern boxPattern = Pattern.compile(".*box.*\\((.*)\\)", Pattern.CASE_INSENSITIVE);

    public int[] sqlTypes() {
        return geometryTypes;
    }

    /**
     * Converts the native geometry object to a JTS <code>Geometry</code>.
     *
     * @param object native database geometry object (depends on the JDBC spatial
     *               extension of the database)
     * @return JTS geometry corresponding to geomObj.
     */
    public Geometry convert2JTS(Object object) {
        if (object == null)
            return null;

        // in some cases, Postgis returns not PGgeometry objects
        // but org.postgis.Geometry instances.
        // This has been observed when retrieving GeometryCollections
        // as the result of an SQL-operation such as Union.
        if (object instanceof org.postgis.Geometry) {
            object = new PGgeometry((org.postgis.Geometry) object);
        }

        if (object instanceof PGgeometry) {
            PGgeometry geom = (PGgeometry) object;
            org.locationtech.jts.geom.Geometry out = null;
            switch (geom.getGeoType()) {
                case org.postgis.Geometry.POINT:
                    out = convertPoint((org.postgis.Point) geom.getGeometry());
                    break;
                case org.postgis.Geometry.LINESTRING:
                    out = convertLineString((org.postgis.LineString) geom
                            .getGeometry());
                    break;
                case org.postgis.Geometry.POLYGON:
                    out = convertPolygon((org.postgis.Polygon) geom.getGeometry());
                    break;
                case org.postgis.Geometry.MULTILINESTRING:
                    out = convertMultiLineString((org.postgis.MultiLineString) geom
                            .getGeometry());
                    break;
                case org.postgis.Geometry.MULTIPOINT:
                    out = convertMultiPoint((org.postgis.MultiPoint) geom
                            .getGeometry());
                    break;
                case org.postgis.Geometry.MULTIPOLYGON:
                    out = convertMultiPolygon((org.postgis.MultiPolygon) geom
                            .getGeometry());
                    break;
                case org.postgis.Geometry.GEOMETRYCOLLECTION:
                    out = convertGeometryCollection((org.postgis.GeometryCollection) geom
                            .getGeometry());
                    break;
                default:
                    throw new RuntimeException("Unknown type of PGgeometry");
            }
            out.setSRID(geom.getGeometry().srid);
            return out;
        } else if (object instanceof org.postgis.PGboxbase) {
            return convertBox((org.postgis.PGboxbase) object);
        } else if (object instanceof PGobject && ((PGobject) object).getType().contains("box")) {
            PGobject pgo = (PGobject) object;
            //try to extract the box object (if available)
            String boxStr = extractBoxString(pgo);
            if (boxStr == null)
                throw new IllegalArgumentException("Can't convert object: " + pgo.getType() + " : " + pgo.getValue());
            String[] pointsStr = boxStr.split(",");
            Point ll = toPoint(pointsStr[0]);
            Point ur = toPoint(pointsStr[1]);
            return cornerPointsToPolygon(ll, ur, false);
        // The Postgis driver not always registers geography objects
        } else if (object instanceof PGobject && (((PGobject)object).getType().equals("geometry") ||
                ((PGobject)object).getType().equals("geography"))) {
            return convertFromPGobject((PGobject) object);
        } else {
            throw new IllegalArgumentException("Can't convert object of type "
                    + object.getClass().getCanonicalName());

        }

    }

    private Geometry convertFromPGobject(PGobject object) {
        String value = object.getValue();
        try {
            PGgeometry pg = new PGgeometry(value);
            return convert2JTS(pg);
        } catch (SQLException e) {
            throw new IllegalArgumentException("Can't convert PGobject of type " + object.getType());
        }
    }

    private Point toPoint(String s) {
        String[] coords = s.split("\\s");
        double x = Double.parseDouble(coords[0]);
        double y = Double.parseDouble(coords[1]);
        return new Point(x, y);
    }

    private String extractBoxString(PGobject pgo) {
        String boxStr = null;
        Matcher m = boxPattern.matcher(pgo.getValue());
        if (m.matches() && m.groupCount() >= 1) {
            boxStr = m.group(1);
        }
        return boxStr;

    }


    private Geometry convertBox(PGboxbase box) {
        Point ll = box.getLLB();
        Point ur = box.getURT();
        Coordinate[] ringCoords = new Coordinate[5];
        boolean is3D = true;
        if (box instanceof org.postgis.PGbox2d) {
            is3D = false;
        }
        return cornerPointsToPolygon(ll, ur, is3D);
    }

    private Geometry cornerPointsToPolygon(Point ll, Point ur, boolean is3D) {
        Coordinate[] ringCoords = new Coordinate[5];
        ringCoords[0] = is3D ? new Coordinate(ll.x, ll.y, ll.z) : new Coordinate(ll.x, ll.y);
        ringCoords[1] = is3D ? new Coordinate(ur.x, ll.y, ll.z) : new Coordinate(ur.x, ll.y);
        ringCoords[2] = is3D ? new Coordinate(ur.x, ur.y, ur.z) : new Coordinate(ur.x, ur.y);
        ringCoords[3] = is3D ? new Coordinate(ll.x, ur.y, ur.z) : new Coordinate(ll.x, ur.y);
        ringCoords[4] = is3D ? new Coordinate(ll.x, ll.y, ll.z) : new Coordinate(ll.x, ll.y);
        org.locationtech.jts.geom.LinearRing shell = getGeometryFactory()
                .createLinearRing(ringCoords);
        return getGeometryFactory().createPolygon(shell, null);
    }


    private Geometry convertGeometryCollection(GeometryCollection collection) {
        org.postgis.Geometry[] geometries = collection.getGeometries();
        org.locationtech.jts.geom.Geometry[] jtsGeometries = new org.locationtech.jts.geom.Geometry[geometries.length];
        for (int i = 0; i < geometries.length; i++) {
            jtsGeometries[i] = convert2JTS(geometries[i]);
            //TODO  - refactor this so the following line is not necessary
            jtsGeometries[i].setSRID(0); // convert2JTS sets SRIDs, but constituent geometries in a collection must have srid  == 0 
        }
        org.locationtech.jts.geom.GeometryCollection jtsGCollection = getGeometryFactory()
                .createGeometryCollection(jtsGeometries);
        return jtsGCollection;
    }

    private Geometry convertMultiPolygon(MultiPolygon pgMultiPolygon) {
        org.locationtech.jts.geom.Polygon[] polygons = new org.locationtech.jts.geom.Polygon[pgMultiPolygon
                .numPolygons()];

        for (int i = 0; i < polygons.length; i++) {
            Polygon pgPolygon = pgMultiPolygon.getPolygon(i);
            polygons[i] = (org.locationtech.jts.geom.Polygon) convertPolygon(pgPolygon);
        }

        org.locationtech.jts.geom.MultiPolygon out = getGeometryFactory()
                .createMultiPolygon(polygons);
        return out;
    }

    private Geometry convertMultiPoint(MultiPoint pgMultiPoint) {
        org.locationtech.jts.geom.Point[] points = new org.locationtech.jts.geom.Point[pgMultiPoint
                .numPoints()];

        for (int i = 0; i < points.length; i++) {
            points[i] = convertPoint(pgMultiPoint.getPoint(i));
        }
        org.locationtech.jts.geom.MultiPoint out = getGeometryFactory()
                .createMultiPoint(points);
        out.setSRID(pgMultiPoint.srid);
        return out;
    }

    private org.locationtech.jts.geom.Geometry convertMultiLineString(
            MultiLineString mlstr) {
        org.locationtech.jts.geom.MultiLineString out;
        if (mlstr.haveMeasure) {
            MLineString[] lstrs = new MLineString[mlstr.numLines()];
            for (int i = 0; i < mlstr.numLines(); i++) {
                MCoordinate[] coordinates = toJTSCoordinates(mlstr.getLine(i)
                        .getPoints());
                lstrs[i] = getGeometryFactory().createMLineString(coordinates);
            }
            out = getGeometryFactory().createMultiMLineString(lstrs);
        } else {
            org.locationtech.jts.geom.LineString[] lstrs = new org.locationtech.jts.geom.LineString[mlstr
                    .numLines()];
            for (int i = 0; i < mlstr.numLines(); i++) {
                lstrs[i] = getGeometryFactory().createLineString(
                        toJTSCoordinates(mlstr.getLine(i).getPoints()));
            }
            out = getGeometryFactory().createMultiLineString(lstrs);
        }
        return out;
    }

    protected org.locationtech.jts.geom.Geometry convertPolygon(
            Polygon polygon) {
        org.locationtech.jts.geom.LinearRing shell = getGeometryFactory()
                .createLinearRing(
                        toJTSCoordinates(polygon.getRing(0).getPoints()));
        org.locationtech.jts.geom.Polygon out = null;
        if (polygon.numRings() > 1) {
            org.locationtech.jts.geom.LinearRing[] rings = new org.locationtech.jts.geom.LinearRing[polygon
                    .numRings() - 1];
            for (int r = 1; r < polygon.numRings(); r++) {
                rings[r - 1] = getGeometryFactory().createLinearRing(
                        toJTSCoordinates(polygon.getRing(r).getPoints()));
            }
            out = getGeometryFactory().createPolygon(shell, rings);
        } else {
            out = getGeometryFactory().createPolygon(shell, null);
        }
        return out;
    }

    protected org.locationtech.jts.geom.Point convertPoint(Point pnt) {
        org.locationtech.jts.geom.Point g = getGeometryFactory().createPoint(
                this.toJTSCoordinate(pnt));
        return g;
    }

    protected org.locationtech.jts.geom.LineString convertLineString(
            org.postgis.LineString lstr) {
        org.locationtech.jts.geom.LineString out = lstr.haveMeasure ? getGeometryFactory()
                .createMLineString(toJTSCoordinates(lstr.getPoints()))
                : getGeometryFactory().createLineString(
                toJTSCoordinates(lstr.getPoints()));
        return out;
    }

    private MCoordinate[] toJTSCoordinates(Point[] points) {
        MCoordinate[] coordinates = new MCoordinate[points.length];
        for (int i = 0; i < points.length; i++) {
            coordinates[i] = this.toJTSCoordinate(points[i]);
        }
        return coordinates;
    }

    private MCoordinate toJTSCoordinate(Point pt) {
        MCoordinate mc;
        if (pt.dimension == 2) {
            mc = pt.haveMeasure ? MCoordinate.create2dWithMeasure(pt.getX(), pt
                    .getY(), pt.getM()) : MCoordinate.create2d(pt.getX(), pt
                    .getY());
        } else {
            mc = pt.haveMeasure ? MCoordinate.create3dWithMeasure(pt.getX(), pt
                    .getY(), pt.getZ(), pt.getM()) : MCoordinate.create3d(pt
                    .getX(), pt.getY(), pt.getZ());
        }
        return mc;
    }

    private Point[] toPoints(Coordinate[] coordinates) {
        Point[] points = new Point[coordinates.length];
        for (int i = 0; i < coordinates.length; i++) {
            Coordinate c = coordinates[i];
            Point pt;
            if (Double.isNaN(c.z)) {
                pt = new Point(c.x, c.y);
            } else {
                pt = new Point(c.x, c.y, c.z);
            }
            if (c instanceof MCoordinate) {
                MCoordinate mc = (MCoordinate) c;
                if (!Double.isNaN(mc.m)) {
                    pt.setM(mc.m);
                }
            }
            points[i] = pt;
        }
        return points;
    }

    /**
     * Converts a JTS <code>Geometry</code> to a native geometry object.
     *
     * @param jtsGeom    JTS Geometry to convert
     * @param connection the current database connection
     * @return native database geometry object corresponding to jtsGeom.
     */
    public Object conv2DBGeometry(Geometry jtsGeom, Connection connection) {
        org.postgis.Geometry geom = null;
        jtsGeom = forceEmptyToGeometryCollection(jtsGeom);
        if (jtsGeom instanceof org.locationtech.jts.geom.Point) {
            geom = convertJTSPoint((org.locationtech.jts.geom.Point) jtsGeom);
        } else if (jtsGeom instanceof org.locationtech.jts.geom.LineString) {
            geom = convertJTSLineString((org.locationtech.jts.geom.LineString) jtsGeom);
        } else if (jtsGeom instanceof org.locationtech.jts.geom.MultiLineString) {
            geom = convertJTSMultiLineString((org.locationtech.jts.geom.MultiLineString) jtsGeom);
        } else if (jtsGeom instanceof org.locationtech.jts.geom.Polygon) {
            geom = convertJTSPolygon((org.locationtech.jts.geom.Polygon) jtsGeom);
        } else if (jtsGeom instanceof org.locationtech.jts.geom.MultiPoint) {
            geom = convertJTSMultiPoint((org.locationtech.jts.geom.MultiPoint) jtsGeom);
        } else if (jtsGeom instanceof org.locationtech.jts.geom.MultiPolygon) {
            geom = convertJTSMultiPolygon((org.locationtech.jts.geom.MultiPolygon) jtsGeom);
        } else if (jtsGeom instanceof org.locationtech.jts.geom.GeometryCollection) {
            geom = convertJTSGeometryCollection((org.locationtech.jts.geom.GeometryCollection) jtsGeom);
        }

        if (geom != null)
            return new PGgeometry(geom);
        else
            throw new UnsupportedOperationException("Conversion of "
                    + jtsGeom.getClass().getSimpleName()
                    + " to PGgeometry not supported");
    }

    //Postgis treats every empty geometry as an empty geometrycollection

    private Geometry forceEmptyToGeometryCollection(Geometry jtsGeom) {
        Geometry forced = jtsGeom;
        if (forced.isEmpty()) {
            GeometryFactory factory = jtsGeom.getFactory();
            if (factory == null) {
                factory = HBSpatialExtension.getDefaultGeomFactory();
            }
            forced = factory.createGeometryCollection(null);
            forced.setSRID(jtsGeom.getSRID());
        }
        return forced;
    }

    private MultiPolygon convertJTSMultiPolygon(
            org.locationtech.jts.geom.MultiPolygon multiPolygon) {
        Polygon[] pgPolygons = new Polygon[multiPolygon.getNumGeometries()];
        for (int i = 0; i < pgPolygons.length; i++) {
            pgPolygons[i] = convertJTSPolygon((org.locationtech.jts.geom.Polygon) multiPolygon
                    .getGeometryN(i));
        }
        MultiPolygon mpg = new MultiPolygon(pgPolygons);
        mpg.setSrid(multiPolygon.getSRID());
        return mpg;
    }

    private MultiPoint convertJTSMultiPoint(
            org.locationtech.jts.geom.MultiPoint multiPoint) {
        Point[] pgPoints = new Point[multiPoint.getNumGeometries()];
        for (int i = 0; i < pgPoints.length; i++) {
            pgPoints[i] = convertJTSPoint((org.locationtech.jts.geom.Point) multiPoint
                    .getGeometryN(i));
        }
        MultiPoint mp = new MultiPoint(pgPoints);
        mp.setSrid(multiPoint.getSRID());
        return mp;
    }

    private Polygon convertJTSPolygon(
            org.locationtech.jts.geom.Polygon jtsPolygon) {
        int numRings = jtsPolygon.getNumInteriorRing();
        org.postgis.LinearRing[] rings = new org.postgis.LinearRing[numRings + 1];
        rings[0] = convertJTSLineStringToLinearRing(jtsPolygon
                .getExteriorRing());
        for (int i = 0; i < numRings; i++) {
            rings[i + 1] = convertJTSLineStringToLinearRing(jtsPolygon
                    .getInteriorRingN(i));
        }
        Polygon polygon = new org.postgis.Polygon(rings);
        polygon.setSrid(jtsPolygon.getSRID());
        return polygon;
    }

    private LinearRing convertJTSLineStringToLinearRing(
            org.locationtech.jts.geom.LineString lineString) {
        LinearRing lr = new org.postgis.LinearRing(toPoints(lineString
                .getCoordinates()));
        lr.setSrid(lineString.getSRID());
        return lr;
    }

    private LineString convertJTSLineString(
            org.locationtech.jts.geom.LineString string) {
        LineString ls = new org.postgis.LineString(toPoints(string
                .getCoordinates()));
        if (string instanceof MGeometry) {
            ls.haveMeasure = true;
        }
        ls.setSrid(string.getSRID());
        return ls;
    }

    private MultiLineString convertJTSMultiLineString(
            org.locationtech.jts.geom.MultiLineString string) {
        org.postgis.LineString[] lines = new org.postgis.LineString[string
                .getNumGeometries()];
        for (int i = 0; i < string.getNumGeometries(); i++) {
            lines[i] = new org.postgis.LineString(toPoints(string.getGeometryN(
                    i).getCoordinates()));
        }
        MultiLineString mls = new MultiLineString(lines);
        if (string instanceof MGeometry) {
            mls.haveMeasure = true;
        }
        mls.setSrid(string.getSRID());
        return mls;
    }

    private Point convertJTSPoint(org.locationtech.jts.geom.Point point) {
        org.postgis.Point pgPoint = new org.postgis.Point();
        pgPoint.srid = point.getSRID();
        pgPoint.x = point.getX();
        pgPoint.y = point.getY();
        Coordinate coordinate = point.getCoordinate();
        if (Double.isNaN(coordinate.z)) {
            pgPoint.dimension = 2;
        } else {
            pgPoint.z = coordinate.z;
            pgPoint.dimension = 3;
        }
        pgPoint.haveMeasure = false;
        if (coordinate instanceof MCoordinate && !Double.isNaN(((MCoordinate) coordinate).m)) {
            pgPoint.m = ((MCoordinate) coordinate).m;
            pgPoint.haveMeasure = true;
        }
        return pgPoint;
    }

    private GeometryCollection convertJTSGeometryCollection(
            org.locationtech.jts.geom.GeometryCollection collection) {
        org.locationtech.jts.geom.Geometry currentGeom;
        org.postgis.Geometry[] pgCollections = new org.postgis.Geometry[collection
                .getNumGeometries()];
        for (int i = 0; i < pgCollections.length; i++) {
            currentGeom = collection.getGeometryN(i);
            currentGeom = forceEmptyToGeometryCollection(currentGeom);
            if (currentGeom.getClass() == org.locationtech.jts.geom.LineString.class) {
                pgCollections[i] = convertJTSLineString((org.locationtech.jts.geom.LineString) currentGeom);
            } else if (currentGeom.getClass() == org.locationtech.jts.geom.LinearRing.class) {
                pgCollections[i] = convertJTSLineStringToLinearRing((org.locationtech.jts.geom.LinearRing) currentGeom);
            } else if (currentGeom.getClass() == org.locationtech.jts.geom.MultiLineString.class) {
                pgCollections[i] = convertJTSMultiLineString((org.locationtech.jts.geom.MultiLineString) currentGeom);
            } else if (currentGeom.getClass() == org.locationtech.jts.geom.MultiPoint.class) {
                pgCollections[i] = convertJTSMultiPoint((org.locationtech.jts.geom.MultiPoint) currentGeom);
            } else if (currentGeom.getClass() == org.locationtech.jts.geom.MultiPolygon.class) {
                pgCollections[i] = convertJTSMultiPolygon((org.locationtech.jts.geom.MultiPolygon) currentGeom);
            } else if (currentGeom.getClass() == org.locationtech.jts.geom.Point.class) {
                pgCollections[i] = convertJTSPoint((org.locationtech.jts.geom.Point) currentGeom);
            } else if (currentGeom.getClass() == org.locationtech.jts.geom.Polygon.class) {
                pgCollections[i] = convertJTSPolygon((org.locationtech.jts.geom.Polygon) currentGeom);
            } else if (currentGeom.getClass() == org.locationtech.jts.geom.GeometryCollection.class) {
                pgCollections[i] = convertJTSGeometryCollection((org.locationtech.jts.geom.GeometryCollection) currentGeom);
            }
        }
        GeometryCollection gc = new GeometryCollection(pgCollections);
        gc.setSrid(collection.getSRID());
        return gc;
    }

}
