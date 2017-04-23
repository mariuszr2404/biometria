package okienko;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Panel;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.util.Observable;

import javax.print.DocFlavor.INPUT_STREAM;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.statistics.HistogramDataset;

public class Histogram {

	private Color pixelColor;
	int kolor = 0;
	private static final int BINS = 256;
	// final zmienic najprawdopodobniej i zamiast imageChanged Buffer zmienic
	// na label obrazek changed
	// private final BufferedImage image;
	BufferedImage image = OknoBiometria.bufferPom;
	private HistogramDataset dataset;
	// diagram z osia x, y
	private XYBarRenderer renderer;
	private double[] sz;

	private ChartPanel createChartPanel() {
		// dataset
		dataset = new HistogramDataset();
		// A class representing a rectangular array of pixels.A Raster
		// encapsulates a DataBuffer that stores
		// the sample values and a SampleModel that describes how to locate a
		// given sample value in a DataBuffer.
		Raster raster = image.getRaster();
		final int w = image.getWidth();
		final int h = image.getHeight();
		double[] r = new double[w * h];
		double[] g = new double[w * h];
		double[] b = new double[w * h];
		sz = new double[w * h];

		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				pixelColor = new Color(image.getRGB(i, j));
				int red = pixelColor.getRed();
				int blue = pixelColor.getBlue();
				int green = pixelColor.getGreen();
				r[j * w + i] = red;
				b[j * w + i] = blue;
				g[j * w + i] = green;
				sz[j * w + i] = (green + red + blue) / 3;
			}
		}

		// r = raster.getSamples(0, 0, w, h, 0, r);
		dataset.addSeries("Red", r, BINS);
		// przedostatni parametr band to return, ostatni parametr, zwraca
		// tablicê ze s³upkami
		// r = raster.getSamples(0, 0, w, h, 1, r);
		dataset.addSeries("Green", g, BINS);
		// r = raster.getSamples(0, 0, w, h, 2, r);
		dataset.addSeries("Blue", b, BINS);
		dataset.addSeries("Gray", sz, BINS);

		// chart

		/*
		 * JFrame constructor
		 * 
		 * @param1 -> title, @param2 -> nazwa osi y, @param3 -> nazwa osi x
		 * 
		 * @param4 -> Plot @param5 -> create legend
		 */

		/*
		 * title, xLabel, yLabel, dataset, orientation, legend, tooltips, urls
		 */
		JFreeChart chart = ChartFactory.createHistogram("Histogram", "Value", "Count", dataset,
				PlotOrientation.VERTICAL, true, true, false);
		XYPlot plot = (XYPlot) chart.getPlot();
		renderer = (XYBarRenderer) plot.getRenderer();
		renderer.setBarPainter(new StandardXYBarPainter());
		// translucent red, green & blue
		Paint[] paintArray = { new Color(0x80ff0000, true), new Color(0x8000ff00, true), new Color(0x800000ff, true),
				new Color(0x80999966, true) };

		// supplier -> dostawca
		// Outline -> zarys
		plot.setDrawingSupplier(new DefaultDrawingSupplier(paintArray,
				DefaultDrawingSupplier.DEFAULT_FILL_PAINT_SEQUENCE,
				DefaultDrawingSupplier.DEFAULT_OUTLINE_PAINT_SEQUENCE, DefaultDrawingSupplier.DEFAULT_STROKE_SEQUENCE,
				DefaultDrawingSupplier.DEFAULT_OUTLINE_STROKE_SEQUENCE, DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE));
		ChartPanel panel = new ChartPanel(chart);
		panel.setMouseWheelEnabled(true);
		return panel;
	}

	private JPanel createControlPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		panel.add(new JCheckBox(new VisibleAction(0)));
		panel.add(new JCheckBox(new VisibleAction(1)));
		panel.add(new JCheckBox(new VisibleAction(2)));
		panel.add(new JCheckBox(new VisibleAction(3)));
		return panel;
	}

	private class VisibleAction extends AbstractAction {

		private final int i;

		public VisibleAction(int i) {
			this.i = i;
			this.putValue(NAME, (String) dataset.getSeriesKey(i));
			this.putValue(SELECTED_KEY, true);
			renderer.setSeriesVisible(i, true);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			renderer.setSeriesVisible(i, !renderer.getSeriesVisible(i));
		}
	}

	public void display() {
		JFrame f = new JFrame("Histogram");
		// f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.add(createChartPanel());
		f.add(createControlPanel(), BorderLayout.SOUTH);

		JScrollPane sP = new JScrollPane();
		sP.setViewportView(new JLabel(new ImageIcon(image)));

		f.add(sP, BorderLayout.WEST);
		f.pack();
		f.setLocationRelativeTo(null);
		f.setVisible(true);
	}

	public void brighter() {
		int[] lutR = new int[256];
		int[] lutG = new int[256];
		int[] lutB = new int[256];

		double maxR = 0;
		double maxG = 0;
		double maxB = 0;
		int red, green, blue;
		Color color;

		ImageIcon pomocniczaImageIcon = (ImageIcon) OknoBiometria.obrazekChanged.getIcon();
		Image pomocniczaImage = pomocniczaImageIcon.getImage();
		BufferedImage bufor = (BufferedImage) pomocniczaImage;
		BufferedImage buforCpy = OknoBiometria.copyBFImage(bufor);

		for (int i = 0; i < buforCpy.getWidth(); i++) {
			for (int j = 0; j < buforCpy.getHeight(); j++) {

				color = new Color(buforCpy.getRGB(i, j));

				if (maxR < color.getRed())
					maxR = color.getRed();
				if (maxG < color.getGreen())
					maxG = color.getGreen();
				if (maxB < color.getBlue())
					maxB = color.getBlue();
			}
		}

		for (int i = 0; i < lutR.length; i++) {

			if (255 * ((Math.log10((double) ((1 + i)))) / (Math.log10((double) (1 + maxR)))) < 0.00)
				lutR[i] = 0;
			else if (255 * ((Math.log10((double) ((1 + i)))) / (Math.log10((double) (1 + maxR)))) > 255)
				lutR[i] = 255;
			else
				lutR[i] = (int) ((int) 255 * ((Math.log10((double) ((1 + i)))) / (Math.log10((double) (1 + maxR)))));

			if (255 * ((Math.log10((double) ((1 + i)))) / (Math.log10((double) (1 + maxG)))) < 0.00)
				lutG[i] = 0;
			else if (255 * ((Math.log10((double) ((1 + i)))) / (Math.log10((double) (1 + maxG)))) > 255)
				lutG[i] = 255;
			else
				lutG[i] = (int) ((int) 255 * ((Math.log10((double) ((1 + i)))) / (Math.log10((double) (1 + maxG)))));

			if (255 * ((Math.log10((double) ((1 + i)))) / (Math.log10((double) (1 + maxB)))) < 0.00)
				lutB[i] = 0;
			else if (255 * ((Math.log10((double) ((1 + i)))) / (Math.log10((double) (1 + maxB)))) > 255)
				lutB[i] = 255;
			else
				lutB[i] = (int) ((int) 255 * ((Math.log10((double) ((1 + i)))) / (Math.log10((double) (1 + maxB)))));

		}

		for (int i = 0; i < buforCpy.getWidth(); i++) {
			for (int j = 0; j < buforCpy.getHeight(); j++) {

				color = new Color(buforCpy.getRGB(i, j));
				red = color.getRed();
				green = color.getGreen();
				blue = color.getBlue();

				buforCpy.setRGB(i, j, new Color(lutR[red], lutG[green], lutB[blue]).getRGB());
			}
		}
		OknoBiometria.bufferPom = buforCpy;
		OknoBiometria.obrazekChanged.setIcon(new ImageIcon(buforCpy));
	}

	public void darker() {
		int[] lutR = new int[256];
		int[] lutG = new int[256];
		int[] lutB = new int[256];

		double maxR = 0;
		double maxG = 0;
		double maxB = 0;
		int red, green, blue;
		Color pom;

		ImageIcon pomocniczaImageIcon = (ImageIcon) OknoBiometria.obrazekChanged.getIcon();
		Image pomocniczaImage = pomocniczaImageIcon.getImage();
		BufferedImage bufor = (BufferedImage) pomocniczaImage;
		BufferedImage buforCpy = OknoBiometria.copyBFImage(bufor);

		for (int i = 0; i < buforCpy.getWidth(); i++) {
			for (int j = 0; j < buforCpy.getHeight(); j++) {

				pom = new Color(buforCpy.getRGB(i, j));

				if (maxR < pom.getRed())
					maxR = pom.getRed();
				if (maxG < pom.getGreen())
					maxG = pom.getGreen();
				if (maxB < pom.getBlue())
					maxB = pom.getBlue();
			}
		}

		for (int i = 0; i < lutR.length; i++) {

			if (255 * Math.pow(((double) i / maxR), 2) < 0)
				lutR[i] = 0;
			else if (255 * Math.pow(((double) i / maxR), 2) > 255)
				lutR[i] = 255;
			else
				lutR[i] = (int) (255 * Math.pow(((double) i / maxR), 2));

			if (255 * Math.pow(((double) i / maxG), 2) < 0)
				lutG[i] = 0;
			else if (255 * Math.pow(((double) i / maxG), 2) > 255)
				lutG[i] = 255;
			else
				lutG[i] = (int) (255 * Math.pow(((double) i / maxG), 2));

			if (255 * Math.pow(((double) i / maxB), 2) < 0)
				lutB[i] = 0;
			else if (255 * Math.pow(((double) i / maxB), 2) > 255)
				lutB[i] = 255;
			else
				lutB[i] = (int) (255 * Math.pow(((double) i / maxB), 2));

		}

		for (int i = 0; i < buforCpy.getWidth(); i++) {
			for (int j = 0; j < buforCpy.getHeight(); j++) {

				pom = new Color(buforCpy.getRGB(i, j));
				red = pom.getRed();
				green = pom.getGreen();
				blue = pom.getBlue();

				buforCpy.setRGB(i, j, new Color(lutR[red], lutG[green], lutB[blue]).getRGB());
			}
		}
		OknoBiometria.bufferPom = buforCpy;
		OknoBiometria.obrazekChanged.setIcon(new ImageIcon(buforCpy));
	}

	void setHisto() {
		double[] pixR = new double[256];
		double[] pixG = new double[256];
		double[] pixB = new double[256];

		int[] lutR = new int[256];
		int[] lutG = new int[256];
		int[] lutB = new int[256];

		double[] dystR = new double[256];
		double[] dystG = new double[256];
		double[] dystB = new double[256];

		double dystRMin = 0;
		double dystGMin = 0;
		double dystBMin = 0;

		double pixCount = 0;

		Color pom;

		ImageIcon pomocniczaImageIcon = (ImageIcon) OknoBiometria.obrazekChanged.getIcon();
		Image pomocniczaImage = pomocniczaImageIcon.getImage();
		BufferedImage bufor = (BufferedImage) pomocniczaImage;
		BufferedImage buforCpy = OknoBiometria.copyBFImage(bufor);

		pixCount = bufor.getHeight() * bufor.getWidth();

		for (int i = 0; i < bufor.getWidth(); i++) {
			for (int j = 0; j < bufor.getHeight(); j++) {

				pom = new Color(bufor.getRGB(i, j));

				pixR[pom.getRed()]++;
				pixG[pom.getGreen()]++;
				pixB[pom.getBlue()]++;
			}
		}

		for (int i = 0; i < pixR.length; i++) {
			if (i == 0) {
				dystR[i] = pixR[i] / pixCount;
				dystG[i] = pixG[i] / pixCount;
				dystB[i] = pixB[i] / pixCount;
			} else {
				dystR[i] = dystR[i - 1] + pixR[i] / pixCount;
				dystG[i] = dystG[i - 1] + pixG[i] / pixCount;
				dystB[i] = dystB[i - 1] + pixB[i] / pixCount;
			}
		}

		int ii = 0;
		while (dystRMin == 0) {
			dystRMin = dystR[ii];
			ii++;
		}
		ii = 0;
		while (dystGMin == 0) {
			dystGMin = dystG[ii];
			ii++;
		}
		ii = 0;
		while (dystBMin == 0) {
			dystBMin = dystB[ii];
			ii++;
		}

		for (int i = 0; i < lutR.length; i++) {

			if ((int) (((dystR[i] - dystRMin) / 1 - dystRMin) * (256 - 1)) < 0)
				lutR[i] = 0;
			else if ((int) (((dystR[i] - dystRMin) / 1 - dystRMin) * (256 - 1)) > 255)
				lutR[i] = 255;
			else
				lutR[i] = (int) (((dystR[i] - dystRMin) / 1 - dystRMin) * (256 - 1));
		}

		for (int i = 0; i < lutR.length; i++) {

			if ((int) (((dystG[i] - dystGMin) / 1 - dystGMin) * (256 - 1)) < 0)
				lutG[i] = 0;
			else if ((int) (((dystG[i] - dystGMin) / 1 - dystGMin) * (256 - 1)) > 255)
				lutG[i] = 255;
			else
				lutG[i] = (int) (((dystG[i] - dystGMin) / 1 - dystGMin) * (256 - 1));
		}

		for (int i = 0; i < lutR.length; i++) {
			if ((int) (((dystB[i] - dystBMin) / 1 - dystBMin) * (256 - 1)) < 0)
				lutB[i] = 0;
			else if ((int) (((dystB[i] - dystBMin) / 1 - dystBMin) * (256 - 1)) > 255)
				lutB[i] = 255;
			else
				lutB[i] = (int) (((dystB[i] - dystBMin) / 1 - dystBMin) * (256 - 1));
		}

		for (int i = 0; i < buforCpy.getWidth(); i++) {
			for (int j = 0; j < buforCpy.getHeight(); j++) {
				pom = new Color(buforCpy.getRGB(i, j));

				buforCpy.setRGB(i, j,
						new Color(lutR[pom.getRed()], lutG[pom.getGreen()], lutB[pom.getBlue()]).getRGB());

			}

		}

		OknoBiometria.bufferPom = buforCpy;
		OknoBiometria.obrazekChanged.setIcon(new ImageIcon(buforCpy));
	}

	void extendHisto(int min, int max) {

		Color pixelColor;
		int red;
		int green;
		int blue;

		ImageIcon pomocniczaImageIcon = (ImageIcon) OknoBiometria.obrazekChanged.getIcon();
		Image pomocniczaImage = pomocniczaImageIcon.getImage();
		BufferedImage bufor = (BufferedImage) pomocniczaImage;
		BufferedImage buforCpy = OknoBiometria.copyBFImage(bufor);

		int width = buforCpy.getWidth();
		int height = buforCpy.getHeight();

		int[] rhistogram = new int[256];
		int[] ghistogram = new int[256];
		int[] bhistogram = new int[256];

		for (int i = 0; i < rhistogram.length; i++)
			rhistogram[i] = 0;
		for (int i = 0; i < ghistogram.length; i++)
			ghistogram[i] = 0;
		for (int i = 0; i < bhistogram.length; i++)
			bhistogram[i] = 0;

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				pixelColor = new Color(buforCpy.getRGB(i, j));
				red = pixelColor.getRed();
				green = pixelColor.getGreen();
				blue = pixelColor.getBlue();

				rhistogram[red]++;
				ghistogram[green]++;
				bhistogram[blue]++;
			}
		}

		// create LUT table
		for (int i = 0; i < rhistogram.length; i++) {
			int valr = (int) (255 * (i - min) / (max - min));
			if (valr > 255) {
				rhistogram[i] = 255;
			}
			if (valr <= 0) {
				rhistogram[i] = 0;
			} else {
				rhistogram[i] = valr;
			}

			int valb = (int) (255 * (i - min) / (max - min));
			if (valb > 255) {
				bhistogram[i] = 255;
			}
			if (valb <= 0) {
				bhistogram[i] = 0;
			} else {
				bhistogram[i] = valb;
			}

			int valg = (int) (255 * (i - min) / (max - min));
			if (valg > 255) {
				ghistogram[i] = 255;
			}
			if (valg <= 0) {
				ghistogram[i] = 0;
			} else {
				ghistogram[i] = valg;
			}
		}

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {

				red = new Color(buforCpy.getRGB(i, j)).getRed();
				green = new Color(buforCpy.getRGB(i, j)).getGreen();
				blue = new Color(buforCpy.getRGB(i, j)).getBlue();

				// Set new pixel values using the histogram lookup table
				red = rhistogram[red];
				green = ghistogram[green];
				blue = bhistogram[blue];

				buforCpy.setRGB(i, j, new Color(red, green, blue).getRGB());

			}
		}

		OknoBiometria.bufferPom = buforCpy;
		OknoBiometria.obrazekChanged.setIcon(new ImageIcon(buforCpy));
	}

	void extendHisto2() {
		Color pixelColor;
		int red;
		int green;
		int blue;

		ImageIcon pomocniczaImageIcon = (ImageIcon) OknoBiometria.obrazekChanged.getIcon();
		Image pomocniczaImage = pomocniczaImageIcon.getImage();
		BufferedImage bufor = (BufferedImage) pomocniczaImage;
		BufferedImage buforCpy = OknoBiometria.copyBFImage(bufor);

		int width = buforCpy.getWidth();
		int height = buforCpy.getHeight();
		int total = width * height;

		double discard_ratio = 0.01;

		int[] rhistogram = new int[256];
		int[] ghistogram = new int[256];
		int[] bhistogram = new int[256];

		for (int i = 0; i < rhistogram.length; i++)
			rhistogram[i] = 0;
		for (int i = 0; i < ghistogram.length; i++)
			ghistogram[i] = 0;
		for (int i = 0; i < bhistogram.length; i++)
			bhistogram[i] = 0;

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				pixelColor = new Color(buforCpy.getRGB(i, j));
				red = pixelColor.getRed();
				green = pixelColor.getGreen();
				blue = pixelColor.getBlue();

				rhistogram[red]++;
				ghistogram[green]++;
				bhistogram[blue]++;
			}
		}

		int[][] hists = new int[3][256];
		hists[0] = rhistogram;
		hists[1] = ghistogram;
		hists[2] = bhistogram;

		int[] vmin = new int[3];
		int[] vmax = new int[3];

		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 255; ++j) {
				hists[i][j + 1] += hists[i][j];
			}
			vmin[i] = 0;
			vmax[i] = 255;
			while (hists[i][vmin[i]] < discard_ratio * total)
				vmin[i] += 1;
			while (hists[i][vmax[i]] > (1 - discard_ratio) * total)
				vmax[i] -= 1;
			if (vmax[i] < 255 - 1)
				vmax[i] += 1;
		}

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {

				int[] rgbValues = new int[3];

				for (int j = 0; j < 3; ++j) {

					int val = 0;
					red = new Color(image.getRGB(x, y)).getRed();
					green = new Color(image.getRGB(x, y)).getGreen();
					blue = new Color(image.getRGB(x, y)).getBlue();

					if (j == 0)
						val = red;
					if (j == 1)
						val = green;
					if (j == 2)
						val = blue;

					if (val < vmin[j])
						val = vmin[j];
					if (val > vmax[j])
						val = vmax[j];

					rgbValues[j] = (int) ((val - vmin[j]) * 255.0 / (vmax[j] - vmin[j]));
				}

				int alpha = new Color(image.getRGB(y, x)).getAlpha();

				red = rgbValues[0];
				green = rgbValues[1];
				blue = rgbValues[2];

				int newPixel = 0;
				newPixel += alpha;
				newPixel = newPixel << 8;
				newPixel += red;
				newPixel = newPixel << 8;
				newPixel += green;
				newPixel = newPixel << 8;
				newPixel += blue;
				
				buforCpy.setRGB(x, y, newPixel);
			}
			
			OknoBiometria.bufferPom = buforCpy;
			OknoBiometria.obrazekChanged.setIcon(new ImageIcon(buforCpy));
		}
	}
}
