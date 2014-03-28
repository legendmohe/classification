package controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import model.DocumentVector;
import model.FeatureTerm;
import model.MyTerm;

public class Storer {
	static public void storeRelevanceBetweenTwoTermsToFile(ArrayList<MyTerm> termArrayList, String path){
		System.out.println("begin storeTermsToFile");
		FileWriter fileWriter = null;
		
		try {
			fileWriter = new FileWriter(new File(path));
			for (MyTerm myTerm : termArrayList) {
				for (String akey : myTerm.occourenceDocsHashMap.keySet()) {
					if (myTerm.occourenceDocsHashMap.get(akey) != null) {
						fileWriter.write(myTerm.nameString + "," + akey + "," + myTerm.occourenceDocsHashMap.get(akey) + "\n");
					}
				}
			}
			fileWriter.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fileWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("end storeTermsToFile");
	}
	
	public static void storeFeatureMapToFilePath(HashMap<String, ArrayList<FeatureTerm>> featureMap, String filePath) {
		
		System.out.println("存储特征项到文件：" + filePath);
		
		File storeFile = new File(filePath);
		if (!storeFile.exists()) {
			try {
				storeFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				
				System.out.println("can not create file:" + filePath);
				
				return;
			}
		}
		if (storeFile.isFile()) {
			BufferedWriter bufferedWriter = null;
			try {
				
				int sumOfFeature = 0;
				bufferedWriter = new BufferedWriter(new FileWriter(storeFile));
				for (String className : featureMap.keySet()) {
					bufferedWriter.write(className + "\n");
					int numOfFeature = 0;
					for (FeatureTerm term : featureMap.get(className)) {
						numOfFeature++;
						bufferedWriter.write(term.getText() + " " + term.getN11() + " " + term.getN10() + " " + term.getN01() + " " + term.getN00() + " " + term.getFiliterWeight() + "\n");
					}
					sumOfFeature += numOfFeature;
					bufferedWriter.write("-\n");
				}
				
				System.out.println("共有" + sumOfFeature + "个特征");
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					bufferedWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}else {
			System.out.println("store file path invaild.");
		}
		
		System.out.println("存储特征项到文件结束");
	}

	public static HashMap<String, ArrayList<FeatureTerm>> loadFeatureMapFromFilePath(String filePath) {
		
		System.out.println("载入特征项，从：" + filePath);
		
		File featureFile = new File(filePath);
		if (!featureFile.exists() || !featureFile.isFile()) {
			System.out.println("featureFilePath invaild:" + filePath);
			return null;
		}
		HashMap<String, ArrayList<FeatureTerm>> featureHashMap = null;
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new FileReader(featureFile));
			String readLine = null;
			featureHashMap = new HashMap<String, ArrayList<FeatureTerm>>();
			while ((readLine = bufferedReader.readLine()) != null) {
				String className = readLine;
				ArrayList<FeatureTerm> classifierTermsaArrayList = new ArrayList<FeatureTerm>();
				while ((readLine = bufferedReader.readLine()) != null) {
					if (readLine.equals("-")) {
						break;
					}
					FeatureTerm newTerm = new FeatureTerm();
					newTerm.setClassName(className);
					String[] dataStrings = readLine.split(" ");
					if (dataStrings.length == 6) {
						newTerm.setText(dataStrings[0]);
						newTerm.setN11(Integer.parseInt(dataStrings[1]));
						newTerm.setN10(Integer.parseInt(dataStrings[2]));
						newTerm.setN01(Integer.parseInt(dataStrings[3]));
						newTerm.setN00(Integer.parseInt(dataStrings[4]));
						newTerm.setFiliterWeight(Double.parseDouble(dataStrings[5]));
						
						classifierTermsaArrayList.add(newTerm);
					}else {
						System.out.println("invaild term");
					}
				}
				featureHashMap.put(className, classifierTermsaArrayList);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				bufferedReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("载入特征项结束");
		
		return featureHashMap;
	}

	public static void storeDocumentVectorToFilePath(ArrayList<DocumentVector> documentVectors, String filePath){
		File saveFile = new File(filePath);
		if (!saveFile.exists()) {
			try {
				saveFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (!saveFile.isFile()) {
			System.err.println("storeDocumentVectorToFilePath:filePath invaild");
			return;
		}
		
		System.out.print("保存向量模型到文件：" + filePath + "...");
		
		BufferedWriter bufferedWriter = null;
		try {
			bufferedWriter = new BufferedWriter(new FileWriter(saveFile));
			for (DocumentVector documentVector : documentVectors) {
				bufferedWriter.write(documentVector.getDoc() + " " + documentVector.getClassName());
				for (String termString : documentVector.getTerms()) {
					bufferedWriter.write(" " + termString + " " + documentVector.getTermWeight(termString));
				}
				bufferedWriter.write("\n");
			}
		} catch (Exception e) {
		} finally {
			try {
				bufferedWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		
		System.out.println("保存完毕");
	}

	public static ArrayList<DocumentVector> loadDocumentVectorsFromFilePath(String filePath) {
		File saveFile = new File(filePath);
		if (!saveFile.exists()) {
			try {
				saveFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (!saveFile.isFile()) {
			System.err.println("loadDocumentVectorsFromFilePath:filePath invaild");
			return null;
		}
		
		ArrayList<DocumentVector> resultArrayList = new ArrayList<DocumentVector>();
		BufferedReader bufferedReader = null;
		double maxWeight = 0.0;
		try {
			bufferedReader = new BufferedReader(new FileReader(saveFile));
			String readLine = null;
			while ((readLine = bufferedReader.readLine()) != null) {
				DocumentVector documentVector = new DocumentVector();
				String[] documentStrings = readLine.split(" ");
				int begin = 0;
				documentVector.setDoc(Integer.parseInt(documentStrings[begin++]));
				documentVector.setClassName(documentStrings[begin++]);
				for (int i = begin; i < documentStrings.length;) { //注意这里的逻辑
					documentVector.setTermWeight(documentStrings[i++], Double.parseDouble(documentStrings[i++]));
					maxWeight = maxWeight >= Double.parseDouble(documentStrings[i - 1]) ? maxWeight : Double.parseDouble(documentStrings[i - 1]);
				}
				if (documentVector.getTerms().size() > 0) {
					resultArrayList.add(documentVector);
				}else {
					System.out.println("忽略空文档：类>" + documentVector.getClassName() + " 文件号>" + documentVector.getDoc());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				bufferedReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("maxWeight:" + maxWeight);
		return resultArrayList;
	}
	
	public static void saveSimilarityToFile(String filePath
			, Double[][] similarityHashMap) {
		
		File saveFile = new File(filePath);
		if (!saveFile.exists()) {
			try {
				saveFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if (!saveFile.isFile()) {
			System.err.println("saveSimilarityToFile:save file invaild");
			return;
		}
		System.out.print("保存相似矩阵...");
		BufferedWriter bufferedWriter = null;
		try {
			bufferedWriter = new BufferedWriter(new FileWriter(saveFile));
//			for (Integer doc1 : similarityHashMap.keySet()) {
			bufferedWriter.write(similarityHashMap.length);
			for (int doc1 = 0; doc1 < similarityHashMap.length; doc1++) {
				bufferedWriter.write(doc1 + "\n");
//				HashMap<Integer, Double> simMap = similarityHashMap.get(doc1);
//				ArrayList<Map.Entry<Integer, Double>> simEntries = new ArrayList<Map.Entry<Integer, Double>>();
//				simEntries.addAll(simMap.entrySet());
//				Collections.sort(simEntries, new Comparator<Map.Entry<Integer, Double>>() {
//					@Override
//					public int compare(Entry<Integer, Double> o1,
//							Entry<Integer, Double> o2) {
//						if (o1.getValue() > o2.getValue()) {
//							return -1;
//						}else if (o1.getValue() < o2.getValue()) {
//							return 1;
//						}else {
//							return 0;
//						}
//					}
//				});
//				for (Map.Entry<Integer, Double> entry : simEntries) {
				for (int doc2 = 0; doc2 < similarityHashMap.length; doc2++) {
					bufferedWriter.write(String.valueOf(doc2) + " " + String.valueOf(similarityHashMap[doc1][doc2]));
					bufferedWriter.write("\n");
				}
				bufferedWriter.write("-\n");
			}
			System.out.println("保存完毕.");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				bufferedWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static Double[][] loadSimilarityHashMap(String templateFilePath) {
		File saveFile = new File(templateFilePath);
		if (!saveFile.exists()) {
			System.err.println("loadSimilarityHashMap:filePath invaild");
			return null;
		}
		
		System.out.print("正在载入相似矩阵...");
		
		Double [][] resultMap = null;
		BufferedReader bufferedReader = null;
		try {
			String readLine	= null;
			bufferedReader = new BufferedReader(new FileReader(saveFile));
			int size = Integer.parseInt(bufferedReader.readLine());
			resultMap = new Double[size][size];
			while((readLine = bufferedReader.readLine()) != null){
				while(!(readLine = bufferedReader.readLine()).equals("-")){
					String[] simPair = readLine.split(" ");
					resultMap[Integer.parseInt(readLine)][Integer.parseInt(simPair[0])] = Double.parseDouble(simPair[1]);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				bufferedReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("载入完毕.");
		
		return resultMap;
	}
}
