package okienko;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import com.mortennobel.imagescaling.ResampleOp;

public class OknoBiometria extends JFrame {

	int thresholdOtsu = 0;
	Integer counter = 0;
	private boolean isChanged = false;
	private JPanel contentPane;
	private JLabel obrazek;
	public static JLabel obrazekChanged;
	private JButton bwczytaj;
	private JButton breset;
	private JButton bzmien;
	private JButton bzapisz;
	private JButton bHistogram;
	private Double zoom = 1.0; // zoom factor
	// private BufferedImage image = null;
	public static BufferedImage imageChanged = null;
	public static BufferedImage bufferPom = null;

	BufferedImage resizedImage;
	private File plik;
	private JTextField wybranyRText;
	private JTextField wybranyBText;
	private JTextField wybranyGText;
	private JTextField zmienRText;
	private JTextField zmienGText;
	private JTextField zmienBText;
	private int x, y;
	private JLabel aktualneWsp;
	private JLabel labelOtsuthres;
	private JTextField f1, f2, f3, f4, f5, f6, f7, f8, f9;
	// private Robot bob;
	private JLabel lzoom;
	// Array witch choosen pixel
	// private ArrayList<Punkt> listaPunktów = new ArrayList<Punkt>();
	// private ArrayList<Color> listPixelColor = new ArrayList<Color>();
	private HashMap<Integer, Color> mapColors = new HashMap<Integer, Color>();
	private HashMap<Integer, Punkt> mapPoints = new HashMap<Integer, Punkt>();

	public OknoBiometria() {
		initWindow();
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		// Create Label that will be spot to our image
		obrazek = new JLabel();
		obrazek.setVerticalAlignment(SwingConstants.TOP);
		// obrazek.setHorizontalAlignment(SwingConstants.CENTER);
		obrazekChanged = new JLabel();
		obrazekChanged.setVerticalAlignment(SwingConstants.TOP);
		// obrazek.setHorizontalAlignment(SwingConstants.CENTER);

		// Create scrollPane for image that not will be change
		JLabel lbAktualnyObrazek = new JLabel("Obraz pierwotny");
		lbAktualnyObrazek.setBounds(10, 12, 200, 14);
		contentPane.add(lbAktualnyObrazek);
		JScrollPane oImageScrollPane = new JScrollPane();
		oImageScrollPane.setBounds(10, 30, 550, 550);
		oImageScrollPane.setViewportView(obrazek);
		contentPane.add(oImageScrollPane);

		// Create scrollPane for image on which operations will be performed
		JLabel lbZmienionyObrazek = new JLabel("Obraz przekszta³cany");
		lbZmienionyObrazek.setBounds(600, 12, 200, 14);
		contentPane.add(lbZmienionyObrazek);
		aktualneWsp = new JLabel("Aktualane wspó³rzêdne: ");
		aktualneWsp.setBounds(850, 12, 250, 14);
		contentPane.add(aktualneWsp);
		JScrollPane oImageChangedScrollPane = new JScrollPane();
		oImageChangedScrollPane.setBounds(600, 30, 550, 550);
		oImageChangedScrollPane.setViewportView(obrazekChanged);
		oImageChangedScrollPane.addMouseWheelListener(new MouseWheelListener() {

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				// Observe notches variable in below code. If user scrolls mouse
				// wheel up
				// then notches value will be -1 or else it will be 1
				int notches = e.getWheelRotation();
				double temp = zoom - (notches + 0.2);
				temp = Math.max(temp, 1.0);
				if (temp != zoom) {
					zoom = temp;
					if (zoom <= 8.0) {
						resizeImage();
					}
				}
			}
		});
		contentPane.add(oImageChangedScrollPane);
		lzoom = new JLabel("Zoom: ");
		lzoom.setBounds(600, 600, 200, 14);
		contentPane.add(lzoom);

