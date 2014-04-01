package similarity;

import java.util.Comparator;

import model.DocumentVector;
import model.ObjectPair;

public interface Distance {
	double getZeroDistance();
	Comparator<ObjectPair<Integer, Double>> getComparator();
	public double getSimilarityBetweenDocuments(DocumentVector document1, DocumentVector document2);
}
