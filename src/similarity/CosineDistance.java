package similarity;

import model.DocumentVector;

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

}
