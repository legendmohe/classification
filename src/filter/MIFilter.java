package filter;

import model.FeatureTerm;

public class MIFilter extends Filter {

	@Override
	public double getFeatureValue(FeatureTerm featureTerm) {
		int N11 = featureTerm.getN11();//在此类含此词
		int N01 = featureTerm.getN01();//在此类不含此词
		int N10 = featureTerm.getN10();//不在此类含此词
		int N00 = featureTerm.getN00();//不在此类不含此词
		int N = N11 + N01 + N10 + N00;
		
//		double part1 = N11*Math.log(N11*1.0) + N10*Math.log(N10*1.0) + N01*Math.log(N01*1.0) + N00*Math.log(N00*1.0);
//		double part2 = (N11 + N10)*Math.log(N11 + N10*1.0) + (N01 + N00)*Math.log(N01 + N00*1.0);
//		
//		return part1 - part2;
		return Math.log((N11*N)*1.0/(N11 + N01)*(N10 + N00));
	}

}
