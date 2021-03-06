/*********************************************************************************************
 *
 *
 * 'DeprecatedOperators.java', in plugin 'msi.gama.core', is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2014 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://code.google.com/p/gama-platform/ for license information and developers contact.
 *
 *
 **********************************************************************************************/
package msi.gaml.operators;

import static msi.gama.util.GAML.nullCheck;
import msi.gama.common.interfaces.IKeyword;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.shape.*;
import msi.gama.metamodel.topology.ITopology;
import msi.gama.metamodel.topology.filter.*;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.example;
import msi.gama.precompiler.GamlAnnotations.operator;
import msi.gama.precompiler.GamlAnnotations.usage;
import msi.gama.precompiler.*;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.*;
import msi.gama.util.file.*;
import msi.gama.util.graph.*;
import msi.gama.util.path.IPath;
import msi.gaml.expressions.IExpression;
import msi.gaml.species.ISpecies;
import msi.gaml.types.*;
import rcaller.exception.*;

/**
 * Class Deprecated.
 *
 * @author drogoul
 * @since 16 janv. 2014
 *
 */
public class DeprecatedOperators {

	@Deprecated
	@operator(value = "neighbours_of", content_type = IType.AGENT, category = { IOperatorCategory.SPATIAL,
		IOperatorCategory.SP_QUERIES })
	@doc(deprecated = "use neighbors_of instead",
	value = "a list, containing all the agents of the same species than the argument (if it is an agent) located at a distance inferior or equal to 1 to the right-hand operand agent considering the left-hand operand topology.",
	masterDoc = true,
	examples = { @example(value = "topology(self) neighbours_of self",
	equals = "returns all the agents located at a distance lower or equal to 1 to the agent applying the operator considering its topology.",
	test = false) },
	see = { "neighbors_of" })
	public static
	IList neighbours_of_deprecated(final IScope scope, final ITopology t, final IAgent agent) {
		return Spatial.Queries.neighbours_of(scope, t, agent);
	}

	@Deprecated
	@operator(value = "neighbours_of", content_type = IType.AGENT, category = { IOperatorCategory.SPATIAL,
		IOperatorCategory.SP_QUERIES })
	/* TODO, expected_content_type = { IType.FLOAT, IType.INT } */
	@doc(deprecated = "Use 'neighbors_of(topology, agent, distance)' instead",
	usages = @usage(value = "a list, containing all the agents of the same species than the key of the pair argument (if it is an agent) located at a distance inferior or equal to the right member (float) of the pair (right-hand operand) to the left member (agent, geometry or point) considering the left-hand operand topology.",
	examples = { @example(value = "topology(self) neighbours_of self::10",
	equals = "all the agents located at a distance lower or equal to 10 to the agent applying the operator considering its topology.",
	test = false) }))
	public static
	IList neighbours_of_deprecated(final IScope scope, final ITopology t, final GamaPair pair) {
		return neighbours_of(scope, t, pair);
		// TODO We could compute a filter based on the population if it is an agent
	}

	@Deprecated
	@operator(value = "neighbours_of", content_type = IType.AGENT, category = { IOperatorCategory.SPATIAL,
		IOperatorCategory.SP_QUERIES })
	/* TODO, expected_content_type = { IType.FLOAT, IType.INT } */
	@doc(deprecated = "use neighbors_of instead",
	usages = @usage(value = "a list, containing all the agents of the same species than the left argument (if it is an agent) located at a distance inferior or equal to the third argument to the second argument (agent, geometry or point) considering the first operand topology.",
	examples = { @example(value = "neighbours_of (topology(self), self,10)",
	equals = "all the agents located at a distance lower or equal to 10 to the agent applying the operator considering its topology.",
	test = false) }))
	public static
	IList
	neighbours_of_deprecated(final IScope scope, final ITopology t, final IShape agent, final Double distance) {
		return Spatial.Queries.neighbours_of(scope, t, agent, distance);
	}

	@Deprecated
	@operator(value = "neighbours_at", content_type = ITypeProvider.FIRST_TYPE, category = { IOperatorCategory.SPATIAL,
		IOperatorCategory.SP_QUERIES })
	@doc(deprecated = "use neighbors_at instead",
	value = "a list, containing all the agents of the same species than the left argument (if it is an agent) located at a distance inferior or equal to the right-hand operand to the left-hand operand (geometry, agent, point).",
	comment = "The topology used to compute the neighbourhood  is the one of the left-operand if this one is an agent; otherwise the one of the agent applying the operator.",
	examples = { @example(value = "(self neighbours_at (10))",
	equals = "all the agents located at a distance lower or equal to 10 to the agent applying the operator.",
	test = false) }, see = { "neighbors_at" })
	public static
	IList neighbours_at_deprecated(final IScope scope, final IShape agent, final Double distance) {
		return Spatial.Queries.neighbours_at(scope, agent, distance);
	}

