package infovis.paracoords;

import java.util.ArrayList;

import infovis.scatterplot.Data;

public class PlotLine {
	private Data data;
	private ArrayList<Integer> xCoords = new ArrayList<Integer>();
	private ArrayList<Integer> yCoords = new ArrayList<Integer>();

	public PlotLine(Data data) {
		this.data = data;
	}

	public Data getData() {
		return data;
	}

	public void setData(Data data) {
		this.data = data;
	}

	public ArrayList<Integer> getyCoords() {
		return yCoords;
	}

	public void setyCoords(ArrayList<Integer> yCoords) {
		this.yCoords = yCoords;
	}

	public ArrayList<Integer> getxCoords() {
		return xCoords;
	}

	public void setxCoords(ArrayList<Integer> xCoords) {
		this.xCoords = xCoords;
	}
}