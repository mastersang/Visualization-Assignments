package infovis.scatterplot;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

public class MatrixCell {
	private Rectangle2D rect;
	private ArrayList<PlotPoint> pointList = new ArrayList<PlotPoint>();
	private int row;
	private int column;

	public MatrixCell(Rectangle2D cell, int row, int column) {
		this.rect = cell;
		this.row = row;
		this.column = column;
	}

	public Rectangle2D getRect() {
		return rect;
	}

	public void setRect(Rectangle2D rect) {
		this.rect = rect;
	}

	public ArrayList<PlotPoint> getPointList() {
		return pointList;
	}

	public void setPointList(ArrayList<PlotPoint> pointList) {
		this.pointList = pointList;
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getColumn() {
		return column;
	}

	public void setColumn(int column) {
		this.column = column;
	}
}