	@Deprecated
	@operator(value = "toChar", can_be_const = true, category = { IOperatorCategory.STRING })
	@doc(deprecated = "Use 'char' instead",
	special_cases = { "convert ACSII integer value to character" },
	examples = { @example(value = "toChar (34)", equals = "'\"'") })
	static public String toChar(final Integer s) {
		return Strings.asChar(s);
	}

	@operator(value = "as_csv", can_be_const = true, index_type = IType.INT)
	@doc(deprecated = "use csv_file(path, separator) instead",
	value = "allows to specify the character to use as a separator for a CSV format and returns the file. Yields an error if the file is not a text file",
	examples = @example("let fileT type: file value: text(\"../includes/Stupid_Cell.csv\") as_csv ';';"))
	@Deprecated
	public static
	IGamaFile as_csv(final IScope scope, final IGamaFile file, final String s) throws GamaRuntimeException {
		return new GamaCSVFile(scope, file.getPath(), s);
		// if ( !(file instanceof GamaTextFile) ) { throw GamaRuntimeException
		// .warning("The 'as_csv' operator can only be applied to text files"); }
		// if ( s == null || s.isEmpty() ) { throw GamaRuntimeException
		// .warning("The 'as_csv' operator expects a non-empty string as its right operand"); }
		// ((GamaTextFile) file).setCsvSeparators(s);
		// return file;
	}

	@operator(value = "gamlfile", can_be_const = true, index_type = IType.INT)
	@doc(deprecated = "use gaml_file instead",
	value = "opens a file that a is a kind of model file.",
	comment = "The file should have a shapefile extension, cf. file type definition for supported file extensions.",
	special_cases = "If the specified string does not refer to an existing shapefile file, an exception is risen.",
	examples = { @example("let fileT type: file value: shapefile(\"../includes/testProperties.shp\");"),
		@example("            // fileT represents the shapefile file \"../includes/testProperties.shp\"") },
		see = { "file", "properties", "image", "text" })
	@Deprecated
	public static
	IGamaFile gamlFile(final IScope scope, final String s) throws GamaRuntimeException {
		return new GAMLFile(scope, s);
	}

	@operator(value = "gridfile", can_be_const = true, index_type = IType.INT)
	@doc(deprecated = "use grid_file instead",
	value = "opens a file that a is a kind of shapefile.",
	comment = "The file should have a gridfile extension, cf. file type definition for supported file extensions.",
	special_cases = "If the specified string does not refer to an existing gridfile file, an exception is risen.",
	examples = { @example("file fileT <- gridfile(\"../includes/testProperties.asc\");"),
		@example("            // fileT represents the gridfile file \"../includes/testProperties.asc\"") },
		see = { "file", "properties", "image", "text", "shapefile" })
	@Deprecated
	public static IGamaFile gridFile(final IScope scope, final String s) throws GamaRuntimeException {
		return new GamaGridFile(scope, s);
	}

	@operator(value = "gridfile", can_be_const = true, index_type = IType.INT)
	@doc(deprecated = "use grid_file instead",
	value = "opens a file that a is a kind of gridfile. The integer parameter allows to specify a coordinate reference system (CRS). If equal to zero, it forces reading the data as alreay projected",
	comment = "The file should have a gridfile extension, cf. file type definition for supported file extensions.",
	special_cases = "If the specified string does not refer to an existing gridfile file, an exception is risen.",
	examples = { @example("file fileT <- gridfile(\"../includes/testProperties.asc\");"),
		@example("            // fileT represents the gridfile file \"../includes/testProperties.asc\"") },
		see = { "file", "properties", "image", "text", "shapefile" })
	@Deprecated
	public static
	IGamaFile gridFile(final IScope scope, final String s, final Integer code) throws GamaRuntimeException {
		return new GamaGridFile(scope, s, code);
	}

