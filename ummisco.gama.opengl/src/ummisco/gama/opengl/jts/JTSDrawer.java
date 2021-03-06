/*********************************************************************************************
 * 
 * 
 * 'JTSDrawer.java', in plugin 'msi.gama.jogl2', is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2014 UMI 209 UMMISCO IRD/UPMC & Partners
 * 
 * Visit https://code.google.com/p/gama-platform/ for license information and developers contact.
 * 
 * 
 **********************************************************************************************/
package ummisco.gama.opengl.jts;

import java.awt.Color;
import java.util.*;
import javax.vecmath.Vector3d;
import msi.gama.common.util.GeometryUtils;
import msi.gama.common.util.GuiUtils;
import msi.gama.metamodel.shape.*;
import msi.gama.runtime.GAMA;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import ummisco.gama.opengl.JOGLRenderer;
import ummisco.gama.opengl.scene.GeometryObject;
import ummisco.gama.opengl.utils.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.glu.*;
import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.util.texture.Texture;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.simplify.DouglasPeuckerSimplifier;

public class JTSDrawer {

	// OpenGL member
	// protected final GLU myGlu;
	private final GLUT myGlut;
	public TessellCallBack tessCallback;
	private final GLUtessellator tobj;
	private static boolean useJTSForTriangulation = false;

	// need to have the GLRenderer to enable texture mapping.
	public JOGLRenderer renderer;

	JTSVisitor visitor;

	// FIXME: Is it better to declare an objet polygon here than in
	// DrawMultiPolygon??
	Polygon curPolygon;
	int numGeometries;

	double tempPolygon[][];
	// double temp[];

	// Use for JTS triangulation
	List<IShape> triangles;
	// Iterator<IShape> it;

	// USe to inverse y composaant
	public int yFlag;

	/** The earth texture. */
	// private Texture earthTexture;
	// public float textureTop, textureBottom, textureLeft, textureRight;
	public Texture[] textures = new Texture[3];
	// Use for texture mapping;
	// BufferedImage image = null;
	// Texture texture = null;

	public boolean colorpicking = false;

	public boolean bigPolygonDecomposition = true;
	public int nbPtsForDecomp = 2000;

	public JTSDrawer(final JOGLRenderer gLRender) {

		// myGlu = glu;
		myGlut = new GLUT();
		renderer = gLRender;
		tessCallback = new TessellCallBack(renderer.getGlu());
		tobj = GLU.gluNewTess();

		GLU.gluTessCallback(tobj, GLU.GLU_TESS_VERTEX, tessCallback);// glVertex3dv);
		GLU.gluTessCallback(tobj, GLU.GLU_TESS_BEGIN, tessCallback);// beginCallback);
		GLU.gluTessCallback(tobj, GLU.GLU_TESS_END, tessCallback);// endCallback);

		visitor = new JTSVisitor();

		yFlag = -1;

		// FIXME: When using erroCallback there is a out of memory problem.
		// myGlu.gluTessCallback(tobj, GLU.GLU_TESS_ERROR, tessCallback);//
		// errorCallback)

	}

	public void drawMultiPolygon(final MultiPolygon polygons, final Color c, final double alpha, final boolean fill,
		final Color border, final boolean isTextured, final GeometryObject object,/* final Integer angle, */
		final double height, final boolean rounded, final double z_fighting_value) {

		numGeometries = polygons.getNumGeometries();

		for ( int i = 0; i < numGeometries; i++ ) {
			curPolygon = (Polygon) polygons.getGeometryN(i);

			if ( height > 0 ) {
				DrawPolyhedre(curPolygon, c, alpha, fill, height,/* angle, */false, border, isTextured, object, rounded,
					z_fighting_value);
			} else {
				DrawPolygon(curPolygon, c, alpha, fill, border, isTextured, object, /* angle, */true, rounded,
					z_fighting_value, 1);
			}
		}
	}

	public void drawGeometryCollection(final GeometryCollection geoms, final Color c, final double alpha,
		final boolean fill, final Color border, final boolean isTextured, final GeometryObject object,/* final Integer angle, */
		final double height, final boolean rounded, final double z_fighting_value, final double z) {

		numGeometries = geoms.getNumGeometries();

		for ( int i = 0; i < numGeometries; i++ ) {
			Geometry geom = geoms.getGeometryN(i);
			if ( geom instanceof Polygon ) {
				curPolygon = (Polygon) geom;
				if ( height > 0 ) {
					DrawPolyhedre(curPolygon, c, alpha, fill, height,/* angle, */false, border, isTextured, object,
						rounded, z_fighting_value);
				} else {
					DrawPolygon(curPolygon, c, alpha, fill, border, isTextured, object, /* angle, */true, rounded,
						z_fighting_value, 1);
				}
			} else if ( geom instanceof LineString ) {
				LineString l = (LineString) geom;
				if ( height > 0 ) {
					drawPlan(l, z, c, alpha, height, 0, true);
				} else {
					drawLineString(l, z, 1.2f, c, alpha);
				}
			}
		}
	}

	public void setColor(final Color c, final double alpha) {
		if ( c == null ) { return; }
		GL2 gl = GLContext.getCurrentGL().getGL2();
		gl.glColor4d(c.getRed() / 255.0, c.getGreen() / 255.0, c.getBlue() / 255.0, alpha * c.getAlpha() / 255.0);
	}

	public void DrawPolygon(final Polygon p, final Color c, final double alpha, final boolean fill, final Color border,
		final boolean isTextured, final GeometryObject object,/* final Integer angle, */
		final boolean drawPolygonContour, final boolean rounded, final double z_fighting_value, final int norm_dir) {
		GL2 gl = GLContext.getCurrentGL().getGL2();
		if ( bigPolygonDecomposition && p.getNumPoints() > nbPtsForDecomp ) {
			List<IShape> shapes = GeometryUtils.geometryDecomposition(new GamaShape(p), 2, 2);
			for ( IShape shp : shapes ) {
				if ( shp.getInnerGeometry().getNumGeometries() > 1 ) {
					for ( int i = 0; i < shp.getInnerGeometry().getNumGeometries(); i++ ) {
						DrawPolygon((Polygon) shp.getInnerGeometry().getGeometryN(i), c, alpha, fill, border,
							isTextured, object, drawPolygonContour, rounded, z_fighting_value, norm_dir);

					}

				} else {
					DrawPolygon((Polygon) shp.getInnerGeometry(), c, alpha, fill, border, isTextured, object,
						drawPolygonContour, rounded, z_fighting_value, norm_dir);
				}
			}
			return;

		}
		// calculate the normal vectors for each of the polygonal facets and then average the normal
		if ( renderer.getComputeNormal() ) {
		  Vertex[] vertices = getExteriorRingVertices(p);
		  GLUtilNormal.HandleNormal(vertices, c, alpha, norm_dir, renderer);
		}
		

		if ( isTextured == false ) {

			if ( fill == true ) {

				if ( !colorpicking ) {
					setColor(c, alpha);
				}

				if ( rounded == true ) {
					drawRoundRectangle(p);
				} else {
					// if ( renderer.data.isTesselation() ) {
					// DrawTesselatedPolygon(p, norm_dir, c, alpha);
					// gl.glColor4d(0.0d, 0.0d, 0.0d, alpha);
					// if ( drawPolygonContour == true ) {
					// DrawPolygonContour(p, border, alpha, z_fighting_value);
					// }
					// }
					// use JTS triangulation on simplified geometry (DouglasPeucker)
					// FIXME: not working with a z_layer value!!!!
					// else {
					// AD 21/5 Use of tesselaton everywhere works better than JTS triangulation (see Issue 1130)
					DrawTesselatedPolygon(p, norm_dir, c, alpha);
					// drawTriangulatedPolygon(p, useJTSForTriangulation, null);
					gl.glColor4d(0.0d, 0.0d, 0.0d, alpha);
					if ( drawPolygonContour == true ) {
						DrawPolygonContour(p, border, alpha, z_fighting_value);
						// }
					}
				}
			}

			else { // fill = false. Draw only the contour of the polygon.
				boolean testZFight = false;
				if ( !testZFight ) {

					// if no border has been define draw empty shape with their original color
					if ( border.equals(Color.black) ) {
						DrawPolygonContour(p, c, alpha, z_fighting_value);
					} else {
						DrawPolygonContour(p, border, alpha, z_fighting_value);
					}
				} else {
					gl.glBegin(GL2GL3.GL_QUADS);
					gl.glVertex3d(p.getExteriorRing().getCoordinateN(0).x, -p.getExteriorRing().getCoordinateN(0).y, p
						.getExteriorRing().getCoordinateN(0).z);
					gl.glVertex3d(p.getExteriorRing().getCoordinateN(1).x, -p.getExteriorRing().getCoordinateN(1).y, p
						.getExteriorRing().getCoordinateN(1).z);
					gl.glVertex3d(p.getExteriorRing().getCoordinateN(2).x, -p.getExteriorRing().getCoordinateN(2).y, p
						.getExteriorRing().getCoordinateN(2).z);
					gl.glVertex3d(p.getExteriorRing().getCoordinateN(3).x, -p.getExteriorRing().getCoordinateN(3).y, p
						.getExteriorRing().getCoordinateN(3).z);
					gl.glEnd();
				}
			}
		}

		// FIXME: Need to check that the polygon is a quad
		else {
			Texture texture = object.getTexture(gl, renderer, 0);
			if ( texture != null ) {
				DrawTexturedPolygon(p, texture);
			}
		}
	}

