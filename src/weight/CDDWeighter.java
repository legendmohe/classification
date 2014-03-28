package weight;

import filter.CDDFilter;
import model.FeatureTerm;

public class CDDWeighter extends Weighter {

	@Override
	public double getDimentionWeight(FeatureTerm featureTerm, int tf) {
		double TF = Math.log(tf + 1);
		int M = featureTerm.getN11() + featureTerm.getN10() + featureTerm.getN01() + featureTerm.getN00();
		int df = featureTerm.getN11() + featureTerm.getN10();
		double IDF = Math.log(M*1.0/df)/Math.log(2.0);
		
		double featureValue = (new CDDFilter()).getFeatureValue(featureTerm);
		if (featureValue > 0) 
			return TF*IDF*featureValue;
		else 
			return TF*IDF;
	}

}
