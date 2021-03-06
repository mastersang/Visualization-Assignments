package infovis.paracoords;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Comparator;

import javax.swing.JPanel;

import infovis.scatterplot.Data;
import infovis.scatterplot.Model;
import infovis.scatterplot.Range;

public class View extends JPanel {
	private Model model = null;
	private Rectangle2D markerRectangle = new Rectangle2D.Double(0, 0, 0, 0);
	private int paddingX = 100;
	private int paddingY = 50;
	private int axisHeight = 0;
	private final int VALUE_COUNT = 5;
	private final int VALUE_LENGTH = 5;
	private final int VALUE_PADDING = 5;
	private ArrayList<Axis> axisList = new ArrayList<Axis>();
	private ArrayList<PlotLine> plotLineList = new ArrayList<PlotLine>();
	private ArrayList<Integer> labelIndexList = new ArrayList<>();

	public Rectangle2D getMarkerRectangle() {
		return markerRectangle;
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;

		// initialise
		for (int i = 0; i < model.getDim(); ++i) {
			labelIndexList.add(i);
		}
	}

	public ArrayList<Axis> getAxisList() {
		return axisList;
	}

	public ArrayList<PlotLine> getPlotLineList() {
		return plotLineList;
	}

	public ArrayList<Integer> getLabelIndexList() {
		return labelIndexList;
	}

	public void setLabelIndexList(ArrayList<Integer> labelIndexList) {
		this.labelIndexList = labelIndexList;
	}

	@Override
	public void paint(Graphics g) {
		plotLineList.clear();
		Graphics2D g2D = (Graphics2D) g;
		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2D.setBackground(Color.WHITE);
		g2D.clearRect(0, 0, getWidth(), getHeight());
		Font font = new Font("Arial", Font.PLAIN, 12);
		g2D.setFont(font);
		FontMetrics metrics = g2D.getFontMetrics(font);
		paintAxis(g2D, metrics);
		paintData(g2D, metrics);
		drawMarker(g2D);

		// update label index list after each draw because x coordinates of axis change
		updateLabelIndexList();
	}

	private void updateLabelIndexList() {
		ArrayList<Axis> temp = new ArrayList<Axis>();
		temp.addAll(axisList);

		temp.sort(new Comparator<Axis>() {
			@Override
			public int compare(Axis o1, Axis o2) {
				return (int) Math.round(o1.getX() + o1.getTranslation() - (o2.getX() + o2.getTranslation()));
			}
		});

		labelIndexList.clear();

		for (Axis axis : temp) {
			labelIndexList.add(axis.getLabelIndex());
		}
	}

	private void paintAxis(Graphics2D g2D, FontMetrics metrics) {
		axisHeight = getHeight() - paddingY * 3;
		int gap = (getWidth() - paddingY) / model.getDim();
		int axisCount = model.getLabels().size();

		for (int i = 0; i < axisCount; ++i) {
			int labelIndex = i;

			if (axisList.size() == axisCount) {
				labelIndex = axisList.get(i).getLabelIndex();
			}

			int axisX = paddingX + gap * i;
			int intOriginalX = axisX;

			if (axisList.size() == axisCount) {
				axisX += axisList.get(i).getTranslation();
			}

			g2D.drawLine(axisX, paddingY, axisX, paddingY + axisHeight);
			Rectangle2D rectangle = drawAxisLabel(g2D, metrics, labelIndex, axisX);
			updateAxisList(intOriginalX, i, labelIndex, rectangle);
		}
	}

	private Rectangle2D drawAxisLabel(Graphics2D g2D, FontMetrics metrics, int labelIndex, int axisX) {
		String label = model.getLabels().get(labelIndex);
		// Center text
		int stringWidth = metrics.stringWidth(label);
		int x = axisX - stringWidth / 2;
		int y = paddingY * 2 + axisHeight;
		g2D.setColor(Color.BLUE);
		g2D.drawString(label, x, y);
		g2D.setColor(Color.BLACK);

		// Create label container object
		Rectangle2D rectangle = new Rectangle2D.Double(x - VALUE_PADDING, y - metrics.getHeight(),
				stringWidth + VALUE_PADDING * 2, metrics.getHeight() * 2);
		g2D.draw(rectangle);
		return rectangle;
	}

