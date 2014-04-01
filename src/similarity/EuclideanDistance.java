package similarity;

import java.util.Comparator;
import java.util.HashSet;

import model.DocumentVector;
import model.ObjectPair;

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

	@Override
	public Comparator<ObjectPair<Integer, Double>> getComparator() {
		return new Comparator<ObjectPair<Integer, Double>>() {
			@Override
			public int compare(ObjectPair<Integer, Double> o1,
					ObjectPair<Integer, Double> o2) {
				if ((Double)o1.getTwo() > (Double)o2.getTwo()) {
					return 1;
				}else if ((Double)o1.getTwo() < (Double)o2.getTwo()) {
					return -1;
				}else {
					return 0;
				}
			}
		};
	}

}
