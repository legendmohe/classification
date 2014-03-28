package filter;

import java.util.Random;

import model.FeatureTerm;

public class RandomFilter extends Filter {

	private static Random randomer = new Random();
	
	@Override
	public double getFeatureValue(FeatureTerm featureTerm) {
		return Math.abs(randomer.nextDouble()%10);
	}

}
