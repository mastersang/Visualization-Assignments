package infovis.diagram;

import infovis.debug.Debug;
import infovis.diagram.elements.DrawingEdge;
import infovis.diagram.elements.Edge;
import infovis.diagram.elements.Element;
import infovis.diagram.elements.GroupingRectangle;
import infovis.diagram.elements.None;
import infovis.diagram.elements.Vertex;
import infovis.diagram.layout.Fisheye;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MouseController implements MouseListener, MouseMotionListener {
	private Model model;
	private View view;
	private Element selectedElement = new None();
	private DrawingEdge drawingEdge = null;
	private GroupingRectangle groupRectangle;
	private double mouseOffsetX;
	private double mouseOffsetY;
	private double currentX = 0;
	private double currentY = 0;
	private boolean edgeDrawMode = false;
	private boolean fisheyeMode;
	private boolean selectingMarker = false;
	private boolean selectingOverviewTopBorder = false;

	/*
	 * Getter And Setter
	 */
	public Element getSelectedElement() {
		return selectedElement;
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model diagramModel) {
		this.model = diagramModel;
	}

	public View getView() {
		return view;
	}

	public void setView(View diagramView) {
		this.view = diagramView;
	}

	/*
	 * Implements MouseListener
	 */
	public void mouseClicked(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		double scale = view.getScale();

		if (e.getButton() == MouseEvent.BUTTON3) {

			/*
			 * add grouped elements to the model
			 */
			Element element = getElementContainingPosition(x / scale, y / scale);

			if (element instanceof Vertex) {
				Vertex groupVertex = (Vertex) element;
				Model m = groupVertex.getGroupedElements();

				if (m != null) {
					for (Iterator<Vertex> iter = groupVertex.getGroupedElements().iteratorVertices(); iter.hasNext();) {
						model.addVertex(iter.next());
					}

					for (Iterator<Edge> iter = groupVertex.getGroupedElements().iteratorEdges(); iter.hasNext();) {
						model.addEdge(iter.next());
					}

					/*
					 * remove elements
					 */
					List<Edge> edgesToRemove = new ArrayList<Edge>();

					for (Iterator<Edge> iter = model.iteratorEdges(); iter.hasNext();) {
						Edge edge = iter.next();
						if (edge.getSource() == groupVertex || edge.getTarget() == groupVertex) {
							edgesToRemove.add(edge);
						}
					}

					model.removeEdges(edgesToRemove);
					model.removeElement(groupVertex);
				}
			}
		}
	}

	public void mouseEntered(MouseEvent arg0) {
	}

	public void mouseExited(MouseEvent arg0) {
	}

	public void mousePressed(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		double scale = view.getScale();

		if (edgeDrawMode) {
			if (drawingEdge != null) {
				drawingEdge = new DrawingEdge((Vertex) getElementContainingPosition(x / scale, y / scale));
				model.addElement(drawingEdge);
			}
		} else {
			// Check if user clicks on marker area
			selectingMarker = checkPointInRectangle(view.getMarker(), x, y);
			// check if user clicks on overview top border
			selectingOverviewTopBorder = checkPointInRectangle(view.getOverviewTopBorder(), x, y);

			if (selectingMarker || selectingOverviewTopBorder) {
				currentX = x;
				currentY = y;
			} else {
				/*
				 * calculate offset
				 */
				selectedElement = getElementContainingPosition(x / scale, y / scale);
				mouseOffsetX = x - selectedElement.getX() * scale;
				mouseOffsetY = y - selectedElement.getY() * scale;
				currentX = 0;
				currentY = 0;
			}
		}
	}

	private boolean checkPointInRectangle(Rectangle2D rectangle, int x, int y) {
		double scale = view.getScale();
		double rectangleX = (rectangle.getX() - view.getTranslateX()) * scale;
		double rectangleY = (rectangle.getY() - view.getTranslateY()) * scale;

		return x >= rectangleX && x <= rectangleX + rectangle.getWidth() * scale && y >= rectangleY
				&& y <= rectangleY + rectangle.getHeight() * scale;
	}

	public void mouseReleased(MouseEvent arg0) {
		selectingMarker = false;
		selectingOverviewTopBorder = false;
		int x = arg0.getX();
		int y = arg0.getY();

		if (drawingEdge != null) {
			Element to = getElementContainingPosition(x, y);
			model.addEdge(new Edge(drawingEdge.getFrom(), (Vertex) to));
			model.removeElement(drawingEdge);
			drawingEdge = null;
		}

		if (groupRectangle != null) {
			Model groupedElements = new Model();

			for (Iterator<Vertex> iter = model.iteratorVertices(); iter.hasNext();) {
				Vertex vertex = iter.next();
				if (groupRectangle.contains(vertex.getShape().getBounds2D())) {
					Debug.p("Vertex found");
					groupedElements.addVertex(vertex);
				}
			}

			if (!groupedElements.isEmpty()) {
				model.removeVertices(groupedElements.getVertices());
				Vertex groupVertex = new Vertex(groupRectangle.getCenterX(), groupRectangle.getCenterX());
				groupVertex.setColor(Color.ORANGE);
				groupVertex.setGroupedElements(groupedElements);
				model.addVertex(groupVertex);

				List<Edge> newEdges = new ArrayList();

				for (Iterator<Edge> iter = model.iteratorEdges(); iter.hasNext();) {
					Edge edge = iter.next();

					if (groupRectangle.contains(edge.getSource().getShape().getBounds2D())
							&& groupRectangle.contains(edge.getTarget().getShape().getBounds2D())) {
						groupVertex.getGroupedElements().addEdge(edge);
						Debug.p("add Edge to groupedElements");
						// iter.remove(); // Warum geht das nicht!
					} else if (groupRectangle.contains(edge.getSource().getShape().getBounds2D())) {
						groupVertex.getGroupedElements().addEdge(edge);
						newEdges.add(new Edge(groupVertex, edge.getTarget()));
					} else if (groupRectangle.contains(edge.getTarget().getShape().getBounds2D())) {
						groupVertex.getGroupedElements().addEdge(edge);
						newEdges.add(new Edge(edge.getSource(), groupVertex));
					}
				}

				model.addEdges(newEdges);
				model.removeEdges(groupedElements.getEdges());
			}

			model.removeElement(groupRectangle);
			groupRectangle = null;
		}

		view.repaint();
	}

	public void mouseDragged(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		double scale = view.getScale();

		if (selectingMarker || selectingOverviewTopBorder) {
			if (selectingMarker) {
				double overviewScale = view.getOverviewScale();
				// update translation, x and y need to be scaled to overview size, translation
				// is added cummulatively
				double translateX = (x - currentX) * overviewScale + view.getTranslateX();
				double translateY = (y - currentY) * overviewScale + view.getTranslateY();
				view.updateTranslation(translateX, translateY);
				updateFisheyeFocus(view.getScale());
				mouseMoved(e);
			} else {
				double translateX = (x - currentX) / scale + view.getOverviewTranslateX();
				double translateY = (y - currentY) / scale + view.getOverviewTranslateY();
				view.setOverviewTranslateX(translateX);
				view.setOverviewTranslateY(translateY);
			}

			currentX = x;
			currentY = y;
		} else if (edgeDrawMode) {
			if (drawingEdge != null) {
				drawingEdge.setX(e.getX());
				drawingEdge.setY(e.getY());
			}
		} else if (selectedElement != null) {
			selectedElement.updatePosition((x - mouseOffsetX) / scale, (y - mouseOffsetY) / scale);
		}

		view.repaint();
	}

	public void mouseMoved(MouseEvent e) {
		if (fisheyeMode) {
			double scale = view.getScale();
			view.setFocusX(e.getX() / scale + view.getTranslateX());
			view.setFocusY(e.getY() / scale + view.getTranslateY());
			setFisheyeModel();
			view.repaint();
		}
	}

	public boolean isDrawingEdges() {
		return edgeDrawMode;
	}

	public void setDrawingEdges(boolean drawingEdges) {
		this.edgeDrawMode = drawingEdges;
	}

	public void setFisheyeMode(boolean b) {
		fisheyeMode = b;

		if (b) {
			double scale = view.getScale();
			// initial focus is at center
			view.setFocusX(view.getWidth() / 2 / scale);
			view.setFocusY(view.getHeight() / 2 / scale);
			setFisheyeModel();
			view.repaint();
		} else {
			view.setModel(model);
			view.repaint();
		}
	}

	public void updateFisheyeFocus(double scale) {
		if (fisheyeMode) {
			view.setFocusX(view.getFocusX() * view.getScale() / scale);
			view.setFocusY(view.getFocusY() * view.getScale() / scale);
			setFisheyeModel();
		}
	}

	private void setFisheyeModel() {
		Fisheye fisheye = new Fisheye();
		Model fisheyeModel = fisheye.transform(model, view);
		view.setModel(fisheyeModel);
	}

	private Element getElementContainingPosition(double x, double y) {
		Element currentElement = new None();
		Iterator<Element> iter = getModel().iterator();

		while (iter.hasNext()) {
			Element element = iter.next();

			if (element.contains(x, y)) {
				currentElement = element;
			}
		}

		return currentElement;
	}
}
