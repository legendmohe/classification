package filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import model.FeatureTerm;
import model.ObjectPair;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;

import constants.Constants;
import controller.Preprocessor;

public abstract class Filter {
	public HashMap<String, ArrayList<FeatureTerm>> processFilesToFeatureTermsHashMap(
			String targetFilePath)
			throws CorruptIndexException, IOException {
		
		System.out.println("预处理开始");
    	System.out.println(targetFilePath);
    	System.out.println("------------------");
    	
   
        HashMap<String, ArrayList<FeatureTerm>> resultHashMap = null;
        ArrayList<String> classNameArrayList = new ArrayList<String>();
        HashMap<String, Integer> classFileNumberHashMap = new HashMap<String, Integer>();
        try {
        	
        	IndexReader indexReader = Preprocessor.preprocessClassifyFiles(targetFilePath, Constants.CINDEX_TESTSTORE_PATH, classNameArrayList, classFileNumberHashMap); 
            resultHashMap = new HashMap<String, ArrayList<FeatureTerm>>();//初始化resultMap
            for (String className : classNameArrayList) {//先将arrayList放入hashMap
				resultHashMap.put(className, new ArrayList<FeatureTerm>());
			}
            TermEnum termEnum = indexReader.terms(new Term("content"));//要指定field，但只是跳到开头而已！
            int totalNumberOfTerm = 0;
            while (termEnum.next()) {
            	if (!termEnum.term().field().equals("content")) { //要过滤
					continue;
				}
            	
            	totalNumberOfTerm++;
				TermDocs termDocs = indexReader.termDocs(termEnum.term());
				//统计N11，N01
				HashMap<String, ObjectPair<Integer, Integer>> nCacheHashMap = new HashMap<String, ObjectPair<Integer, Integer>>();
				while (termDocs.next()) {
					Document document = indexReader.document(termDocs.doc());
					String classNameString = document.get("class");
					ObjectPair<Integer, Integer> pair = nCacheHashMap.get(classNameString);
					if (pair == null) {
						Integer N11 = 1;
						Integer N01 = classFileNumberHashMap.get(classNameString) - 1;
						ObjectPair<Integer, Integer> newPair = new ObjectPair<Integer, Integer>(N11, N01);
						nCacheHashMap.put(classNameString, newPair);
					}else {
						pair.setOne(pair.getOne() + 1);
						pair.setTwo(pair.getTwo() - 1);
					}
				}
				
				//生成各类的特征项统计数据,并计算特征筛选值
				for (String className : nCacheHashMap.keySet()) {
					ArrayList<FeatureTerm> resultArrayList = resultHashMap.get(className);//取出当前文档的classifierTermArrayList
					FeatureTerm newTerm = new FeatureTerm();
					newTerm.setText(termEnum.term().text());
					newTerm.setClassName(className);
					ObjectPair<Integer, Integer> pair = nCacheHashMap.get(className);
					newTerm.setN11((Integer) pair.getOne());
					newTerm.setN01((Integer) pair.getTwo());
					newTerm.setN10(termEnum.docFreq() - newTerm.getN11());
					newTerm.setN00(indexReader.numDocs() - termEnum.docFreq() - newTerm.getN01());
					newTerm.setFiliterWeight(this.getFeatureValue(newTerm));
					
					resultArrayList.add(newTerm);
				}
			}
            
            //sort result
            for (String className : resultHashMap.keySet()) {
        		 Collections.sort(resultHashMap.get(className), new Comparator<FeatureTerm>() {
					@Override
					public int compare(FeatureTerm o1, FeatureTerm o2) {
						if (o1.getFiliterWeight() > o2.getFiliterWeight())
							return -1;
						else if (o1.getFiliterWeight() == o2.getFiliterWeight())
							return 0;
						else
							return 1;
					}
				});
    		}
            
            System.out.println("HashMap已生成, 共" + totalNumberOfTerm + "个特征项");
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			
		}
        
        System.out.println("预处理结束");
    	System.out.println("------------------");

    	return resultHashMap;//返回结果
	}
	
	/*
	 * int N11 = featureTerm.getN11();//在此类含此词
	 * int N01 = featureTerm.getN01();//在此类不含此词
	 * int N10 = featureTerm.getN10();//不在此类含此词
	 * int N00 = featureTerm.getN00();//不在此类不含此词
	 */
	public abstract double getFeatureValue(FeatureTerm featureTerm);
}
