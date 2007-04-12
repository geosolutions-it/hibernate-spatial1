--
-- Create test tables
--

CREATE TABLE public.linestringtest (
	id DECIMAL(10,0),
	name VARCHAR(50),
	geom geometry
);

CREATE TABLE public.multilinestringtest(
	id DECIMAL(10,0),
	name VARCHAR(50),
	geom geometry
);

CREATE TABLE public.polygontest(
	id DECIMAL(10,0),
	name VARCHAR(50),
	geom geometry
);


--
-- TODO -- create spatial index
--