		// Inicialaze "wczytaj" button and creates event methods and listeners
		bwczytaj = new JButton("Wczytaj");
		bwczytaj.setBounds(1170, 30, 100, 24);
		contentPane.add(bwczytaj);
		bwczytaj.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				btnWczytajPlik();
			}
		});
		;

		// button that reset operation made on image
		breset = new JButton("Reset");
		breset.setBounds(1290, 30, 100, 24);
		contentPane.add(breset);
		breset.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				obrazekChanged.setIcon(obrazek.getIcon());
				bufferPom = imageChanged;
				lzoom.setText("Zoom: " + 1.0);
				labelOtsuthres.setText("Próg Otsu: 0");
			}
		});

		// Create Label and TextField for RGB Value
		JLabel lbWybranyPiksel = new JLabel("Wybrany Piksel: ");
		lbWybranyPiksel.setBounds(1170, 70, 200, 14);
		contentPane.add(lbWybranyPiksel);

		// R TextField and Lable for picker pixel
		JLabel lbR = new JLabel("R");
		lbR.setBounds(1170, 94, 40, 16);
		contentPane.add(lbR);
		wybranyRText = new JTextField();
		wybranyRText.setEditable(false);
		wybranyRText.setBounds(1210, 94, 40, 20);
		contentPane.add(wybranyRText);

		// G TextField and Lable for picker pixel
		JLabel lbG = new JLabel("G");
		lbG.setBounds(1170, 124, 40, 16);
		contentPane.add(lbG);
		wybranyGText = new JTextField();
		wybranyGText.setEditable(false);
		wybranyGText.setBounds(1210, 124, 40, 20);
		contentPane.add(wybranyGText);

		// B TextField and Lable for picker pixel
		JLabel lbB = new JLabel("B");
		lbB.setBounds(1170, 154, 40, 16);
		contentPane.add(lbB);
		wybranyBText = new JTextField();
		wybranyBText.setEditable(false);
		wybranyBText.setBounds(1210, 154, 40, 20);
		contentPane.add(wybranyBText);

		/****************************************************/

		JLabel lbZmienPiksel = new JLabel("Zmieñ piksel: ");
		lbZmienPiksel.setBounds(1290, 70, 200, 14);
		contentPane.add(lbZmienPiksel);

		// R TextField and Lable for change pixel
		JLabel lbRCh = new JLabel("R");
		lbRCh.setBounds(1290, 94, 40, 16);
		contentPane.add(lbRCh);
		zmienRText = new JTextField();
		zmienRText.setBounds(1330, 94, 40, 20);
		contentPane.add(zmienRText);

		// G TextField and Lable for change pixel
		JLabel lbGCh = new JLabel("G");
		lbGCh.setBounds(1290, 124, 40, 16);
		contentPane.add(lbGCh);
		zmienGText = new JTextField();
		zmienGText.setBounds(1330, 124, 40, 20);
		contentPane.add(zmienGText);

		// B TextField and Lable for change pixel
		JLabel lbBCh = new JLabel("B");
		lbBCh.setBounds(1290, 154, 40, 16);
		contentPane.add(lbBCh);
		zmienBText = new JTextField();
		zmienBText.setBounds(1330, 154, 40, 20);
		contentPane.add(zmienBText);

		// Button and EventHandler for change RGB value pixel
		bzmien = new JButton("Zmieñ px");
		bzmien.setBounds(1170, 190, 100, 24);
		bzmien.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				changePiksel();
			}
		});
		contentPane.add(bzmien);

		// Button that save file
		bzapisz = new JButton("Zapisz");
		bzapisz.setBounds(1290, 190, 100, 24);
		bzapisz.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				saveFile();
			}
		});
		contentPane.add(bzapisz);

		// JPanel histogram operation
		JPanel histogramPanel = new JPanel();
		histogramPanel.setBorder(
				new TitledBorder(null, "Histogram Operacje", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		histogramPanel.setBounds(1170, 224, 244, 160);
		contentPane.add(histogramPanel);
		histogramPanel.setLayout(null);

		JButton btnRozjasnij = new JButton("Rozjasnij");
		btnRozjasnij.setBounds(10, 20, 100, 24);
		btnRozjasnij.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (imageChanged == null) {
					JOptionPane.showMessageDialog(new JFrame(), "Najpierw za³aduj zdjêcie", "B³¹d",
							JOptionPane.ERROR_MESSAGE);
				} else {
					new Histogram().brighter();
				}

			}
		});
		histogramPanel.add(btnRozjasnij);

		JButton btnPrzyciemnij = new JButton("Przyciemnij");
		btnPrzyciemnij.setBounds(10, 54, 100, 24);
		btnPrzyciemnij.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (imageChanged == null) {
					JOptionPane.showMessageDialog(new JFrame(), "Najpierw za³aduj zdjêcie", "B³¹d",
							JOptionPane.ERROR_MESSAGE);
				} else {
					new Histogram().darker();
				}
			}
		});
		histogramPanel.add(btnPrzyciemnij);

		// display histogram button
		bHistogram = new JButton("Wyœwietl histogram");
		bHistogram.setBounds(10, 122, 220, 24);
		bHistogram.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (imageChanged == null) {
					JOptionPane.showMessageDialog(new JFrame(), "Najpierw za³aduj zdjêcie", "B³¹d",
							JOptionPane.ERROR_MESSAGE);
				} else {
					new Histogram().display();
				}
			}
		});
		histogramPanel.add(bHistogram);

		JLabel lbRozciagnij = new JLabel("Rozci¹gniecie histogramu:");
		lbRozciagnij.setBounds(130, 20, 100, 24);
		histogramPanel.add(lbRozciagnij);

		JLabel lbA = new JLabel("A:");
		lbA.setBounds(130, 54, 20, 20);
		histogramPanel.add(lbA);

		JTextField textA = new JTextField();
		textA.setText("0");
		textA.setBounds(150, 54, 30, 20);
		textA.setColumns(10);
		histogramPanel.add(textA);

		JLabel lbb = new JLabel("B:");
		lbb.setBounds(185, 54, 20, 20);
		histogramPanel.add(lbb);

		JTextField textB = new JTextField();
		textB.setText("255");
		textB.setBounds(200, 54, 30, 20);
		textB.setColumns(10);
		histogramPanel.add(textB);

		JButton btnRozciagnij = new JButton("Rozciagnij");
		btnRozciagnij.setBounds(130, 88, 100, 24);
		btnRozciagnij.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (imageChanged == null) {
					JOptionPane.showMessageDialog(new JFrame(), "Najpierw za³aduj zdjêcie", "B³¹d",
							JOptionPane.ERROR_MESSAGE);
				} else {
					new Histogram().extendHisto2();
					// new
					// Histogram().extendHisto(Integer.valueOf(textA.getText()),
					// Integer.valueOf(textB.getText()));
				}
			}
		});
		histogramPanel.add(btnRozciagnij);

		JButton btnWyrownaj = new JButton("Wyrównaj");
		btnWyrownaj.setBounds(10, 88, 100, 24);
		btnWyrownaj.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (imageChanged == null) {
					JOptionPane.showMessageDialog(new JFrame(), "Najpierw za³aduj zdjêcie", "B³¹d",
							JOptionPane.ERROR_MESSAGE);
				} else {
					new Histogram().setHisto();
				}
			}
		});
		histogramPanel.add(btnWyrownaj);

		// Get Pixel RGB color
		obrazekChanged.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				_mouseClick();

			}
		});

		// JPanel binaryzacja
		JPanel binaryzacjaPanel = new JPanel();
		binaryzacjaPanel
				.setBorder(new TitledBorder(null, "Binaryzacja", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		binaryzacjaPanel.setBounds(1170, 394, 244, 170);
		contentPane.add(binaryzacjaPanel);
		binaryzacjaPanel.setLayout(null);

		JLabel binLabel = new JLabel("Binaryzacja wzglêdna");
		binLabel.setBounds(10, 10, 200, 24);
		binaryzacjaPanel.add(binLabel);

		JTextField tvBin = new JTextField();
		tvBin.setBounds(10, 30, 40, 24);
		tvBin.setText("150");
		binaryzacjaPanel.add(tvBin);

		// binaryzacja wzglêdna
		JButton btnBin = new JButton("Binaryzacja");
		btnBin.setBounds(60, 30, 170, 24);
		binaryzacjaPanel.add(btnBin);
		btnBin.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (imageChanged == null) {
					JOptionPane.showMessageDialog(new JFrame(), "Najpierw za³aduj zdjêcie", "B³¹d",
							JOptionPane.ERROR_MESSAGE);
				} else {
					new Binaryzacja().changebyPivot(Integer.valueOf(tvBin.getText()));
				}
			}
		});

		labelOtsuthres = new JLabel("Próg Otsu: " + Integer.toString(thresholdOtsu));
		labelOtsuthres.setBounds(850, 600, 200, 16);
		contentPane.add(labelOtsuthres);

		// binaryzacja Otsu
		JButton btnBinOtsu = new JButton("Binaryzacja Otsu");
		btnBinOtsu.setBounds(10, 64, 220, 24);
		binaryzacjaPanel.add(btnBinOtsu);
		btnBinOtsu.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (imageChanged == null) {
					JOptionPane.showMessageDialog(new JFrame(), "Najpierw za³aduj zdjêcie", "B³¹d",
							JOptionPane.ERROR_MESSAGE);
				} else {

					thresholdOtsu = new Binaryzacja().otsuMethod();
					labelOtsuthres.setText("Próg Otsu: " + Integer.toString(thresholdOtsu));
					new Binaryzacja().changebyPivot(thresholdOtsu);
				}
			}
		});

		// binaryzacja Niblack
		JButton btnBinNiblack = new JButton("Binaryzacja Niblack");
		btnBinNiblack.setBounds(10, 98, 220, 24);
		binaryzacjaPanel.add(btnBinNiblack);
		btnBinNiblack.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String roz = JOptionPane.showInputDialog("Podaj rozmiar okna o nieparzystym rozmiarze:");
				if (roz != null) {
					int a = Integer.parseInt(roz);
					String ka = JOptionPane.showInputDialog("Podaj parametr progowania z zakresu (-1,0):");
					if (ka != null) {
						double k = Double.parseDouble(ka);
						Binaryzacja.changebyPivotNiblack(new Binaryzacja().niblackMethod(a, k));
					}
				}
			}
		});

		// btn zamiana obrazu kolorowego na obraz w skali szaroœci
		JButton btnToGrey = new JButton("Zmien obraz na cz/b");
		btnToGrey.setBounds(10, 132, 220, 24);
		binaryzacjaPanel.add(btnToGrey);
		btnToGrey.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (imageChanged == null) {
					JOptionPane.showMessageDialog(new JFrame(), "Najpierw za³aduj zdjêcie", "B³¹d",
							JOptionPane.ERROR_MESSAGE);
				} else {
					new Binaryzacja().converToGrayScale();
				}
			}
		});

		// JPanel Filtry
		JPanel filtryPanel = new JPanel();
		filtryPanel.setBorder(new TitledBorder(null, "Filtry", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		filtryPanel.setBounds(1170, 574, 244, 170);
		contentPane.add(filtryPanel);
		filtryPanel.setLayout(null);

		f1 = new JTextField();
		f1.setText("1");
		f1.setBounds(10, 39, 20, 20);
		filtryPanel.add(f1);
		f1.setColumns(10);

		f2 = new JTextField();
		f2.setText("1");
		f2.setBounds(34, 39, 20, 20);
		filtryPanel.add(f2);
		f2.setColumns(10);

		f3 = new JTextField();
		f3.setText("1");
		f3.setBounds(58, 39, 20, 20);
		filtryPanel.add(f3);
		f3.setColumns(10);

		f4 = new JTextField();
		f4.setText("1");
		f4.setBounds(10, 61, 20, 20);
		filtryPanel.add(f4);
		f4.setColumns(10);

		f5 = new JTextField();
		f5.setText("1");
		f5.setBounds(34, 61, 20, 20);
		filtryPanel.add(f5);
		f5.setColumns(10);

		f6 = new JTextField();
		f6.setText("1");
		f6.setBounds(58, 61, 20, 20);
		filtryPanel.add(f6);
		f6.setColumns(10);

		f7 = new JTextField();
		f7.setText("1");
		f7.setBounds(10, 82, 20, 20);
		filtryPanel.add(f7);
		f7.setColumns(10);

		f8 = new JTextField();
		f8.setText("1");
		f8.setBounds(34, 82, 20, 20);
		filtryPanel.add(f8);
		f8.setColumns(10);

		f9 = new JTextField();
		f9.setText("1");
		f9.setBounds(58, 82, 20, 20);
		filtryPanel.add(f9);
		f9.setColumns(10);

		JButton btnFiltry = new JButton("Filtruj");
		btnFiltry.setBounds(10, 103, 68, 23);
		filtryPanel.add(btnFiltry);
		

		JComboBox filtrComboBox = new JComboBox();
		filtrComboBox.setModel(new DefaultComboBoxModel(new String[] { "", "Rozmywajacy", "Prewitt (poziom)",
				"Prewitt (pion)", "Sobel", "Laplace v1", "Laplace v2", "Wykryj narozniki" }));
		filtrComboBox.setBounds(10, 15, 122, 20);
		filtryPanel.add(filtrComboBox);
		
		// btn in line 529
		btnFiltry.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JTextField[] pom = { f1, f2, f3, f4, f5, f6, f7, f8, f9 };
				int[][] mask = new int[3][3];
				int[][] mask2 = new int[3][3];
				int index = 0;
				for (int i = 0; i < 3; i++) {
					for (int j = 0; j < 3; j++) {
						mask[i][j] = Integer.valueOf(pom[index++].getText());
						mask2[i][j] = Integer.valueOf(pom[i+j*3].getText());
					}
				}
				
				String s = (String) filtrComboBox.getSelectedItem();
				if(s.equals("Sobel")){
					new Filtry().sobelFilter(mask, mask2);
				}
				
				if(!s.equals("Sobel")){
					new Filtry().splot(mask);
					System.out.println(s);
				}
				
			}
		});

		JComboBox medianComboBox = new JComboBox();
		medianComboBox.setModel(new DefaultComboBoxModel(new String[] { "3x3", "5x5", "11x11" }));
		medianComboBox.setBounds(132, 39, 88, 20);
		filtryPanel.add(medianComboBox);

		JLabel lblMaska = new JLabel("Maska:");
		lblMaska.setBounds(88, 42, 34, 14);
		filtryPanel.add(lblMaska);

		JButton btnFiltruj = new JButton("Filtr Medianowy");
		btnFiltruj.setBounds(88, 61, 132, 31);
		filtryPanel.add(btnFiltruj);
		btnFiltruj.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String s = (String) medianComboBox.getSelectedItem();
				
				int mask = 0;
				if(s.equals("3x3")){
					mask = 3;
				}	
				else if(s.equals("5x5")){
					mask = 5;
				}
				else{
					mask = 11;
				}
				new Filtry().medianaFilter(mask);
			}
		});

		JLabel lblFiltrMedianowy = new JLabel("Filtr medianowy:");
		lblFiltrMedianowy.setBounds(142, 21, 89, 14);
		filtryPanel.add(lblFiltrMedianowy);

		JButton KuwaharaBtn = new JButton("Filtr kuwahara");
		KuwaharaBtn.setBounds(88, 93, 133, 31);
		filtryPanel.add(KuwaharaBtn);
		KuwaharaBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				new Filtry().kuwaharaFilter();
			}
		});

		// Set textfield for for kind of filters

		filtrComboBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String s = (String) filtrComboBox.getSelectedItem();

				if (s.equals("Rozmywajacy")) {
					f1.setText("1");
					f2.setText("1");
					f3.setText("1");
					f4.setText("1");
					f5.setText("4");
					f6.setText("1");
					f7.setText("1");
					f8.setText("1");
					f9.setText("1");
				}
				if (s.equals("Prewitt (poziom)")) {
					f1.setText("-1");
					f2.setText("-1");
					f3.setText("-1");
					f4.setText("0");
					f5.setText("0");
					f6.setText("0");
					f7.setText("1");
					f8.setText("1");
					f9.setText("1");
				}
				if (s.equals("Prewitt (pion)")) {
					f1.setText("-1");
					f2.setText("0");
					f3.setText("1");
					f4.setText("-1");
					f5.setText("0");
					f6.setText("1");
					f7.setText("-1");
					f8.setText("0");
					f9.setText("1");
				}
				if (s.equals("Sobel")) {
					f1.setText("-1");
					f2.setText("-2");
					f3.setText("-1");
					f4.setText("0");
					f5.setText("0");
					f6.setText("0");
					f7.setText("1");
					f8.setText("2");
					f9.setText("1");
				}
				
				if (s.equals("Laplace v1")) {
					f1.setText("0");
					f2.setText("1");
					f3.setText("0");
					f4.setText("1");
					f5.setText("-4");
					f6.setText("1");
					f7.setText("0");
					f8.setText("1");
					f9.setText("0");
				}
				if (s.equals("Laplace v2")) {
					f1.setText("1");
					f2.setText("1");
					f3.setText("1");
					f4.setText("1");
					f5.setText("-8");
					f6.setText("1");
					f7.setText("1");
					f8.setText("1");
					f9.setText("1");
				}
				if (s.equals("Wykryj narozniki")) {
					f1.setText("1");
					f2.setText("1");
					f3.setText("1");
					f4.setText("1");
					f5.setText("-2");
					f6.setText("-1");
					f7.setText("1");
					f8.setText("-1");
					f9.setText("-1");
				}
			}

		});

	}

	/*
	 * function define basic method that are necessary to create them.
	 */
	public void initWindow() {
		setResizable(false);
		setLocationRelativeTo(null); // center location of window
		setTitle("Biometria->Mariusz_Rakiec");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(1440, 800);
	}

	static BufferedImage copyBFImage(BufferedImage source) {
		return new BufferedImage(source.getColorModel(), source.copyData(null), source.isAlphaPremultiplied(), null);
	}

	private void btnWczytajPlik() {
		BufferedImage image;
		JFileChooser fc = new JFileChooser();
		int result = fc.showOpenDialog(null);
		if (result == JFileChooser.APPROVE_OPTION) {
			plik = fc.getSelectedFile();
			try {
				image = ImageIO.read(plik);
				// Set image as icon
				Icon imageIcon = new ImageIcon(image);
				obrazek.setIcon(imageIcon);

				imageChanged = copyBFImage(image);
				bufferPom = copyBFImage(imageChanged);

				obrazekChanged.setIcon(new ImageIcon(imageChanged));

				obrazek.setVerticalAlignment(SwingConstants.TOP);
				// obrazek.setHorizontalAlignment(SwingConstants.CENTER);
				obrazekChanged.setVerticalAlignment(SwingConstants.TOP);
				// obrazekChanged.setHorizontalAlignment(SwingConstants.CENTER);

				mapColors.clear();
				mapPoints.clear();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void _mouseClick() {
		if (obrazekChanged.getIcon() != null) {
			Color pixelColor = new Color(0, 0, 0);

			Point punkt = MouseInfo.getPointerInfo().getLocation();
			SwingUtilities.convertPointFromScreen(punkt, obrazekChanged);
			x = (int) punkt.getX();
			y = (int) punkt.getY();

			ImageIcon pomocniczaImageIcon = (ImageIcon) obrazekChanged.getIcon();
			Image pomocniczaImage = pomocniczaImageIcon.getImage();
			BufferedImage bufor = (BufferedImage) pomocniczaImage;
			BufferedImage buforCpy = copyBFImage(bufor);

			if (x <= obrazekChanged.getIcon().getIconWidth() && y <= obrazekChanged.getIcon().getIconHeight()) {
				aktualneWsp.setText("Aktualne Wspó³rzêdne: " + "X:" + x + ", Y:" + y);

				// pixelColor = bob.getPixelColor(x, y);

				pixelColor = new Color(buforCpy.getRGB(x, y));
				// listPixelColor.add(pixelColor);
				// listaPunktów.add(new Punkt(x,y));
				mapPoints.put(counter, new Punkt(x, y));
				mapColors.put(counter, pixelColor);
				counter++;

				wybranyRText.setText(String.valueOf(pixelColor.getRed()));
				wybranyGText.setText(String.valueOf(pixelColor.getGreen()));
				wybranyBText.setText(String.valueOf(pixelColor.getBlue()));
			}
		}
	}

	private void resizeImage() {
		lzoom.setText("Zoom: " + zoom);
		ResampleOp resampleOp = new ResampleOp((int) (bufferPom.getWidth() * zoom),
				(int) (bufferPom.getHeight() * zoom));
		resizedImage = resampleOp.filter(bufferPom, null);
		Icon imageIcon = new ImageIcon(resizedImage);
		obrazekChanged.setIcon(imageIcon);
	}

	private void changePiksel() {
		Color kolor2 = Color.BLACK;
		isChanged = true;
		if (obrazekChanged.getIcon() != null) {
			if (x > obrazekChanged.getWidth() || y > obrazekChanged.getHeight()) {
			} else {

				if (Integer.parseInt(zmienRText.getText()) > 255 || Integer.parseInt(zmienBText.getText()) > 255
						|| Integer.parseInt(zmienGText.getText()) > 255 || zmienGText.getText().equals("")
						|| zmienBText.getText().equals("") || zmienRText.getText().equals("")) {

					if (Integer.parseInt(zmienRText.getText()) > 255 || zmienRText.getText().equals("")) {
						kolor2 = new Color(255, 0, 0);
						zmienRText.setText(Integer.toString(255));
						zmienGText.setText(Integer.toString(0));
						zmienBText.setText(Integer.toString(0));
					}
					if (Integer.parseInt(zmienGText.getText()) > 255 || zmienGText.getText().equals("")) {
						kolor2 = new Color(0, 255, 0);
						zmienRText.setText(Integer.toString(0));
						zmienGText.setText(Integer.toString(255));
						zmienBText.setText(Integer.toString(0));
					}
					if (Integer.parseInt(zmienBText.getText()) > 255 || zmienBText.getText().equals("")) {
						kolor2 = new Color(0, 0, 255);
						zmienRText.setText(Integer.toString(0));
						zmienGText.setText(Integer.toString(0));
						zmienBText.setText(Integer.toString(255));
					}

				} else {
					kolor2 = new Color(Integer.valueOf(zmienRText.getText()), Integer.valueOf(zmienGText.getText()),
							Integer.valueOf(zmienBText.getText()));
				}

				for (int i = 0; i < zoom; i++) {
					for (int j = 0; j < zoom; j++) {
						resizedImage.setRGB(x + j, y + i, kolor2.getRGB());
					}
				}

				bufferPom = resizedImage; // aktualizacja buuffera dla metody
											// resizeImage
				obrazekChanged.setIcon(new ImageIcon(resizedImage));

			}
		}
	}

	/*
	 * private void changePiksel2() { if (obrazekChanged.getIcon() != null) { if
	 * (x > obrazekChanged.getWidth() || y > obrazekChanged.getHeight()) { }
	 * else {
	 * 
	 * for(int i=0; i< zoom; i++){ for(int j=0; j < zoom; j++){
	 * resizedImage.setRGB(x + j, y + i, kolor2.getRGB()); } }
	 * 
	 * bufferPom = resizedImage; // aktualizacja buuffera dla metody resizeImage
	 * obrazekChanged.setIcon(new ImageIcon(resizedImage));
	 * 
	 * } } }
	 */

	private void saveFile() {
		JFileChooser Zapiszobrazek = new JFileChooser();

		int sf = Zapiszobrazek.showSaveDialog(null);

		if (sf == JFileChooser.APPROVE_OPTION) {
			ImageIcon pomocniczaImageIcon = (ImageIcon) obrazekChanged.getIcon();
			Image pomocniczaImage = pomocniczaImageIcon.getImage();
			BufferedImage do_zapisu_Pom = (BufferedImage) pomocniczaImage;

			try {
				File outputfile = new File(Zapiszobrazek.getSelectedFile().getAbsolutePath());
				ImageIO.write(do_zapisu_Pom, "png", outputfile);

			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	public static void main(String... args) throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, UnsupportedLookAndFeelException {
		// define style respond our system
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				try {
					OknoBiometria okno = new OknoBiometria();
					okno.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		;
	}
}