	@operator(value = "osmfile", can_be_const = true, index_type = IType.INT)
	@doc(deprecated = "use osm_file instead",
	value = "opens a file that a is a kind of osmfile.",
	comment = "The file should have a osmfile extension, cf. file type definition for supported file extensions.",
	special_cases = "If the specified string does not refer to an existing osmfile file, an exception is risen.",
	examples = { @example("file fileT <- osmfile(\"../includes/testProperties.osm\");"),
		@example("            // fileT represents the osm file \"../includes/testProperties.osm\"") },
		see = { "file", "properties", "image", "text", "shapefile" })
	@Deprecated
	public static IGamaFile osmFile(final IScope scope, final String s) throws GamaRuntimeException {
		return new GamaOsmFile(scope, s);
	}

	@operator(value = "osmfile", can_be_const = true, index_type = IType.INT)
	@doc(deprecated = "use osm_file instead",
	value = "opens a file that a is a kind of osmfile, specifying an optional CRS EPSG code",
	comment = "The file should have an osmfile extension, cf. file type definition for supported file extensions.",
	special_cases = "If the specified string does not refer to an existing osmfile file, an exception is risen.",
	examples = { @example("file fileT <- osmfile(\"../includes/testProperties.osm\", 4326);"),
		@example("            // fileT represents the osm file \"../includes/testProperties.osm\"") },
		see = { "file", "properties", "image", "text", "shapefile" })
	@Deprecated
	public static IGamaFile osmFile(final IScope scope, final String s, final Integer i) throws GamaRuntimeException {
		return new GamaOsmFile(scope, s, i);
	}

	@operator(value = "image", can_be_const = true, index_type = IType.POINT)
	@doc(deprecated = "use image_file instead",
	value = "opens a file that is a kind of image.",
	comment = "The file should have an image extension, cf. file type deifnition for supported file extensions.",
	special_cases = "If the specified string does not refer to an existing image file, an exception is risen.",
	examples = { @example("let fileT type: file value: image(\"../includes/testImage.png\");  // fileT represents the file \"../includes/testShape.png\"") },
	see = { "file", "shapefile", "properties", "text" })
	@Deprecated
	public static
	IGamaFile imageFile(final IScope scope, final String s) throws GamaRuntimeException {
		return new GamaImageFile(scope, s);
	}

	@operator(value = "read",
		type = ITypeProvider.FIRST_TYPE,
		content_type = ITypeProvider.FIRST_CONTENT_TYPE,
		index_type = ITypeProvider.FIRST_KEY_TYPE)
	@doc(deprecated = "use the operator \"writable\" instead",
	value = "marks the file so that only read operations are allowed.",
	comment = "A file is created by default in read-only mode. The operator write can change the mode.",
	examples = { @example("read(shapefile(\"../images/point_eau.shp\"))  --:  returns a file in read-only mode representing \"../images/point_eau.shp\"") },
	see = { "file", "writable" })
	@Deprecated
	public static
	IGamaFile opRead(final IScope scope, final IGamaFile s) {
		s.setWritable(false);
		return s;
	}

	@operator(value = IKeyword.WRITE,
		type = ITypeProvider.FIRST_TYPE,
		content_type = ITypeProvider.FIRST_CONTENT_TYPE,
		index_type = ITypeProvider.FIRST_KEY_TYPE)
	@doc(deprecated = "use the operator \"writable\" instead",
	value = "marks the file so that read and write operations are allowed.",
	comment = "A file is created by default in read-only mode.",
	examples = { @example("write(shapefile(\"../images/point_eau.shp\"))   --: returns a file in read-write mode representing \"../images/point_eau.shp\"") },
	see = { "file", "writable" })
	@Deprecated
	public static
	IGamaFile opWrite(final IScope scope, final IGamaFile s) {
		s.setWritable(true);
		return s;
	}

	@operator(value = "text", can_be_const = true, index_type = IType.INT)
	@doc(deprecated = "use text_file instead",
	value = "opens a file that a is a kind of text.",
	comment = "The file should have a text extension, cf. file type definition for supported file extensions.",
	special_cases = "If the specified string does not refer to an existing text file, an exception is risen.",
	examples = { @example("let fileT type: file value: text(\"../includes/Stupid_Cell.Data\");"),
		@example("				// fileT represents the text file \"../includes/Stupid_Cell.Data\"") },
		see = { "file", "properties", "image", "shapefile" })
	@Deprecated
	public static IGamaFile textFile(final IScope scope, final String s) throws GamaRuntimeException {
		return new GamaTextFile(scope, s);
	}

