package classifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import model.ClassifyResult;
import model.DocumentVector;
import model.ObjectPair;
import similarity.EuclideanDistance;
import similarity.Similarity;
import constants.Constants;

public class WeightKNNClassifier extends Classifier {
	
	private ArrayList<Double> template;
	private double c;
	
	public WeightKNNClassifier(double c) {
		this.template = this.getTemplate(Constants.K);
		this.c = c;
	}
	
	private ArrayList<Double> getTemplate(Integer r) {
		ArrayList<Double> aTemplate = new ArrayList<Double>();
		double c = 0;
		for (int i = 0; i < r; i++) {
			double e = (-i*i*1.0)/(2*Math.pow(Constants.GaussianTemplate6, 2));
			e = (1.0/(Math.sqrt(2*Math.PI)*Constants.GaussianTemplate6))*Math.pow(Math.E, e);
			c += e;
			aTemplate.add(e);
		}
		ArrayList<Double> resultTemplate = new ArrayList<Double>();//乘以平衡因子
		for (int j = 0; j < aTemplate.size(); j++) {
			double result = aTemplate.get(j)/c;
			resultTemplate.add(result);
		}
		
		return resultTemplate;
	}
	
	@Override
	protected ArrayList<ClassifyResult> classify(
			ArrayList<DocumentVector> testTrainArrayList,
			ArrayList<DocumentVector> trainArrayList) {

		ArrayList<ClassifyResult> resultArrayList = new ArrayList<ClassifyResult>();
		for (DocumentVector testDocument : testTrainArrayList) {
			System.out.print(".");
			//计算相似度
			ArrayList<ObjectPair<DocumentVector, Double>> pairArrayList = Similarity.getSimilarityPairArrayList(trainArrayList, testDocument, new EuclideanDistance());
			
			Collections.sort(pairArrayList, new Comparator<ObjectPair<DocumentVector, Double>>() {//相似度排序
				@Override
				public int compare(ObjectPair<DocumentVector, Double> o1, ObjectPair<DocumentVector, Double> o2) {
					if ((Double)(o1.getTwo()) > (Double)(o2.getTwo()))
						return -1;
					if ((Double)(o1.getTwo()) < (Double)(o2.getTwo()))
						return 1;
					else
						return 0;
				}
			});
			
			HashMap<String, Double> scoreHashMap = new HashMap<String, Double>();//分类投票
			int end = Constants.K < pairArrayList.size() ? Constants.K : pairArrayList.size();
			
			for (int i = 0; i < end; i++) {
				DocumentVector documentVector = (DocumentVector) pairArrayList.get(i).getOne();
				Double score = scoreHashMap.get(documentVector.getClassName());
				Double simValueDouble = (Double) pairArrayList.get(i).getTwo();
				if (score == null) {
					score = 0.0;
				}
				double templateValue = this.template.get(i);
				scoreHashMap.put(documentVector.getClassName(), score + (1 - c)*templateValue + c*simValueDouble);
			}
			
			Double maxScore = -1.0;//选出最高票数
			String resultClass = null;
			for (String className : scoreHashMap.keySet()) {
				if (scoreHashMap.get(className) > maxScore) {
					resultClass = className;
					maxScore = scoreHashMap.get(className);
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
