package main;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

import junit.framework.Test;

import model.ClassifyResult;
import model.DocumentVector;
import model.FeatureTerm;
import model.MyTerm;
import model.SearchResult;
import model.SimilarityRate;
import model.WeightTerm;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.FSDirectory;

import result.ResultParser;
import similarity.CosineDistance;
import similarity.Distance;
import similarity.MahalanobisDistance;
import similarity.Similarity;
import svm.SVMClassifier;
import svm.SVMPreprocessor;
import template.GaussianTemplate;
import template.MeanTemplate;
import template.MedianTemplate;
import template.Template;
import util.VectorImageBuilder;
import vector.VectorBuilder;
import weight.CDDWeighter;
import weight.FeatureWeighter;
import weight.LTFIDFWeighter;
import weight.TFIDFWeighter;
import weight.Weighter;
import classifier.CenterClassifier;
import classifier.CenterClassifier.CENTERCLASSIFIER_INIT_TYPE;
import classifier.Classifier;
import classifier.DDKNNClassifier;
import classifier.KNNClassifier;
import classifier.NativeBayesClassifier;
import constants.Constants;
import constants.Constants.FILTER_THRESHOLD_TYPE;
import constants.Constants.KEYWORD_TYPE;
import constants.Constants.TEMPLATE_THRESHOLD_TYPE;
import controller.Preprocessor;
import controller.Processor;
import controller.Searcher;
import controller.Storer;
import filter.CHIFilter;
import filter.DFFilter;
import filter.IGFilter;
import filter.MIFilter;
import filter.CDDFilter;
import filter.OddsRatioFilter;
import filter.RandomFilter;
  
public   class  MainClass {  
    public   static   void  main(String[] args)  throws  Exception {  
//    	preprocessData();
    	
//    	Preprocessor.loadSettings();
//    	Processor processer = new Processor();
    	
//    	analyzeCoocourence(processer);
    	
//    	int doc = 116;
//    	printNameOfDoc(doc);
    	
//      analyzeKeyword(processer, Constants.KEYWORD_TYPE.KEYWORD_DLH);
//      analyzeKeyword(doc, processer, Constants.KEYWORD_TYPE.KEYWORD_BM25);
//    	analyzeKeyword(processer, Constants.KEYWORD_TYPE.KEYWORD_TFIDFSCORE);
        
//    	analyzeKeywordPositions(processer);
    	
//    	searchForQuery("nba", 10);
    	
    	classifyFiles();
    	
//    	runSVMClassify();
    	
//    	runDimensionDiscretizationKNN();
    	
//    	runDifferentFilterClassify();
    	
    }

