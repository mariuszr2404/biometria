package okienko;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import javax.swing.ImageIcon;

public class Filtry {

	BufferedImage image = OknoBiometria.bufferPom;

	void splot(int[][] mask) {

		ImageIcon pomocniczaImageIcon = (ImageIcon) OknoBiometria.obrazekChanged.getIcon();
		Image pomocniczaImage = pomocniczaImageIcon.getImage();
		BufferedImage bufor = (BufferedImage) pomocniczaImage;
		BufferedImage buforCpy = OknoBiometria.copyBFImage(bufor);

		Color pom;
		int x1, x2, y1, y2;
		int index1 = 0, index2 = 0;
		int red, green, blue;
		int sumRed = 0, sumGreen = 0, sumBlue = 0;
		int sumMask = 0;

		// W zale¿noœci o d³ugoœci ustaw ile pikseli na lewo i na prawo od
		// centralnego
		int offset = mask.length / 2;

		for (int z = 0; z < 3; z++) {
			for (int y = 0; y < 3; y++) {
				sumMask += mask[z][y];
			}
		}

		if (sumMask == 0)
			sumMask = 1;

		for (int i = 1; i < bufor.getWidth() - 1; i++) {

			for (int j = 1; j < bufor.getHeight() - 1; j++) {

				for (int k = i - offset; k <= i + offset; k++) {
					for (int l = j - offset; l <= j + offset; l++) {
						pom = new Color(bufor.getRGB(k, l));
						red = pom.getRed();
						blue = pom.getBlue();
						green = pom.getGreen();

						// f`(x,y) = sumaf(x+i,y+i)*W gdzie W to wartosc piksela
						sumRed += red * mask[index1][index2];
						sumBlue += blue * mask[index1][index2];
						sumGreen += green * mask[index1][index2];

						index2++;

					}
					index1++;
					index2 = 0;
				}

				sumRed = sumRed / sumMask;
				if (sumRed < 0)
					sumRed = 0;
				else if (sumRed > 255)
					sumRed = 255;

				sumBlue = sumBlue / sumMask;
				if (sumBlue < 0)
					sumBlue = 0;
				else if (sumBlue > 255)
					sumBlue = 255;

				sumGreen = sumGreen / sumMask;
				if (sumGreen < 0)
					sumGreen = 0;
				else if (sumGreen > 255)
					sumGreen = 255;

				buforCpy.setRGB(i, j, new Color(sumRed, sumGreen, sumBlue).getRGB());
				index1 = 0;
				index2 = 0;
				sumRed = 0;
				sumBlue = 0;
				sumGreen = 0;
			}
		}
		OknoBiometria.bufferPom = buforCpy;
		OknoBiometria.obrazekChanged.setIcon(new ImageIcon(buforCpy));
	}

