package result;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import model.ClassifyResult;
import constants.Constants;

public class ResultParser {
	public static void parseResult(ArrayList<ClassifyResult> classifyResults, boolean isSaveToFile) throws Exception {
		HashMap<String, Integer> correctNumberHashMap = new HashMap<String, Integer>();
		HashMap<String, Integer> resultFileNumberInEachClassHashMap = new HashMap<String, Integer>();
		HashMap<String, Integer> trainFileNumberInEachClassHashMap = new HashMap<String, Integer>();
		
		System.out.println("共有：" + classifyResults.size() + "个结果");
		
		for (ClassifyResult classifyResult : classifyResults) {
			String originalClass = classifyResult.getOriginalClassString();
			String resultClass = classifyResult.getClassString();
			
//			System.out.println("doc:" + classifyResult.getDoc() + " original:" + classifyResult.getOriginalClassString() + " result:" + classifyResult.getClassString());
			
			Integer pNumber = correctNumberHashMap.get(originalClass);
			if (pNumber == null) {
				if (originalClass.equals(resultClass))
					correctNumberHashMap.put(originalClass, 1);
			}else {
				if (originalClass.equals(resultClass))
					correctNumberHashMap.put(originalClass, pNumber + 1);
			}
			
			Integer rNumber = resultFileNumberInEachClassHashMap.get(resultClass);
			if (rNumber == null) {
				resultFileNumberInEachClassHashMap.put(resultClass, 1);
			}else {
				resultFileNumberInEachClassHashMap.put(resultClass, rNumber + 1);
			}
			
			Integer cNumber = trainFileNumberInEachClassHashMap.get(originalClass);
			if (cNumber == null) {
				trainFileNumberInEachClassHashMap.put(originalClass, 1);
			}else {
				trainFileNumberInEachClassHashMap.put(originalClass, cNumber + 1);
			}
		}
		
		StringBuffer resultBuffer = new StringBuffer();
		resultBuffer.append("-------------------统计结果--------------------" + "\n");
		resultBuffer.append("-train:" + Constants.CLASSIFIER_TRAINDOCUMENTVECTORFILE_PATH + "\n");
		resultBuffer.append("-test:" + Constants.CLASSIFIER_TESTDOCUMENTVECTORFILE_PATH + "\n");
		resultBuffer.append("-K:" + Constants.K + "\n");
		resultBuffer.append("-dimension:" + Constants.filiterNumberThreshold + "\n");
		resultBuffer.append("\n");
		
		BufferedWriter fileWriter = null;
		File resultFile = new File(Constants.CLASSIFIER_RESULTFILE_PATH);
		if (!resultFile.exists() && isSaveToFile) {
			resultFile.createNewFile();
		}
		if (isSaveToFile) {
			if (resultFile.exists() && resultFile.isFile()) {
				fileWriter = new BufferedWriter(new FileWriter(resultFile, true));
				resultBuffer.append("-" + new Date() + "-\n");
			}else {
				System.err.println("resultFile invaild.");
			}
		}
		
		System.out.print(resultBuffer.toString());
		
		double pSum = 0;
		double cSum = 0;
		ArrayList<String> classNameArrayList = new ArrayList<String>();
		classNameArrayList.addAll(trainFileNumberInEachClassHashMap.keySet());
		Collections.sort(classNameArrayList);
		for (String className : classNameArrayList) {
			Integer correctNum = correctNumberHashMap.get(className);
			if (correctNum == null) {
				continue;
			}
			double precision = (double)correctNum/resultFileNumberInEachClassHashMap.get(className);
			double recall = (double)correctNum/trainFileNumberInEachClassHashMap.get(className);
			double F1 = (precision*recall*2)/(precision + recall);
			
			System.out.println(className + " P:"
					+ String.format("%.2f", precision)
					+ " C:" + String.format("%.2f",recall)
					+ " F1:" + String.format("%.2f",F1));
			resultBuffer.append(className + " P:"
					+ String.format("%.2f", precision)
					+ " C:" + String.format("%.2f",recall)
					+ " F1:" + String.format("%.2f",F1) + "\n");
			
			pSum += precision;
			cSum += recall;
		}
		
		double marco_F1 = (2*pSum*cSum)/((pSum + cSum)*trainFileNumberInEachClassHashMap.keySet().size());
		if(isSaveToFile) {
			resultBuffer.append("-marco_F1:" + marco_F1 + "-\n\n");
			fileWriter.write(String.valueOf(marco_F1) + "\n");
			fileWriter.close();
		}
		
		System.out.println();
		System.out.println("-marco_F1:" + marco_F1);
		System.out.println("---------------------------------------------");
	}
}