	public static void classifyFiles() throws CorruptIndexException,
			Exception {
		HashMap<String, ArrayList<FeatureTerm>> classifierTermsaArrayListHashMap = null;
    	classifierTermsaArrayListHashMap = (new CHIFilter()).processFilesToFeatureTermsHashMap(Constants.CLASSIFIER_TRAINFILE_PATH);
    	Storer.storeFeatureMapToFilePath(classifierTermsaArrayListHashMap, Constants.CLASSIFIER_FEATUREFILE_PATH);
    	
    	classifierTermsaArrayListHashMap = Storer.loadFeatureMapFromFilePath(Constants.CLASSIFIER_FEATUREFILE_PATH);
    	
    	Weighter weightor = new TFIDFWeighter();
    
//    	VectorImageBuilder vectorImageBuilder = new VectorImageBuilder(Constants.CLASSIFIER_DOCPIC_PATH
//    																	, classifierTermsaArrayListHashMap);
    	
    	int tPart = 0;
    	int time = 10;

    	for(int i = 0; i < time; i++) {
			tPart += Constants.filiterNumberThreshold/time;
			VectorBuilder vectorBuilder = new VectorBuilder(classifierTermsaArrayListHashMap, tPart, FILTER_THRESHOLD_TYPE.FILTER_THRESHOLD_TYPE_MAX, weightor);
			vectorBuilder.setNormalization(true);
//			IndexReader testIndexReader = IndexReader.open(FSDirectory.open(new File(Constants.CINDEX_TESTSTORE_PATH)));
			IndexReader testIndexReader = Preprocessor.preprocessClassifyFiles(Constants.CLASSIFIER_TESTFILE_PATH, Constants.CINDEX_TESTSTORE_PATH, null, null);
//			IndexReader trainIndexReader = IndexReader.open(FSDirectory.open(new File(Constants.CINDEX_TRAINSTORE_PATH)));
			IndexReader trainIndexReader = Preprocessor.preprocessClassifyFiles(Constants.CLASSIFIER_TRAINFILE_PATH, Constants.CINDEX_TRAINSTORE_PATH, null, null);
			
			ArrayList<DocumentVector> testVectors = vectorBuilder.getDocumentVectorsFromReader(testIndexReader, Constants.CLASSIFIER_TESTDOCUMENTVECTORFILE_PATH);
//			ArrayList<DocumentVector> testVectors = vectorBuilder.getDocumentVectorsFromFilePath(Constants.CLASSIFIER_TESTDOCUMENTVECTORFILE_PATH);
			ArrayList<DocumentVector> trainVectors = vectorBuilder.getDocumentVectorsFromReader(trainIndexReader, Constants.CLASSIFIER_TRAINDOCUMENTVECTORFILE_PATH);
//			ArrayList<DocumentVector> trainVectors = vectorBuilder.getDocumentVectorsFromFilePath(Constants.CLASSIFIER_TRAINDOCUMENTVECTORFILE_PATH);

            Distance distance = new CosineDistance();
			
//			Distance distance = new MahalanobisDistance(vectorBuilder.getFeatureTermMap(), trainVectors);
			
//			Template template = new GaussianTemplate(Constants.CLASSIFIER_SIMILARITYFILE_PATH, vectorBuilder.getFeatureTermMap());
			Template template = new MeanTemplate(trainVectors, false, vectorBuilder.getFeatureTermMap(), distance);
			HashMap<Integer, SimilarityRate> similarityRateHashMap = Similarity.getSimilarityRate(trainVectors, template, distance);
			template.similarityRateHashMap = similarityRateHashMap;
//			template.setJustProcessBros(false);
			template.setThresholdTpye(TEMPLATE_THRESHOLD_TYPE.TEMPLATE_BRO);
			template.processTemplateOnTrainDocumentVector(trainVectors, false);
			
//			template = new GaussianTemplate(testVectors, false, vectorBuilder.getFeatureTermMap());
//			template.processTemplateOnTrainDocumentVector(testVectors, false);
			
//			HashMap<Integer, HashMap<Integer, Double>> simHashMap = Processor.getSimilarityMap(trainVectors, SIMILARITY_TYPE.SIMILARITY_TYPE_COS);
//			Storer.saveSimilarityToFile(Constants.CLASSIFIER_SIMILARITYFILE_PATH, simHashMap);
//			ArrayList<String> termsOrder = vectorBuilder.getTermsOrder();
//			vectorImageBuilder.saveVectorsAsBitmap(trainVectors, termsOrder);
			
			Date startDate = new Date();
			Classifier classifier = new KNNClassifier(Constants.K, distance);
//			Classifier classifier = new CenterClassifier();
//			Classifier classifier = new FastKNNClassifier(Constants.K, Storer.loadSimilarityHashMap(Constants.CLASSIFIER_SIMILARITYFILE_PATH));
			ArrayList<ClassifyResult> classifyResults = classifier.classifyFiles(testVectors, trainVectors);
			Date endDate = new Date();
			System.out.println("分类时间：" + (endDate.getTime() - startDate.getTime()));
			
			ResultParser.parseResult(classifyResults, true);
		}
	}
	
