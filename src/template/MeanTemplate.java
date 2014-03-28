package template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import model.DocumentVector;
import model.FeatureTerm;
import model.ObjectPair;
import similarity.Distance;

public class MeanTemplate extends Template {

	public MeanTemplate(ArrayList<DocumentVector> trainArrayList,
			boolean isSaveToFile, HashMap<String, FeatureTerm> featureTerms, Distance distance) {
		super(trainArrayList, isSaveToFile, featureTerms, distance);
	}
	
	public MeanTemplate(String templateFilePath, HashMap<String, FeatureTerm> featureTerms) {
		super(templateFilePath, featureTerms);
	}
	
	@Override
	public ArrayList<Double> getTemplate(int r) {
		ArrayList<Double> aTemplate = new ArrayList<Double>();
		double c = 0;
		for (int i = 0; i < r; i++) {
			double e = 1.0;//均值
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
			for (int i = 0; i < this.template.size(); i++) {
				if (i >= simEntries.size()) {
					break;
				}
				if ((Double)simEntries.get(i).getTwo() < threshold) {
					break;//阀值(相似度) 优化
				}
				
				Double simVectorWeight = documentHashMap.get(simEntries.get(i).getOne()).getTermWeight(term);
				if (simVectorWeight != null) {
					weight += simVectorWeight*this.template.get(i);
				}
			}
			
			documentVector.setTermWeight(term, weight);
		}
	}
}
