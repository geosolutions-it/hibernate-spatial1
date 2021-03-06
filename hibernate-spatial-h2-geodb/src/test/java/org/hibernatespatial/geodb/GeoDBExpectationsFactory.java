/**
 *
 */
package org.hibernatespatial.geodb;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import org.hibernatespatial.test.AbstractExpectationsFactory;
import org.hibernatespatial.test.NativeSQLStatement;

/**
 * A Factory class that generates expected {@link NativeSQLStatement}s for
 * GeoDB.
 * 
 * @Author Jan Boonen, Geodan IT b.v.
 */
public class GeoDBExpectationsFactory extends AbstractExpectationsFactory {

	private final GeoDBGeometryUserType decoder = new GeoDBGeometryUserType();

	public GeoDBExpectationsFactory(GeoDBDataSourceUtils dataSourceUtils) {
		super(dataSourceUtils);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.hibernatespatial.test.AbstractExpectationsFactory#
	 * createNativeAsBinaryStatement()
	 */

	@Override
	protected NativeSQLStatement createNativeAsBinaryStatement() {
		return createNativeSQLStatement("select id, ST_AsEWKB(geom) from GEOMTEST");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.hibernatespatial.test.AbstractExpectationsFactory#
	 * createNativeAsTextStatement()
	 */

	@Override
	protected NativeSQLStatement createNativeAsTextStatement() {
		return createNativeSQLStatement("select id, ST_AsText(geom) from GEOMTEST");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.hibernatespatial.test.AbstractExpectationsFactory#
	 * createNativeBoundaryStatement()
	 */

	@Override
	protected NativeSQLStatement createNativeBoundaryStatement() {
		throw new UnsupportedOperationException(
				"Method ST_Bounday() is not implemented in the current version of GeoDB.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.hibernatespatial.test.AbstractExpectationsFactory#
	 * createNativeBufferStatement(java.lang.Double)
	 */

	@Override
	protected NativeSQLStatement createNativeBufferStatement(Double distance) {
		return createNativeSQLStatement(
				"select t.id, ST_Buffer(t.geom,?) from GEOMTEST t where ST_SRID(t.geom) = 4326",
				new Object[] { distance });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.hibernatespatial.test.AbstractExpectationsFactory#
	 * createNativeContainsStatement(com.vividsolutions.jts.geom.Geometry)
	 */

	@Override
	protected NativeSQLStatement createNativeContainsStatement(Geometry geom) {
		return createNativeSQLStatementAllWKTParams(
				"select t.id, ST_Contains(t.geom, ST_GeomFromText(?, 4326)) from GEOMTEST t where ST_Contains(t.geom, ST_GeomFromText(?, 4326)) = 1 and ST_SRID(t.geom) = 4326",
				geom.toText());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.hibernatespatial.test.AbstractExpectationsFactory#
	 * createNativeConvexHullStatement(com.vividsolutions.jts.geom.Geometry)
	 */

	@Override
	protected NativeSQLStatement createNativeConvexHullStatement(Geometry geom) {
		throw new UnsupportedOperationException(
				"Method ST_ConvexHull() is not implemented in the current version of GeoDB.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.hibernatespatial.test.AbstractExpectationsFactory#
	 * createNativeCrossesStatement(com.vividsolutions.jts.geom.Geometry)
	 */

	@Override
	protected NativeSQLStatement createNativeCrossesStatement(Geometry geom) {
		return createNativeSQLStatementAllWKTParams(
				"select t.id, ST_Crosses(t.geom, ST_GeomFromText(?, 4326)) from GEOMTEST t where ST_Crosses(t.geom, ST_GeomFromText(?, 4326)) = 1 and ST_SRID(t.geom) = 4326",
				geom.toText());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.hibernatespatial.test.AbstractExpectationsFactory#
	 * createNativeDifferenceStatement(com.vividsolutions.jts.geom.Geometry)
	 */

	@Override
	protected NativeSQLStatement createNativeDifferenceStatement(Geometry geom) {
		throw new UnsupportedOperationException(
				"Method ST_Difference() is not implemented in the current version of GeoDB.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.hibernatespatial.test.AbstractExpectationsFactory#
	 * createNativeDimensionSQL()
	 */

	@Override
	protected NativeSQLStatement createNativeDimensionSQL() {
		throw new UnsupportedOperationException(
				"Method ST_Dimension() is not implemented in the current version of GeoDB.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.hibernatespatial.test.AbstractExpectationsFactory#
	 * createNativeDisjointStatement(com.vividsolutions.jts.geom.Geometry)
	 */

	@Override
	protected NativeSQLStatement createNativeDisjointStatement(Geometry geom) {
		return createNativeSQLStatementAllWKTParams(
				"select t.id, ST_Disjoint(t.geom, ST_GeomFromText(?, 4326)) from GEOMTEST t where ST_Disjoint(t.geom, ST_GeomFromText(?, 4326)) = 1 and ST_SRID(t.geom) = 4326",
				geom.toText());
	}

	@Override
	protected NativeSQLStatement createNativeTransformStatement(int epsg) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected NativeSQLStatement createNativeHavingSRIDStatement(int srid) {
		return createNativeSQLStatement("select t.id, (ST_SRID(t.geom) = "
				+ srid + ") from GeomTest t where ST_SRID(t.geom) =  " + srid);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.hibernatespatial.test.AbstractExpectationsFactory#
	 * createNativeDistanceStatement(com.vividsolutions.jts.geom.Geometry)
	 */

	@Override
	protected NativeSQLStatement createNativeDistanceStatement(Geometry geom) {
        return createNativeSQLStatementAllWKTParams(
                "select t.id, st_distance(t.geom, ST_GeomFromText(?, 4326)) from GeomTest t where ST_SRID(t.geom) = 4326",
                geom.toText());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.hibernatespatial.test.AbstractExpectationsFactory#
	 * createNativeEnvelopeStatement()
	 */

	@Override
	protected NativeSQLStatement createNativeEnvelopeStatement() {
		return createNativeSQLStatement("select id, ST_Envelope(geom) from GEOMTEST");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.hibernatespatial.test.AbstractExpectationsFactory#
	 * createNativeEqualsStatement(com.vividsolutions.jts.geom.Geometry)
	 */

	@Override
	protected NativeSQLStatement createNativeEqualsStatement(Geometry geom) {
		return createNativeSQLStatementAllWKTParams(
				"select t.id, ST_Equals(t.geom, ST_GeomFromText(?, 4326)) from GEOMTEST t where ST_Equals(t.geom, ST_GeomFromText(?, 4326)) = 1 and ST_SRID(t.geom) = 4326",
				geom.toText());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.hibernatespatial.test.AbstractExpectationsFactory#
	 * createNativeFilterStatement(com.vividsolutions.jts.geom.Geometry)
	 */

	@Override
	protected NativeSQLStatement createNativeFilterStatement(Geometry geom) {
		throw new UnsupportedOperationException(
				"Filter is not implemented in the current version of GeoDB.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.hibernatespatial.test.AbstractExpectationsFactory#
	 * createNativeGeomUnionStatement(com.vividsolutions.jts.geom.Geometry)
	 */

	@Override
	protected NativeSQLStatement createNativeGeomUnionStatement(Geometry geom) {
		throw new UnsupportedOperationException(
				"Method ST_GeomUnion() is not implemented in the current version of GeoDB.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.hibernatespatial.test.AbstractExpectationsFactory#
	 * createNativeGeometryTypeStatement()
	 */

	@Override
	protected NativeSQLStatement createNativeGeometryTypeStatement() {
		return createNativeSQLStatement("select id, GeometryType(geom) from GEOMTEST");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.hibernatespatial.test.AbstractExpectationsFactory#
	 * createNativeIntersectionStatement(com.vividsolutions.jts.geom.Geometry)
	 */

	@Override
	protected NativeSQLStatement createNativeIntersectionStatement(Geometry geom) {
		throw new UnsupportedOperationException(
				"Method ST_Intersection() is not implemented in the current version of GeoDB.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.hibernatespatial.test.AbstractExpectationsFactory#
	 * createNativeIntersectsStatement(com.vividsolutions.jts.geom.Geometry)
	 */

	@Override
	protected NativeSQLStatement createNativeIntersectsStatement(Geometry geom) {
		return createNativeSQLStatementAllWKTParams(
				"select t.id, ST_Intersects(t.geom, ST_GeomFromText(?, 4326)) from GEOMTEST t where ST_Intersects(t.geom, ST_GeomFromText(?, 4326)) = 1 and ST_SRID(t.geom) = 4326",
				geom.toText());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.hibernatespatial.test.AbstractExpectationsFactory#
	 * createNativeIsEmptyStatement()
	 */

	@Override
	protected NativeSQLStatement createNativeIsEmptyStatement() {
		return createNativeSQLStatement("select id, ST_IsEmpty(geom) from GEOMTEST");
	}

	@Override
	protected NativeSQLStatement createNativeIsNotEmptyStatement() {
		return createNativeSQLStatement("select id, not ST_IsEmpty(geom) from geomtest");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.hibernatespatial.test.AbstractExpectationsFactory#
	 * createNativeIsSimpleStatement()
	 */

	@Override
	protected NativeSQLStatement createNativeIsSimpleStatement() {
		return createNativeSQLStatement("select id, ST_IsSimple(geom) from GEOMTEST");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.hibernatespatial.test.AbstractExpectationsFactory#
	 * createNativeOverlapsStatement(com.vividsolutions.jts.geom.Geometry)
	 */

	@Override
	protected NativeSQLStatement createNativeOverlapsStatement(Geometry geom) {
		return createNativeSQLStatementAllWKTParams(
				"select t.id, ST_Overlaps(t.geom, ST_GeomFromText(?, 4326)) from GEOMTEST t where ST_Overlaps(t.geom, ST_GeomFromText(?, 4326)) = 1 and ST_SRID(t.geom) = 4326",
				geom.toText());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.hibernatespatial.test.AbstractExpectationsFactory#
	 * createNativeRelateStatement(com.vividsolutions.jts.geom.Geometry,
	 * java.lang.String)
	 */

	@Override
	protected NativeSQLStatement createNativeRelateStatement(Geometry geom,
			String matrix) {
		throw new UnsupportedOperationException(
				"Method ST_Relate() is not implemented in the current version of GeoDB.");
	}

	@Override
	protected NativeSQLStatement createNativeDwithinStatement(Point geom,
			double distance) {
		String sql = "select t.id, ST_DWithin(t.geom, ST_GeomFromText(?, 4326), "
				+ distance
				+ " ) from GEOMTEST t where st_dwithin(t.geom, ST_GeomFromText(?, 4326), "
				+ distance + ") = 'true' and ST_SRID(t.geom) = 4326";
		return createNativeSQLStatementAllWKTParams(sql, geom.toText());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.hibernatespatial.test.AbstractExpectationsFactory#
	 * createNativeSridStatement()
	 */

	@Override
	protected NativeSQLStatement createNativeSridStatement() {
		return createNativeSQLStatement("select id, ST_SRID(geom) from GEOMTEST");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.hibernatespatial.test.AbstractExpectationsFactory#
	 * createNativeSymDifferenceStatement(com.vividsolutions.jts.geom.Geometry)
	 */

	@Override
	protected NativeSQLStatement createNativeSymDifferenceStatement(
			Geometry geom) {
		throw new UnsupportedOperationException(
				"Method ST_SymDifference() is not implemented in the current version of GeoDB.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.hibernatespatial.test.AbstractExpectationsFactory#
	 * createNativeTouchesStatement(com.vividsolutions.jts.geom.Geometry)
	 */

	@Override
	protected NativeSQLStatement createNativeTouchesStatement(Geometry geom) {
		return createNativeSQLStatementAllWKTParams(
				"select t.id, ST_Touches(t.geom, ST_GeomFromText(?, 4326)) from GEOMTEST t where ST_Touches(t.geom, ST_GeomFromText(?, 4326)) = 1 and ST_SRID(t.geom) = 4326",
				geom.toText());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.hibernatespatial.test.AbstractExpectationsFactory#
	 * createNativeWithinStatement(com.vividsolutions.jts.geom.Geometry)
	 */

	@Override
	protected NativeSQLStatement createNativeWithinStatement(
			Geometry testPolygon) {
		return createNativeSQLStatementAllWKTParams(
				"select t.id, ST_Within(t.geom, ST_GeomFromText(?, 4326)) from GEOMTEST t where ST_Within(t.geom, ST_GeomFromText(?, 4326)) = 1 and ST_SRID(t.geom) = 4326",
				testPolygon.toText());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.hibernatespatial.test.AbstractExpectationsFactory#decode(java.lang
	 * .Object)
	 */

	@Override
	protected Geometry decode(Object o) {
		return decoder.convert2JTS(o);
	}

}
