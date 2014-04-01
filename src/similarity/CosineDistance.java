package similarity;

import java.util.Comparator;

import model.DocumentVector;
import model.ObjectPair;

public class CosineDistance implements Distance {

	@Override
	public double getZeroDistance() {
		return 1.0;
	}

	@Override
	public double getSimilarityBetweenDocuments(DocumentVector document1,
			DocumentVector document2) {
		double iSum = 0;//i文档的权重和
		for (String iWeight: document1.getTerms()) 
		{
			iSum += Math.pow(document1.getTermWeight(iWeight), 2);
		}
		
		double similarity = 0;//计算相似度
		double jSum = 0;
		double ijSum = 0;
		for (String jWeight: document2.getTerms()) {
			jSum += Math.pow(document2.getTermWeight(jWeight), 2);//j文档的权重和
			
			Double iWeight = document1.getTermWeight(jWeight);
			if (iWeight != null) {
				ijSum += iWeight*document2.getTermWeight(jWeight);//同时有i、j的文档的权重和
			}
		}
		similarity = ijSum/Math.sqrt(iSum*jSum);//cos相似度计算
		
		return similarity;
	}

	@Override
	public Comparator<ObjectPair<Integer, Double>> getComparator() {
		return new Comparator<ObjectPair<Integer, Double>>() {
			@Override
			public int compare(ObjectPair<Integer, Double> o1,
					ObjectPair<Integer, Double> o2) {
				if ((Double)o1.getTwo() > (Double)o2.getTwo()) {
					return -1;
				}else if ((Double)o1.getTwo() < (Double)o2.getTwo()) {
					return 1;
				}else {
					return 0;
				}
			}
		};
	}

}