	void DrawTesselatedPolygon(final Polygon p, final int norm_dir, final Color c, final double alpha) {
		GL2 gl = GLContext.getCurrentGL().getGL2();
		GLU.gluTessBeginPolygon(tobj, null);

		// Exterior contour
		GLU.gluTessBeginContour(tobj);

		if ( renderer.getComputeNormal() ) {
			Vertex[] vertices = getExteriorRingVertices(p);

			double[] normalmean = new double[3];
			for ( int i = 0; i < vertices.length - 2; i++ ) {
				double[] normal = GLUtilNormal.CalculateNormal(vertices[i + 2], vertices[i + 1], vertices[i]);
				normalmean[0] = (normalmean[0] + normal[0]) / (i + 1);
				normalmean[1] = (normalmean[1] + normal[1]) / (i + 1);
				normalmean[2] = (normalmean[2] + normal[2]) / (i + 1);
			}

			if ( renderer.data.isDraw_norm() ) {
				Vertex center = GLUtilNormal.GetCenter(vertices);
				gl.glBegin(GL.GL_LINES);
				gl.glColor3d(1.0, 0.0, 0.0);
				gl.glVertex3d(center.x, center.y, center.z);
				gl.glVertex3d(center.x + normalmean[0] * norm_dir, center.y + normalmean[1] * norm_dir, center.z +
					normalmean[2] * norm_dir);
				gl.glEnd();

				gl.glPointSize(2.0f);
				gl.glBegin(GL.GL_POINTS);
				gl.glVertex3d(center.x + normalmean[0] * norm_dir, center.y + normalmean[1] * norm_dir, center.z +
					normalmean[2] * norm_dir);
				gl.glEnd();

				if ( !colorpicking ) {
					if ( c != null ) {
						setColor(c, alpha);
					}

				}
			}

			GLU.gluTessNormal(tobj, normalmean[0] * norm_dir, normalmean[1] * norm_dir, normalmean[2] * norm_dir);
		}

		tempPolygon = new double[p.getExteriorRing().getNumPoints()][3];
		// Convert vertices as a list of double for gluTessVertex
		for ( int j = 0; j < p.getExteriorRing().getNumPoints(); j++ ) {
			tempPolygon[j][0] = p.getExteriorRing().getPointN(j).getX();
			tempPolygon[j][1] = yFlag * p.getExteriorRing().getPointN(j).getY();

			if ( Double.isNaN(p.getExteriorRing().getPointN(j).getCoordinate().z) == true ) {
				tempPolygon[j][2] = 0.0d;
			} else {
				tempPolygon[j][2] = 0.0d + p.getExteriorRing().getPointN(j).getCoordinate().z;
			}
		}

		for ( int j = 0; j < p.getExteriorRing().getNumPoints(); j++ ) {
			GLU.gluTessVertex(tobj, tempPolygon[j], 0, tempPolygon[j]);
		}

		GLU.gluTessEndContour(tobj);

		// interior contour
		for ( int i = 0; i < p.getNumInteriorRing(); i++ ) {
			GLU.gluTessBeginContour(tobj);
			int numIntPoints = p.getInteriorRingN(i).getNumPoints();
			tempPolygon = new double[numIntPoints][3];
			// Convert vertices as a list of double for gluTessVertex
			for ( int j = 0; j < numIntPoints; j++ ) {
				tempPolygon[j][0] = p.getInteriorRingN(i).getPointN(j).getX();
				tempPolygon[j][1] = yFlag * p.getInteriorRingN(i).getPointN(j).getY();

				if ( Double.isNaN(p.getInteriorRingN(i).getPointN(j).getCoordinate().z) == true ) {
					tempPolygon[j][2] = 0.0d;
				} else {
					tempPolygon[j][2] = 0.0d + p.getInteriorRingN(i).getPointN(j).getCoordinate().z;
				}
			}

			for ( int j = 0; j < numIntPoints; j++ ) {
				GLU.gluTessVertex(tobj, tempPolygon[j], 0, tempPolygon[j]);
			}
			GLU.gluTessEndContour(tobj);
		}

		GLU.gluTessEndPolygon(tobj);
	}

	void drawTriangulatedPolygon(Polygon p, final boolean showTriangulation, final Texture texture) {
		boolean simplifyGeometry = false;
		if ( simplifyGeometry ) {
			double sizeTol = Math.sqrt(p.getArea()) / 100.0;
			Geometry g2 = DouglasPeuckerSimplifier.simplify(p, sizeTol);
			if ( g2 instanceof Polygon ) {
				p = (Polygon) g2;
			}
		}
		// Workaround to compute the z value of each triangle as triangulation
		// create new point during the triangulation that are set with z=NaN
		if ( p.getNumPoints() > 4 ) {
			triangles = GeometryUtils.triangulationSimple(null, p); // VERIFY NULL SCOPE

			List<Geometry> segments = new ArrayList<Geometry>();
			for ( int i = 0; i < p.getNumPoints() - 1; i++ ) {
				Coordinate[] cs = new Coordinate[2];
				cs[0] = p.getCoordinates()[i];
				cs[1] = p.getCoordinates()[i + 1];
				segments.add(GeometryUtils.FACTORY.createLineString(cs));
			}
			for ( IShape tri : triangles ) {
				for ( int i = 0; i < tri.getInnerGeometry().getNumPoints(); i++ ) {
					Coordinate coord = tri.getInnerGeometry().getCoordinates()[i];
					if ( Double.isNaN(coord.z) ) {
						Point pt = GeometryUtils.FACTORY.createPoint(coord);
						double distMin = Double.MAX_VALUE;
						Geometry closestSeg = null;
						for ( Geometry seg : segments ) {
							double dist = seg.distance(pt);
							if ( dist < distMin ) {
								distMin = dist;
								closestSeg = seg;
							}
						}
						Point pt1 = GeometryUtils.FACTORY.createPoint(closestSeg.getCoordinates()[0]);
						Point pt2 = GeometryUtils.FACTORY.createPoint(closestSeg.getCoordinates()[1]);

						double dist1 = pt.distance(pt1);
						double dist2 = pt.distance(pt2);
						// FIXME: Work only for geometry
						coord.z =
							(1 - dist1 / closestSeg.getLength()) * closestSeg.getCoordinates()[0].z +
								(1 - dist2 / closestSeg.getLength()) * closestSeg.getCoordinates()[1].z;
						DrawTriangulatedPolygonShape(p, tri, showTriangulation, texture);
					}
				}
			}
		} else if ( p.getNumPoints() == 4 ) {
			triangles = new ArrayList<IShape>();
			triangles.add(new GamaShape(p));
		}
		for ( IShape tri : triangles ) {
			DrawTriangulatedPolygonShape(p, tri, showTriangulation, texture);
		}
	}

	// FIXME: This function only work for quad (otherwise it draw a gray polygon)
	void DrawTexturedPolygon(final Polygon p,/* final int angle, */final Texture texture) {
		GL2 gl = GLContext.getCurrentGL().getGL2();
		gl.glColor3d(1.0, 1.0, 1.0);// Set the color to white to avoid color and texture mixture
		// Enables this texture's target (e.g., GL_TEXTURE_2D) in the
		texture.enable(gl);
		texture.bind(gl);

		if ( p.getNumPoints() > 5 ) {
			// FIXME AD 05/15 Should we use drawTesselatedPolygon instead ?
			drawTriangulatedPolygon(p, useJTSForTriangulation, texture);
			GAMA.reportError(GAMA.getRuntimeScope(), GamaRuntimeException.warning("Texture can only be applied on quad or rectangle"),false);

			
		} else {
			if ( renderer.getComputeNormal() ) {
			  Vertex[] vertices = this.getExteriorRingVertices(p);
			  GLUtilNormal.HandleNormal(vertices, null, 0, 1, renderer);
			}
			
			gl.glColor3d(1.0, 1.0, 1.0);// Set the color to white to avoid color and texture mixture

			gl.glBegin(GL2GL3.GL_QUADS);
			gl.glTexCoord2f(0.0f, 1.0f);
			gl.glVertex3d(p.getExteriorRing().getPointN(0).getX(), yFlag * p.getExteriorRing().getPointN(0).getY(), p
				.getExteriorRing().getCoordinateN(0).z);

			gl.glTexCoord2f(1.0f, 1.0f);;
			gl.glVertex3d(p.getExteriorRing().getPointN(1).getX(), yFlag * p.getExteriorRing().getPointN(1).getY(), p
				.getExteriorRing().getCoordinateN(1).z);

			gl.glTexCoord2f(1.0f, 0.0f);;
			gl.glVertex3d(p.getExteriorRing().getPointN(2).getX(), yFlag * p.getExteriorRing().getPointN(2).getY(), p
				.getExteriorRing().getCoordinateN(2).z);

			gl.glTexCoord2f(0.0f, 0.0f);
			gl.glVertex3d(p.getExteriorRing().getPointN(3).getX(), yFlag * p.getExteriorRing().getPointN(3).getY(), p
				.getExteriorRing().getCoordinateN(3).z);
			gl.glEnd();
		}

		texture.disable(gl);
	}

