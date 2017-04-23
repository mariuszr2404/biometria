package okienko;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

import org.jfree.chart.labels.BubbleXYItemLabelGenerator;

public class Binaryzacja {
	BufferedImage image = OknoBiometria.bufferPom;

	// Innym sposobem na bin jest skorzystanie z wzoru 0.21*red + 0.71*green +
	// 0.07*blue
	public void converToGrayScale() {
		ImageIcon pomocniczaImageIcon = (ImageIcon) OknoBiometria.obrazekChanged.getIcon();
		Image pomocniczaImage = pomocniczaImageIcon.getImage();
		BufferedImage bufor = (BufferedImage) pomocniczaImage;
		BufferedImage buforCpy = OknoBiometria.copyBFImage(bufor);

		for (int i = 0; i < buforCpy.getWidth(); i++) {
			for (int j = 0; j < buforCpy.getHeight(); j++) {
				int p = buforCpy.getRGB(i, j);
				// Extract Alpha, Red, Green and Blue value from pixel value
				int a = (p >> 24) & 0xff;
				int r = (p >> 16) & 0xff;
				int g = (p >> 8) & 0xff;
				int b = p & 0xff;

				// calculate average
				int avg = (r + g + b) / 3;

				// replace RGB value with avg
				p = (a << 24) | (avg << 16) | (avg << 8) | avg;
				buforCpy.setRGB(i, j, p);
			}
		}

		OknoBiometria.bufferPom = buforCpy;
		OknoBiometria.obrazekChanged.setIcon(new ImageIcon(buforCpy));
	}

	public void changebyPivot(int pivot) {

		Color color;

		ImageIcon pomocniczaImageIcon = (ImageIcon) OknoBiometria.obrazekChanged.getIcon();
		Image pomocniczaImage = pomocniczaImageIcon.getImage();
		BufferedImage bufor = (BufferedImage) pomocniczaImage;
		BufferedImage buforCpy = OknoBiometria.copyBFImage(bufor);

		for (int i = 0; i < buforCpy.getWidth(); i++) {
			for (int j = 0; j < buforCpy.getHeight(); j++) {

				color = new Color(buforCpy.getRGB(i, j));
				if (color.getRed() < pivot) {
					buforCpy.setRGB(i, j, new Color(0, 0, 0).getRGB());
				} else {
					buforCpy.setRGB(i, j, new Color(255, 255, 255).getRGB());
				}

			}
		}

		OknoBiometria.bufferPom = buforCpy;
		OknoBiometria.obrazekChanged.setIcon(new ImageIcon(buforCpy));
	}

	public int otsuMethod() {

		int[] histogram;
		int kolor = 0;
		histogram = new int[256];
		Color pixelColor;

		ImageIcon pomocniczaImageIcon = (ImageIcon) OknoBiometria.obrazekChanged.getIcon();
		Image pomocniczaImage = pomocniczaImageIcon.getImage();
		BufferedImage bufor = (BufferedImage) pomocniczaImage;
		BufferedImage buforCpy = OknoBiometria.copyBFImage(bufor);

		int width = buforCpy.getWidth();
		int height = buforCpy.getHeight();

		for (int i = 0; i < histogram.length; i++) {
			histogram[i] = 0;
		}

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				pixelColor = new Color(buforCpy.getRGB(i, j));
				kolor = pixelColor.getRed();
				histogram[kolor]++;
			}
		}
		// ca³kowita liczba pikseli
		int total = width * height;
		// suma pikseli obiektu pomno¿ona przez liczebnoœc w danym odcieniu (0,255). Przydatna do policzenia jasnoœci t³a
		float sum = 0; 
		for (int i = 0; i < 256; i++)
			sum += i * histogram[i];

		float sumB = 0; 
		int wB = 0;   // ilosc pikseli t³a
		int wF = 0;	  // ilosc pikseli obiektu

		float varMax = 0;
		int threshold = 0;

		for (int i = 0; i < 256; i++) {
			wB += histogram[i];
			if (wB == 0)
				continue;
			wF = total - wB; // dzielenie na dwie warstwy

			if (wF == 0)
				break;

			sumB += (float) (i * histogram[i]);
			float mB = sumB / wB;
			float mF = (sum - sumB) / wF;

			// wyznaczanie wariancji miêdzy klasowej
			float varBetween = (float) wB * (float) wF * (mB - mF) * (mB - mF);

			if (varBetween > varMax) {
				varMax = varBetween;
				threshold = i;
			}
		}

		return threshold;
	}

	public double[][] niblackMethod(int aa, double kk) {

		ImageIcon pomocniczaImageIcon = (ImageIcon) OknoBiometria.obrazekChanged.getIcon();
		Image pomocniczaImage = pomocniczaImageIcon.getImage();
		BufferedImage bufor = (BufferedImage) pomocniczaImage;
		BufferedImage buforCpy = OknoBiometria.copyBFImage(bufor);

		double[][] tab = new double[pomocniczaImageIcon.getIconWidth()][pomocniczaImageIcon.getIconHeight()];
		int a = aa; // rozmiar ramki
		double k = kk; // parametr progowy k (-1.0, 0)
		int sum, color;
		Color pixelColor;
		int l = (a - 1) / 2;
		int x1, x2, y1, y2;
		for (int i = 0; i < buforCpy.getWidth(); i++) {
			for (int j = 0; j < buforCpy.getHeight(); j++) {
				
				x1 = i - l;
				y1 = j - l;
				if (x1<0) x1=0;
				if (y1<0) y1=0;
				
				x2 = i + l + 1;
				y2 = j + l + 1;		
				
				if (x2 > buforCpy.getWidth()) x2 = buforCpy.getWidth();
				if (y2 > buforCpy.getHeight()) y2 = buforCpy.getHeight();	
				
				
				int count = 0;
				sum = 0;
				
				for(int r = x1; r < x2; r++){
					for(int s = y1; s< y2; s++){
						
						pixelColor = new Color(bufor.getRGB(r, s));				
						sum += pixelColor.getRed();
						count++;
					}
				}
				
				double avarage = (double)(sum/count);
				double sumvar=0;
				double stdev=0;
				
				for(int r = x1; r < x2; r++){
					for(int s = y1; s< y2; s++){
						
						pixelColor = new Color(bufor.getRGB(r, s));				
						color = pixelColor.getRed();
						
						sumvar += Math.pow((double)(color - avarage), 2); 
					}
				}
				
				double var = sumvar/count;
				stdev = Math.sqrt(var); 
				tab[i][j] = avarage + (k*stdev);
			}
		}
		return tab;
	}
	
	public static void changebyPivotNiblack(double[][] pivot) {

		Color pom;

		ImageIcon pomocniczaImageIcon = (ImageIcon) OknoBiometria.obrazekChanged.getIcon();
		Image pomocniczaImage = pomocniczaImageIcon.getImage();
		BufferedImage bufor = (BufferedImage) pomocniczaImage;
		BufferedImage buforCpy = OknoBiometria.copyBFImage(bufor);

		for (int i = 0; i < buforCpy.getWidth(); i++) {
			for (int j = 0; j < buforCpy.getHeight(); j++) {

				pom = new Color(buforCpy.getRGB(i, j));

				if (pom.getRed() < pivot[i][j]) {
					buforCpy.setRGB(i, j, new Color(0, 0, 0).getRGB());
				} else
					buforCpy.setRGB(i, j, new Color(255, 255, 255).getRGB());
			}
		}
		OknoBiometria.bufferPom = buforCpy;
		OknoBiometria.obrazekChanged.setIcon(new ImageIcon(buforCpy));
	}
}
