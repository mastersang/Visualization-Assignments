package infovis.paracoords;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.SwingUtilities;

import infovis.gui.GUI;
import infovis.scatterplot.Model;

public class ParaCoords {
	private MouseController controller = null;
	private Model model = null;
	private View view = null;

	public View getView() {
		if (view == null)
			generateDiagram();
		return view;
	}

	public void generateDiagram() {
		model = new Model();
		view = new View();
		controller = new MouseController();
		view.addMouseListener(controller);
		view.addMouseMotionListener(controller);
		view.setModel(model);
		controller.setModel(model);
		controller.setView(view);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				GUI application = new GUI();
				application.setView(new ParaCoords().getView());
				application.getJFrame().setVisible(true);
			}
		});
	}

}