	public static void runDifferentFilterClassify() throws CorruptIndexException,
	Exception {
		HashMap<String, ArrayList<FeatureTerm>> classifierTermsaArrayListHashMap = null;
    	classifierTermsaArrayListHashMap = (new OddsRatioFilter()).processFilesToFeatureTermsHashMap(Constants.CLASSIFIER_TRAINFILE_PATH);
    	Storer.storeFeatureMapToFilePath(classifierTermsaArrayListHashMap, Constants.CLASSIFIER_FEATUREFILE_PATH);
    	
    	classifierTermsaArrayListHashMap = Storer.loadFeatureMapFromFilePath(Constants.CLASSIFIER_FEATUREFILE_PATH);
    	
    	Weighter weightor = new FeatureWeighter();//权重计算方式
    	
    	int tPart = 0;
    	int time = 10;
		for(int i = 0; i < time; i++) {
			tPart += Constants.filiterNumberThreshold/time;
			VectorBuilder vectorBuilder = new VectorBuilder(classifierTermsaArrayListHashMap, tPart, FILTER_THRESHOLD_TYPE.FILTER_THRESHOLD_TYPE_EACH, weightor);
			vectorBuilder.setNormalization(true);
//			IndexReader testIndexReader = IndexReader.open(FSDirectory.open(new File(Constants.CINDEX_TESTSTORE_PATH)));
			IndexReader testIndexReader = Preprocessor.preprocessClassifyFiles(Constants.CLASSIFIER_TESTFILE_PATH, Constants.CINDEX_TESTSTORE_PATH, null, null);
//			IndexReader trainIndexReader = IndexReader.open(FSDirectory.open(new File(Constants.CINDEX_TRAINSTORE_PATH)));
			IndexReader trainIndexReader = Preprocessor.preprocessClassifyFiles(Constants.CLASSIFIER_TRAINFILE_PATH, Constants.CINDEX_TRAINSTORE_PATH, null, null);
			
			ArrayList<DocumentVector> testVectors = vectorBuilder.getDocumentVectorsFromReader(testIndexReader, Constants.CLASSIFIER_TESTDOCUMENTVECTORFILE_PATH);
//			ArrayList<DocumentVector> testVectors = vectorBuilder.getDocumentVectorsFromFilePath(Constants.CLASSIFIER_TESTDOCUMENTVECTORFILE_PATH);
			ArrayList<DocumentVector> trainVectors = vectorBuilder.getDocumentVectorsFromReader(trainIndexReader, Constants.CLASSIFIER_TRAINDOCUMENTVECTORFILE_PATH);
//			ArrayList<DocumentVector> trainVectors = vectorBuilder.getDocumentVectorsFromFilePath(Constants.CLASSIFIER_TRAINDOCUMENTVECTORFILE_PATH);
			
//			Classifier classifier = new NativeBayesClassifier(classifierTermsaArrayListHashMap, trainVectors);
//			Classifier classifier = new KNNClassifier(Constants.K, SIMILARITY_TYPE.SIMILARITY_TYPE_COS);
			Classifier classifier = new CenterClassifier(CENTERCLASSIFIER_INIT_TYPE.CENTERCLASSIFIER_INIT_SAVE, Constants.CLASSIFIER_CENTERFILE_PATH);
			Date startDate = new Date();
			ArrayList<ClassifyResult> classifyResults = classifier.classifyFiles(testVectors, trainVectors);
			Date endDate = new Date();
			System.out.println("分类时间：" + (endDate.getTime() - startDate.getTime()));
			
			ResultParser.parseResult(classifyResults, true);
		}
	}
	
