package constants;

import java.io.File;
import java.math.BigDecimal;

public class Constants {
	public static final String DATA_FILE_PATH =  "." + File.separator + "data" + File.separator + "data" ;  
	public static final String INDEX_STORE_PATH =  "." + File.separator + "data" + File.separator + "index" ;
	public static final String DATA_RESULT_PATH = "." + File.separator + "data" + File.separator + "occourence.txt";
	public static final String TEST_FILE_PATH = "." + File.separator + "data" + File.separator + "utf8";
	public static final String SETTINGS_FILE_PATH = "." + File.separator + "data" + File.separator + "settings.txt";
	
	public static final String CLASSIFIER_TERMFILE_PATH = "." + File.separator + "data" + File.separator + "classifier_term.txt";
	public static final String CLASSIFIER_VECTORFILE_PATH = "." + File.separator + "data" + File.separator + "classifier_vector.txt";
	public static final String CINDEX_TESTSTORE_PATH =  "." + File.separator + "data" + File.separator + "cTestIndex";
	public static final String CINDEX_TRAINSTORE_PATH =  "." + File.separator + "data" + File.separator + "cTrainIndex";
	public static final String CLASSIFIER_TRAINFILE_PATH =  "." + File.separator + "data" + File.separator + "cdata" + File.separator + "r8-train";
	public static final String CLASSIFIER_TESTFILE_PATH =  "." + File.separator + "data" + File.separator + "cdata" + File.separator + "r8-test";
	public static final String CLASSIFIER_TRAINDOCUMENTVECTORFILE_PATH =  "." + File.separator + "data" + File.separator + "cdata" + File.separator + "trainDocument.txt";
	public static final String CLASSIFIER_TESTDOCUMENTVECTORFILE_PATH =  "." + File.separator + "data" + File.separator + "cdata" + File.separator + "testDocument.txt";
	public static final String CLASSIFIER_FEATUREFILE_PATH =  "." + File.separator + "data" + File.separator + "cdata" + File.separator + "feature.txt" ;
	public static final String CLASSIFIER_RESULTFILE_PATH =  "." + File.separator + "data" + File.separator + "cdata" + File.separator + "result.txt" ;
	public static final String CLASSIFIER_SIMILARITYFILE_PATH =  "." + File.separator + "data" + File.separator + "cdata" + File.separator + "similarity.txt" ;
	public static final String CLASSIFIER_SVM_TRAINFILE_PATH =  "." + File.separator + "data" + File.separator + "cdata" + File.separator + "svm" + File.separator + "train.txt";
	public static final String CLASSIFIER_SVM_TESTFILE_PATH =  "." + File.separator + "data" + File.separator + "cdata" + File.separator + "svm" + File.separator + "test.txt";
	public static final String CLASSIFIER_SVM_MODELFILE_PATH =  "." + File.separator + "data" + File.separator + "cdata" + File.separator + "svm" + File.separator + "model.txt";
	public static final String CLASSIFIER_SVM_RESULTFILE_PATH =  "." + File.separator + "data" + File.separator + "cdata" + File.separator + "svm" + File.separator + "result.txt";
	
	public static final String CLASSIFIER_CENTERFILE_PATH =  "." + File.separator + "data" + File.separator + "cdata" + File.separator + "center.txt" ;
	
	public static final String CLASSIFIER_DDKNNFILE_PATH =  "." + File.separator + "data" + File.separator + "cdata" + File.separator + "discretization.txt" ;

	public static final String CLASSIFIER_DOCPIC_PATH =  "." + File.separator + "data" + File.separator + "cdata" + File.separator + "pic";
	
	public static final int minOccourence = 10;
	public static final int minDocFreq = 10;
   	public static final int minTremFreq = 2;
   	public static final int minTremLength = 2;
   	public static final int topNWeightTerm = 10;
   
   	public static final double a = 1.2;
   	public static final double b = 0.5;
   	public static final double c = 0.5;
   	public static final double d = 0.5;
   	
   	public enum TEMPLATE_THRESHOLD_TYPE {
   		TEMPLATE_NORMAL,
   		TEMPLATE_BRO,
   		TEMPLATE_FIRST,
   		TEMPLATE_BROEQUALFIRST
   	}
   	
   	public enum KEYWORD_TYPE{
   		KEYWORD_TFIDFSCORE,
   		KEYWORD_MAXENTROPY,
   		KEYWORD_BM25,
   		KEYWORD_DLH
   	}
   	
   	public enum FILTER_THRESHOLD_TYPE{
   		FILTER_THRESHOLD_TYPE_MAX,
   		FILTER_THRESHOLD_TYPE_EACH
   	}
   	
   	public static final int filiterNumberThreshold = 4000;
   	public static final int K = 10;
   	
   	public static final double TemplateSimilarityThreshold = 0.0;
   	public static final double GaussianTemplate6 = 1.2;
   	public static final int TemplateRadios = 5;
   	public static final int BroRange = 9;
   	
   	public static final double FastKNN6 = 1.4;
   	public static final double FastKNNt = 2.9;
   	
   	public static final double DDKNNStep = 1.0;
   	public static final BigDecimal minScope = new BigDecimal("0.0");
   	public static final BigDecimal maxScope = new BigDecimal("10.0");
   	public static final BigDecimal beginRadius = new BigDecimal("0.0");
}


