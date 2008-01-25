package org.hibernatespatial.postgis.test.cfg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;

import org.hibernate.cfg.Configuration;
import org.hibernatespatial.HBSpatialExtension;
import org.hibernatespatial.cfg.HSConfiguration;
import org.hibernatespatial.cfg.HSProperty;
import org.junit.Test;

import com.vividsolutions.jts.geom.PrecisionModel;

public class HSConfigurationTest {

	private static final String hibernate_config_location = "/Users/maesenka/workspaces/hibernate-spatial/hibernate-spatial-mysql/src/test/java/hibernate.cfg.xml";
	private static final String hs_config_location = "/Users/maesenka/workspaces/hibernate-spatial/hibernate-spatial/src/test/java/hibernate-spatial.cfg.xml";
	@Test
	public void testConfigure(){
		
		HSConfiguration config = new HSConfiguration();
		Configuration hibConfig = new Configuration();
		hibConfig.configure(new File(hibernate_config_location));
		config.configure(hibConfig);
		assertEquals("org.hibernatespatial.mysql.MySQLSpatialDialect", config.getProperty(HSProperty.DEFAULT_DIALECT));
	
		config.configure();
		testResults(config);
		
	}
	
	
	@Test
	public void testConfigureFile(){
		HSConfiguration config = new HSConfiguration();
		config.configure(new File(hs_config_location));
		testResults(config);
	}
	
	@Test
	public void testConfigureFailure(){
		HSConfiguration config = new HSConfiguration();
		config.configure("non-existing-file");
	}
	
	@Test
	public void testHBSpatExtConfigure(){
		HSConfiguration config = new HSConfiguration();
		config.configure();
		HBSpatialExtension.setConfiguration(config);
		
		PrecisionModel pm = HBSpatialExtension.getDefaultGeomFactory().getPrecisionModel(); 
		double scale = pm.getScale();
		assertEquals(5.0, scale);
		assertFalse(pm.isFloating());
	}
	
	private void testResults(HSConfiguration config){
		assertEquals("org.hibernatespatial.postgis.PostgisDialect", config.getProperty(HSProperty.DEFAULT_DIALECT));
		assertEquals("FIXED", config.getProperty(HSProperty.PRECISION_MODEL));
		assertEquals("5", config.getProperty(HSProperty.PRECISION_MODEL_SCALE));
	}
}
