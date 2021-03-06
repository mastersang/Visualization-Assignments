package infovis.scatterplot;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class MouseController implements MouseListener, MouseMotionListener {
	private Model model = null;
	private View view = null;
	private double startingX = 0;
	private double startingY = 0;
	private MatrixCell selectedCell;

	public void setModel(Model model) {
		this.model = model;
	}

	public void setView(View view) {
		this.view = view;
	}

	public void mouseClicked(MouseEvent arg0) {
	}

	public void mouseEntered(MouseEvent arg0) {
	}

	public void mouseExited(MouseEvent arg0) {
	}

	public void mousePressed(MouseEvent e) {
		startingX = e.getX();
		startingY = e.getY();
		view.getMarkerRectangle().setRect(startingX, startingY, 0, 0);

		// find which cell user is creeating marker on
		for (MatrixCell cell : view.getCellList()) {
			if (cell.getRect().contains(new Point2D.Double(startingX, startingY))) {
				selectedCell = cell;
				break;
			}
		}

		// clear old selection
		for (Data data : model.getList()) {
			data.setSelected(false);
		}
	}

	public void mouseReleased(MouseEvent arg0) {
		// set selected data
		for (PlotPoint point : selectedCell.getPointList()) {
			if (view.getMarkerRectangle().contains(new Point2D.Double(point.getX(), point.getY()))) {
				for (Data data : model.getList()) {
					if (data.getValue(selectedCell.getColumn()) == point.getValueHorizontal()
							&& data.getValue(selectedCell.getRow()) == point.getValueVertical()) {
						data.setSelected(true);
					}
				}
			}
		}

		view.getMarkerRectangle().setRect(0, 0, 0, 0);
		view.repaint();
	}

	public void mouseDragged(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();

		Rectangle2D marker = view.getMarkerRectangle();
		double markerX = startingX;
		double markerY = startingY;
		double width = Math.abs(x - startingX);
		double height = Math.abs(y - startingY);
		double maxWidth = 0;
		double maxHeight = 0;
		Rectangle2D rect = selectedCell.getRect();

		// set x and y to top left corner if dragged outside
		if (x < rect.getX()) {
			x = (int) Math.round(rect.getX());
		}

		if (y < rect.getY()) {
			y = (int) Math.round(rect.getY());
		}

		if (x > startingX) {
			// draw to the right
			maxWidth = rect.getX() + rect.getWidth() - startingX;
		} else {
			// draw to the left
			markerX = x;
			maxWidth = startingX - rect.getX();
		}

		if (y > startingY) {
			// draw downwards
			maxHeight = rect.getY() + rect.getHeight() - startingY;
		} else {
			// draw upwards
			markerY = y;
			maxHeight = startingY - rect.getY();
		}

		// keep marker within cell
		if (width > maxWidth) {
			width = maxWidth;
		}

		if (height > maxHeight) {
			height = maxHeight;
		}

		marker.setRect(markerX, markerY, width, height);
		view.repaint();
	}

	public void mouseMoved(MouseEvent arg0) {
	}
}