	@operator(value = "properties", can_be_const = true, index_type = IType.STRING)
	@doc(deprecated = "use property_file instead",
	value = "opens a file that is a kind of properties.",
	comment = "The file should have a properties extension, cf. type file definition for supported file extensions.",
	special_cases = "If the specified string does not refer to an existing propserites file, an exception is risen.",
	examples = { @example("let fileT type: file value: properties(\"../includes/testProperties.properties\");  // fileT represents the properties file \"../includes/testProperties.properties\"") },
	see = { "file", "shapefile", "image", "text" })
	@Deprecated
	public static
	IGamaFile propertyFile(final IScope scope, final String s) throws GamaRuntimeException {
		return new GamaPropertyFile(scope, s);
	}

	@operator(value = "shapefile", can_be_const = true, index_type = IType.INT)
	@doc(deprecated = "use shape_file instead",
	value = "opens a file that a is a kind of shapefile.",
	comment = "The file should have a shapefile extension, cf. file type definition for supported file extensions.",
	special_cases = "If the specified string does not refer to an existing shapefile file, an exception is risen.",
	examples = { @example("let fileT type: file value: shapefile(\"../includes/testProperties.shp\");"),
		@example("            // fileT represents the shapefile file \"../includes/testProperties.shp\"") },
		see = { "file", "properties", "image", "text" })
	@Deprecated
	public static
	IGamaFile shapeFile(final IScope scope, final String s) throws GamaRuntimeException {
		return new GamaShapeFile(scope, s);
	}

	@operator(value = "shapefile", can_be_const = true, index_type = IType.INT)
	@doc(deprecated = "use shape_file instead",
	value = "opens a file that a is a kind of shapefile, forcing the initial CRS to be the one indicated by the second int parameter (see http://spatialreference.org/ref/epsg/). If this int parameter is equal to 0, the data is considered as already projected",
	comment = "The file should have a shapefile extension, cf. file type definition for supported file extensions.",
	special_cases = "If the specified string does not refer to an existing shapefile file, an exception is risen.",
	examples = { @example("let fileT type: file value: shapefile(\"../includes/testProperties.shp\");"),
		@example("            // fileT represents the shapefile file \"../includes/testProperties.shp\"") },
		see = { "file", "properties", "image", "text" })
	@Deprecated
	public static
	IGamaFile shapeFile(final IScope scope, final String s, final Integer code) throws GamaRuntimeException {
		return new GamaShapeFile(scope, s, code);
	}

	@operator(value = { "add_z" })
	@doc(deprecated = "use set location instead",
	value = "add_z",
	comment = "Return a geometry with a z value" + "The add_z operator set the z value of the whole shape."
		+ "For each point of the cell the same z value is set.",
		examples = { @example("set shape <- shape add_z rnd(100);") },
		see = { "add_z_pt" })
	@Deprecated
	public static IShape add_z(final IShape g, final Double z) {
		GamaPoint p = new GamaPoint(g.getLocation().getX(), g.getLocation().getY(), z);
		g.setLocation(p);
		/*
		 * final Coordinate[] coordinates = g.getInnerGeometry().getCoordinates();
		 * ((GamaPoint) g.getLocation()).z = z;
		 * for ( int i = 0; i < coordinates.length; i++ ) {
		 * coordinates[i].z = z;
		 * }
		 */
		return g;
	}

	@operator(value = "to_java")
	@doc(value = "represents the java way to write an expression in java, depending on its type",
	deprecated = "NOT YET IMPLEMENTED",
	see = { "to_gaml" })
	@Deprecated
	public static String toJava(final Object val) throws GamaRuntimeException {
		throw GamaRuntimeException.error("to_java is not yet implemented");
	}

	// @operator(value = IKeyword.AS_SKILL, type = ITypeProvider.FIRST_TYPE)
	// @doc(value =
	// "casting an object (left-operand) to an agent if the left-operand is an agent having skill specified by the right-operand.",
	// special_cases =
	// "if the object can not be viewed as an agent having skill specified by the right-operand, then a GamaRuntimeException is thrown.")
	// public static IAgent asSkill(final IScope scope, final Object val, final String skill) {
	// if ( isSkill(scope, val, skill) ) { return (IAgent) val; }
	// throw GamaRuntimeException.error("Cast exception: " + val + " can not be viewed as a " + skill);
	// }

	@operator(value = IKeyword.UNKNOWN, can_be_const = true)
	@doc(deprecated = "generated automatically now", value = "returns the operand itself")
	@Deprecated
	public static Object asObject(final Object obj) {
		return obj;
	}

