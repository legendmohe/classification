package svm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import model.DocumentVector;
import model.FeatureTerm;

public class SVMPreprocessor {
	public static HashMap<String, String> processClassLabel(Set<String> labels, String path) {
		int labelindex = 0;
		HashMap<String, String> classlabel = new HashMap<String, String>();
		for (String label : labels) {
			classlabel.put(label, String.valueOf(++labelindex) + ".0");
		}
		BufferedWriter bufferedWriter = null;
		try {
			bufferedWriter = new BufferedWriter(new FileWriter(path));
			for (String label : classlabel.keySet()) {
				bufferedWriter.write(label + ":" + classlabel.get(label) + "\n");
			}
		} catch (Exception e) {
			System.out.println(e);
		}finally {
			try {
				bufferedWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return classlabel;
	}
	public static void processDocumentVectorToSVMFormatFile(HashMap<String, String> labels, HashMap<String, FeatureTerm> featureTermMap, ArrayList<DocumentVector> documentVectors, String filePathString) throws IOException {
		File saveFile = new File(filePathString);
		if (saveFile.exists() == false) {
			saveFile.createNewFile();
		}
		if (!saveFile.isFile()) {
			System.err.println("processDocumentVectorToSVMFormatFile:file path invaild." + filePathString);
			return;
		}
		
		System.out.println("开始生成SVM格式文件...");
		System.out.println("维度：" + featureTermMap.size());
		System.out.println("文档数：" + documentVectors.size());
		
		BufferedWriter bufferedWriter = null;
		try {
			bufferedWriter = new BufferedWriter(new FileWriter(saveFile));
			for (DocumentVector documentVector : documentVectors) {
				int index = 1;//从1开始
				String label = labels.get(documentVector.getClassName());
				bufferedWriter.write(label);
				for (String term : featureTermMap.keySet()) {
					Double weight = documentVector.getTermWeight(term);
					if (weight != null) {
						bufferedWriter.write(" " + index + ":" + String.format("%.6f", weight));
					}
					index++;
				}
				bufferedWriter.write("\n");
			}
		} catch (Exception e) {
			// TODO: handle exception
		}finally {
			bufferedWriter.close();
		}
		
		System.out.println("已生成");
	}
}
