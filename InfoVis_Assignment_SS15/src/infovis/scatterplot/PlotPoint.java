package infovis.scatterplot;

public class PlotPoint {
	private double valueHorizontal;
	private double valueVertical;
	private double x;
	private double y;

	public PlotPoint(double valueHorizontal, double valueVertical, double x, double y) {
		this.valueHorizontal = valueHorizontal;
		this.valueVertical = valueVertical;
		this.x = x;
		this.y = y;
	}

	public double getValueHorizontal() {
		return valueHorizontal;
	}

	public void setValueHorizontal(double valueHorizontal) {
		this.valueHorizontal = valueHorizontal;
	}

	public double getValueVertical() {
		return valueVertical;
	}

	public void setValueVertical(double valueVertical) {
		this.valueVertical = valueVertical;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}
}
