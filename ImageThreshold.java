// Skeletal program for the "Image Threshold" assignment
// Written by:  Minglun Gong
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.io.*;
import javax.imageio.*;

// Main class
public class ImageThreshold extends Frame implements ActionListener {

    BufferedImage input;
    int width, height;
    TextField texThres, texOffset;
    ImageCanvas source, target;
    PlotCanvas2 plot;
    final int GREY_LEVEL = 256;
    // Constructor

    public ImageThreshold(String name) {
        super("Image Histogram");
        // load image
        try {
            input = ImageIO.read(new File(name));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        width = input.getWidth();
        height = input.getHeight();
        // prepare the panel for image canvas.
        Panel main = new Panel();
        source = new ImageCanvas(input);
        plot = new PlotCanvas2(256, 200);
        target = new ImageCanvas(width, height);
        //target.copyImage(input);
        target.resetImage(input);
        main.setLayout(new GridLayout(1, 3, 10, 10));
        main.add(source);
        main.add(plot);
        main.add(target);
        // prepare the panel for buttons.
        Panel controls = new Panel();
        controls.add(new Label("Threshold:"));
        texThres = new TextField("128", 2);
        controls.add(texThres);
        Button button = new Button("Manual Selection");
        button.addActionListener(this);
        controls.add(button);
        button = new Button("Automatic Selection");
        button.addActionListener(this);
        controls.add(button);
        button = new Button("Otsu's Method");
        button.addActionListener(this);
        controls.add(button);
        controls.add(new Label("Offset:"));
        texOffset = new TextField("10", 2);
        controls.add(texOffset);
        button = new Button("Adaptive Mean-C");
        button.addActionListener(this);
        controls.add(button);
        // add two panels
        add("Center", main);
        add("South", controls);
        addWindowListener(new ExitListener());
        setSize(width * 2 + 400, height + 100);
        setVisible(true);
    }

    class ExitListener extends WindowAdapter {

        public void windowClosing(WindowEvent e) {
            System.exit(0);
        }
    }
    // Action listener for button click events

    public void actionPerformed(ActionEvent e) {
        // example -- compute the average color for the image
        if (((Button) e.getSource()).getLabel().equals("Adaptive Mean-C")) {
            //selects an individual threshold for each pixel based on the range of intensity values in its local neighborhood
            int c = 7;
            int kSize = c * c;
            int w = 3;
            for (int q = w; q < height - w; q++) {
                for (int p = w; p < width - w; p++) {
                    int rSum = 0;
                    int gSum = 0;
                    int bSum = 0;
                    for (int v = -w; v <= w; v++) {
                        for (int u = -v; u <= w; u++) {
                            // [p + u] = x, [q + v] = y
                            Color clr = new Color(source.image.getRGB(p + u, q + v));
                            int red = clr.getRed();
                            int green = clr.getGreen();
                            int blue = clr.getBlue();
                            rSum += red;
                            gSum += green;
                            bSum += blue;
                        }
                    }
                    Color clr = new Color(source.image.getRGB(p, q));
                    int red = clr.getRed();
                    int green = clr.getGreen();
                    int blue = clr.getBlue();
                    int rT = (rSum / kSize) - c;
                    int gT = (gSum / kSize) - c;
                    int bT = (bSum / kSize) - c;
                    red = (red < rT) ? 0 : GREY_LEVEL - 1;
                    green = (green < gT) ? 0 : GREY_LEVEL - 1;
                    blue = (blue < bT) ? 0 : GREY_LEVEL - 1;
                    target.image.setRGB(p, q, red << 16 | green << 8 | blue);                    
                }
            }
            target.repaint();
        }

        if ( ((Button)e.getSource()).getLabel().equals("Otsu's Method") ) {
            int ptr = 0;
            int red=0, green=0, blue=0, black =0;
                    
            int[] rH = new int[256];
            int[] gH = new int[256];
            int[] bH = new int[256];
            int[] histData = new int[256];
            for ( int y=0, i=0 ; y<height ; y++ ) {
                for ( int x=0 ; x<width ; x++, i++ ) {
                    Color clr = new Color(source.image.getRGB(x, y));
                    red = clr.getRed();
                    green = clr.getGreen();
                    blue = clr.getBlue();
                    rH[red]++;
                    gH[green]++;
                    bH[blue]++;
                }
            }

            // Total number of pixels
            int total = width * height;

            float sum = 0;
            float sumR=0;
            float sumG=0;
            float sumB=0;
            for (int t=0 ; t<256 ; t++) {
                sum += t * histData[t];
                sumR += t * rH[t];
                sumG += t * gH[t];
                 sumB += t * bH[t];
            }

            float sum2 = 0;
            float sum2R = 0;
            float sum2G = 0;
            float sum2B = 0;
            int wBack = 0;
            int wBackR = 0;
            int wBackG = 0;
            int wBackB = 0;
            int wF = 0;
            int wFR = 0;
            int wFG = 0;
            int wFB = 0;

            float varMax = 0;
            float varMaxR = 0;
            float varMaxG = 0;
            float varMaxB = 0;
            float threshold = 0;
            float thresholdR = 0;
            float thresholdG = 0;
            float thresholdB = 0;
                    

            for (int t=0 ; t<256 ; t++) {
                wBack += histData[t];               // Weight Background
                wBackR += rH[t]; 
                wBackG += gH[t]; 
                wBackB += bH[t]; 
                if (wBack == 0) continue;

                wF = total - wBack;                 // Weight Foreground
                wFR = total - wBackR;
                wFG = total - wBackG;
                wFB = total - wBackB;
                if (wF == 0) break;

                sum2 += (float) (t * histData[t]);
                sum2R += (float) (t * rH[t]);
                sum2G += (float) (t * gH[t]);
                sum2B += (float) (t * bH[t]);
                
                // Mean Background
                float mBack = sum2 / wBack;           
                float mBackR = sum2R / wBackR; 
                float mBackG = sum2G / wBackG; 
                float mBackB = sum2B / wBackB; 
                
                // Mean Foreground
                float mF = (sum - sum2) / wF;    
                float mFR = (sumR - sum2R) / wFR;
                float mFG = (sumG - sum2G) / wFG;
                float mFB = (sumB - sum2B) / wFB;         

                // Calculate Between Class Variance
                float varBetween = (float)wBack * (float)wF * (mBack - mF) * (mBack - mF);
                float varBetweenR = (float)wBackR * (float)wFR * (mBackR - mFR) * (mBackR - mFR);
                float varBetweenG = (float)wBackG * (float)wFG * (mBackG - mFG) * (mBackG - mFG);
                float varBetweenB = (float)wBackB * (float)wFB * (mBackB - mFB) * (mBackB - mFB);

                // Check if new maximum found
                if (varBetween > varMax) {
                varMax = varBetween;
                threshold = t;
                }
                if (varBetweenR > varMaxR) {
                    varMaxR = varBetweenR;
                    threshold = t;
                }
                if (varBetweenG > varMaxG) {
                    varMaxG = varBetweenG;
                    thresholdG = t;
                }
                if (varBetweenB > varMaxB) {
                    varMaxB = varBetweenB;
                    thresholdB = t;
                }
            }                 
            
            for ( int y=0, i=0 ; y<height ; y++ ) {
                for ( int x=0 ; x<width ; x++, i++ ) {
                    Color clr = new Color(source.image.getRGB(x, y));
                    red = clr.getRed();
                    green = clr.getGreen();
                    blue = clr.getBlue();
                    red = (red < thresholdR) ? 0 : GREY_LEVEL - 1;
                    green = (green < thresholdG) ? 0 : GREY_LEVEL - 1;
                    blue = (blue < thresholdB) ? 0 : GREY_LEVEL - 1;
                    target.image.setRGB(x, y, red << 16 | green << 8 | blue);
                }
            }                 
         target.repaint();   
        }
        
        if (((Button) e.getSource()).getLabel().equals("Automatic Selection")) {

            Vector<Integer> groupR1 = new Vector<Integer>();
            Vector<Integer> groupR2 = new Vector<Integer>();
            Vector<Integer> groupG1 = new Vector<Integer>();
            Vector<Integer> groupG2 = new Vector<Integer>();
            Vector<Integer> groupB1 = new Vector<Integer>();
            Vector<Integer> groupB2 = new Vector<Integer>();
            int tR = 128;
            int tG = 128;
            int tB = 128;
            int tR2 = 9999;
            int tG2 = 9999;
            int tB2 = 9999;
            int rMean1;
            int gMean1;
            int bMean1;
            int rMean2;
            int gMean2;
            int bMean2;

            // Automatic threshold selection independently for each channel:
            while (Math.abs(tR2 - tR) > 5 && Math.abs(tG2 - tG) > 5 && Math.abs(tB2 - tB) > 5) {
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        Color clr = new Color(source.image.getRGB(x, y));
                        int red = clr.getRed();
                        int green = clr.getGreen();
                        int blue = clr.getBlue();
                        if (red < tR) {
                            groupR1.add(red);
                        } else {
                            groupR2.add(red);
                        }
                        if (green < tG) {
                            groupG1.add(red);
                        } else {
                            groupG2.add(red);
                        }
                        if (blue < tB) {
                            groupB1.add(red);
                        } else {
                            groupB2.add(red);
                        }
                    }
                }
                int sum = 0;
                for (int i : groupR1) {
                    sum += groupR1.get(i);
                }
                rMean1 = sum / groupR1.size();
                sum = 0;
                for (int i : groupR2) {
                    sum += groupR2.get(i);
                }
                rMean2 = sum / groupR2.size();
                sum = 0;
                for (int i : groupG1) {
                    sum += groupG1.get(i);
                }
                gMean1 = sum / groupG1.size();
                sum = 0;
                for (int i : groupG2) {
                    sum += groupG2.get(i);
                }
                gMean2 = sum / groupG2.size();
                sum = 0;
                for (int i : groupB1) {
                    sum += groupB1.get(i);
                }
                bMean1 = sum / groupB1.size();
                sum = 0;
                for (int i : groupB2) {
                    sum += groupB2.get(i);
                }
                bMean2 = sum / groupB2.size();
                tR2 = tR;
                tG2 = tG;
                tB2 = tB;
                tR = (rMean1 + rMean2) / 2;
                tG = (gMean1 + gMean2) / 2;
                tB = (bMean1 + bMean2) / 2;
            }

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    Color clr = new Color(source.image.getRGB(x, y));
                    int red = clr.getRed();
                    int green = clr.getGreen();
                    int blue = clr.getBlue();
                    red = (red < tR) ? 0 : GREY_LEVEL - 1;
                    green = (green < tG) ? 0 : GREY_LEVEL - 1;
                    blue = (blue < tB) ? 0 : GREY_LEVEL - 1;
                    target.image.setRGB(x, y, red << 16 | green << 8 | blue);
                }
            }
            target.repaint();
            plot.clearObjects();
            //plot.addObject(new VerticalBar(Color.BLACK, t, 100));
        }

        if (((Button) e.getSource()).getLabel().equals("Manual Selection")) {
            int threshold;
            try {
                threshold = Integer.parseInt(texThres.getText());
            } catch (Exception ex) {
                texThres.setText("128");
                threshold = 128;
            }
            plot.clearObjects();
            plot.addObject(new VerticalBar(Color.BLACK, threshold, 100));

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    Color clr = new Color(source.image.getRGB(x, y));
                    int red = clr.getRed();
                    int green = clr.getGreen();
                    int blue = clr.getBlue();
                    red = (red < threshold) ? 0 : GREY_LEVEL - 1;
                    green = (green < threshold) ? 0 : GREY_LEVEL - 1;
                    blue = (blue < threshold) ? 0 : GREY_LEVEL - 1;
                    target.image.setRGB(x, y, red << 16 | green << 8 | blue);
                }
            }
            target.repaint();
        }
    }

    public static void main(String[] args) {
        new ImageThreshold(args.length == 1 ? args[0] : "fingerprint.png");
    }
}
