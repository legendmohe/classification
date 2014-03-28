package util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;

import model.DocumentVector;
import model.FeatureTerm;

public class VectorImageBuilder {
	private String savePath;
	private HashMap<String, ArrayList<FeatureTerm>> featureMap;
	public VectorImageBuilder(String savePath, HashMap<String, ArrayList<FeatureTerm>> featureMap) {
		this.savePath = savePath;
		this.featureMap = featureMap;
	}
	
	public boolean saveVectorsAsBitmap(ArrayList<DocumentVector> documentVectors, ArrayList<String> termsOrder) {
		System.out.println("开始生成图像：" + this.savePath);

		HashMap<String, HashMap<String, FeatureTerm>> featureCacheMap = new HashMap<String, HashMap<String,FeatureTerm>>();
        for (String className : featureMap.keySet()) {
                ArrayList<FeatureTerm> termArrayList = featureMap.get(className);
                HashMap<String , FeatureTerm> f = new HashMap<String, FeatureTerm>();
                featureCacheMap.put(className, f);
                for (FeatureTerm featureTerm : termArrayList) {
                   f.put(featureTerm.getText(), featureTerm);
                }
        }
		
        HashMap<String, ArrayList<Double>> ff = new HashMap<String, ArrayList<Double>>();
        for (String className : featureCacheMap.keySet()) {
        	HashMap<String, FeatureTerm> cache = featureCacheMap.get(className);
        	ArrayList<Double> aList = new ArrayList<Double>();
        	ff.put(className, aList);
	        for (String term: termsOrder) {
	        	double valueDouble = 255.0;
	        	if (cache.get(term) != null) {
	        		valueDouble = cache.get(term).getFiliterWeight();
	        	}
	        	aList.add(valueDouble);
	        }
        }
		
		int index = 0;
        for(DocumentVector documentVector: documentVectors) {
        	BufferedImage bi = this.bufferedImageFromVector(documentVector, termsOrder, ff);
        	String filePath = this.savePath + File.separator
        			+ documentVector.getClassName()
        			+ "_" + documentVector.getDoc()
        			+ ".png";
        	this.saveBitmapsToFile(bi, filePath);
        	System.out.print("进度：" + (++index) + "/" + documentVectors.size() + "\r");
        }
		System.out.println("\n");
		return false;
	}
	
	public void saveBitmapsToFile(BufferedImage bi, String filePath) {
		try {
            // write out image to file as .png
            ImageIO.write(bi, "png", new File(filePath));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
	}
	
	public BufferedImage bufferedImageFromVector(DocumentVector documentVector
                                                , ArrayList<String> termsOrder
                                                , HashMap<String, ArrayList<Double>> ff) {
		BufferedImage bi = new BufferedImage(termsOrder.size(), ff.size(), BufferedImage.TYPE_BYTE_GRAY);
        for (int i = 0; i < termsOrder.size(); i++){
        	int j = 0;
        	for (String className: ff.keySet()) {
            	String term = termsOrder.get(i);
            	if(documentVector.hasTerm(term)){
            		int value = (int)(documentVector.getTermWeight(term)*255);
//            		System.out.println("value:" + value);
            		value = (value << 16) | (value << 8) | value;
            		
            		bi.setRGB(i,j,value);
            	}else {
            		bi.setRGB(i,j,(255 << 16) | (255 << 8) | 255);
            	}
            	j++;
            }
        }
        return bi;
	}
}
