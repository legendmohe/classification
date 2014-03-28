package filter;

import model.FeatureTerm;

public class CHIFilter extends Filter {

	@Override
	public double getFeatureValue(FeatureTerm featureTerm) {
		int N11 = featureTerm.getN11();//在此类含此词
		int N01 = featureTerm.getN01();//在此类不含此词
		int N10 = featureTerm.getN10();//不在此类含此词
		int N00 = featureTerm.getN00();//不在此类不含此词
		double value = Math.pow((1.0*N11*N00 - N10*N01), 2)/(1.0*(N11 + N01)*(N11 + N10)*(N10 + N00)*(N01 + N00));
		return value;
	}

}
