package filter;

import model.FeatureTerm;

public class IGFilter extends Filter {

	@Override
	public double getFeatureValue(FeatureTerm featureTerm) {
		int N11 = featureTerm.getN11();//在此类含此词 A
		int N01 = featureTerm.getN01();//在此类不含此词 C
		int N10 = featureTerm.getN10();//不在此类含此词 B
		int N00 = featureTerm.getN00();//不在此类不含此词 D
		int N = N11 + N01 + N10 + N00;
		
		double part1 = (N11 + N01*1.0)/N;
		double part2 = N11*1.0/(N11 + N01);
		double part3 = 1.0/(N11 + N10);
		double IGValue = part1*Math.log(part1) + 1.0/N*((N11 + N00)*part2*part3*Math.log(part2*part3));
		
		return IGValue;
	}

}