	public static void runDimensionDiscretizationKNN() throws Exception, IOException {
		HashMap<String, ArrayList<FeatureTerm>> classifierTermsaArrayListHashMap = null;
//    	classifierTermsaArrayListHashMap = (new Preprocessor()).processFilesToFeatureTermsHashMap(Constants.CLASSIFIER_TRAINFILE_PATH, FILTER_TYPE.FILTER_TYPE_CHI);
//    	Storer.storeFeatureMapToFilePath(classifierTermsaArrayListHashMap, Constants.CLASSIFIER_FEATUREFILE_PATH);
    	
    	classifierTermsaArrayListHashMap = Storer.loadFeatureMapFromFilePath(Constants.CLASSIFIER_FEATUREFILE_PATH);
    	
    	Weighter weightor = new LTFIDFWeighter();//权重计算方式
    	
    	int begin = 0;
    	int tPart = begin;
    	int time = 10;
    	
		for(int i = 0; i < time; i++) {
			tPart += (Constants.filiterNumberThreshold - begin)/time;
			VectorBuilder vectorBuilder = new VectorBuilder(classifierTermsaArrayListHashMap, tPart, FILTER_THRESHOLD_TYPE.FILTER_THRESHOLD_TYPE_MAX, weightor);
			vectorBuilder.setNormalization(false);
			IndexReader testIndexReader = IndexReader.open(FSDirectory.open(new File(Constants.CINDEX_TESTSTORE_PATH)));
//			IndexReader testIndexReader = Preprocessor.preprocessClassifyFiles(Constants.CLASSIFIER_TESTFILE_PATH, Constants.CINDEX_TESTSTORE_PATH, null, null);
			IndexReader trainIndexReader = IndexReader.open(FSDirectory.open(new File(Constants.CINDEX_TRAINSTORE_PATH)));
//			IndexReader trainIndexReader = Preprocessor.preprocessClassifyFiles(Constants.CLASSIFIER_TRAINFILE_PATH, Constants.CINDEX_TRAINSTORE_PATH, null, null);
			
			ArrayList<DocumentVector> testVectors = vectorBuilder.getDocumentVectorsFromReader(testIndexReader, Constants.CLASSIFIER_TESTDOCUMENTVECTORFILE_PATH);
//			ArrayList<DocumentVector> testVectors = vectorBuilder.getDocumentVectorsFromFilePath(Constants.CLASSIFIER_TESTDOCUMENTVECTORFILE_PATH);
			ArrayList<DocumentVector> trainVectors = vectorBuilder.getDocumentVectorsFromReader(trainIndexReader, Constants.CLASSIFIER_TRAINDOCUMENTVECTORFILE_PATH);
//			ArrayList<DocumentVector> trainVectors = vectorBuilder.getDocumentVectorsFromFilePath(Constants.CLASSIFIER_TRAINDOCUMENTVECTORFILE_PATH);
			
//			HashMap<Integer, HashMap<Integer, Double>> simHashMap = Processor.getSimilarityMap(trainVectors, SIMILARITY_TYPE.SIMILARITY_TYPE_EUCLIDEAN);
//			Storer.saveSimilarityToFile(Constants.CLASSIFIER_SIMILARITYFILE_PATH, simHashMap);
			
			DDKNNClassifier classifier = new DDKNNClassifier(trainVectors, vectorBuilder.getFeatureTermMap(), Constants.K, Constants.DDKNNStep, Constants.CLASSIFIER_DDKNNFILE_PATH);
//			DDKNNClassifier classifier = new DDKNNClassifier(Constants.K, Constants.DDKNNStep, Constants.CLASSIFIER_DDKNNFILE_PATH, vectorBuilder.getFeatureTermMap());
//			classifier.setSimilarHashMap(Storer.loadSimilarityHashMap(Constants.CLASSIFIER_SIMILARITYFILE_PATH));
			Date startDate = new Date();
			ArrayList<ClassifyResult> classifyResults = classifier.classifyFiles(testVectors, trainVectors);
			Date endDate = new Date();
			System.out.println("分类时间：" + (endDate.getTime() - startDate.getTime()));
			
			ResultParser.parseResult(classifyResults, true);
		}
	}
	
	public static void runSVMClassify() throws Exception {
		HashMap<String, ArrayList<FeatureTerm>> classifierTermsaArrayListHashMap = null;
//    	classifierTermsaArrayListHashMap = (new OddsRatioFilter()).processFilesToFeatureTermsHashMap(Constants.CLASSIFIER_TRAINFILE_PATH);
//    	Storer.storeFeatureMapToFilePath(classifierTermsaArrayListHashMap, Constants.CLASSIFIER_FEATUREFILE_PATH);
    	
    	classifierTermsaArrayListHashMap = Storer.loadFeatureMapFromFilePath(Constants.CLASSIFIER_FEATUREFILE_PATH);
		
    	Weighter weighter = new FeatureWeighter();//权重计算方式
    	
    	int begin = 0;
    	int tPart = begin;
    	int time = 1;
    	
		for(int i = 0; i < time; i++) {
			tPart += (Constants.filiterNumberThreshold - begin)/time;
			VectorBuilder vectorBuilder = new VectorBuilder(classifierTermsaArrayListHashMap, tPart, FILTER_THRESHOLD_TYPE.FILTER_THRESHOLD_TYPE_EACH, weighter);
			IndexReader testIndexReader = Preprocessor.preprocessClassifyFiles(Constants.CLASSIFIER_TESTFILE_PATH, Constants.CINDEX_TESTSTORE_PATH, null, null);
			IndexReader trainIndexReader = Preprocessor.preprocessClassifyFiles(Constants.CLASSIFIER_TRAINFILE_PATH, Constants.CINDEX_TRAINSTORE_PATH, null, null);
//			IndexReader testIndexReader = IndexReader.open(FSDirectory.open(new File(Constants.CINDEX_TESTSTORE_PATH)));
//			IndexReader trainIndexReader = IndexReader.open(FSDirectory.open(new File(Constants.CINDEX_TRAINSTORE_PATH)));
			ArrayList<DocumentVector> testVectors = vectorBuilder.getDocumentVectorsFromReader(testIndexReader, Constants.CLASSIFIER_TESTDOCUMENTVECTORFILE_PATH);
			ArrayList<DocumentVector> trainVectors = vectorBuilder.getDocumentVectorsFromReader(trainIndexReader, Constants.CLASSIFIER_TRAINDOCUMENTVECTORFILE_PATH);
//			Template template = new MedianTemplate(trainVectors, true, vectorBuilder.getFeatureTermMap());
	//		Template template = new GaussianTemplate(Constants.CLASSIFIER_SIMILARITYFILE_PATH, vectorBuilder.getFeatureTermMap());
//			template.processTemplateOnTrainDocumentVector(trainVectors, false);
			SVMPreprocessor.processDocumentVectorToSVMFormatFile(vectorBuilder.getFeatureTermMap(), testVectors, Constants.CLASSIFIER_SVM_TESTFILE_PATH);
			SVMPreprocessor.processDocumentVectorToSVMFormatFile(vectorBuilder.getFeatureTermMap(), trainVectors, Constants.CLASSIFIER_SVM_TRAINFILE_PATH);
		
			Classifier classifier = new SVMClassifier();
			ArrayList<ClassifyResult> classifyResults = classifier.classifyFiles(testVectors, trainVectors);
			ResultParser.parseResult(classifyResults, true);
		}
	}

