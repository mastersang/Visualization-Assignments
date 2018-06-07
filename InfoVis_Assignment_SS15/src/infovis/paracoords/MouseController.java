package infovis.paracoords;

import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import infovis.scatterplot.Data;
import infovis.scatterplot.Model;

public class MouseController implements MouseListener, MouseMotionListener {
	private View view = null;
	private Model model = null;
	private double startingX = 0;
	private double startingY = 0;
	private Axis selectedAxis = null;

	public void mouseClicked(MouseEvent e) {

	}

	public void mouseEntered(MouseEvent e) {

	}

	public void mouseExited(MouseEvent e) {

	}

	public void mousePressed(MouseEvent e) {
		startingX = e.getX();
		int y = e.getY();

		// Check if user is dragging an axis
		for (int i = 0; i < view.getAxisList().size(); ++i) {
			Axis container = view.getAxisList().get(i);

			if (container.getRectangle().contains(new Point2D.Double(startingX, y))) {
				selectedAxis = container;
				break;
			}
		}

		if (selectedAxis == null) {
			startingY = y;
			view.getMarkerRectangle().setRect(startingX, startingY, 0, 0);

			// clear old selection
			for (Data data : model.getList()) {
				data.setSelected(false);
			}
		}
	}

	public void mouseReleased(MouseEvent arg0) {
		if (selectedAxis == null) {
			// set selected data
			for (PlotLine line : view.getPlotLineList()) {
				// loop through the lines
				for (int i = 0; i < line.getxCoords().size() - 1; ++i) {
					int startX = line.getxCoords().get(i);
					int endX = line.getxCoords().get(i + 1);
					int startY = line.getyCoords().get(i);
					int endY = line.getyCoords().get(i + 1);
					Line2D line2D = new Line2D.Double(startX, startY, endX, endY);

					// check if line intersects with marker area
					if (line2D.intersects(view.getMarkerRectangle())) {
						line.getData().setSelected(true);
						break;
					}
				}
			}

			view.getMarkerRectangle().setRect(0, 0, 0, 0);
		}

		selectedAxis = null;
		startingX = 0;
		startingY = 0;
		view.repaint();
	}

	public void mouseDragged(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();

		if (selectedAxis != null) {
			updateAxis(x);
		} else {
			updateMarker(x, y);
		}

		view.repaint();
	}

	private void updateAxis(int x) {
		double translation = x - startingX;
		startingX = x;
		selectedAxis.setTranslation(selectedAxis.getTranslation() + translation);
	}

	private void updateMarker(int x, int y) {
		Rectangle2D marker = view.getMarkerRectangle();
		double markerX = startingX;
		double markerY = startingY;
		double width = Math.abs(x - startingX);
		double height = Math.abs(y - startingY);

		if (x < startingX) {
			markerX = x;
		}

		if (y < startingY) {
			markerY = y;
		}

		marker.setRect(markerX, markerY, width, height);
	}

	public void mouseMoved(MouseEvent e) {

	}

	public View getView() {
		return view;
	}

	public void setView(View view) {
		this.view = view;
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}
}
