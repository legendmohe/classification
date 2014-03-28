package similarity;

import model.DocumentVector;

public interface Distance {
	double getZeroDistance();
	public double getSimilarityBetweenDocuments(DocumentVector document1, DocumentVector document2);
}
