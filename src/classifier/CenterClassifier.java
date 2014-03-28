package classifier;

import java.util.ArrayList;
import java.util.HashMap;

import similarity.CosineDistance;
import similarity.Distance;
import similarity.Similarity;

import model.ClassifyResult;
import model.DocumentVector;
import controller.Scorer;
import controller.Storer;

public class CenterClassifier extends Classifier {
	
	public enum CENTERCLASSIFIER_INIT_TYPE{
		CENTERCLASSIFIER_INIT_LOAD,
		CENTERCLASSIFIER_INIT_SAVE
   	}
	
	private CENTERCLASSIFIER_INIT_TYPE initType;
	private String saveFilePath;
	
	public CenterClassifier() {
		this.initType = CENTERCLASSIFIER_INIT_TYPE.CENTERCLASSIFIER_INIT_LOAD;
		saveFilePath = null;
	}
	
	public CenterClassifier(CENTERCLASSIFIER_INIT_TYPE type, String saveFilePath){
		super();
		this.initType = type;
		this.saveFilePath = saveFilePath;
	}

	@Override
	protected ArrayList<ClassifyResult> classify(
			ArrayList<DocumentVector> testTrainArrayList,
			ArrayList<DocumentVector> trainArrayList) {

		HashMap<String, DocumentVector> centerVectorHashMap = null;
		if (this.saveFilePath != null) {
			switch (this.initType) {
			case CENTERCLASSIFIER_INIT_LOAD:
			{
				centerVectorHashMap = this.loadCenterVectorMapFromFile(this.saveFilePath);
			}
				break;
			case CENTERCLASSIFIER_INIT_SAVE:
			{
				centerVectorHashMap = this.getCenterVectorMapFromDocuments(trainArrayList);
				this.saveCenterVector(centerVectorHashMap, this.saveFilePath);
			}
				break;
			default:
				break;
			}
		}else {
			centerVectorHashMap = this.getCenterVectorMapFromDocuments(trainArrayList);
		}
		
		ArrayList<ClassifyResult> resultArrayList = new ArrayList<ClassifyResult>();
		Distance distance = new CosineDistance();
		for (DocumentVector documentVector : testTrainArrayList) {
			ClassifyResult result = new ClassifyResult();
			double maxSim = -1.0;
			String maxClassNameString = null;
			
			for (DocumentVector centerVector : centerVectorHashMap.values()) {
				double sim = distance.getSimilarityBetweenDocuments(centerVector, documentVector);
				if (maxSim < sim) {
					maxSim = sim;
					maxClassNameString = centerVector.getClassName();
				}
			}
			
			result.setOriginalClassString(documentVector.getClassName());
			result.setClassString(maxClassNameString);
			result.setDoc(documentVector.getDoc());
			resultArrayList.add(result);
		}
		
		return resultArrayList;
	}
	
	public HashMap<String, DocumentVector> getCenterVectorMapFromDocuments(ArrayList<DocumentVector> documentVectors){
		HashMap<String, DocumentVector> resultHashMap = new HashMap<String, DocumentVector>();
		
		HashMap<String, Integer> classVectorCountHashMap = new HashMap<String, Integer>();
		for (DocumentVector documentVector : documentVectors) {
			String className = documentVector.getClassName();
			DocumentVector resultVector = resultHashMap.get(className);
			if (resultVector == null) {
				resultVector = new DocumentVector();
				resultVector.setDoc(-1);
				resultVector.setClassName(className);
				resultHashMap.put(className, resultVector);
			}
			for (String term : documentVector.getTerms()) {
				Double weight = resultVector.getTermWeight(term);
				if (weight == null) {
					 weight = 0.0;
				};
				resultVector.setTermWeight(term, weight + documentVector.getTermWeight(term));
			}
			
			Integer count = classVectorCountHashMap.get(className);
			if (count == null) {
				count = 0;
			}
			classVectorCountHashMap.put(className, count + 1);
		}
		for (String className : resultHashMap.keySet()) {
			DocumentVector resultVector = resultHashMap.get(className);
			Integer count = classVectorCountHashMap.get(className);
			for (String term : resultVector.getTerms()) {
				double weight = resultVector.getTermWeight(term)/count;
				resultVector.setTermWeight(term, weight);
			}
		}
		
		System.out.println("中心向量已生成");
		
		return resultHashMap;
	}
	
	public HashMap<String, DocumentVector> loadCenterVectorMapFromFile(String filePath) {
		ArrayList<DocumentVector> documentVectors = Storer.loadDocumentVectorsFromFilePath(filePath);
		HashMap<String, DocumentVector> resultHashMap = new HashMap<String, DocumentVector>();
		for (DocumentVector documentVector : documentVectors) {
			resultHashMap.put(documentVector.getClassName(), documentVector);
		}
		System.out.println("中心向量已载入：" + documentVectors.size());
		return resultHashMap;
	}
	
	public void saveCenterVector(HashMap<String, DocumentVector> centerVectorMap, String filePath){
		ArrayList<DocumentVector> documentVectors = new ArrayList<DocumentVector>();
		documentVectors.addAll(centerVectorMap.values());
		Storer.storeDocumentVectorToFilePath(documentVectors, filePath);
		System.out.println("中心向量已保存：" + filePath);
	}
}
