package filter;

import model.FeatureTerm;

public class CDDFilter extends Filter {

	@Override
	public double getFeatureValue(FeatureTerm featureTerm) {
		int N11 = featureTerm.getN11();//在此类含此词
//		int N01 = classifierTerm.getN01();//在此类不含此词
		int N10 = featureTerm.getN10();//不在此类含此词
//		int N00 = classifierTerm.getN00();//不在此类不含此词
		
		double between = N11*Math.log((N11*1.0)/(N10 + 1) + 1.0);
		if (between <= 0) {
			between = 0;
		}
		return Math.log(between + 1);
	}

}
