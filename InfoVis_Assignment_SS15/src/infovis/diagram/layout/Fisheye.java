package infovis.diagram.layout;

import infovis.diagram.Model;
import infovis.diagram.View;
import infovis.diagram.elements.Element;
import infovis.diagram.elements.Vertex;

/*
 * 
 */

public class Fisheye implements Layout {
	private final int MAGNIFICATION_FACTOR = 4;

	public void setMouseCoords(int x, int y, View view) {
		// TODO Auto-generated method stub
	}

	public Model transform(Model model, View view) {
		Model fisheyeModel = new Model();

		for (Element element : model.getElements()) {
			Vertex current = (Vertex) element;
			double ratio = current.getWidth() / current.getHeight();
			double boundaryX = current.getX() + current.getWidth();
			double topLeftX = getFisheyeCoordinate(current.getX(), view.getWidth(), view.getFocusX());
			double rightX = getFisheyeCoordinate(boundaryX, view.getWidth(), view.getFocusX());
			double boundaryY = current.getY() + current.getHeight();
			double topLeftY = getFisheyeCoordinate(current.getY(), view.getHeight(), view.getFocusY());
			double bottomY = getFisheyeCoordinate(boundaryY, view.getHeight(), view.getFocusY());
			double width = rightX - topLeftX;
			double height = bottomY - topLeftY;
			double heightByWidth = width / ratio;
			double widthByHeight = height * ratio;

			// Select minimum size and preserve ratio
			if (width < widthByHeight) {
				height = heightByWidth;
			} else {
				width = widthByHeight;
			}

			Vertex newVertex = new Vertex(topLeftX, topLeftY, width, height);
			fisheyeModel.addVertex(newVertex);
		}

		return fisheyeModel;
	}

	private double getFisheyeCoordinate(double original, double boundary, double focus) {
		double dNorm = original - focus;
		double dMax;

		if (original >= focus) {
			dMax = boundary - focus;
		} else {
			dMax = 0 - focus;
		}

		double temp = dNorm / dMax;
		return focus + (((MAGNIFICATION_FACTOR + 1) * temp) / ((MAGNIFICATION_FACTOR * temp) + 1) * dMax);
	}
}
