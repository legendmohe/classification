package template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import model.DocumentVector;
import model.FeatureTerm;
import model.ObjectPair;
import similarity.Distance;

public class GaussianTemplate extends Template {

	public GaussianTemplate(ArrayList<DocumentVector> trainArrayList,
			boolean isSaveToFile, HashMap<String, FeatureTerm> featureTerms, Distance distance) {
		super(trainArrayList, isSaveToFile, featureTerms, distance);
	}
	
	public GaussianTemplate(String templateFilePath, HashMap<String, FeatureTerm> featureTerms) {
		super(templateFilePath, featureTerms);
	}
	
	@Override
	public ArrayList<Double> getTemplate(int templateradios) {
		ArrayList<Double> aTemplate = new ArrayList<Double>();
		double sum = 0;
		for (int i = 0; i < templateradios; i++) {
//			double e = (-i*i*1.0)/(2*Math.pow(Constants.GaussianTemplate6, 2));
//			double cur = (1.0/(Math.sqrt(2*Math.PI)*Constants.GaussianTemplate6))*Math.pow(Math.E, e);
			double cur = this.conbination(templateradios*2 - 1, templateradios + i);
			sum += cur;
			aTemplate.add(cur);
		}
		ArrayList<Double> resultTemplate = new ArrayList<Double>();//乘以平衡因子
		for (int j = 0; j < aTemplate.size(); j++) {
			Double result = aTemplate.get(j)/sum;
//			if (j == 0) {//锐化
//				result = 2 - result;
//			}else {
//				result = -result;
//			}
			resultTemplate.add(result);
		}
		
		return resultTemplate;
	}
	
	private int conbination(int n, int r) {
		int i;
		int maxvalue,minvalue;
		int result=1;
		maxvalue=Math.max(r,n-r);
		minvalue=Math.min(r,n-r);
		for(i=n;i>0;i--) {
			if(i>maxvalue)
				result*=i;
			else if(i<=minvalue)
				result/=i;
		}
		return result;
	}

	@Override
	protected void templating(HashMap<Integer, DocumentVector> documentHashMap,
			DocumentVector documentVector,
			ArrayList<ObjectPair<Integer, Double>> simEntries,
			double threshold) {
//		System.out.println(threshold);
		HashSet<String> totalTermSet = new HashSet<String>();
		for (int i = 0; i < this.template.size(); i++) {
			if ((Double)simEntries.get(i).getTwo() < threshold) {
				break;//阀值(相似度) 优化
			}
			
			DocumentVector aDocument = documentHashMap.get(simEntries.get(i).getOne());
			totalTermSet.addAll(aDocument.getTerms());
		}
		
//		for (String term : documentVector.getTerms()) {
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
