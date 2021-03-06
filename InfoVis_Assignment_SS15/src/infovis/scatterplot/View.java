package infovis.scatterplot;

import infovis.debug.Debug;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import javax.swing.JPanel;

public class View extends JPanel {
	private Model model = null;
	private Rectangle2D markerRectangle = new Rectangle2D.Double(0, 0, 0, 0);
	private int cellSize = 40;
	private int pointSize = 5;
	private int padding = 20;
	private ArrayList<MatrixCell> cellList = new ArrayList<MatrixCell>();

	public Rectangle2D getMarkerRectangle() {
		return markerRectangle;
	}

	public ArrayList<MatrixCell> getCellList() {
		return cellList;
	}

	public void setModel(Model model) {
		this.model = model;
	}

	@Override
	public void paint(Graphics g) {
		Graphics2D g2D = (Graphics2D) g;
		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2D.setBackground(Color.WHITE);
		g2D.clearRect(0, 0, getWidth(), getHeight());
		debug();

		// set cell size depending on frame size
		cellSize = (getHeight() - 50) / model.getLabels().size();
		g2D.setStroke(new BasicStroke(2.f));
		paintScatterPlot(g2D);
		drawMarker(g2D);
	}

	private void paintScatterPlot(Graphics2D g2D) {
		int labelsSize = model.getLabels().size();
		Font font = new Font("Arial", Font.PLAIN, 10);
		g2D.setFont(font);

		// columnn loop
		for (int i = 0; i < labelsSize; ++i) {
			// row loop
			for (int j = 0; j < labelsSize; ++j) {
				Rectangle2D rect = new Rectangle2D.Double(cellSize * i + 5, cellSize * j + 5, cellSize, cellSize);
				MatrixCell cell;

				// create new and add to cell list the first time, otherwise get from existing
				// list
				if (cellList.size() < labelsSize * labelsSize) {
					cell = new MatrixCell(rect, j, i);
					cellList.add(cell);
				} else {
					cell = cellList.get(i * labelsSize + j);
					cell.setRect(rect);
				}

				g2D.setPaint(Color.BLACK);
				g2D.draw(rect);

				// Fill cell with white background
				g2D.setPaint(Color.WHITE);
				g2D.fill(rect);
				g2D.setPaint(Color.BLACK);

				// diagonal cell
				if (j == i) {
					drawDiagonalCells(g2D, cell, model.getLabels().get(i), font);
				} else {
					drawPlotCells(g2D, cell, i, j);
				}
			}
		}
	}

	private void debug() {
		Debug.println("------------------");
		Debug.println("Labels: ");

		for (String l : model.getLabels()) {
			Debug.print(l);
			Debug.print(",  ");
			Debug.println("");
		}

		Debug.println("------------------");
		Debug.println("Ranges:");

		for (Range range : model.getRanges()) {
			Debug.print(range.toString());
			Debug.print(",  ");
			Debug.println("");
		}

		Debug.println("------------------");
		Debug.println("Data:");

		for (Data d : model.getList()) {
			Debug.print(d.toString());
			Debug.println("");
		}
	}

	private void drawDiagonalCells(Graphics2D g2D, MatrixCell cell, String label, Font font) {
		// center the text
		// https://stackoverflow.com/questions/27706197/how-can-i-center-graphics-drawstring-in-java
		FontMetrics metrics = g2D.getFontMetrics(font);
		Rectangle2D rect = cell.getRect();
		int x = (int) Math.round(rect.getX()) + (cellSize - metrics.stringWidth(label)) / 2;
		int y = (int) Math.round(rect.getY()) + ((cellSize - metrics.getHeight()) / 2) + metrics.getAscent();
		g2D.drawString(label, x, y);
	}

	private void drawPlotCells(Graphics2D g2D, MatrixCell cell, int column, int row) {
		// scale the data to pixels
		Range rangeRow = model.getRanges().get(row);
		double minVertical = rangeRow.getMin();
		double maxVertical = rangeRow.getMax();
		double stepVertical = (maxVertical - minVertical) / (cellSize - padding);
		Range rangeColumn = model.getRanges().get(column);
		double minHorizontal = rangeColumn.getMin();
		double maxHorizontal = rangeColumn.getMax();
		double stepHorizontal = (maxHorizontal - minHorizontal) / (cellSize - padding);
		int dataSize = model.getList().size();

		for (int i = 0; i < dataSize; ++i) {
			Data data = model.getList().get(i);
			double valueVertical = data.getValue(row);
			double valueHorizontal = data.getValue(column);
			Rectangle2D rect = cell.getRect();
			// set point position
			double x = rect.getX() + 5 + (valueHorizontal - minHorizontal) / stepHorizontal;
			double y = rect.getY() + 5 + (valueVertical - minVertical) / stepVertical;

			if (data.getSelected()) {
				g2D.setColor(Color.RED);
			} else {
				g2D.setColor(Color.BLACK);
			}

			// draw point
			// https://stackoverflow.com/questions/19386951/how-to-draw-a-circle-with-given-x-and-y-coordinates-as-the-middle-spot-of-the-ci
			g2D.fillOval((int) Math.round(x), (int) Math.round(y), pointSize, pointSize);
			updatePointList(cell, dataSize, valueHorizontal, valueVertical, x, y, i);
		}
	}

	private void updatePointList(MatrixCell cell, int dataSize, double valueHorizontal, double valueVertical, double x,
			double y, int index) {
		if (cell.getPointList().size() < dataSize) {
			// add point to list if first time
			cell.getPointList().add(new PlotPoint(valueHorizontal, valueVertical, x, y));
		} else {
			// update point if not first time
			PlotPoint point = cell.getPointList().get(index);
			point.setX(x);
			point.setY(y);
			point.setValueHorizontal(valueHorizontal);
			point.setValueVertical(valueVertical);
		}
	}

	private void drawMarker(Graphics2D g2D) {
		g2D.setPaint(Color.RED);
		g2D.draw(markerRectangle);
	}
}
