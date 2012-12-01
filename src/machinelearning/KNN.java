package machinelearning;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class KNN {

	/**
	 * Return the most similar image found in a given folder
	 * @throws IOException 
	 * */
	public static String findMostSimilar(String string, BufferedImage s) throws IOException {
		File folder=new File(string);
		String best="";
		double max=Double.NEGATIVE_INFINITY;
		for(File f:folder.listFiles()){
			double tv=KNN.similarity(s,ImageIO.read(f));
			if(tv>max){
				max=tv;
				best=f.getName();
			}
		}
		return best;
	}

	private static double similarity(BufferedImage s, BufferedImage r) {
		double diff=0.0;
		for(int x=0;x<s.getWidth();x++)
			for(int y=0;y<s.getHeight();y++)
				diff+=compareRGB(s.getRGB(x, y),r.getRGB(x, y));
		return diff;
	}
	/**
	 * Calculate the difference between two RGB colors
	 * @return a value between 0 and 1
	 * 
	 * */
	private static double compareRGB(int a,int b){
		if(a==b) return 1.0;
		return (Math.abs((a & 0xFF) - (b & 0xFF))
        + Math.abs(((a & 0xFF00) >> 8) - ((b & 0xFF00) >> 8))
        + Math.abs(((a & 0xFF0000) >> 16) - ((b & 0xFF0000) >> 16)))/(256*3);	
	}

}
