package filter;

import model.FeatureTerm;

public class DFFilter extends Filter {

	@Override
	public double getFeatureValue(FeatureTerm featureTerm) {
		return featureTerm.getN11() + featureTerm.getN10();
	}

}
