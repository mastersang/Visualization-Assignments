package infovis.paracoords;

import java.awt.geom.Rectangle2D;

public class Axis {
	private int x;
	private int labelIndex;
	private double translation;
	private double min;
	private double max;
	private double step;
	private Rectangle2D rectangle;

	public Axis(int x, int labelIndex, double min, double max, double step, Rectangle2D rectangle) {
		this.x = x;
		this.labelIndex = labelIndex;
		this.min = min;
		this.max = max;
		this.step = step;
		this.rectangle = rectangle;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getLabelIndex() {
		return labelIndex;
	}

	public void setLabelIndex(int labelIndex) {
		this.labelIndex = labelIndex;
	}

	public double getMin() {
		return min;
	}

	public void setMin(double min) {
		this.min = min;
	}

	public double getStep() {
		return step;
	}

	public void setStep(double step) {
		this.step = step;
	}

	public double getMax() {
		return max;
	}

	public void setMax(double max) {
		this.max = max;
	}

	public double getTranslation() {
		return translation;
	}

	public void setTranslation(double offset) {
		this.translation = offset;
	}

	public Rectangle2D getRectangle() {
		return rectangle;
	}

	public void setRectangle(Rectangle2D rectangle) {
		this.rectangle = rectangle;
	}
}