	@operator(value = { "collate" }, content_type = ITypeProvider.FIRST_ELEMENT_CONTENT_TYPE)
	@doc(deprecated = "The idiom 'collate' is considered as deprecated. Please use 'interleave' instead.",
	value = "a new list containing the interleaved elements of the containers contained in the operand",
	comment = "the operand should be a list of lists of elements. The result is a list of elements. ",
	examples = {
		@example("interleave([1,2,4,3,5,7,6,8]) 	--: 	[1,2,3,4,5,7,6,8]"),
		@example("interleave([['e11','e12','e13'],['e21','e22','e23'],['e31','e32','e33']])  --:  [e11,e21,e31,e12,e22,e32,e13,e23,e33]") })
	@Deprecated
	public static
	IList collate(final IScope scope, final IContainer cc) {
		return Containers.interleave(scope, cc);
		// final Iterator it = new Guava.InterleavingIterator(toArray(nullCheck(scope, cc).iterable(scope), Object.class));
		// return GamaListFactory.create(Iterators.toArray(it, Object.class), Types.NO_TYPE);
	}

	@operator(value = "evaluate_with", can_be_const = false, category = { IOperatorCategory.SYSTEM })
	@doc(deprecated = "This operator has been deprecated and there are no plans to replace it soon.",
	value = "evaluates the left-hand java expressions with the map of parameters (right-hand operand)",
	see = { "eval_gaml" })
	public static Object opEvalJava(final IScope scope, final String code, final IExpression parameters) {
		return code;
		// try {
		// GamaMap param;
		// if ( parameters instanceof MapExpression ) {
		// param = ((MapExpression) parameters).getElements();
		// } else {
		// param = GamaMapFactory.create();
		// }
		// final String[] parameterNames = new String[param.size() + 1];
		// final Class[] parameterTypes = new Class[param.size() + 1];
		// final Object[] parameterValues = new Object[param.size() + 1];
		// parameterNames[0] = "scope";
		// parameterTypes[0] = IScope.class;
		// parameterValues[0] = scope;
		// int i = 1;
		// for ( final Object e : param.entrySet() ) {
		// final Map.Entry<IExpression, IExpression> entry = (Map.Entry<IExpression, IExpression>) e;
		// parameterNames[i] = entry.getKey().literalValue();
		// parameterTypes[i] = entry.getValue().getType().toClass();
		// parameterValues[i] = entry.getValue().value(scope);
		// i++;
		// }
		// final ScriptEvaluator se = new ScriptEvaluator();
		// se.setReturnType(Object.class);
		// se.setDefaultImports(gamaDefaultImports);
		// se.setParameters(parameterNames, parameterTypes);
		// se.cook(code);
		// // Evaluate script with actual parameter values.
		// return se.evaluate(parameterValues);
		//
		// } catch (final Exception e) {
		// final Throwable ee =
		// e instanceof InvocationTargetException ? ((InvocationTargetException) e).getTargetException() : e;
		// GuiUtils.informConsole("Error in evaluating Java code : '" + code + "' in " + scope.getAgentScope() +
		// java.lang.System.getProperty("line.separator") + "Reason: " + ee.getMessage());
		// return null;
		// }
	}

	@operator(value = { IKeyword.AT, "@" }, type = ITypeProvider.FIRST_CONTENT_TYPE)
	@doc(deprecated = "The use of at on a species is deprecated, please use it one a population instead (list(species_name) instead of species_name)")
	public static
	IAgent at(final IScope scope, final ISpecies s, final GamaPoint val) throws GamaRuntimeException {
		return scope.getAgentScope().getPopulationFor(s).getAgent(scope, val);
	}

	// @doc(deprecated = "use 'is_property' instead", value =
	// "the operator tests whether the operand represents the name of a supported properties file", comment =
	// "cf. file type definition for supported (espacially image) file extensions.", examples = {
	// "is_properties(\"../includes/Stupid_Cell.Data\")    --:  false;",
	// "is_properties(\"../includes/test.png\")            --:  false;",
	// "is_properties(\"../includes/test.properties\")     --:  true;",
	// "is_properties(\"../includes/test.shp\")            --:  false;" }, see = { "properties", "is_text",
	// "is_shape", "is_image" })
	@operator(value = "is_properties")
	@doc(deprecated = "use 'is_property' instead")
	@Deprecated
	public static Boolean isProperties(final String f) {
		return GamaFileType.verifyExtension("property", f);
	}

