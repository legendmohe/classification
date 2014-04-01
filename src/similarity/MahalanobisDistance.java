package similarity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import Jama.Matrix;

import model.DocumentVector;
import model.FeatureTerm;
import model.ObjectPair;

public class MahalanobisDistance implements Distance {
	
	private Matrix covarianceMatrix;
	private Matrix documentMatrix;
	private HashMap<Integer, Integer> documentIndexHashMap;
	
	public MahalanobisDistance(HashMap<String, FeatureTerm> featureTermHashMap, List<DocumentVector> vectors) {
		System.out.println("加载协方差矩阵：" +  vectors.size() + "*" + vectors.size());
		
		double[][] covM = this.CovarianceMatrix(featureTermHashMap, vectors);
		this.covarianceMatrix = new Matrix(covM).inverse(); //逆矩阵
		
		documentIndexHashMap = new HashMap<Integer, Integer>();
		for (int i = 0; i < vectors.size(); i++) {
			documentIndexHashMap.put(vectors.get(i).getDoc(), i);
		}
		
		System.out.println("协方差矩阵加载完毕");
	}

	@Override
	public double getZeroDistance() {
		return 0.0;
	}

	@Override
	public double getSimilarityBetweenDocuments(DocumentVector document1,
			DocumentVector document2) {
		int d1 = this.documentIndexHashMap.get(document1.getDoc());
		int d2 = this.documentIndexHashMap.get(document2.getDoc());
		int dimension = this.documentMatrix.getColumnDimension();
		Matrix m1 = this.documentMatrix.getMatrix(d1, d1, 0, dimension - 1);
		Matrix m2 = this.documentMatrix.getMatrix(d2, d2, 0, dimension - 1);
		Matrix mm = m1.minus(m2);
		Matrix resultMatrix = mm.times(this.covarianceMatrix).times(mm.transpose());
		double sim = resultMatrix.get(0, 0);
//		System.out.println(sim);
		return sim;
	}
	
	private double[][] CovarianceMatrix(HashMap<String, FeatureTerm> featureTermHashMap, List<DocumentVector> vectors) {
		int csize = featureTermHashMap.size();
		int rsize = vectors.size();
		double[] ulist = new double[csize];
		double documentMatrix[][] = new double[rsize][csize];
		double result[][] = new double[csize][csize];
		String[] features = featureTermHashMap.keySet().toArray(new String[0]);
		
		for (int j = 0; j < csize; j++) {
			for (int i = 0; i < rsize; i++) {
				Double weight = vectors.get(i).getTermWeight(features[j]);
				if (weight != null) {
					documentMatrix[i][j] = weight;
					ulist[j] += weight/rsize;
				}
			}
		}
		
		this.documentMatrix = new Matrix(documentMatrix);
		
		for (int i = 0; i < csize; i++) {
			for (int j = 0; j < csize; j++) {
				double sum = 0;
				for (int k = 0; k < csize; k++) {
					sum += documentMatrix[i][k]*documentMatrix[j][k];
				}
				result[i][j] = sum/(rsize - 1);
			}
		}
		return result;
	}

	@Override
	public Comparator<ObjectPair<Integer, Double>> getComparator() {
		return new Comparator<ObjectPair<Integer, Double>>() {
			@Override
			public int compare(ObjectPair<Integer, Double> o1,
					ObjectPair<Integer, Double> o2) {
				if ((Double)o1.getTwo() > (Double)o2.getTwo()) {
					return 1;
				}else if ((Double)o1.getTwo() < (Double)o2.getTwo()) {
					return -1;
				}else {
					return 0;
				}
			}
		};
	}
}
