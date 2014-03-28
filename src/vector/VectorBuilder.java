package vector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import model.DocumentVector;
import model.FeatureTerm;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermFreqVector;

import weight.Weighter;
import constants.Constants.FILTER_THRESHOLD_TYPE;
import controller.Storer;

public class VectorBuilder {
	
	private HashMap<String, FeatureTerm> featureTermMap;
	private ArrayList<String> termsOrder;
	private boolean normalization;
	private Weighter weightEvaluator;
	
	public VectorBuilder(HashMap<String, ArrayList<FeatureTerm>> featureMap, int threshold, FILTER_THRESHOLD_TYPE type, Weighter weightEvaluator) {
		this.setFeatureTermMap(new HashMap<String, FeatureTerm>());//方便查找
		this.setTermsOrder(new ArrayList<String>());
		this.setNormalization(true);//归一化默认为true;
		this.weightEvaluator = weightEvaluator;//权重计算方式
		
		int sumOfFeature = 0;
		switch (type) {
		case FILTER_THRESHOLD_TYPE_EACH:
		{
			int numEachClass = threshold/featureMap.size();
			System.out.println("每个类筛选" + numEachClass + "个特征");
			
			for (String className : featureMap.keySet()) {
				ArrayList<FeatureTerm> termArrayList = featureMap.get(className);
				int numOfFeature = 0;
				for (FeatureTerm featureTerm : termArrayList) {
					this.getFeatureTermMap().put(featureTerm.getText(), featureTerm);
					this.getTermsOrder().add(featureTerm.getText());
					numOfFeature++;
					if (numOfFeature >= numEachClass) {
						break;
					}
				}
				sumOfFeature += numOfFeature;
			}
		}
			break;
		case FILTER_THRESHOLD_TYPE_MAX:
		{
			for (String className : featureMap.keySet()) {
				ArrayList<FeatureTerm> termArrayList = featureMap.get(className);
				for (FeatureTerm featureTerm : termArrayList) {
					FeatureTerm resultTerm = this.getFeatureTermMap().get(featureTerm.getText());
					if (resultTerm != null) {
						if (featureTerm.getFiliterWeight() > resultTerm.getFiliterWeight()) {
							resultTerm.setFiliterWeight(featureTerm.getFiliterWeight());//maxCHI
						}
					}else {
						this.getFeatureTermMap().put(featureTerm.getText(), featureTerm);
					}
				}
			}
			
			ArrayList<Map.Entry<String, FeatureTerm>> sortArrayList = new ArrayList<Map.Entry<String,FeatureTerm>>();
			sortArrayList.addAll(this.getFeatureTermMap().entrySet());
			Collections.sort(sortArrayList, new Comparator<Map.Entry<String, FeatureTerm>>() {
				@Override
				public int compare(Entry<String, FeatureTerm> o1,
						Entry<String, FeatureTerm> o2) {
				
					if (o1.getValue().getFiliterWeight() > o2.getValue().getFiliterWeight()) {
						return -1;
					}else if (o1.getValue().getFiliterWeight() < o2.getValue().getFiliterWeight()){
						return 1;
					}else {
						return 0;
					}
				
				}
			});
			
			this.getFeatureTermMap().clear();
			for (int i = 0; i < threshold; i++) {
				Map.Entry<String, FeatureTerm> termEntry = sortArrayList.get(i);
				this.getTermsOrder().add(termEntry.getKey());
				this.getFeatureTermMap().put(termEntry.getKey(), termEntry.getValue());
			}
			
			sumOfFeature = threshold;
		}
			break;
		default:
			break;
		}
		
		System.out.println("筛选后有：" + sumOfFeature + "个特征");
	}
	
	public ArrayList<DocumentVector> getDocumentVectorsFromFilePath(String filePath) {
		return Storer.loadDocumentVectorsFromFilePath(filePath);
	}
	
	public ArrayList<DocumentVector> getDocumentVectorsFromReader(IndexReader indexReader, String saveToFilePath) throws IOException {
		ArrayList<DocumentVector> resultDocuments = new ArrayList<DocumentVector>();
		
		System.out.println("正在生成文档向量：");
		
		int index = 0;
		for (int i = 0; i < indexReader.numDocs(); i++) {
			TermFreqVector resultTermFreqVector = indexReader.getTermFreqVector(i, "content");
			if (resultTermFreqVector == null) {
				continue;
			}
			DocumentVector resultDocument = new DocumentVector();//包装类
			resultDocument.setClassName(indexReader.document(i).get("class"));	
			resultDocument.setDoc(index);
			
			double sumOfWeight = 0;
			for (int j = 0; j < resultTermFreqVector.size(); j++) {
				FeatureTerm featureTerm = this.getFeatureTermMap().get(resultTermFreqVector.getTerms()[j]);
				if (featureTerm == null) {
					continue;//特征筛选
				}
				double weight = this.weightEvaluator.getDimentionWeight(featureTerm, resultTermFreqVector.getTermFrequencies()[j]);
				sumOfWeight += Math.pow(weight, 2);
				
				resultDocument.setTermWeight(resultTermFreqVector.getTerms()[j], weight);
			}
			if (isNormalization()) {
				for (String termString : resultDocument.getTerms()) {//归一化
					Double weight = resultDocument.getTermWeight(termString);
					if (weight == 0) {
						System.err.println(termString);
					}
					resultDocument.setTermWeight(termString, weight/Math.sqrt(sumOfWeight));
				}
			}
			if (resultDocument.getTerms().size() > 0) {
				resultDocuments.add(resultDocument);
				index++;
			}else {
				System.out.println("\n忽略空文档：类>" + resultDocument.getClassName() + " 文件号>" + resultDocument.getDoc());
			}
			
			System.out.print(".");
		}
		
		System.out.println("生成文档向量完毕.长度：" + resultDocuments.size());
		
		if (saveToFilePath != null) {
			Storer.storeDocumentVectorToFilePath(resultDocuments, saveToFilePath);
		}
		
		return resultDocuments;
	}

	public HashMap<String, FeatureTerm> getFeatureTermMap() {
		return featureTermMap;
	}

	public void setFeatureTermMap(HashMap<String, FeatureTerm> featureTermMap) {
		this.featureTermMap = featureTermMap;
	}

	public boolean isNormalization() {
		return normalization;
	}

	public void setNormalization(boolean normalization) {
		this.normalization = normalization;
	}

	public ArrayList<String> getTermsOrder() {
		return termsOrder;
	}

	public void setTermsOrder(ArrayList<String> termsOrder) {
		this.termsOrder = termsOrder;
	}
}
