package filter;

import model.FeatureTerm;

public class LLDRFilter extends Filter {

	@Override
	public double getFeatureValue(FeatureTerm featureTerm) {
		double c = featureTerm.getN11()*1.0/(featureTerm.getN11() + featureTerm.getN01());
		double _c = featureTerm.getN10()*1.0/(877 - featureTerm.getN11() + featureTerm.getN01());
		return c > _c ? c : _c;
	}

}
