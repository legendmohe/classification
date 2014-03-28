package classifier;

import java.util.ArrayList;

import model.ClassifyResult;
import model.DocumentVector;

public abstract class Classifier {
	
	public Classifier() {
	}
	
	public ArrayList<ClassifyResult> classifyFiles(ArrayList<DocumentVector> testArrayList, ArrayList<DocumentVector> trainArrayList) {
		
		System.out.println("开始分类...");
		
		try {
			System.out.println("训练集：" + trainArrayList.size() + " | 测试集: " + testArrayList.size());
			
			ArrayList<ClassifyResult> resultArrayList = this.classify(testArrayList, trainArrayList);//调用子类的分类方法
			
			System.out.println("分类结束...");
			return resultArrayList;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	protected abstract ArrayList<ClassifyResult> classify(ArrayList<DocumentVector> testTrainArrayList, ArrayList<DocumentVector> trainArrayList);

}
