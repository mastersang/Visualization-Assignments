package infovis.diagram;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;

import javax.swing.JPanel;

import infovis.diagram.elements.Element;
import infovis.diagram.elements.Vertex;

public class View extends JPanel {
	private Model model = null;
	private Model overviewModel = null;
	private Color color = Color.BLUE;
	private double scale = 1;
	private double translateX = 0;
	private double translateY = 0;

	// Max translate X and Y so marker can't be dragged outside of overview
	private double maxTranslateX = -1;
	private double maxTranslateY = -1;
	private double overviewTranslateX = 0;
	private double overviewTranslateY = 0;
	private double focusX = 0;
	private double focusY = 0;
	private Rectangle2D marker;
	private Rectangle2D overviewTopBorder;
	private Rectangle2D overviewRect;
	private final int OVERVIEW_SCALE = 4;
	private final int DEFAULT_BORDER_THICKNESS = 2;
	private final int OVERVIEW_TOP_BORDER_HEIGHT = 15;

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public void setScale(double scale) {
		this.scale = scale;

		// update translation when scale is updated
		updateTranslation(translateX, translateY);
	}

	public double getScale() {
		return scale;
	}

	public double getTranslateX() {
		return translateX;
	}

	public void setTranslateX(double translateX) {
		this.translateX = translateX;
	}

	public double getTranslateY() {
		return translateY;
	}

	public void setTranslateY(double tansslateY) {
		this.translateY = tansslateY;
	}

	public void updateTranslation(double x, double y) {
		if (x > maxTranslateX) {
			x = maxTranslateX;
		} else if (x < 0) {
			x = 0;
		}

		if (y > maxTranslateY) {
			y = maxTranslateY;
		} else if (y < 0) {
			y = 0;
		}

		setTranslateX(x);
		setTranslateY(y);
	}

	public void updateMarker(int x, int y) {
		marker.setRect(x, y, 16, 10);
	}

	public double getOverviewScale() {
		return OVERVIEW_SCALE;
	}

	public Rectangle2D getMarker() {
		return marker;
	}

	public Rectangle2D getOverview() {
		return overviewRect;
	}

	public Rectangle2D getOverviewTopBorder() {
		return overviewTopBorder;
	}

	public void paint(Graphics g) {
		Graphics2D g2D = (Graphics2D) g;
		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2D.setBackground(Color.WHITE);
		g2D.clearRect(0, 0, getWidth(), getHeight());
		setOverviewModel();
		paintDiagram(g2D);
		paintOverview(g2D);
	}

	// Set overview model to draw on overview
	private void setOverviewModel() {
		overviewModel = new Model();

		for (Element element : model.getElements()) {
			Vertex current = (Vertex) element;

			// Keep position and sizes of vertexes constant on overview
			Vertex newVertex = new Vertex((element.getX() / OVERVIEW_SCALE) / scale,
					(element.getY() / OVERVIEW_SCALE) / scale, (current.getWidth() / OVERVIEW_SCALE) / scale,
					(current.getHeight() / OVERVIEW_SCALE) / scale);
			overviewModel.addVertex(newVertex);
		}
	}

	private void paintDiagram(Graphics2D g2D) {
		g2D.scale(scale, scale);
		g2D.translate(-translateX, -translateY);
		paintElements(model, g2D);
	}

	// Wrap painting elements for convenience
	private void paintElements(Model m, Graphics2D g2D) {
		for (Element element : m.getElements()) {
			element.paint(g2D);
		}
	}

	private void paintOverview(Graphics2D g2D) {
		g2D.scale(1, 1);

		double overviewWidth = getWidth() / OVERVIEW_SCALE / scale;
		double overvewHeight = getHeight() / OVERVIEW_SCALE / scale;

		// Keep overview border constant
		g2D.setStroke(new BasicStroke((float) (DEFAULT_BORDER_THICKNESS / scale)));

		// Put overview at top right corner
		double overviewX = ((getWidth() - 1) / scale) - overviewWidth + translateX + overviewTranslateX;
		double overviewBorderY = (1 / scale) + translateY + overviewTranslateY;
		double overviewBorderHeight = OVERVIEW_TOP_BORDER_HEIGHT / scale;
		overviewTopBorder = new Rectangle2D.Double(overviewX, overviewBorderY, overviewWidth, overviewBorderHeight);
		g2D.draw(overviewTopBorder);
		g2D.fill(overviewTopBorder);
		double overviewY = overviewBorderY + overviewBorderHeight;
		overviewRect = new Rectangle2D.Double(overviewX, overviewY, overviewWidth, overvewHeight);
		g2D.draw(overviewRect);
		g2D.setPaint(Color.WHITE);
		g2D.fill(overviewRect);

		// Translate coordinates to overview
		g2D.translate(overviewRect.getX(), overviewRect.getY());
		paintElements(overviewModel, g2D);

		// Translate coordinates back to main view
		g2D.translate(-overviewRect.getX(), -overviewRect.getY());
		paintMarker(g2D);
	}

	private void paintMarker(Graphics2D g2D) {
		// Scale the marker to match the area being shown on the main view
		double x = overviewRect.getX() + (translateX / scale) / OVERVIEW_SCALE;
		double y = overviewRect.getY() + (translateY / scale) / OVERVIEW_SCALE;
		double width = overviewRect.getWidth() / scale;
		double height = overviewRect.getHeight() / scale;
		marker = new Rectangle2D.Double(x, y, width, height);
		g2D.setColor(Color.RED);
		g2D.draw(marker);

		// set max translate X and Y
		maxTranslateX = (overviewRect.getWidth() - marker.getWidth()) * scale * OVERVIEW_SCALE;
		maxTranslateY = (overviewRect.getHeight() - marker.getHeight()) * scale * OVERVIEW_SCALE;
	}

	public boolean markerContains(int x, int y) {
		return marker.contains(x, y);
	}

	public double getOverviewTranslateX() {
		return overviewTranslateX;
	}

	public void setOverviewTranslateX(double overviewTranslateX) {
		this.overviewTranslateX = overviewTranslateX;
	}

	public double getOverviewTranslateY() {
		return overviewTranslateY;
	}

	public void setOverviewTranslateY(double overviewTranslateY) {
		this.overviewTranslateY = overviewTranslateY;
	}

	public double getFocusX() {
		return focusX;
	}

	public void setFocusX(double focusX) {
		this.focusX = focusX;
	}

	public double getFocusY() {
		return focusY;
	}

	public void setFocusY(double focusY) {
		this.focusY = focusY;
	}
}
