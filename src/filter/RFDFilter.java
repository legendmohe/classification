package filter;

import model.FeatureTerm;

public class RFDFilter extends Filter {

	@Override
	public double getFeatureValue(FeatureTerm featureTerm) {
		int N11 = featureTerm.getN11();//在此类含此词
		int N01 = featureTerm.getN01();//在此类不含此词
		int N10 = featureTerm.getN10();//不在此类含此词
		int N00 = featureTerm.getN00();//不在此类不含此词
		return Math.pow(N11*N00 - N01*N10*1.0, 2);
	}

}