	public static void searchForQuery(String queryString, int limit) throws Exception,
			CorruptIndexException, IOException {
		Preprocessor.loadSettings();
		Processor processer = new Processor();
		
		Searcher searcher = new Searcher(Processor.shareIndexReader());
    	ArrayList<SearchResult> searchResults = searcher.searchForQuery(queryString, limit);
    	for (SearchResult searchResult : searchResults) {
    		System.out.println(Processor.shareIndexReader().document(searchResult.doc).get("filename"));
			System.out.println(searchResult.doc + "|" + searchResult.score);
			
			analyzeKeyword(searchResult.doc, processer, KEYWORD_TYPE.KEYWORD_BM25);
		}
	}

	public static void preprocessData() throws Exception {
    	Preprocessor indexer = new  Preprocessor(Constants.INDEX_STORE_PATH);  
        Date start = new  Date();
        indexer.indexFilesFromPath(Constants.TEST_FILE_PATH);
        Date end = new  Date();
        System.out.println("preprocess time:"  + (end.getTime() - start.getTime()) +  " seconds" );  
        indexer.close();
	}

	public static void analyzeCoocourence(Processor processer) throws Exception {
        ArrayList<MyTerm> termsArrayList = processer.myTermsArrayFromIndexReader(Processor.shareIndexReader());
        processer.calculateRelevanceBetweenTwoTermsForMyTermArray(termsArrayList);
        Storer.storeRelevanceBetweenTwoTermsToFile(termsArrayList, Constants.DATA_RESULT_PATH);
	}

	public static void analyzeKeyword(int doc, Processor processer, Constants.KEYWORD_TYPE type) throws IOException {
        ArrayList<WeightTerm> weightArrayList = processer.sortedTermWeightArrayOfDoc(doc, Processor.shareIndexReader(), type);
        
        int limit = 10;
        int i = 0;
        for (WeightTerm weightTerm : weightArrayList) {
        	if (i < limit) {
        		System.out.println(weightTerm.toString());
				i++;
			}
		}
	}

	public static void printNameOfDoc(int doc) throws IOException {
        TermEnum docTermEnum = Processor.shareIndexReader().terms(new Term("filename"));
		int index = 0;
		while (docTermEnum.next()) {
			if (index >= doc) {
				break;
			}
			index++;
		}
		System.out.println(docTermEnum.term().text());
		System.out.println("indexReader.numDocs()" + Processor.shareIndexReader().numDocs());
	}

	public static void analyzeKeywordPositions(Processor processer) throws IOException {
		String keyword = "生物医药";
		int doc = 45;
		
		TermEnum docTermEnum = Processor.shareIndexReader().terms(new Term("year"));
		int index = 0;
		while (docTermEnum.next()) {
			if (index >= doc) {
				break;
			}
			index++;
		}
		System.out.println(docTermEnum.term().text());
		
		ArrayList<Integer> resultList = processer.distributionOfTermInDoc(new Term("content", keyword), doc, Processor.shareIndexReader());
	
		if (resultList != null) {
			for (Integer integer : resultList) {
				System.out.println(integer);
			}
		}
	}

	
}