	void medianaFilter(int maskSize) {

		ImageIcon pomocniczaImageIcon = (ImageIcon) OknoBiometria.obrazekChanged.getIcon();
		Image pomocniczaImage = pomocniczaImageIcon.getImage();
		BufferedImage bufor = (BufferedImage) pomocniczaImage;
		BufferedImage buforCpy = OknoBiometria.copyBFImage(bufor);

		// image dimension
		int width = buforCpy.getWidth();
		int height = buforCpy.getHeight();
		int outputPixel[] = new int[width * height];

		int pixels[] = new int[width * height];

		// get value all pixels
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				pixels[j + i * width] = buforCpy.getRGB(i, j);
			}
		}

		// red, green and blue are a 2D square of odd size like 3X3 or 5X5
		int red[] = null, green[] = null, blue[] = null;

		int count = 0;

		// Mediana filter operation
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int a = (x + (y * width)) >> 24 & 0xFF;

				red = new int[maskSize * maskSize];
				blue = new int[maskSize * maskSize];
				green = new int[maskSize * maskSize];

				count = 0;

				for (int r = y - (maskSize / 2); r <= y + (maskSize / 2); r++) {
					for (int c = x - (maskSize / 2); c <= x + (maskSize / 2); c++) {
						if (r < 0 || r >= height || c < 0 || c >= width) {
							// pewna czêsc maski znajduje sie poza obrazem
							continue;
						} else {
							// pom2 = new Color(buforCpy.getRGB(r, c));
							red[count] = pixels[c + (r * width)] >> 16 & 0xFF;
							green[count] = pixels[c + (r * width)] >> 8 & 0xFF;
							blue[count] = pixels[c + (r * width)] & 0xFF;
							count++;
						}
					}
				}

				Arrays.sort(red);
				Arrays.sort(green);
				Arrays.sort(blue);

				int index = (count % 2 == 0) ? count / 2 - 1 : count / 2;
				int p = getPixel(a, red[index], green[index], blue[index]);
				outputPixel[x + y * width] = p;

			}
		}

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				buforCpy.setRGB(i, j, outputPixel[j + i * width]);
			}
		}

		OknoBiometria.bufferPom = buforCpy;
		OknoBiometria.obrazekChanged.setIcon(new ImageIcon(buforCpy));
	}

	public static int getPixel(int alpha, int red, int green, int blue) {
		return (alpha << 24) | (red << 16) | (green << 8) | blue;
	}

	void kuwaharaFilter() {

		ImageIcon pomocniczaImageIcon = (ImageIcon) OknoBiometria.obrazekChanged.getIcon();
		Image pomocniczaImage = pomocniczaImageIcon.getImage();
		BufferedImage bufor = (BufferedImage) pomocniczaImage;
		BufferedImage buforCpy = OknoBiometria.copyBFImage(bufor);

		Color pom;

		int okno = 2;
		int minR = 0, minG = 0, minB = 0;

		double[] rs = new double[4];
		double[] gs = new double[4];
		double[] bs = new double[4];

		double[] rw = new double[4];
		double[] gw = new double[4];
		double[] bw = new double[4];

		for (int i = 2; i < bufor.getWidth() - 2; i++) {
			for (int j = 2; j < bufor.getHeight() - 2; j++) {

				for (int res = 0; res < 4; res++) {
					rs[res] = 0;
					bs[res] = 0;
					gs[res] = 0;
				}

				// dzieelenie przez k * l w celu wyliczenia wartoœci œrednie
				for (int k = 0; k < 3; k++) {
					for (int l = 0; l < 3; l++) {
						pom = new Color(bufor.getRGB(i + k - okno, j + l - okno));
						rs[0] += (double) pom.getRed() / 9.0;
						gs[0] += (double) pom.getGreen() / 9.0;
						bs[0] += (double) pom.getBlue() / 9.0;

						pom = new Color(bufor.getRGB(i + k, j + l - okno));
						rs[1] += (double) pom.getRed() / 9.0;
						gs[1] += (double) pom.getGreen() / 9.0;
						bs[1] += (double) pom.getBlue() / 9.0;

						pom = new Color(bufor.getRGB(i + k - okno, j + l));
						rs[2] += (double) pom.getRed() / 9.0;
						gs[2] += (double) pom.getGreen() / 9.0;
						bs[2] += (double) pom.getBlue() / 9.0;

						pom = new Color(bufor.getRGB(i + k, j + l));
						rs[3] += (double) pom.getRed() / 9.0;
						gs[3] += (double) pom.getGreen() / 9.0;
						bs[3] += (double) pom.getBlue() / 9.0;
					}
				}

				for (int res2 = 0; res2 < 4; res2++) {
					rw[res2] = 0;
					gw[res2] = 0;
					bw[res2] = 0;
				}

				for (int k = 0; k < 3; k++) {
					for (int l = 0; l < 3; l++) {
						pom = new Color(bufor.getRGB(i + k - okno, j + l - okno));
						rw[0] += Math.pow((double) pom.getRed() - rs[0], 2.0);
						gw[0] += Math.pow((double) pom.getGreen() - gs[0], 2.0);
						bw[0] += Math.pow((double) pom.getBlue() - bs[0], 2.0);

						pom = new Color(bufor.getRGB(i + k, j + l - okno));
						rw[1] += Math.pow((double) pom.getRed() - rs[1], 2.0);
						gw[1] += Math.pow((double) pom.getGreen() - gs[1], 2.0);
						bw[1] += Math.pow((double) pom.getBlue() - bs[1], 2.0);

						pom = new Color(bufor.getRGB(i + k - okno, j + l));
						rw[2] += Math.pow((double) pom.getRed() - rs[2], 2.0);
						gw[2] += Math.pow((double) pom.getGreen() - gs[2], 2.0);
						bw[2] += Math.pow((double) pom.getBlue() - bs[2], 2.0);

						pom = new Color(bufor.getRGB(i + k, j + l));
						rw[3] += Math.pow((double) pom.getRed() - rs[3], 2.0);
						gw[3] += Math.pow((double) pom.getGreen() - gs[3], 2.0);
						bw[3] += Math.pow((double) pom.getBlue() - bs[3], 2.0);
					}
				}

				minR = 0;
				for (int k = 1; k < 4; k++)
					if (rw[k] < rw[minR])
						minR = k;

				minG = 0;
				for (int k = 1; k < 4; k++)
					if (gw[k] < gw[minG])
						minG = k;

				minB = 0;
				for (int k = 1; k < 4; k++)
					if (bw[k] < bw[minB])
						minB = k;

				buforCpy.setRGB(i, j, new Color((int) rs[minR], (int) gs[minG], (int) bs[minB]).getRGB());
			}
		}

		OknoBiometria.bufferPom = buforCpy;
		OknoBiometria.obrazekChanged.setIcon(new ImageIcon(buforCpy));
	}

	public void sobelFilter(int [][]mask, int [][]mask2) {

		ImageIcon pomocniczaImageIcon = (ImageIcon) OknoBiometria.obrazekChanged.getIcon();
		Image pomocniczaImage = pomocniczaImageIcon.getImage();
		BufferedImage bufor = (BufferedImage) pomocniczaImage;
		BufferedImage buforCpy = OknoBiometria.copyBFImage(bufor);

		for(int i = 0 ; i < mask.length ; i++){
			for(int j = 0; j < mask.length; j++) {
				System.out.println(mask[i][j] + " -> " + mask2[i][j]);
			}
		}

		int[][] sx = mask;
		int[][] sy = mask2;

		// a sobel template 2D array for calculation
		int[][] sob;

		// get image width and height
		int width = buforCpy.getWidth();
		int height = buforCpy.getHeight();

		// at first need to greyscale and populate sob[][] array
		sob = new int[width][height];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int pixel = buforCpy.getRGB(x, y);
				int a = (pixel >> 24) & 0xff;
				int r = (pixel >> 16) & 0xff;
				int g = (pixel >> 8) & 0xff;
				int b = pixel & 0xff;

				// calculate average
				int avg = (r + g + b) / 3;

				sob[x][y] = avg;
				// replace RGB value with average
				pixel = (avg << 24) | (avg << 16) | (avg << 8) | avg;
				buforCpy.setRGB(x, y, pixel);
			}
		}

		// sobel calculation
		for (int y = 1; y < height - 1; y++) {
			for (int x = 1; x < width - 1; x++) {
				int px = (sx[0][0] * sob[x - 1][y - 1]) + (sx[0][1] * sob[x][y - 1]) + (sx[0][2] * sob[x + 1][y - 1])
						+ (sx[1][0] * sob[x - 1][y]) + (sx[1][1] * sob[x][y]) + (sx[1][2] * sob[x + 1][y])
						+ (sx[2][0] * sob[x - 1][y + 1]) + (sx[2][1] * sob[x][y + 1]) + (sx[2][2] * sob[x + 1][y + 1]);

				int py = (sy[0][0] * sob[x - 1][y - 1]) + (sy[0][1] * sob[x][y - 1]) + (sy[0][2] * sob[x + 1][y - 1])
						+ (sy[1][0] * sob[x - 1][y]) + (sy[1][1] * sob[x][y]) + (sy[1][2] * sob[x + 1][y])
						+ (sy[2][0] * sob[x - 1][y + 1]) + (sy[2][1] * sob[x][y + 1]) + (sy[2][2] * sob[x + 1][y + 1]);

				int pixel = (int) Math.sqrt((px * px) + (py * py));

				if (pixel > 255) {
					pixel = 255;
				} else if (pixel < 0) {
					pixel = 0;
				}

				Color pix = new Color(pixel, pixel, pixel);
				buforCpy.setRGB(x, y, pix.getRGB());
			}
		}
		
		OknoBiometria.bufferPom = buforCpy;
		OknoBiometria.obrazekChanged.setIcon(new ImageIcon(buforCpy));
	}
}
