package template;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import model.DocumentVector;
import model.FeatureTerm;
import model.ObjectPair;
import model.SimilarityRate;
import similarity.Distance;
import similarity.Similarity;
import constants.Constants;
import constants.Constants.TEMPLATE_THRESHOLD_TYPE;
import controller.Storer;

public abstract class Template {
	
	public double threshold;
	private boolean justProcessBros;
	public TEMPLATE_THRESHOLD_TYPE thresholdTpye;
	ArrayList<Double> template;
	public HashMap<Integer, SimilarityRate> similarityRateHashMap;
	private Double[][] similarHashMap;
	protected HashMap<String, FeatureTerm> featureTermHashMap;
	
	public Template(ArrayList<DocumentVector> trainArrayList, boolean isSaveToFile, HashMap<String, FeatureTerm> featureTermHashMap, Distance distance) {
		this.setSimilarHashMap(Similarity.getSimilarityMap(trainArrayList, distance));
		this.featureTermHashMap = featureTermHashMap;
		template = this.getTemplate(Constants.TemplateRadios);
		this.threshold = Constants.TemplateSimilarityThreshold;
		this.setJustProcessBros(true);
		this.thresholdTpye = TEMPLATE_THRESHOLD_TYPE.TEMPLATE_NORMAL;
		if (isSaveToFile) {
			Storer.saveSimilarityToFile(Constants.CLASSIFIER_SIMILARITYFILE_PATH, this.getSimilarHashMap());
		}
	}
	

	public Template(String similarityFilePath, HashMap<String, FeatureTerm> featureTermHashMap) {
		this.featureTermHashMap = featureTermHashMap;
		this.setSimilarHashMap(Storer.loadSimilarityHashMap(similarityFilePath));
		template = this.getTemplate(Constants.TemplateRadios);
		this.threshold = Constants.TemplateSimilarityThreshold;
		this.setJustProcessBros(true);
		this.thresholdTpye = TEMPLATE_THRESHOLD_TYPE.TEMPLATE_NORMAL;
	}

	public void processTemplateOnTrainDocumentVector(
			ArrayList<DocumentVector> trainArrayList, boolean isReplaceVectorFile) {
		
		HashMap<Integer, DocumentVector> documentHashMap = new HashMap<Integer, DocumentVector>();
		ArrayList<DocumentVector> tempDocumentVectors = new ArrayList<DocumentVector>(); //copy
		for (DocumentVector documentVector : trainArrayList) {
			DocumentVector copyDocumentVector = new DocumentVector(documentVector);
			tempDocumentVectors.add(copyDocumentVector);
		}
		for (DocumentVector documentVector : tempDocumentVectors) {
			documentHashMap.put(documentVector.getDoc(), documentVector);
		}
		
		
		System.out.println("开始应用模版：");
		for (DocumentVector documentVector : trainArrayList) {//相似度排序
			int doc1 = documentVector.getDoc();
			ArrayList<ObjectPair<Integer, Double>> simEntries = new ArrayList<ObjectPair<Integer, Double>>();
			Double[][] simM = this.getSimilarHashMap();
			for (DocumentVector documentVector2: trainArrayList) {
				int doc2 = documentVector2.getDoc();
				simEntries.add(new ObjectPair<Integer, Double>(doc2, simM[doc1][doc2]));
			}
			if (this.isJustProcessBros()) {
				for (int i = 0; i < simEntries.size(); i++) {
					DocumentVector d = documentHashMap.get(simEntries.get(i).getOne());
					if(d != null)
						if(!documentVector.getClassName().equals(d.getClassName())) {
							simEntries.remove(i);
						}
				}
			}
			
			Collections.sort(simEntries, new Comparator<ObjectPair<Integer, Double>>() {
				@Override
				public int compare(ObjectPair<Integer, Double> o1,
						ObjectPair<Integer, Double> o2) {
					if ((Double)o1.getTwo() > (Double)o2.getTwo()) {
						return -1;
					}else if ((Double)o1.getTwo() < (Double)o2.getTwo()) {
						return 1;
					}else {
						return 0;
					}
				}
			});
			
			double threshold = this.threshold;
			if (this.similarityRateHashMap != null) {
				switch (this.thresholdTpye) {
				case TEMPLATE_NORMAL:
					break;
				case TEMPLATE_BRO:
					threshold = similarityRateHashMap.get(documentVector.getDoc()).getBroInRange();
					break;
				case TEMPLATE_FIRST:
					threshold = similarityRateHashMap.get(documentVector.getDoc()).getFirstMetPos();
					break;
				case TEMPLATE_BROEQUALFIRST:
						double firstDouble = similarityRateHashMap.get(documentVector.getDoc()).getFirstMetPos();
						double broDouble = similarityRateHashMap.get(documentVector.getDoc()).getBroInRange();
		                if (Math.abs(broDouble - firstDouble) <= 0.0001) {
		                	threshold = firstDouble;
		    			}
					break;
	
				default:
					break;
				}
			}
			System.out.println("迭代完毕，阈值取" + threshold);
			this.templating(documentHashMap, documentVector, simEntries, threshold);
		}
		
		if (isReplaceVectorFile) {
			Storer.storeDocumentVectorToFilePath(trainArrayList, Constants.CLASSIFIER_TRAINDOCUMENTVECTORFILE_PATH);
		}
		
		System.out.println("应用模版完毕.");
	}

	abstract public ArrayList<Double> getTemplate(int templateradios); 

	abstract protected void templating(HashMap<Integer, DocumentVector> documentHashMap,
			DocumentVector documentVector,
			ArrayList<ObjectPair<Integer, Double>> simEntries,
			double threshold); 

	public Double[][] getSimilarHashMap() {
		return similarHashMap;
	}

	public void setSimilarHashMap(Double[][] similarHashMap) {
		this.similarHashMap = similarHashMap;
	}
	
	public boolean isJustProcessBros() {
		return justProcessBros;
	}

	public void setJustProcessBros(boolean justProcessBros) {
		this.justProcessBros = justProcessBros;
	}

	public TEMPLATE_THRESHOLD_TYPE getThresholdTpye() {
		return thresholdTpye;
	}

	public void setThresholdTpye(TEMPLATE_THRESHOLD_TYPE thresholdTpye) {
		this.thresholdTpye = thresholdTpye;
	}

}
