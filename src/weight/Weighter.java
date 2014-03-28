package weight;

import model.FeatureTerm;

public abstract class Weighter {
	abstract public double getDimentionWeight(FeatureTerm featureTerm, int tf);
}
