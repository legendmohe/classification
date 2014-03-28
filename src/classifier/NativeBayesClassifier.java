package classifier;

import java.util.ArrayList;
import java.util.HashMap;

import filter.CDDFilter;
import filter.CHIFilter;

import weight.CDDWeighter;

import model.ClassifyResult;
import model.DocumentVector;
import model.FeatureTerm;

public class NativeBayesClassifier extends Classifier {
	
	HashMap<String, Integer> priorHashMap;
	HashMap<String, HashMap<String, FeatureTerm>> classFeatureHashMap; 
	int totalDocNumber;
	
	public NativeBayesClassifier(HashMap<String, ArrayList<FeatureTerm>> classFeatureMap, ArrayList<DocumentVector> trainVectors){
		//初始化
		System.out.print("初始化贝叶斯分类器...");
		priorHashMap = new HashMap<String, Integer>();
		classFeatureHashMap = new HashMap<String, HashMap<String,FeatureTerm>>();
		totalDocNumber = trainVectors.size();
		//统计先验信息
		for (DocumentVector documentVector : trainVectors) {
			Integer countInteger = priorHashMap.get(documentVector.getClassName());
			if (countInteger == null) {
				countInteger = 0;
			}
			priorHashMap.put(documentVector.getClassName(), countInteger + 1);
		}
		//统计
		for (String className : classFeatureMap.keySet()) {
			ArrayList<FeatureTerm> featureArrayList = classFeatureMap.get(className);
			HashMap<String, FeatureTerm> featureTermHashMap = new HashMap<String, FeatureTerm>();
			for (FeatureTerm featureTerm : featureArrayList) {
				featureTermHashMap.put(featureTerm.getText(), featureTerm);
			}
			classFeatureHashMap.put(className, featureTermHashMap);
		}
		
		//
		System.out.println("初始化完毕.");
	}

	@Override
	protected ArrayList<ClassifyResult> classify(
			ArrayList<DocumentVector> testTrainArrayList,
			ArrayList<DocumentVector> trainArrayList) {

		ArrayList<ClassifyResult> resultArrayList = new ArrayList<ClassifyResult>();
		for (DocumentVector testDocument : testTrainArrayList) {
			System.out.print(".");
			
			String resultClass = null;
			double maxNB = 0.0;
			for (String className : priorHashMap.keySet()) {
//				double prior = priorHashMap.get(className)*1.0/totalDocNumber;
				double compairSum = 0.0;//每个类别的分类值
				for (String termString : testDocument.getTerms()) {
					FeatureTerm featureTerm = classFeatureHashMap.get(className).get(termString);
					//计算部分
					double NBValue = 0.0;
					if (featureTerm == null) {
						NBValue = 1.0/(priorHashMap.get(className) + 2);
					}else {
						NBValue = (featureTerm.getN11() + 1.0)/(priorHashMap.get(className) + 2);
//						NBValue = (new CHIFilter()).getFeatureValue(featureTerm);
					}
					compairSum += Math.log(NBValue + 1);
					//
				}
//				compairSum += Math.log(prior + 1);
				if (compairSum >= maxNB) {
					maxNB = compairSum;
					resultClass = className;
				}
			}
			
			ClassifyResult classifyResult = new ClassifyResult();//包装分类结果
			classifyResult.setDoc(testDocument.getDoc());
			classifyResult.setClassString(resultClass);
			classifyResult.setOriginalClassString(testDocument.getClassName());
			
			resultArrayList.add(classifyResult);
		}
		
		return resultArrayList;//返回结果
	}
}
