package template;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import model.DocumentVector;
import model.FeatureTerm;
import model.ObjectPair;
import similarity.Distance;

public class MedianTemplate extends Template {

	public MedianTemplate(ArrayList<DocumentVector> trainArrayList,
			boolean isSaveToFile, HashMap<String, FeatureTerm> featureTerms, Distance distance) {
		super(trainArrayList, isSaveToFile, featureTerms, distance);
	}
	
	public MedianTemplate(String templateFilePath, HashMap<String, FeatureTerm> featureTerms) {
		super(templateFilePath, featureTerms);
	}
	
	@Override
	public ArrayList<Double> getTemplate(int r) {
		ArrayList<Double> aTemplate = new ArrayList<Double>();
		for (int i = 0; i < r; i++) {
			//中值
			aTemplate.add(1.0);
		}
		
		return aTemplate;
	}

	@Override
	protected void templating(HashMap<Integer, DocumentVector> documentHashMap,
			DocumentVector documentVector, ArrayList<ObjectPair<Integer, Double>> simEntries,
			double threshold) {
		HashSet<String> totalTermSet = new HashSet<String>();
		for (int i = 0; i < this.template.size(); i++) {
			if ((Double)simEntries.get(i).getTwo() < threshold) {
				break;//阀值(相似度) 优化
			}
			
			DocumentVector aDocument = documentHashMap.get(simEntries.get(i).getOne());
			totalTermSet.addAll(aDocument.getTerms());
		}
		
		for (String term : totalTermSet) {
			double weight = 0;
			ArrayList<Double> termWeightArrayList = new ArrayList<Double>();
			for (int i = 0; i < this.template.size(); i++) {
				if (i >= simEntries.size()) {
					break;
				}
				if ((Double)simEntries.get(i).getTwo() < threshold) {
					break;//阀值(相似度) 优化
				}
				
				Double simVectorWeight = documentHashMap.get(simEntries.get(i).getOne()).getTermWeight(term);
				if (simVectorWeight == null) {
					simVectorWeight = 0.0;
				}
				termWeightArrayList.add(simVectorWeight);
			}
			
			//选中值
			Collections.sort(termWeightArrayList);

			weight = termWeightArrayList.get(termWeightArrayList.size()/2);//包括自己？不要加1
			documentVector.setTermWeight(term, weight);
		}
	}
}
