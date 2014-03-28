package similarity;

import java.util.HashSet;

import model.DocumentVector;

public class EuclideanDistance implements Distance {

	@Override
	public double getZeroDistance() {
		return 0.0;
	}

	@Override
	public double getSimilarityBetweenDocuments(DocumentVector document1,
			DocumentVector document2) {
		HashSet<String> termHashSet = new HashSet<String>();
		termHashSet.addAll(document1.getTerms());
		termHashSet.addAll(document2.getTerms());
		double similarity = 0.0;
		for (String term : termHashSet) {
			Double iweight = document1.getTermWeight(term) ;
			if (iweight == null) {
				iweight = 0.0;
			}
			Double jweight = document2.getTermWeight(term) ;
			if (jweight == null) {
				jweight = 0.0;
			}
			similarity += Math.pow(iweight - jweight, 2);
		}
		similarity = Math.sqrt(similarity);
		
		return similarity;
	}

}
