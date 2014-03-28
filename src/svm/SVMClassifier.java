package svm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import classifier.Classifier;


import constants.Constants;

import model.ClassifyResult;
import model.DocumentVector;

public class SVMClassifier extends Classifier {

	@Override
	protected ArrayList<ClassifyResult> classify(
			ArrayList<DocumentVector> testTrainArrayList,
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
		return this.resultsFromSVMResultFile(Constants.CLASSIFIER_SVM_RESULTFILE_PATH,trainArrayList);
	}

	public ArrayList<ClassifyResult> resultsFromSVMResultFile(String filePath, ArrayList<DocumentVector> trainArrayList) {
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
			while ((readLine = bufferedReader.readLine()) != null && index < trainArrayList.size()) {
				String resultClass = String.valueOf((int)Double.parseDouble(readLine));//类别为数字时
				ClassifyResult result = new ClassifyResult();
				DocumentVector documentVector = trainArrayList.get(index);
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