	public void DrawPolygonContour(final Polygon p, final Color border, final double alpha,
		final double z_fighting_value) {
		if ( border == null ) { return; }
		GL2 gl = GLContext.getCurrentGL().getGL2();
		// FIXME: when rendering with this method the triangulation does not work anymore
		if ( renderer.data.isZ_fighting() ) {
			gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_LINE);
			// }

			// myGl.glDisable(GL2.GL_POLYGON_OFFSET_FILL);
			gl.glEnable(GL2GL3.GL_POLYGON_OFFSET_LINE);
			gl.glPolygonOffset(0.0f, -(float) (z_fighting_value * 1.1));
			// myGl.glPolygonOffset(0.0f,10.0f);
			gl.glBegin(GL2.GL_POLYGON);
			if ( !colorpicking ) {
				setColor(border, alpha);
			}
			p.getExteriorRing().apply(visitor);
			gl.glEnd();

			if ( p.getNumInteriorRing() > 0 ) {
				// Draw Interior ring
				for ( int i = 0; i < p.getNumInteriorRing(); i++ ) {
					gl.glBegin(GL2.GL_POLYGON);
					p.getInteriorRingN(i).apply(visitor);
					gl.glEnd();
				}
			}

			// myGl.glPolygonMode( GL.GL_FRONT_AND_BACK, GL2GL3.GL_FILL);
			// myGl.glEnable(GL2.GL_POLYGON_OFFSET_FILL);
			gl.glDisable(GL2GL3.GL_POLYGON_OFFSET_LINE);
			if ( !renderer.data.isTriangulation() ) {
				gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_FILL);
			}
		} else {
			gl.glBegin(GL.GL_LINES);
			if ( !colorpicking ) {
				setColor(border, alpha);
			}
			p.getExteriorRing().apply(visitor);
			gl.glEnd();

			if ( p.getNumInteriorRing() > 0 ) {
				// Draw Interior ring
				for ( int i = 0; i < p.getNumInteriorRing(); i++ ) {
					gl.glBegin(GL.GL_LINES);
					p.getInteriorRingN(i).apply(visitor);
					gl.glEnd();
				}
			}
		}
	}

	void SetLine(final Point src, final Point dest, final double z, final boolean hasZValue) {
		GL2 gl = GLContext.getCurrentGL().getGL2();
		if ( hasZValue == false ) {
			gl.glVertex3d(src.getX(), yFlag * src.getY(), z);
			gl.glVertex3d(dest.getX(), yFlag * dest.getY(), z);
		} else {
			gl.glVertex3d(src.getX(), yFlag * src.getY(), z + src.getCoordinate().z);
			gl.glVertex3d(dest.getX(), yFlag * dest.getY(), z + dest.getCoordinate().z);
		}
	}

	public void DrawPolyhedre(final Polygon p, final Color c, final double alpha, final boolean fill,
		final double height, /* final Integer angle, */final boolean drawPolygonContour, final Color border,
		final boolean isTextured, final GeometryObject object, final boolean rounded, final Double z_fighting_value) {
		GL2 gl = GLContext.getCurrentGL().getGL2();
		int face_norm_dir = -1;
		int p_norm_dir = 1;;
		if ( renderer.getComputeNormal() ) {
			Vertex[] vertices = getExteriorRingVertices(p);
			if ( IsClockwise(vertices) ) {
				face_norm_dir = -1;
				p_norm_dir = 1;
			} else {
				face_norm_dir = 1;
				p_norm_dir = -1;
			}
		}

		DrawPolygon(p, c, alpha, fill, border, isTextured, object, drawPolygonContour, rounded, z_fighting_value,
			-p_norm_dir);
		gl.glTranslated(0, 0, height);
		DrawPolygon(p, c, alpha, fill, border, isTextured, object/* ,angle */, drawPolygonContour, rounded,
			z_fighting_value, p_norm_dir);
		gl.glTranslated(0, 0, -height);
		// FIXME : Will be wrong if angle =!0

		if ( isTextured ) {
			if ( object.hasTextures() ) {
				DrawTexturedFaces(p, c, alpha, fill, border, isTextured, object.getTexture(gl, renderer, 1), height,
					drawPolygonContour);
			} else {
				DrawTexturedFaces(p, c, alpha, fill, border, isTextured, object.getTexture(gl, renderer, 0), height,
					drawPolygonContour);
			}

		} else {
			DrawFaces(p, c, alpha, fill, border, isTextured, height, drawPolygonContour, face_norm_dir);
		}

	}

	// //////////////////////////////FACE DRAWER
	// //////////////////////////////////////////////////////////////////////////////////

	/**
	 * Given a polygon this will draw the different faces of the 3D polygon.
	 * 
	 * @param p
	 *            :Base polygon
	 * @param c
	 *            : color
	 * @param height
	 *            : height of the polygon
	 */
	public void DrawFaces(final Polygon p, final Color c, final double alpha, final boolean fill, final Color b,
		final boolean isTextured, final double height, final boolean drawPolygonContour, final int norm_dir) {
		GL2 gl = GLContext.getCurrentGL().getGL2();
		if ( !colorpicking ) {
			setColor(c, alpha);
		}

		double elevation = 0.0d;

		if ( Double.isNaN(p.getExteriorRing().getPointN(0).getCoordinate().z) == false ) {
			elevation = p.getExteriorRing().getPointN(0).getCoordinate().z;
		}

		int curPolyGonNumPoints = p.getExteriorRing().getNumPoints();

		for ( int j = 0; j < curPolyGonNumPoints; j++ ) {
			int k = (j + 1) % curPolyGonNumPoints;
			Vertex[] vertices = getFaceVertices(p, j, k, elevation, height);

			if ( fill ) {
				if ( renderer.getComputeNormal() ) { 
				  GLUtilNormal.HandleNormal(vertices, c, alpha, norm_dir, renderer);
				}
				
				gl.glBegin(GL2GL3.GL_QUADS);
				gl.glVertex3d(vertices[0].x, vertices[0].y, vertices[0].z);
				gl.glVertex3d(vertices[1].x, vertices[1].y, vertices[1].z);
				gl.glVertex3d(vertices[2].x, vertices[2].y, vertices[2].z);
				gl.glVertex3d(vertices[3].x, vertices[3].y, vertices[3].z);
				gl.glEnd();
			}

			if ( drawPolygonContour == true || fill == false ) {

				if ( !colorpicking ) {
					setColor(b, alpha);
				}
				gl.glBegin(GL.GL_LINES);
				gl.glVertex3d(vertices[0].x, vertices[0].y, vertices[0].z);
				gl.glVertex3d(vertices[1].x, vertices[1].y, vertices[1].z);
				gl.glVertex3d(vertices[1].x, vertices[1].y, vertices[1].z);
				gl.glVertex3d(vertices[2].x, vertices[2].y, vertices[2].z);
				gl.glVertex3d(vertices[2].x, vertices[2].y, vertices[2].z);
				gl.glVertex3d(vertices[3].x, vertices[3].y, vertices[3].z);
				gl.glVertex3d(vertices[3].x, vertices[3].y, vertices[3].z);
				gl.glVertex3d(vertices[0].x, vertices[0].y, vertices[0].z);
				gl.glEnd();
				if ( !colorpicking ) {
					setColor(c, alpha);
				}
			}
		}
	}

	/**
	 * Given a polygon this will draw the different faces of the 3D polygon.
	 * 
	 * @param p
	 *            :Base polygon
	 * @param c
	 *            : color
	 * @param height
	 *            : height of the polygon
	 */
	public void DrawTexturedFaces(final Polygon p, final Color c, final double alpha, final boolean fill,
		final Color b, final boolean isTextured, final Texture texture, final double height,
		final boolean drawPolygonContour) {
		GL2 gl = GLContext.getCurrentGL().getGL2();
		texture.enable(gl);
		texture.bind(gl);

		double elevation = 0.0d;

		if ( Double.isNaN(p.getExteriorRing().getPointN(0).getCoordinate().z) == false ) {
			elevation = p.getExteriorRing().getPointN(0).getCoordinate().z;
		}

		int curPolyGonNumPoints = p.getExteriorRing().getNumPoints();

		for ( int j = 0; j < curPolyGonNumPoints; j++ ) {

			int k = (j + 1) % curPolyGonNumPoints;

			Vertex[] vertices = getFaceVertices(p, j, k, elevation, height);		
			GLUtilNormal.HandleNormal(vertices, null, alpha, 1, renderer);
			
			
			gl.glColor3d(0.25, 0.25, 0.25);// Set the color to white to avoid color and texture mixture
			gl.glBegin(GL2GL3.GL_QUADS);
			gl.glColor3d(1.0, 1.0, 1.0);// Set the color to white to avoid color and texture mixture
			gl.glTexCoord2f(0.0f, 0.0f);
			gl.glVertex3d(vertices[0].x, vertices[0].y, vertices[0].z);
			gl.glTexCoord2f(1.0f, 0.0f);
			gl.glVertex3d(vertices[1].x, vertices[1].y, vertices[1].z);
			gl.glTexCoord2f(1.0f, 1.0f);
			gl.glVertex3d(vertices[2].x, vertices[2].y, vertices[2].z);
			gl.glTexCoord2f(0.0f, 1.0f);
			gl.glVertex3d(vertices[3].x, vertices[3].y, vertices[3].z);
			gl.glEnd();

		}
		texture.disable(gl);
	}

	public Vertex[] getFaceVertices(final Polygon p, final int j, final int k, final double elevation,
		final double height) {
		// Build the 4 vertices of the face.
		Vertex[] vertices = new Vertex[4];
		for ( int i = 0; i < 4; i++ ) {
			vertices[i] = new Vertex();
		}
		// FIXME; change double to double in Vertex
		vertices[0].x = p.getExteriorRing().getPointN(j).getX();
		vertices[0].y = yFlag * p.getExteriorRing().getPointN(j).getY();
		vertices[0].z = elevation + height;

		vertices[1].x = p.getExteriorRing().getPointN(k).getX();
		vertices[1].y = yFlag * p.getExteriorRing().getPointN(k).getY();
		vertices[1].z = elevation + height;

		vertices[2].x = p.getExteriorRing().getPointN(k).getX();
		vertices[2].y = yFlag * p.getExteriorRing().getPointN(k).getY();
		vertices[2].z = elevation;

		vertices[3].x = p.getExteriorRing().getPointN(j).getX();
		vertices[3].y = yFlag * p.getExteriorRing().getPointN(j).getY();
		vertices[3].z = elevation;

		return vertices;
	}

	public Vertex[] getTriangleVertices(final Polygon p) {
		// Build the 3 vertices of the face from the 3 first point (maybe wrong in some case).
		Vertex[] vertices = new Vertex[3];
		for ( int i = 0; i < 3; i++ ) {
			vertices[i] = new Vertex();
		}
		// FIXME; change double to double in Vertex
		vertices[0].x = p.getExteriorRing().getPointN(0).getX();
		vertices[0].y = yFlag * p.getExteriorRing().getPointN(0).getY();
		vertices[0].z = p.getExteriorRing().getPointN(0).getCoordinate().z;

		vertices[1].x = p.getExteriorRing().getPointN(1).getX();
		vertices[1].y = yFlag * p.getExteriorRing().getPointN(1).getY();
		vertices[1].z = p.getExteriorRing().getPointN(1).getCoordinate().z;

		vertices[2].x = p.getExteriorRing().getPointN(2).getX();
		vertices[2].y = yFlag * p.getExteriorRing().getPointN(2).getY();
		vertices[2].z = p.getExteriorRing().getPointN(2).getCoordinate().z;

		return vertices;
	}

	public Vertex[] getExteriorRingVertices(final Polygon p) {
		// Build the n vertices of the facet of the polygon.
		Vertex[] vertices = new Vertex[p.getExteriorRing().getNumPoints() - 1];
		for ( int i = 0; i < p.getExteriorRing().getNumPoints() - 1; i++ ) {
			vertices[i] = new Vertex();
			vertices[i].x = p.getExteriorRing().getPointN(i).getX();
			vertices[i].y = yFlag * p.getExteriorRing().getPointN(i).getY();
			vertices[i].z = p.getExteriorRing().getPointN(i).getCoordinate().z;
		}
		return vertices;
	}

	// ////////////////////////////// LINE DRAWER
	// //////////////////////////////////////////////////////////////////////////////////

	public void DrawMultiLineString(final MultiLineString lines, final double z, final Color c, final double alpha,
		final double height) {

		// get the number of line in the multiline.
		numGeometries = lines.getNumGeometries();

		// FIXME: Why setting the color here?
		if ( !colorpicking ) {
			setColor(c, alpha);
		}

		// for each line of a multiline, get each point coordinates.
		for ( int i = 0; i < numGeometries; i++ ) {

			LineString l = (LineString) lines.getGeometryN(i);
			if ( height > 0 ) {
				drawPlan(l, z, c, alpha, height, 0, true);
			} else {
				drawLineString(l, z, 1.2f, c, alpha);
			}

		}
	}

	public void drawLineString(final LineString line, final double z, final double size, final Color c,
		final double alpha) {
		GL2 gl = GLContext.getCurrentGL().getGL2();
		if ( !colorpicking ) {
			setColor(c, alpha);
		}

		int numPoints = line.getNumPoints();

		gl.glLineWidth((float) size);

		// Add z value (if the whole line as a z value (add_z)
		/*
		 * if (Double.isNaN (line.getCoordinate().z) == false) {
		 * z = z + (double) line.getCoordinate().z; }
		 */

		// FIXME: this will draw a 3d line if the z value of each point has been
		// set thanks to add_z_pt but if
		gl.glBegin(GL.GL_LINES);
		for ( int j = 0; j < numPoints - 1; j++ ) {

			if ( Double.isNaN(line.getPointN(j).getCoordinate().z) == true ) {
				gl.glVertex3d(line.getPointN(j).getX(), yFlag * line.getPointN(j).getY(), z);

			} else {
				gl.glVertex3d(line.getPointN(j).getX(), yFlag * line.getPointN(j).getY(), z +
					line.getPointN(j).getCoordinate().z);
			}
			if ( Double.isNaN(line.getPointN(j + 1).getCoordinate().z) == true ) {
				gl.glVertex3d(line.getPointN(j + 1).getX(), yFlag * line.getPointN(j + 1).getY(), z);
			} else {
				gl.glVertex3d(line.getPointN(j + 1).getX(), yFlag * line.getPointN(j + 1).getY(),
					z + line.getPointN(j + 1).getCoordinate().z);
			}

		}
		gl.glEnd();

	}

	public void drawPlan(final LineString l, double z, final Color c, final double alpha, final double height,
		final Integer angle, final boolean drawPolygonContour) {
		GL2 gl = GLContext.getCurrentGL().getGL2();
		drawLineString(l, z, 1.2f, c, alpha);
		drawLineString(l, z + height, 1.2f, c, alpha);

		// Draw a quad
		gl.glColor4d(c.getRed() / 255.0, c.getGreen() / 255.0, c.getBlue() / 255.0, alpha * c.getAlpha() / 255.0);
		int numPoints = l.getNumPoints();

		// Add z value
		if ( Double.isNaN(l.getCoordinate().z) == false ) {
			z = z + l.getCoordinate().z;
		}

		for ( int j = 0; j < numPoints - 1; j++ ) {

			if ( renderer.getComputeNormal() ) {
				Vertex[] vertices = new Vertex[3];
				for ( int i = 0; i < 3; i++ ) {
					vertices[i] = new Vertex();
				}
				vertices[0].x = l.getPointN(j).getX();
				vertices[0].y = yFlag * l.getPointN(j).getY();
				vertices[0].z = z;

				vertices[1].x = l.getPointN(j + 1).getX();
				vertices[1].y = yFlag * l.getPointN(j + 1).getY();
				vertices[1].z = z;

				vertices[2].x = l.getPointN(j + 1).getX();
				vertices[2].y = yFlag * l.getPointN(j + 1).getY();
				vertices[2].z = z + height;
				GLUtilNormal.HandleNormal(vertices, c, alpha, 1, renderer);
			}

			gl.glBegin(GL2GL3.GL_QUADS);
			gl.glVertex3d(l.getPointN(j).getX(), yFlag * l.getPointN(j).getY(), z);
			gl.glVertex3d(l.getPointN(j + 1).getX(), yFlag * l.getPointN(j + 1).getY(), z);
			gl.glVertex3d(l.getPointN(j + 1).getX(), yFlag * l.getPointN(j + 1).getY(), z + height);
			gl.glVertex3d(l.getPointN(j).getX(), yFlag * l.getPointN(j).getY(), z + height);
			gl.glEnd();
		}

		if ( drawPolygonContour == true ) {
			if ( !colorpicking ) {
				gl.glColor4d(0.0d, 0.0d, 0.0d, alpha * c.getAlpha() / 255.0);
			}

			for ( int j = 0; j < numPoints - 1; j++ ) {
				gl.glBegin(GL.GL_LINES);
				gl.glVertex3d(l.getPointN(j).getX(), yFlag * l.getPointN(j).getY(), z);
				gl.glVertex3d(l.getPointN(j + 1).getX(), yFlag * l.getPointN(j + 1).getY(), z);

				gl.glVertex3d(l.getPointN(j + 1).getX(), yFlag * l.getPointN(j + 1).getY(), z);
				gl.glVertex3d(l.getPointN(j + 1).getX(), yFlag * l.getPointN(j + 1).getY(), z + height);

				gl.glVertex3d(l.getPointN(j + 1).getX(), yFlag * l.getPointN(j + 1).getY(), z + height);
				gl.glVertex3d(l.getPointN(j).getX(), yFlag * l.getPointN(j).getY(), z + height);

				gl.glVertex3d(l.getPointN(j).getX(), yFlag * l.getPointN(j).getY(), z + height);
				gl.glVertex3d(l.getPointN(j).getX(), yFlag * l.getPointN(j).getY(), z);

				gl.glEnd();
			}
			if ( !colorpicking ) {
				setColor(c, alpha);
			}

		}
	}

	public void DrawPoint(final Point point, double z, final int numPoints, final double radius, final Color c,
		final double alpha) {
		GL2 gl = GLContext.getCurrentGL().getGL2();
		if ( !colorpicking ) {
			setColor(c, alpha);
		}

		GLU.gluTessBeginPolygon(tobj, null);
		GLU.gluTessBeginContour(tobj);
		// FIXME: Does not work for Point.
		// Add z value
		if ( Double.isNaN(point.getCoordinate().z) == false ) {
			z = z + point.getCoordinate().z;
		}

		double angle;
		double tempPolygon[][] = new double[100][3];
		for ( int k = 0; k < numPoints; k++ ) {
			angle = k * 2 * Math.PI / numPoints;

			tempPolygon[k][0] = point.getCoordinate().x + Math.cos(angle) * radius;
			tempPolygon[k][1] = yFlag * (point.getCoordinate().y + Math.sin(angle) * radius);
			tempPolygon[k][2] = z;
		}

		for ( int k = 0; k < numPoints; k++ ) {
			GLU.gluTessVertex(tobj, tempPolygon[k], 0, tempPolygon[k]);
		}

		GLU.gluTessEndContour(tobj);
		GLU.gluTessEndPolygon(tobj);

		// Add a line around the circle
		// FIXME/ Check the cost of this line
		if ( !colorpicking ) {
			gl.glColor4d(0.0d, 0.0d, 0.0d, alpha * c.getAlpha() / 255.0);
		}
		gl.glLineWidth(1.1f);
		gl.glBegin(GL.GL_LINES);
		double xBegin, xEnd, yBegin, yEnd;
		for ( int k = 0; k < numPoints; k++ ) {
			angle = k * 2 * Math.PI / numPoints;
			xBegin = point.getCoordinate().x + Math.cos(angle) * radius;
			yBegin = yFlag * (point.getCoordinate().y + Math.sin(angle) * radius);
			angle = (k + 1) * 2 * Math.PI / numPoints;
			xEnd = point.getCoordinate().x + Math.cos(angle) * radius;
			yEnd = yFlag * (point.getCoordinate().y + Math.sin(angle) * radius);
			gl.glVertex3d(xBegin, yBegin, z);
			gl.glVertex3d(xEnd, yEnd, z);
		}
		gl.glEnd();

	}

	// //////////////////////////////SPECIAL 3D SHAPE DRAWER
	// //////////////////////////////////////////////////////////////////////////////////

	public void drawSphere(final GeometryObject g) {
		GL2 gl = GLContext.getCurrentGL().getGL2();
		double z = 0.0;
		Polygon p = (Polygon) g.geometry;
		if ( Double.isNaN(p.getCoordinate().z) == false ) {
			z = p.getExteriorRing().getPointN(0).getCoordinate().z;
		}

		gl.glTranslated(p.getCentroid().getX(), yFlag * p.getCentroid().getY(), z);
		Color c = g.getColor();
		if ( !colorpicking ) {
			setColor(c, g.getAlpha());
		}
		Texture t = null;
		if ( g.isTextured ) {

			if ( g.hasTextures() ) {
				t = g.getTexture(gl, renderer, 1);
			} else {
				t = g.getTexture(gl, renderer, 0);
			}
			t.enable(gl);
			t.bind(gl);
			gl.glColor3d(1.0, 1.0, 1.0);
		}

		GLU glu = renderer.getGlu();

		GLUquadric quad = glu.gluNewQuadric();

		if ( g.isTextured ) {
			glu.gluQuadricTexture(quad, true);
		}

		if ( !renderer.data.isTriangulation() ) {

			glu.gluQuadricDrawStyle(quad, GLU.GLU_FILL);
		} else {
			glu.gluQuadricDrawStyle(quad, GLU.GLU_LINE);
		}
		glu.gluQuadricNormals(quad, GLU.GLU_FLAT);
		if ( g.isTextured ) {
			glu.gluQuadricOrientation(quad, GLU.GLU_INSIDE);
		} else {
			glu.gluQuadricOrientation(quad, GLU.GLU_OUTSIDE);
		}

		final int slices = 16;
		final int stacks = 16;

		glu.gluSphere(quad, g.height, slices, stacks);
		glu.gluDeleteQuadric(quad);
		if ( t != null ) {
			t.disable(gl);
		}

		gl.glTranslated(-p.getCentroid().getX(), -yFlag * p.getCentroid().getY(), -z);

	}

	public void drawCone3D(final GeometryObject g) {
		// (final Polygon p, final double radius, final Color c, final double alpha) {
		// Add z value (Note: getCentroid does not return a z value)
		GL2 gl = GLContext.getCurrentGL().getGL2();
		double z = 0.0;
		Polygon p = (Polygon) g.geometry;
		if ( Double.isNaN(p.getCoordinate().z) == false ) {
			z = p.getExteriorRing().getPointN(0).getCoordinate().z;
		}

		gl.glTranslated(p.getCentroid().getX(), yFlag * p.getCentroid().getY(), z);
		if ( !colorpicking ) {
			Color c = g.getColor();
			setColor(c, g.getAlpha());
		}
		if ( !renderer.data.isTriangulation() ) {
			myGlut.glutSolidCone(g.height, g.height, 10, 10);
		} else {
			myGlut.glutWireCone(g.height, g.height, 10, 10);
		}

		gl.glTranslated(-p.getCentroid().getX(), -yFlag * p.getCentroid().getY(), -z);
	}

	public void drawTeapot(final GeometryObject g) {
		// final Polygon p, final double radius, final Color c, final double alpha) {
		// Add z value (Note: getCentroid does not return a z value)
		GL2 gl = GLContext.getCurrentGL().getGL2();
		double z = 0.0;
		Polygon p = (Polygon) g.geometry;
		if ( !Double.isNaN(p.getCoordinate().z) ) {
			// TODO Normally, the NaN case is not true anymore
			z = p.getExteriorRing().getPointN(0).getCoordinate().z;
		}

		gl.glTranslated(p.getCentroid().getX(), yFlag * p.getCentroid().getY(), z);
		if ( !colorpicking ) {
			setColor(g.getColor(), g.getAlpha());
		}
		gl.glRotated(90, 1.0, 0.0, 0.0);
		myGlut.glutSolidTeapot(g.height);
		gl.glRotated(-90, 1.0, 0.0, 0.0);
		gl.glTranslated(-p.getCentroid().getX(), -yFlag * p.getCentroid().getY(), -z);
	}

	public void drawPyramid(final GeometryObject g) {

		GL2 gl = GLContext.getCurrentGL().getGL2();
		double z = 0.0;
		Polygon p = (Polygon) g.geometry;
		if ( Double.isNaN(p.getCoordinate().z) == false ) {
			z = p.getExteriorRing().getPointN(0).getCoordinate().z;
		}

		gl.glTranslated(0, 0, z);
		if ( !colorpicking ) {
			setColor(g.getColor(), g.getAlpha());;
		}
		PyramidSkeleton(p, g.height, g.getColor(), g.getAlpha());
		// border
		if ( g.border != null ) {
			if ( !colorpicking ) {
				setColor(g.border, g.getAlpha());
			}
			gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_LINE);
			gl.glEnable(GL2GL3.GL_POLYGON_OFFSET_LINE);
			gl.glPolygonOffset(0.0f, -(float) 1.1);
			PyramidSkeleton(p, g.height, g.border, g.getAlpha());
			gl.glDisable(GL2GL3.GL_POLYGON_OFFSET_LINE);
			if ( !renderer.data.isTriangulation() ) {
				gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_FILL);
			}
		}
		gl.glTranslated(0, 0, -z);
	}

	public void drawRGBCube(final GeometryObject g) {
		// final Polygon p, final double radius, final Color c, final double alpha) {
		// Add z value (Note: getCentroid does not return a z value)
		GL2 gl = GLContext.getCurrentGL().getGL2();
		double z = 0.0;
		Polygon p = (Polygon) g.geometry;
		if ( !Double.isNaN(p.getCoordinate().z) ) {
			// TODO Normally, the NaN case is not true anymore
			z = p.getExteriorRing().getPointN(0).getCoordinate().z;
		}

		if ( !colorpicking ) {
			setColor(g.getColor(), g.getAlpha());
		}

		if ( g.picked ) {
			setColor(g.getColor(), g.getAlpha());
			gl.glBegin(GL2GL3.GL_QUADS);
			gl.glNormal3d(0.0, 0.0, -1.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(0).getX(), yFlag * p.getExteriorRing().getPointN(0).getY(),
				0.0d);
			gl.glVertex3d(p.getExteriorRing().getPointN(1).getX(), yFlag * p.getExteriorRing().getPointN(1).getY(),
				0.0d);
			gl.glVertex3d(p.getExteriorRing().getPointN(2).getX(), yFlag * p.getExteriorRing().getPointN(2).getY(),
				0.0d);
			gl.glVertex3d(p.getExteriorRing().getPointN(3).getX(), yFlag * p.getExteriorRing().getPointN(3).getY(),
				0.0d);
			gl.glEnd();

			gl.glBegin(GL2GL3.GL_QUADS);
			gl.glNormal3d(1.0, 0.0, 0.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(1).getX(), yFlag * p.getExteriorRing().getPointN(1).getY(),
				0.0d);
			gl.glVertex3d(p.getExteriorRing().getPointN(2).getX(), yFlag * p.getExteriorRing().getPointN(2).getY(),
				0.0d);
			gl.glVertex3d(p.getExteriorRing().getPointN(2).getX(), yFlag * p.getExteriorRing().getPointN(2).getY(),
				g.height);
			gl.glVertex3d(p.getExteriorRing().getPointN(1).getX(), yFlag * p.getExteriorRing().getPointN(1).getY(),
				g.height);
			gl.glEnd();

			gl.glBegin(GL2GL3.GL_QUADS);
			gl.glNormal3d(0.0, -1.0, 0.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(2).getX(), yFlag * p.getExteriorRing().getPointN(2).getY(),
				0.0d);
			gl.glVertex3d(p.getExteriorRing().getPointN(3).getX(), yFlag * p.getExteriorRing().getPointN(3).getY(),
				0.0d);
			gl.glVertex3d(p.getExteriorRing().getPointN(3).getX(), yFlag * p.getExteriorRing().getPointN(3).getY(),
				g.height);
			gl.glVertex3d(p.getExteriorRing().getPointN(2).getX(), yFlag * p.getExteriorRing().getPointN(2).getY(),
				g.height);
			gl.glEnd();

			gl.glBegin(GL2GL3.GL_QUADS);
			gl.glNormal3d(-1.0, 0.0, 0.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(3).getX(), yFlag * p.getExteriorRing().getPointN(3).getY(),
				0.0d);
			gl.glVertex3d(p.getExteriorRing().getPointN(0).getX(), yFlag * p.getExteriorRing().getPointN(0).getY(),
				0.0d);
			gl.glVertex3d(p.getExteriorRing().getPointN(0).getX(), yFlag * p.getExteriorRing().getPointN(0).getY(),
				g.height);
			gl.glVertex3d(p.getExteriorRing().getPointN(3).getX(), yFlag * p.getExteriorRing().getPointN(3).getY(),
				g.height);
			gl.glEnd();

			gl.glBegin(GL2GL3.GL_QUADS);
			gl.glNormal3d(0.0, 1.0, 0.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(0).getX(), yFlag * p.getExteriorRing().getPointN(0).getY(),
				0.0d);
			gl.glVertex3d(p.getExteriorRing().getPointN(1).getX(), yFlag * p.getExteriorRing().getPointN(1).getY(),
				0.0d);
			gl.glVertex3d(p.getExteriorRing().getPointN(1).getX(), yFlag * p.getExteriorRing().getPointN(1).getY(),
				g.height);
			gl.glVertex3d(p.getExteriorRing().getPointN(0).getX(), yFlag * p.getExteriorRing().getPointN(0).getY(),
				g.height);
			gl.glEnd();

			gl.glBegin(GL2GL3.GL_QUADS);
			gl.glNormal3d(0.0, 0.0, 1.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(0).getX(), yFlag * p.getExteriorRing().getPointN(0).getY(),
				g.height);
			gl.glVertex3d(p.getExteriorRing().getPointN(1).getX(), yFlag * p.getExteriorRing().getPointN(1).getY(),
				g.height);
			gl.glVertex3d(p.getExteriorRing().getPointN(2).getX(), yFlag * p.getExteriorRing().getPointN(2).getY(),
				g.height);
			gl.glVertex3d(p.getExteriorRing().getPointN(3).getX(), yFlag * p.getExteriorRing().getPointN(3).getY(),
				g.height);
			gl.glEnd();

		} else {
			gl.glBegin(GL2GL3.GL_QUADS);
			gl.glNormal3d(0.0, 0.0, -1.0);
			gl.glColor3d(1.0, 0.0, 0.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(0).getX(), yFlag * p.getExteriorRing().getPointN(0).getY(),
				0.0d);
			gl.glColor3d(1.0, 1.0, 0.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(1).getX(), yFlag * p.getExteriorRing().getPointN(1).getY(),
				0.0d);
			gl.glColor3d(0.0, 1.0, 0.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(2).getX(), yFlag * p.getExteriorRing().getPointN(2).getY(),
				0.0d);
			gl.glColor3d(0.0, 0.0, 0.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(3).getX(), yFlag * p.getExteriorRing().getPointN(3).getY(),
				0.0d);
			gl.glEnd();

			gl.glBegin(GL2GL3.GL_QUADS);
			gl.glNormal3d(1.0, 0.0, 0.0);
			gl.glColor3d(1.0, 1.0, 0.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(1).getX(), yFlag * p.getExteriorRing().getPointN(1).getY(),
				0.0d);
			gl.glColor3d(0.0, 1.0, 0.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(2).getX(), yFlag * p.getExteriorRing().getPointN(2).getY(),
				0.0d);
			gl.glColor3d(0.0, 1.0, 1.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(2).getX(), yFlag * p.getExteriorRing().getPointN(2).getY(),
				g.height);
			gl.glColor3d(1.0, 1.0, 1.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(1).getX(), yFlag * p.getExteriorRing().getPointN(1).getY(),
				g.height);
			gl.glEnd();

			gl.glBegin(GL2GL3.GL_QUADS);
			gl.glNormal3d(0.0, -1.0, 0.0);
			gl.glColor3d(0.0, 1.0, 0.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(2).getX(), yFlag * p.getExteriorRing().getPointN(2).getY(),
				0.0d);
			gl.glColor3d(0.0, 0.0, 0.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(3).getX(), yFlag * p.getExteriorRing().getPointN(3).getY(),
				0.0d);
			gl.glColor3d(0.0, 0.0, 1.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(3).getX(), yFlag * p.getExteriorRing().getPointN(3).getY(),
				g.height);
			gl.glColor3d(0.0, 1.0, 1.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(2).getX(), yFlag * p.getExteriorRing().getPointN(2).getY(),
				g.height);
			gl.glEnd();

			gl.glBegin(GL2GL3.GL_QUADS);
			gl.glNormal3d(-1.0, 0.0, 0.0);
			gl.glColor3d(0.0, 0.0, 0.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(3).getX(), yFlag * p.getExteriorRing().getPointN(3).getY(),
				0.0d);
			gl.glColor3d(1.0, 0.0, 0.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(0).getX(), yFlag * p.getExteriorRing().getPointN(0).getY(),
				0.0d);
			gl.glColor3d(1.0, 0.0, 1.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(0).getX(), yFlag * p.getExteriorRing().getPointN(0).getY(),
				g.height);
			gl.glColor3d(0.0, 0.0, 1.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(3).getX(), yFlag * p.getExteriorRing().getPointN(3).getY(),
				g.height);
			gl.glEnd();

			gl.glBegin(GL2GL3.GL_QUADS);
			gl.glNormal3d(0.0, 1.0, 0.0);
			gl.glColor3d(1.0, 0.0, 0.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(0).getX(), yFlag * p.getExteriorRing().getPointN(0).getY(),
				0.0d);
			gl.glColor3d(1.0, 1.0, 0.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(1).getX(), yFlag * p.getExteriorRing().getPointN(1).getY(),
				0.0d);
			gl.glColor3d(1.0, 1.0, 1.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(1).getX(), yFlag * p.getExteriorRing().getPointN(1).getY(),
				g.height);
			gl.glColor3d(1.0, 0.0, 1.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(0).getX(), yFlag * p.getExteriorRing().getPointN(0).getY(),
				g.height);
			gl.glEnd();

			gl.glBegin(GL2GL3.GL_QUADS);
			gl.glNormal3d(0.0, 0.0, 1.0);
			gl.glColor3d(1.0, 0.0, 1.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(0).getX(), yFlag * p.getExteriorRing().getPointN(0).getY(),
				g.height);
			gl.glColor3d(1.0, 1.0, 1.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(1).getX(), yFlag * p.getExteriorRing().getPointN(1).getY(),
				g.height);
			gl.glColor3d(0.0, 1.0, 1.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(2).getX(), yFlag * p.getExteriorRing().getPointN(2).getY(),
				g.height);
			gl.glColor3d(0.0, 0.0, 1.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(3).getX(), yFlag * p.getExteriorRing().getPointN(3).getY(),
				g.height);
			gl.glEnd();
		}

		if ( !colorpicking ) {
			setColor(g.getColor(), g.getAlpha());
		}

		if ( g.picked ) {
			Color c = g.getColor();
			gl.glColor4d((double) c.getRed() / 255, (double) c.getGreen() / 255, (double) c.getBlue() / 255,
				g.getAlpha() * c.getAlpha() / 255);
			gl.glBegin(GL2GL3.GL_QUADS);
			gl.glNormal3d(0.0, 0.0, -1.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(0).getX(), yFlag * p.getExteriorRing().getPointN(0).getY(),
				0.0d);
			gl.glVertex3d(p.getExteriorRing().getPointN(1).getX(), yFlag * p.getExteriorRing().getPointN(1).getY(),
				0.0d);
			gl.glVertex3d(p.getExteriorRing().getPointN(2).getX(), yFlag * p.getExteriorRing().getPointN(2).getY(),
				0.0d);
			gl.glVertex3d(p.getExteriorRing().getPointN(3).getX(), yFlag * p.getExteriorRing().getPointN(3).getY(),
				0.0d);
			gl.glEnd();

			gl.glBegin(GL2GL3.GL_QUADS);
			gl.glNormal3d(1.0, 0.0, 0.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(1).getX(), yFlag * p.getExteriorRing().getPointN(1).getY(),
				0.0d);
			gl.glVertex3d(p.getExteriorRing().getPointN(2).getX(), yFlag * p.getExteriorRing().getPointN(2).getY(),
				0.0d);
			gl.glVertex3d(p.getExteriorRing().getPointN(2).getX(), yFlag * p.getExteriorRing().getPointN(2).getY(),
				g.height);
			gl.glVertex3d(p.getExteriorRing().getPointN(1).getX(), yFlag * p.getExteriorRing().getPointN(1).getY(),
				g.height);
			gl.glEnd();

			gl.glBegin(GL2GL3.GL_QUADS);
			gl.glNormal3d(0.0, -1.0, 0.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(2).getX(), yFlag * p.getExteriorRing().getPointN(2).getY(),
				0.0d);
			gl.glVertex3d(p.getExteriorRing().getPointN(3).getX(), yFlag * p.getExteriorRing().getPointN(3).getY(),
				0.0d);
			gl.glVertex3d(p.getExteriorRing().getPointN(3).getX(), yFlag * p.getExteriorRing().getPointN(3).getY(),
				g.height);
			gl.glVertex3d(p.getExteriorRing().getPointN(2).getX(), yFlag * p.getExteriorRing().getPointN(2).getY(),
				g.height);
			gl.glEnd();

			gl.glBegin(GL2GL3.GL_QUADS);
			gl.glNormal3d(-1.0, 0.0, 0.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(3).getX(), yFlag * p.getExteriorRing().getPointN(3).getY(),
				0.0d);
			gl.glVertex3d(p.getExteriorRing().getPointN(0).getX(), yFlag * p.getExteriorRing().getPointN(0).getY(),
				0.0d);
			gl.glVertex3d(p.getExteriorRing().getPointN(0).getX(), yFlag * p.getExteriorRing().getPointN(0).getY(),
				g.height);
			gl.glVertex3d(p.getExteriorRing().getPointN(3).getX(), yFlag * p.getExteriorRing().getPointN(3).getY(),
				g.height);
			gl.glEnd();

			gl.glBegin(GL2GL3.GL_QUADS);
			gl.glNormal3d(0.0, 1.0, 0.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(0).getX(), yFlag * p.getExteriorRing().getPointN(0).getY(),
				0.0d);
			gl.glVertex3d(p.getExteriorRing().getPointN(1).getX(), yFlag * p.getExteriorRing().getPointN(1).getY(),
				0.0d);
			gl.glVertex3d(p.getExteriorRing().getPointN(1).getX(), yFlag * p.getExteriorRing().getPointN(1).getY(),
				g.height);
			gl.glVertex3d(p.getExteriorRing().getPointN(0).getX(), yFlag * p.getExteriorRing().getPointN(0).getY(),
				g.height);
			gl.glEnd();

			gl.glBegin(GL2GL3.GL_QUADS);
			gl.glNormal3d(0.0, 0.0, 1.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(0).getX(), yFlag * p.getExteriorRing().getPointN(0).getY(),
				g.height);
			gl.glVertex3d(p.getExteriorRing().getPointN(1).getX(), yFlag * p.getExteriorRing().getPointN(1).getY(),
				g.height);
			gl.glVertex3d(p.getExteriorRing().getPointN(2).getX(), yFlag * p.getExteriorRing().getPointN(2).getY(),
				g.height);
			gl.glVertex3d(p.getExteriorRing().getPointN(3).getX(), yFlag * p.getExteriorRing().getPointN(3).getY(),
				g.height);
			gl.glEnd();

		} else {
			gl.glBegin(GL2GL3.GL_QUADS);
			gl.glNormal3d(0.0, 0.0, -1.0);
			gl.glColor3d(1.0, 0.0, 0.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(0).getX(), yFlag * p.getExteriorRing().getPointN(0).getY(),
				0.0d);
			gl.glColor3d(1.0, 1.0, 0.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(1).getX(), yFlag * p.getExteriorRing().getPointN(1).getY(),
				0.0d);
			gl.glColor3d(0.0, 1.0, 0.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(2).getX(), yFlag * p.getExteriorRing().getPointN(2).getY(),
				0.0d);
			gl.glColor3d(0.0, 0.0, 0.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(3).getX(), yFlag * p.getExteriorRing().getPointN(3).getY(),
				0.0d);
			gl.glEnd();

			gl.glBegin(GL2GL3.GL_QUADS);
			gl.glNormal3d(1.0, 0.0, 0.0);
			gl.glColor3d(1.0, 1.0, 0.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(1).getX(), yFlag * p.getExteriorRing().getPointN(1).getY(),
				0.0d);
			gl.glColor3d(0.0, 1.0, 0.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(2).getX(), yFlag * p.getExteriorRing().getPointN(2).getY(),
				0.0d);
			gl.glColor3d(0.0, 1.0, 1.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(2).getX(), yFlag * p.getExteriorRing().getPointN(2).getY(),
				g.height);
			gl.glColor3d(1.0, 1.0, 1.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(1).getX(), yFlag * p.getExteriorRing().getPointN(1).getY(),
				g.height);
			gl.glEnd();

			gl.glBegin(GL2GL3.GL_QUADS);
			gl.glNormal3d(0.0, -1.0, 0.0);
			gl.glColor3d(0.0, 1.0, 0.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(2).getX(), yFlag * p.getExteriorRing().getPointN(2).getY(),
				0.0d);
			gl.glColor3d(0.0, 0.0, 0.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(3).getX(), yFlag * p.getExteriorRing().getPointN(3).getY(),
				0.0d);
			gl.glColor3d(0.0, 0.0, 1.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(3).getX(), yFlag * p.getExteriorRing().getPointN(3).getY(),
				g.height);
			gl.glColor3d(0.0, 1.0, 1.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(2).getX(), yFlag * p.getExteriorRing().getPointN(2).getY(),
				g.height);
			gl.glEnd();

			gl.glBegin(GL2GL3.GL_QUADS);
			gl.glNormal3d(-1.0, 0.0, 0.0);
			gl.glColor3d(0.0, 0.0, 0.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(3).getX(), yFlag * p.getExteriorRing().getPointN(3).getY(),
				0.0d);
			gl.glColor3d(1.0, 0.0, 0.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(0).getX(), yFlag * p.getExteriorRing().getPointN(0).getY(),
				0.0d);
			gl.glColor3d(1.0, 0.0, 1.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(0).getX(), yFlag * p.getExteriorRing().getPointN(0).getY(),
				g.height);
			gl.glColor3d(0.0, 0.0, 1.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(3).getX(), yFlag * p.getExteriorRing().getPointN(3).getY(),
				g.height);
			gl.glEnd();

			gl.glBegin(GL2GL3.GL_QUADS);
			gl.glNormal3d(0.0, 1.0, 0.0);
			gl.glColor3d(1.0, 0.0, 0.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(0).getX(), yFlag * p.getExteriorRing().getPointN(0).getY(),
				0.0d);
			gl.glColor3d(1.0, 1.0, 0.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(1).getX(), yFlag * p.getExteriorRing().getPointN(1).getY(),
				0.0d);
			gl.glColor3d(1.0, 1.0, 1.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(1).getX(), yFlag * p.getExteriorRing().getPointN(1).getY(),
				g.height);
			gl.glColor3d(1.0, 0.0, 1.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(0).getX(), yFlag * p.getExteriorRing().getPointN(0).getY(),
				g.height);
			gl.glEnd();

			gl.glBegin(GL2GL3.GL_QUADS);
			gl.glNormal3d(0.0, 0.0, 1.0);
			gl.glColor3d(1.0, 0.0, 1.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(0).getX(), yFlag * p.getExteriorRing().getPointN(0).getY(),
				g.height);
			gl.glColor3d(1.0, 1.0, 1.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(1).getX(), yFlag * p.getExteriorRing().getPointN(1).getY(),
				g.height);
			gl.glColor3d(0.0, 1.0, 1.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(2).getX(), yFlag * p.getExteriorRing().getPointN(2).getY(),
				g.height);
			gl.glColor3d(0.0, 0.0, 1.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(3).getX(), yFlag * p.getExteriorRing().getPointN(3).getY(),
				g.height);
			gl.glEnd();
		}

		if ( !colorpicking ) {
			Color c = g.getColor();
			gl.glColor4d((double) c.getRed() / 255, (double) c.getGreen() / 255, (double) c.getBlue() / 255,
				g.getAlpha() * c.getAlpha() / 255);
		}
	}

	public void drawRGBTriangle(final GeometryObject g) {
		GL2 gl = GLContext.getCurrentGL().getGL2();
		double z = 0.0;
		Polygon p = (Polygon) g.geometry;
		if ( !Double.isNaN(p.getCoordinate().z) ) {
			// TODO Normally, the NaN case is not true anymore
			z = p.getExteriorRing().getPointN(0).getCoordinate().z;
		}
		if ( g.picked ) {
			Color c = g.getColor();
			gl.glColor4d((double) c.getRed() / 255, (double) c.getGreen() / 255, (double) c.getBlue() / 255,
				g.getAlpha() * c.getAlpha() / 255);
			gl.glBegin(GL.GL_TRIANGLES);
			gl.glVertex3d(p.getExteriorRing().getPointN(0).getX(), yFlag * p.getExteriorRing().getPointN(0).getY(),
				0.0d);
			gl.glVertex3d(p.getExteriorRing().getPointN(1).getX(), yFlag * p.getExteriorRing().getPointN(1).getY(),
				0.0d);
			gl.glVertex3d(p.getExteriorRing().getPointN(2).getX(), yFlag * p.getExteriorRing().getPointN(2).getY(),
				0.0d);
			gl.glEnd();

		} else {
			gl.glBegin(GL.GL_TRIANGLES);
			gl.glColor3d(1.0, 0.0, 0.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(0).getX(), yFlag * p.getExteriorRing().getPointN(0).getY(),
				0.0d);
			gl.glColor3d(0.0, 1.0, 0.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(1).getX(), yFlag * p.getExteriorRing().getPointN(1).getY(),
				0.0d);
			gl.glColor3d(0.0, 0.0, 1.0);
			gl.glVertex3d(p.getExteriorRing().getPointN(2).getX(), yFlag * p.getExteriorRing().getPointN(2).getY(),
				0.0d);
			gl.glEnd();
		}
	}

	public void DrawMultiLineCylinder(final Geometry g, final Color c, final double alpha, final double height) {
		// get the number of line in the multiline.
		MultiLineString lines = (MultiLineString) g;
		int numGeometries = lines.getNumGeometries();

		// for each line of a multiline, get each point coordinates.
		for ( int i = 0; i < numGeometries; i++ ) {
			Geometry gg = lines.getGeometryN(i);
			drawLineCylinder(gg, c, alpha, height);

		}
	}

	public void drawLineCylinder(final Geometry g, final Color c, final double alpha, final double height) {
		GL2 gl = GLContext.getCurrentGL().getGL2();
		double z = 0.0;

		Geometry gg = g;

		if ( Double.isNaN(gg.getCoordinate().z) == false ) {
			z = gg.getCentroid().getCoordinate().z;
		}
		if ( gg instanceof Point ) {
			// drawSphere(g, z);
			return;
		}
		LineString l = (LineString) gg;

		for ( int i = 0; i <= l.getNumPoints() - 2; i++ ) {

			if ( Double.isNaN(l.getCoordinate().z) == false ) {
				z = l.getPointN(i).getCoordinate().z;
			}

			double x_length = l.getPointN(i + 1).getX() - l.getPointN(i).getX();
			double y_length = l.getPointN(i + 1).getY() - l.getPointN(i).getY();
			double z_length = l.getPointN(i + 1).getCoordinate().z - l.getPointN(i).getCoordinate().z;

			double distance = Math.sqrt(x_length * x_length + y_length * y_length + z_length * z_length);

			gl.glTranslated(l.getPointN(i).getX(), yFlag * l.getPointN(i).getY(), z);
			Vector3d d;
			if ( Double.isNaN(l.getCoordinate().z) == false ) {
				d =
					new Vector3d((l.getPointN(i + 1).getX() - l.getPointN(i).getX()) / distance, -(l.getPointN(i + 1)
						.getY() - l.getPointN(i).getY()) / distance, (l.getPointN(i + 1).getCoordinate().z - l
						.getPointN(i).getCoordinate().z) / distance);
			} else {
				d =
					new Vector3d((l.getPointN(i + 1).getX() - l.getPointN(i).getX()) / distance, -(l.getPointN(i + 1)
						.getY() - l.getPointN(i).getY()) / distance, 0);
			}

			Vector3d z_up = new Vector3d(0, 0, 1);

			Vector3d a = new Vector3d();
			a.cross(z_up, d);

			double omega = Math.acos(z_up.dot(d));
			omega = omega * 180 / Math.PI;
			gl.glRotated(omega, a.x, a.y, a.z);

			if ( !colorpicking ) {
				setColor(c, alpha);
			}
			GLU myGlu = renderer.getGlu();
			GLUquadric quad = myGlu.gluNewQuadric();
			if ( !renderer.data.isTriangulation() ) {
				myGlu.gluQuadricDrawStyle(quad, GLU.GLU_FILL);
			} else {
				myGlu.gluQuadricDrawStyle(quad, GLU.GLU_LINE);
			}
			myGlu.gluQuadricNormals(quad, GLU.GLU_FLAT);
			myGlu.gluQuadricOrientation(quad, GLU.GLU_OUTSIDE);
			final int slices = 16;
			final int stacks = 16;
			myGlu.gluCylinder(quad, height, height, distance, slices, stacks);
			myGlu.gluDeleteQuadric(quad);

			gl.glRotated(-omega, a.x, a.y, a.z);
			gl.glTranslated(-l.getPointN(i).getX(), -yFlag * l.getPointN(i).getY(), -z);
		}

	}

	public Vertex[] GetPyramidfaceVertices(final Polygon p, final int i, final int j, final double size, final int x,
		final int y) {
		Vertex[] vertices = new Vertex[3];
		for ( int i1 = 0; i1 < 3; i1++ ) {
			vertices[i1] = new Vertex();
		}

		vertices[0].x = p.getExteriorRing().getPointN(i).getX();
		vertices[0].y = yFlag * p.getExteriorRing().getPointN(i).getY();
		vertices[0].z = 0.0d;

		vertices[1].x = p.getExteriorRing().getPointN(j).getX();
		vertices[1].y = yFlag * p.getExteriorRing().getPointN(j).getY();
		vertices[1].z = 0.0d;

		vertices[2].x = p.getExteriorRing().getPointN(i).getX() + size / 2 * x;
		vertices[2].y = yFlag * (p.getExteriorRing().getPointN(i).getY() + size / 2 * y);
		vertices[2].z = size;
		return vertices;
	}

	public void PyramidSkeleton(final Polygon p, final double size, final Color c, final double alpha) {
		GL2 gl = GLContext.getCurrentGL().getGL2();
		Vertex[] vertices;
		double[] normal;

		if ( renderer.getComputeNormal() ) {
		  vertices = getExteriorRingVertices(p);
		  GLUtilNormal.HandleNormal(vertices, c, alpha,1, renderer);
		}
		

		gl.glBegin(GL2GL3.GL_QUADS);
		gl.glVertex3d(p.getExteriorRing().getPointN(0).getX(), yFlag * p.getExteriorRing().getPointN(0).getY(), 0.0d);
		gl.glVertex3d(p.getExteriorRing().getPointN(1).getX(), yFlag * p.getExteriorRing().getPointN(1).getY(), 0.0d);
		gl.glVertex3d(p.getExteriorRing().getPointN(2).getX(), yFlag * p.getExteriorRing().getPointN(2).getY(), 0.0d);
		gl.glVertex3d(p.getExteriorRing().getPointN(3).getX(), yFlag * p.getExteriorRing().getPointN(3).getY(), 0.0d);
		gl.glEnd();

		if ( renderer.getComputeNormal() ) {
		  vertices = GetPyramidfaceVertices(p, 0, 1, size, 1, -1);
		  GLUtilNormal.HandleNormal(vertices, c, alpha, -1, renderer);
		}
		
		gl.glBegin(GL.GL_TRIANGLES);
		gl.glVertex3d(p.getExteriorRing().getPointN(0).getX(), yFlag * p.getExteriorRing().getPointN(0).getY(), 0.0d);
		gl.glVertex3d(p.getExteriorRing().getPointN(1).getX(), yFlag * p.getExteriorRing().getPointN(1).getY(), 0.0d);
		gl.glVertex3d(p.getExteriorRing().getPointN(0).getX() + size / 2, yFlag *
			(p.getExteriorRing().getPointN(0).getY() - size / 2), size);
		gl.glEnd();

		if ( renderer.getComputeNormal() ) {
		  vertices = GetPyramidfaceVertices(p, 1, 2, size, -1, -1);
		  GLUtilNormal.HandleNormal(vertices, c, alpha, -1, renderer);
		}
		
		gl.glBegin(GL.GL_TRIANGLES);
		gl.glVertex3d(p.getExteriorRing().getPointN(1).getX(), yFlag * p.getExteriorRing().getPointN(1).getY(), 0.0d);
		gl.glVertex3d(p.getExteriorRing().getPointN(2).getX(), yFlag * p.getExteriorRing().getPointN(2).getY(), 0.0d);
		gl.glVertex3d(p.getExteriorRing().getPointN(1).getX() - size / 2, yFlag *
			(p.getExteriorRing().getPointN(1).getY() - size / 2), size);
		gl.glEnd();

		if ( renderer.getComputeNormal() ) {
		  vertices = GetPyramidfaceVertices(p, 2, 3, size, -1, 1);
		  GLUtilNormal.HandleNormal(vertices, c, alpha, -1, renderer);
		}
		
		gl.glBegin(GL.GL_TRIANGLES);
		gl.glVertex3d(p.getExteriorRing().getPointN(2).getX(), yFlag * p.getExteriorRing().getPointN(2).getY(), 0.0d);
		gl.glVertex3d(p.getExteriorRing().getPointN(3).getX(), yFlag * p.getExteriorRing().getPointN(3).getY(), 0.0d);
		gl.glVertex3d(p.getExteriorRing().getPointN(2).getX() - size / 2, yFlag *
			(p.getExteriorRing().getPointN(2).getY() + size / 2), size);
		gl.glEnd();

		if ( renderer.getComputeNormal() ) {
		  vertices = GetPyramidfaceVertices(p, 3, 0, size, 1, 1);
		  GLUtilNormal.HandleNormal(vertices, c, alpha, -1, renderer);
		}
		
		gl.glBegin(GL.GL_TRIANGLES);
		gl.glVertex3d(p.getExteriorRing().getPointN(3).getX(), yFlag * p.getExteriorRing().getPointN(3).getY(), 0.0d);
		gl.glVertex3d(p.getExteriorRing().getPointN(0).getX(), yFlag * p.getExteriorRing().getPointN(0).getY(), 0.0d);
		gl.glVertex3d(p.getExteriorRing().getPointN(3).getX() + size / 2, yFlag *
			(p.getExteriorRing().getPointN(3).getY() + size / 2), size);
		gl.glEnd();
	}

	public void DrawTriangulatedPolygonShape(final Polygon triangulatedPolygon, final IShape shape,
		final boolean showTriangulation, final Texture texture) {
		GL2 gl = GLContext.getCurrentGL().getGL2();
		Polygon polygon = (Polygon) shape.getInnerGeometry();

		final Envelope env = triangulatedPolygon.getEnvelopeInternal();
		final double xMin = env.getMinX();
		final double xMax = env.getMaxX();
		final double yMin = env.getMinY();
		final double yMax = env.getMaxY();

		if ( showTriangulation ) {

			if ( Double.isNaN(polygon.getExteriorRing().getPointN(0).getCoordinate().z) == true ) {
				gl.glBegin(GL.GL_LINES); // draw using triangles
				gl.glVertex3d(polygon.getExteriorRing().getPointN(0).getX(), yFlag *
					polygon.getExteriorRing().getPointN(0).getY(), 0.0d);
				gl.glVertex3d(polygon.getExteriorRing().getPointN(1).getX(), yFlag *
					polygon.getExteriorRing().getPointN(1).getY(), 0.0d);

				gl.glVertex3d(polygon.getExteriorRing().getPointN(1).getX(), yFlag *
					polygon.getExteriorRing().getPointN(1).getY(), 0.0d);
				gl.glVertex3d(polygon.getExteriorRing().getPointN(2).getX(), yFlag *
					polygon.getExteriorRing().getPointN(2).getY(), 0.0d);

				gl.glVertex3d(polygon.getExteriorRing().getPointN(2).getX(), yFlag *
					polygon.getExteriorRing().getPointN(2).getY(), 0.0d);
				gl.glVertex3d(polygon.getExteriorRing().getPointN(0).getX(), yFlag *
					polygon.getExteriorRing().getPointN(0).getY(), 0.0d);
				gl.glEnd();
			} else {
				gl.glBegin(GL.GL_LINES); // draw using triangles
				gl.glVertex3d(polygon.getExteriorRing().getPointN(0).getX(), yFlag *
					polygon.getExteriorRing().getPointN(0).getY(), polygon.getExteriorRing().getPointN(0)
					.getCoordinate().z);
				gl.glVertex3d(polygon.getExteriorRing().getPointN(1).getX(), yFlag *
					polygon.getExteriorRing().getPointN(1).getY(), polygon.getExteriorRing().getPointN(0)
					.getCoordinate().z);

				gl.glVertex3d(polygon.getExteriorRing().getPointN(1).getX(), yFlag *
					polygon.getExteriorRing().getPointN(1).getY(), polygon.getExteriorRing().getPointN(1)
					.getCoordinate().z);
				gl.glVertex3d(polygon.getExteriorRing().getPointN(2).getX(), yFlag *
					polygon.getExteriorRing().getPointN(2).getY(), polygon.getExteriorRing().getPointN(2)
					.getCoordinate().z);

				gl.glVertex3d(polygon.getExteriorRing().getPointN(2).getX(), yFlag *
					polygon.getExteriorRing().getPointN(2).getY(), polygon.getExteriorRing().getPointN(2)
					.getCoordinate().z);
				gl.glVertex3d(polygon.getExteriorRing().getPointN(0).getX(), yFlag *
					polygon.getExteriorRing().getPointN(0).getY(), polygon.getExteriorRing().getPointN(0)
					.getCoordinate().z);
				gl.glEnd();

			}
		} else {
			if ( Double.isNaN(polygon.getExteriorRing().getPointN(0).getCoordinate().z) == true ) {
				if ( texture != null ) {
					gl.glColor3d(1.0, 1.0, 1.0);// Set the color to white to avoid color and texture mixture
					texture.enable(gl);
					texture.bind(gl);
					gl.glBegin(GL.GL_TRIANGLES); // draw using triangles

					gl.glTexCoord2d(polygon.getExteriorRing().getPointN(0).getX() / (xMax - xMin), yFlag *
						polygon.getExteriorRing().getPointN(0).getY() / (yMax - yMin));
					gl.glVertex3d(polygon.getExteriorRing().getPointN(0).getX(), yFlag *
						polygon.getExteriorRing().getPointN(0).getY(), 0.0d);
					gl.glTexCoord2d(polygon.getExteriorRing().getPointN(1).getX() / (xMax - xMin), yFlag *
						polygon.getExteriorRing().getPointN(1).getY() / (yMax - yMin));
					gl.glVertex3d(polygon.getExteriorRing().getPointN(1).getX(), yFlag *
						polygon.getExteriorRing().getPointN(1).getY(), 0.0d);
					gl.glTexCoord2d(polygon.getExteriorRing().getPointN(2).getX() / (xMax - xMin), yFlag *
						polygon.getExteriorRing().getPointN(2).getY() / (yMax - yMin));
					gl.glVertex3d(polygon.getExteriorRing().getPointN(2).getX(), yFlag *
						polygon.getExteriorRing().getPointN(2).getY(), 0.0d);
					gl.glEnd();
					texture.disable(gl);

				} else {
					gl.glBegin(GL.GL_TRIANGLES); // draw using triangles
					gl.glVertex3d(polygon.getExteriorRing().getPointN(0).getX(), yFlag *
						polygon.getExteriorRing().getPointN(0).getY(), 0.0d);

					gl.glVertex3d(polygon.getExteriorRing().getPointN(1).getX(), yFlag *
						polygon.getExteriorRing().getPointN(1).getY(), 0.0d);

					gl.glVertex3d(polygon.getExteriorRing().getPointN(2).getX(), yFlag *
						polygon.getExteriorRing().getPointN(2).getY(), 0.0d);
					gl.glEnd();
				}

			} else {
				if ( texture != null ) {
					gl.glColor3d(1.0, 1.0, 1.0);// Set the color to white to avoid color and texture mixture
					texture.enable(gl);
					texture.bind(gl);
					gl.glBegin(GL.GL_TRIANGLES); // draw using triangles
					gl.glTexCoord2d(polygon.getExteriorRing().getPointN(0).getX() / (xMax - xMin), yFlag *
						polygon.getExteriorRing().getPointN(0).getY() / (yMax - yMin));
					gl.glVertex3d(polygon.getExteriorRing().getPointN(0).getX(), yFlag *
						polygon.getExteriorRing().getPointN(0).getY(), polygon.getExteriorRing().getPointN(0)
						.getCoordinate().z);
					gl.glTexCoord2d(polygon.getExteriorRing().getPointN(1).getX() / (xMax - xMin), yFlag *
						polygon.getExteriorRing().getPointN(1).getY() / (yMax - yMin));
					gl.glVertex3d(polygon.getExteriorRing().getPointN(1).getX(), yFlag *
						polygon.getExteriorRing().getPointN(1).getY(), polygon.getExteriorRing().getPointN(1)
						.getCoordinate().z);
					gl.glTexCoord2d(polygon.getExteriorRing().getPointN(2).getX() / (xMax - xMin), yFlag *
						polygon.getExteriorRing().getPointN(2).getY() / (yMax - yMin));
					gl.glVertex3d(polygon.getExteriorRing().getPointN(2).getX(), yFlag *
						polygon.getExteriorRing().getPointN(2).getY(), polygon.getExteriorRing().getPointN(2)
						.getCoordinate().z);
					gl.glEnd();
					texture.disable(gl);
				} else {
					gl.glBegin(GL.GL_TRIANGLES); // draw using triangles

					gl.glVertex3d(polygon.getExteriorRing().getPointN(0).getX(), yFlag *
						polygon.getExteriorRing().getPointN(0).getY(), polygon.getExteriorRing().getPointN(0)
						.getCoordinate().z);

					gl.glVertex3d(polygon.getExteriorRing().getPointN(1).getX(), yFlag *
						polygon.getExteriorRing().getPointN(1).getY(), polygon.getExteriorRing().getPointN(1)
						.getCoordinate().z);

					gl.glVertex3d(polygon.getExteriorRing().getPointN(2).getX(), yFlag *
						polygon.getExteriorRing().getPointN(2).getY(), polygon.getExteriorRing().getPointN(2)
						.getCoordinate().z);
					gl.glEnd();
				}
			}

		}
	}

	/*
	 * Return 9 array with the 3 vertex coordinates of the traingle
	 */
	public double[] GetTriangleVertices(final IShape shape) {

		Polygon polygon = (Polygon) shape.getInnerGeometry();
		double[] vertices = new double[9];
		for ( int i = 0; i < 3; i++ ) {
			vertices[i * 3] = polygon.getExteriorRing().getPointN(0).getX();
			vertices[i * 3 + 1] = yFlag * polygon.getExteriorRing().getPointN(0).getY();
			vertices[i * 3 + 2] = 0.0d;
		}
		return vertices;
	}

	public boolean IsClockwise(final Vertex[] vertices) {
		double sum = 0.0;
		for ( int i = 0; i < vertices.length; i++ ) {
			Vertex v1 = vertices[i];
			Vertex v2 = vertices[(i + 1) % vertices.length];
			sum += (v2.x - v1.x) * (v2.y + v1.y);
		}
		return sum > 0.0;
	}

	public void drawRoundRectangle(final Polygon p) {
		GL2 gl = GLContext.getCurrentGL().getGL2();
		double width = p.getEnvelopeInternal().getWidth();
		double height = p.getEnvelopeInternal().getHeight();

		gl.glTranslated(p.getCentroid().getX(), -p.getCentroid().getY(), 0.0d);
		DrawRectangle(width, height * 0.8, p.getCentroid());
		DrawRectangle(width * 0.8, height, p.getCentroid());
		DrawRoundCorner(width, height, width * 0.1, height * 0.1, 5);
		gl.glTranslated(-p.getCentroid().getX(), p.getCentroid().getY(), 0.0d);

	}

	void DrawRectangle(final double width, final double height, final Point point) {
		GL2 gl = GLContext.getCurrentGL().getGL2();
		gl.glBegin(GL2.GL_POLYGON); // draw using quads
		gl.glVertex3d(-width / 2, height / 2, 0.0d);
		gl.glVertex3d(width / 2, height / 2, 0.0d);
		gl.glVertex3d(width / 2, -height / 2, 0.0d);
		gl.glVertex3d(-width / 2, -height / 2, 0.0d);
		gl.glEnd();
	}

		void
		DrawFan(final double radius, final double x, final double y, final int or_x, final int or_y, final int timestep) {
		GL2 gl = GLContext.getCurrentGL().getGL2();
		gl.glBegin(GL.GL_TRIANGLE_FAN); // upper right
		gl.glVertex3d(or_x * x, or_y * y, 0.0d);
		for ( int i = 0; i <= timestep; i++ ) {
			double anglerad = Math.PI / 2 * i / timestep;
			double xi = Math.cos(anglerad) * radius;
			double yi = Math.sin(anglerad) * radius;
			gl.glVertex3d(or_x * (x + xi), y + yi, 0.0d);
		}
		gl.glEnd();
	}

	void DrawRoundCorner(final double width, final double height, final double x_radius, final double y_radius,
		final int nbPoints) {
		GL2 gl = GLContext.getCurrentGL().getGL2();
		double xc = width / 2 * 0.8;
		double yc = height / 2 * 0.8;
		// Enhancement implement DrawFan(radius, xc, yc, 10);

		gl.glBegin(GL.GL_TRIANGLE_FAN); // upper right
		gl.glVertex3d(xc, yc, 0.0d);
		for ( int i = 0; i <= nbPoints; i++ ) {
			double anglerad = Math.PI / 2 * i / nbPoints;
			double xi = Math.cos(anglerad) * x_radius;
			double yi = Math.sin(anglerad) * y_radius;
			gl.glVertex3d(xc + xi, yc + yi, 0.0d);
		}
		gl.glEnd();

		gl.glBegin(GL.GL_TRIANGLE_FAN); // upper right

		gl.glVertex3d(xc, -yc, 0.0d);
		for ( int i = 0; i <= nbPoints; i++ ) {
			double anglerad = Math.PI / 2 * i / nbPoints;
			double xi = Math.cos(anglerad) * x_radius;
			double yi = Math.sin(anglerad) * y_radius;
			gl.glVertex3d(xc + xi, -(yc + yi), 0.0d);
		}
		gl.glEnd();

		gl.glBegin(GL.GL_TRIANGLE_FAN); // upper left

		gl.glVertex3d(-xc, yc, 0.0d);
		for ( int i = 0; i <= nbPoints; i++ ) {
			double anglerad = Math.PI / 2 * i / nbPoints;
			double xi = Math.cos(anglerad) * x_radius;
			double yi = Math.sin(anglerad) * y_radius;
			gl.glVertex3d(-(xc + xi), yc + yi, 0.0d);
		}
		gl.glEnd();

		gl.glBegin(GL.GL_TRIANGLE_FAN);
		gl.glVertex3d(-xc, -yc, 0.0d); // down left
		for ( int i = 0; i <= nbPoints; i++ ) {
			double anglerad = Math.PI / 2 * i / nbPoints;
			double xi = Math.cos(anglerad) * x_radius;
			double yi = Math.sin(anglerad) * y_radius;
			gl.glVertex3d(-(xc + xi), -(yc + yi), 0.0d);
		}
		gl.glEnd();
	}

}