	// @doc(deprecated = "use 'is_gaml' instead", value =
	// "the operator tests whether the operand represents the name of a supported gamlfile", comment =
	// "cf. file type definition for supported (espacially model) file extensions.", examples = {
	// "is_shape(\"../includes/Stupid_Cell.Data\")    --:  false;",
	// "is_shape(\"../includes/test.png\")            --:  false;",
	// "is_shape(\"../includes/test.properties\")     --:  false;",
	// "is_shape(\"../includes/test.gaml\")            --:  true;" }, see = { "image", "is_text", "is_properties",
	// "is_image" })
	@operator(value = "is_GAML")
	@doc(deprecated = "use 'is_gaml' instead")
	@Deprecated
	public static Boolean isGAML(final String f) {
		return GamaFileType.verifyExtension("gaml", f);
	}

	@Deprecated
	@operator(value = "neighbors_of", content_type = IType.AGENT, category = { IOperatorCategory.SPATIAL,
		IOperatorCategory.SP_QUERIES })
	/* TODO, expected_content_type = { IType.FLOAT, IType.INT } */
	@doc(deprecated = "Use 'neighbours_of(topology, agent, distance)' instead",
	usages = @usage(value = "a list, containing all the agents of the same species than the key of the pair argument (if it is an agent) located at a distance inferior or equal to the right member (float) of the pair (right-hand operand) to the left member (agent, geometry or point) considering the left-hand operand topology.",
	examples = { @example(value = "topology(self) neighbors_of self::10",
	equals = "all the agents located at a distance lower or equal to 10 to the agent applying the operator considering its topology.",
	test = false) }))
	public static
	IList neighbours_of(final IScope scope, final ITopology t, final GamaPair pair) {
		if ( pair == null ) { return GamaListFactory.EMPTY_LIST; }
		Object agent = pair.key;
		return Spatial.Queries._neighbours(scope,
			agent instanceof IAgent ? In.list(scope, ((IAgent) agent).getPopulation()) : Different.with(), agent,
				pair.value, t);
		// TODO We could compute a filter based on the population if it is an agent
	}