	private void updateAxisList(int axisX, int axisIndex, int labelIndex, Rectangle2D rectangle) {
		Range range = model.getRanges().get(labelIndex);
		double min = range.getMin();
		double step = (range.getMax() - min) / axisHeight;

		// only add axis the first time
		if (axisList.size() < model.getLabels().size()) {
			// add new Axis
			axisList.add(new Axis(axisX, labelIndex, min, range.getMax(), step, rectangle));
		} else {
			// update existing axis
			Axis axis = axisList.get(axisIndex);
			axis.setX(axisX);
			axis.setStep(step);
			axis.setRectangle(rectangle);
		}
	}

	private void paintData(Graphics2D g2D, FontMetrics metrics) {
		for (int i = 0; i < labelIndexList.size(); ++i) {
			int labelIndex = labelIndexList.get(i);
			Axis currentAxis = axisList.get(labelIndex);
			Range range = model.getRanges().get(labelIndex);

			// draw top value;
			paintValue(g2D, currentAxis, paddingY, metrics, range.getMax());

			// draw bottom value;
			paintValue(g2D, currentAxis, paddingY + axisHeight, metrics, range.getMin());

			// draw middle values;
			double step = axisHeight / (VALUE_COUNT + 1);

			for (int j = 0; j < VALUE_COUNT; ++j) {
				int y = (int) Math.round(paddingY + axisHeight - step * (j + 1));
				paintValue(g2D, currentAxis, y, metrics,
						currentAxis.getMin() + (paddingY + axisHeight - y) * currentAxis.getStep());
			}

			// draw lines except on the last one
			if (i < axisList.size() - 1) {
				Axis nextAxis = findNextAxis(i + 1);
				paintLines(g2D, currentAxis, nextAxis);
			}
		}
	}

	private Axis findNextAxis(int index) {
		for (Axis axis : axisList) {
			if (axis.getLabelIndex() == labelIndexList.get(index)) {
				return axis;
			}
		}

		return null;
	}

	private void paintLines(Graphics2D g2D, Axis currentAxis, Axis nextAxis) {
		for (int j = 0; j < model.getList().size(); ++j) {
			Data data = model.getList().get(j);
			int startX = (int) Math.round(currentAxis.getX() + currentAxis.getTranslation());
			int endX = (int) Math.round(nextAxis.getX() + nextAxis.getTranslation());
			int startY = (int) Math.round(paddingY + axisHeight
					- (data.getValue(currentAxis.getLabelIndex()) - currentAxis.getMin()) / currentAxis.getStep());
			int endY = (int) Math.round(paddingY + axisHeight
					- (data.getValue(nextAxis.getLabelIndex()) - nextAxis.getMin()) / nextAxis.getStep());

			if (data.getSelected()) {
				g2D.setColor(Color.RED);
			} else {
				g2D.setColor(Color.BLACK);
			}

			g2D.drawLine(startX, startY, endX, endY);
			updatePlotLineList(data, j, startX, endX, startY, endY);
		}
	}

	private void updatePlotLineList(Data data, int index, int startX, int endX, int startY, int endY) {
		PlotLine line;

		if (plotLineList.size() < model.getList().size()) {
			line = new PlotLine(data);
			plotLineList.add(line);
		} else {
			line = plotLineList.get(index);
		}

		line.getxCoords().add(startX);
		line.getxCoords().add(endX);
		line.getyCoords().add(startY);
		line.getyCoords().add(endY);
	}

	private void paintValue(Graphics2D g2D, Axis axis, int y, FontMetrics metrics, double value) {
		// draw top value
		int axisX = (int) Math.round(axis.getX() + axis.getTranslation());
		g2D.drawLine(axisX - VALUE_LENGTH, y, axisX, y);
		String label = String.format("%.1f", value);
		int x = axisX - (metrics.stringWidth(label) + VALUE_PADDING);
		g2D.setColor(Color.ORANGE);
		g2D.drawString(label, x, y);
		g2D.setColor(Color.BLACK);
	}

	private void drawMarker(Graphics2D g2D) {
		g2D.setPaint(Color.RED);
		g2D.draw(markerRectangle);
	}

	@Override
	public void update(Graphics g) {
		paint(g);
	}
}
