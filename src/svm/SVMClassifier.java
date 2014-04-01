package svm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import classifier.Classifier;


import constants.Constants;

import model.ClassifyResult;
import model.DocumentVector;

public class SVMClassifier extends Classifier {
	
	private HashMap<String, String> labels;
	
	public SVMClassifier(HashMap<String, String> labels) {
		this.labels = new HashMap<String, String>();
		for (String classname : labels.keySet()) { // inverst
			this.labels.put(labels.get(classname), classname);
		}
	}

	@Override
	protected ArrayList<ClassifyResult> classify(
			ArrayList<DocumentVector> testArrayList,
			ArrayList<DocumentVector> trainArrayList) {

		String[] trainArgs = {Constants.CLASSIFIER_SVM_TRAINFILE_PATH, Constants.CLASSIFIER_SVM_MODELFILE_PATH};//directory of training file
		String[] testArgs = {Constants.CLASSIFIER_SVM_TESTFILE_PATH, Constants.CLASSIFIER_SVM_MODELFILE_PATH, Constants.CLASSIFIER_SVM_RESULTFILE_PATH};//directory of test file, model file, result file
		try {
			svm_train.main(trainArgs);
			svm_predict.main(testArgs);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return this.resultsFromSVMResultFile(Constants.CLASSIFIER_SVM_RESULTFILE_PATH,testArrayList);
	}

	public ArrayList<ClassifyResult> resultsFromSVMResultFile(String filePath, ArrayList<DocumentVector> testArrayList) {
		File file = new File(filePath);
		if (file.exists() == false) {
			System.err.println("svmresult文件不存在:" + filePath);
			return null;
		}
		ArrayList<ClassifyResult> resultArrayList = new ArrayList<ClassifyResult>();
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new FileReader(file));
			int index = 0;
			String readLine = null;
			while ((readLine = bufferedReader.readLine()) != null && index < testArrayList.size()) {
				String resultClass = this.labels.get(readLine); //类别数字
				ClassifyResult result = new ClassifyResult();
//				System.out.println(index + "|" + testArrayList.get(index).getDoc() + "|" + testArrayList.get(index).getClassName());
				DocumentVector documentVector = testArrayList.get(index);
				result.setOriginalClassString(documentVector.getClassName());
				result.setClassString(resultClass);
				result.setDoc(documentVector.getDoc());
				resultArrayList.add(result);
				
				index++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				bufferedReader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return resultArrayList;
	}
}