	@Deprecated
	@operator(value = "covered_by", category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_PROPERTIES })
	@doc(deprecated = "Use 'g2 covers g1' instead of 'g1 covered_by g2'",
	value = "A boolean, equal to true if the left-geometry (or agent/point) is covered by the right-geometry (or agent/point).",
	usages = { @usage("if one of the operand is null, returns false.") },
	examples = { @example(value = "square(5) covered_by square(2)", equals = "false") },
	see = { "disjoint_from", "crosses", "overlaps", "partially_overlaps", "touches" })
	public static
	Boolean covered_by(final IShape g1, final IShape g2) {
		return Spatial.Properties.covers(g2, g1);
	}

	@Deprecated
	@operator(value = { "copy_between" /* , "copy" */},
		can_be_const = true,
		content_type = ITypeProvider.FIRST_CONTENT_TYPE,
		category = { IOperatorCategory.LIST })
	@doc(deprecated = "Deprecated. Use copy_between(list, int, int) instead")
	public static IList copy_between(final IScope scope, final IList l1, final GamaPoint p) {
		return Containers.copy_between(scope, l1, (int) nullCheck(scope, p).x, (int) p.y);
	}

	@Deprecated
	@operator(value = "add_z", can_be_const = true, category = IOperatorCategory.POINT)
	@doc(deprecated = "This idiom is deprecated. Use the standard construction {x,y,z} instead. ")
	public static ILocation add_z(final GamaPoint p, final Double z) {
		return new GamaPoint(p.x, p.y, z);
	}

	@Deprecated
	@operator(value = "add_z", can_be_const = true, category = IOperatorCategory.POINT)
	@doc(deprecated = "This idiom is deprecated. Use the standard construction {x,y,z} instead. ")
	public static ILocation add_z(final GamaPoint p, final Integer z) {
		return new GamaPoint(p.x, p.y, z);
	}

	@Deprecated
	@operator(value = "binomial", category = { IOperatorCategory.RANDOM })
	@doc(deprecated = " Use binomial(int, float) instead",
		value = "A value from a random variable following a binomial distribution. The operand {n,p} represents the number of experiments n and the success probability p.",
		comment = "The binomial distribution is the discrete probability distribution of the number of successes in a sequence of n independent yes/no experiments, each of which yields success with probability p, cf. Binomial distribution on Wikipedia.",
		examples = { @example(value = "binomial({15,0.6})", equals = "a random positive integer", test = false) },
		see = { "poisson", "gauss" })
	public static
		Integer opBinomial(final IScope scope, final GamaPoint point) {
		final Integer n = (int) point.x;
		final Double p = point.y;
		return Random.opBinomial(scope, n, p);
	}

	@Deprecated
	@operator(value = "rnd_float")
	@doc(deprecated = "Use rnd instead with a float argument", examples = { @example(value = "rnd_float(3)",
		equals = "a random float between 0.0 and 3.0",
		test = false) }, see = { "rnd" })
	public static Double opRndFloat(final IScope scope, final Double max) {
		return Random.opRnd(scope, max);
	}

	@operator(value = { "rgbcube" }, category = { IOperatorCategory.SPATIAL, IOperatorCategory.SHAPE })
	@doc(deprecated = "use rgb_cube instead",
		value = "A cube geometry which side size is equal to the operand.",
	usages = { @usage(value = "returns nil if the operand is nil.") },
	comment = "the centre of the cube is by default the location of the current agent in which has been called this operator.",
	examples = { @example(value = "cube(10)", equals = "a geometry as a square of side size 10.", test = false) },
	see = { "rgb_cube" })
	public static
	IShape rgbcube_deprecated(final IScope scope, final Double side_size) {
		return Spatial.Creation.rgbcube(scope, side_size);
	}

	@operator(value = "rgbtriangle", category = { IOperatorCategory.SPATIAL, IOperatorCategory.SHAPE })
	@doc(deprecated = "use rgb_triangle instead",
		value = "A triangle geometry which side size is given by the operand.",
	usages = { @usage("returns nil if the operand is nil.") },
	comment = "the centre of the triangle is by default the location of the current agent in which has been called this operator.",
	examples = { @example(value = "triangle(5)",
	equals = "a geometry as a triangle with side_size = 5.",
	test = false) }, see = { "rgb_triangle" })
	public static
	IShape rgbtriangle_deprecated(final IScope scope, final Double side_size) {
		return Spatial.Creation.rgbtriangle(scope, side_size);
	}

	@Deprecated
	@operator(value = { "add_z_pt" }, category = { IOperatorCategory.SPATIAL, IOperatorCategory.THREED })
	@doc(deprecated = "Use 'set_z' instead",
	value = "add_z_pt",
	comment = "Return a geometry with a z value",
	examples = { @example("loop i from: 0 to: length(shape.points) - 1{" + "shape <- shape add_z_pt {i,valZ};"
		+ "}") },
		see = { "add_z" })
	public static IShape add_z_pt(final IShape geom, final GamaPoint data) {
		geom.getInnerGeometry().getCoordinates()[(int) data.x].z = data.y;
		return geom;
	}

	@operator(value = "R_compute_param",
		can_be_const = false,
		type = IType.MAP,
		content_type = IType.LIST,
		index_type = IType.STRING,
		category = { IOperatorCategory.STATISTICAL })
	@doc(deprecated = "Use R_file instead",
	value = "returns the value of the last left-hand operand of given R file (right-hand operand) in given vector  (left-hand operand), R file (first right-hand operand) reads the vector (second right-hand operand) as the parameter vector",
	examples = { @example(value = "file f <- file('AddParam.r');", isTestOnly = true),
		@example(value = "save \"v1 <- vectorParam[1];\" to: f.path;", isTestOnly = true),
		@example(value = "save \"v2<-vectorParam[2];\" to: f.path;", isTestOnly = true),
		@example(value = "save \"v3<-vectorParam[3];\" to: f.path;", isTestOnly = true),
		@example(value = "save \"result<-v1+v2+v3;\" to: f.path;", isTestOnly = true),
		@example("list<int> X <- [2, 3, 1];"),
		@example(value = "R_compute_param('AddParam.R', X)", var = "result", equals = "['result'::['6']]"),
		@example("////// AddParam.R file:"), @example("// v1 <- vectorParam[1];"),
		@example("// v2<-vectorParam[2];"), @example("// v3<-vectorParam[3];"), @example("// result<-v1+v2+v3;"),
		@example("////// Output:"), @example("// 'result'::[6]") })
	@Deprecated
	public static
	GamaMap operateRFileEvaluate(final IScope scope, final String RFile, final IContainer param)
		throws GamaRuntimeException, RCallerParseException, RCallerExecutionException {
		RFile obj = new RFile(scope, RFile, param);
		return obj.getContents(scope);
	}

	@Deprecated
	@operator(value = { "copy_between" /* , "copy" */}, can_be_const = true, category = { IOperatorCategory.STRING })
	@doc(deprecated = "Deprecated. Use copy_between(string, int, int) instead")
	public static String opCopy(final String target, final GamaPoint p) {
		final int beginIndex = (int) p.x;
		final int endIndex = (int) p.y;
		return Strings.opCopy(target, beginIndex, endIndex);
	}

	@operator(value = { "neighbours_of" },
		type = IType.LIST,
		content_type = ITypeProvider.FIRST_KEY_TYPE,
		category = { IOperatorCategory.GRAPH })
	@doc(deprecated = "use neighbors_of instead",
		value = "returns the list of neighbours of the given vertex (right-hand operand) in the given graph (left-hand operand)",
		examples = {
			@example(value = "graphEpidemio neighbours_of (node(3))", equals = "[node0,node2]", isExecutable = false),
			@example(value = "graphFromMap neighbours_of node({12,45})",
				equals = "[{1.0,5.0},{34.0,56.0}]",
				isExecutable = false) },
		see = { "neighbors_of" })
	public static
		IList neighboursOf_deprecated(final IScope scope, final IGraph graph, final Object vertex) {
		return Graphs.neighboursOf(scope, graph, vertex);
	}

	@operator(value = "R_compute",
		can_be_const = false,
		content_type = IType.LIST,
		index_type = IType.STRING,
		category = { IOperatorCategory.STATISTICAL })
	@doc(deprecated = "Use R_file instead",
		value = "returns the value of the last left-hand operand of given R file (right-hand operand) in given vector  (left-hand operand).",
		examples = {
			@example(value = "file f <- file('Correlation.r');", isTestOnly = true),
			@example(value = "save \"x <- c(1, 2, 3);\" to: f.path;", isTestOnly = true),
			@example(value = "save \"y <- c(1, 2, 4);\" to: f.path;", isTestOnly = true),
			@example(value = "save \"result<- cor(x, y);\" to: f.path;", isTestOnly = true),
			@example(value = "R_compute('Correlation.R')", var = "result", equals = "['result'::['0.981980506061966']]"),
			@example("////// Correlation.R file:"), @example("// x <- c(1, 2, 3);"), @example("// y <- c(1, 2, 4);"),
			@example("// result <- cor(x, y);"), @example("// Output:"), @example("// result::[0.981980506061966]") })
	@Deprecated
	public static
		GamaMap opRFileEvaluate(final IScope scope, final String RFile) throws GamaRuntimeException,
			RCallerParseException, RCallerExecutionException {
		RFile obj = new RFile(scope, RFile);
		return obj.getContents(scope);
	}

	@Deprecated
	@operator(value = "path_between", content_type = ITypeProvider.FIRST_CONTENT_TYPE, category = {
		IOperatorCategory.GRAPH, IOperatorCategory.PATH })
	@doc(value = "The shortest path between a list of two objects in a graph",
	examples = { @example(value = "my_graph path_between (ag1:: ag2)",
	equals = "A path between ag1 and ag2",
	isExecutable = false) }, deprecated = "use 'path_between(graph, geometry, geometry)' instead")
	public static IPath path_between(final IScope scope, final GamaGraph graph, final GamaPair sourTarg)
		throws GamaRuntimeException {
		// java.lang.System.out.println("Cast.asTopology(scope, graph) : " + Cast.asTopology(scope, graph));
		return Graphs.path_between(scope, graph, (IShape) sourTarg.key, (IShape) sourTarg.value);

		// return graph.computeShortestPathBetween(sourTarg.key, sourTarg.value);

	}

	@Deprecated
	@operator(value = "rewire_p", category = { IOperatorCategory.GRAPH })
	@doc(value = "Rewires a graph (in the Watts-Strogatz meaning)", deprecated = "Does not work now", examples = {
		@example(value = "graph graphEpidemio <- graph([]);", isTestOnly = true),
		@example(value = "graphEpidemio rewire_p 0.2", test = false) }, see = "rewire_p")
	public static IGraph rewireGraph(final IScope scope, final IGraph g, final Double probability) {
		GraphAlgorithmsHandmade.rewireGraphProbability(scope, g, probability);
		g.incVersion();
		return g;
	}
}
