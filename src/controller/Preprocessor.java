package controller;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import model.FeatureTerm;
import model.ObjectPair;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import constants.Constants;

public class Preprocessor {
	private  IndexWriter writer =  null ;
	public  static long avgFileLength = -1;
	public  static ArrayList<Long> fileLengArrayList = new ArrayList<Long>();
    
    public  Preprocessor(String indexFilePath) {  
        try  {  
        	Analyzer analyzer = new SmartChineseAnalyzer(Version.LUCENE_36, true);
        	IndexWriterConfig indexWriterConfig = new IndexWriterConfig(    
                    Version.LUCENE_36, analyzer);
        	indexWriterConfig.setOpenMode(OpenMode.CREATE);
            writer = new  IndexWriter(FSDirectory.open( new  File(indexFilePath)), indexWriterConfig); // �б仯�ĵط�   
        } catch  (Exception e) {  
            e.printStackTrace();  
        }  
    }
    
    public Preprocessor() {}    
    
    public void loadIndexWriterFromFilePath(String indexFilePath) {
    	try  {  
    		SmartChineseAnalyzer analyzer = new SmartChineseAnalyzer(Version.LUCENE_36, true);
        	IndexWriterConfig indexWriterConfig = new IndexWriterConfig(    
                    Version.LUCENE_36, analyzer);
        	indexWriterConfig.setOpenMode(OpenMode.CREATE);
            writer = new  IndexWriter(FSDirectory.open( new  File(indexFilePath)), indexWriterConfig); // �б仯�ĵط�   
        } catch  (Exception e) {  
            e.printStackTrace();  
        } 
    }
    
    public static void loadSettings() {
    	File settingsFile = new  File(Constants.SETTINGS_FILE_PATH);  
        if  (settingsFile.isFile()) {  
        	BufferedReader bufferedReader = null;
            try {
            	FileReader fileReader = new FileReader(settingsFile);
				bufferedReader = new BufferedReader(fileReader);
                String lineString = null;
				avgFileLength = Long.parseLong(bufferedReader.readLine()) ;
				fileLengArrayList = new ArrayList<Long>();
	            while ((lineString = bufferedReader.readLine()) != null) {
	            	fileLengArrayList.add(Long.parseLong(lineString));
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					bufferedReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
        }else  {  
            System.out.println("-----folder.isFile():false." );  
        }
	}
    
    public static void saveSettings() {
    	File settingsFile = new  File(Constants.SETTINGS_FILE_PATH);  
        if  (settingsFile.isFile()) {  
            try {
            	FileWriter fileWriter = new FileWriter(settingsFile);
                @SuppressWarnings("resource")
				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                bufferedWriter.write(String.valueOf(avgFileLength/fileLengArrayList.size()) + "\n");
                for (Long fileLength : fileLengArrayList) {
                	bufferedWriter.write(String.valueOf(fileLength) + "\n");
				}
                
                bufferedWriter.flush();
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }else  {  
            System.out.println("-----folder.isFile():false." );  
        }
	}
  
    public  Document getDocument(File f)  throws  Exception {  
        Document doc = new  Document();  
        BufferedReader bufferedReader = new  BufferedReader(new FileReader(f));
        StringBuffer stringBuffer = new StringBuffer();
        String lineString = null;
        while((lineString = bufferedReader.readLine()) != null) {
        	stringBuffer.append(lineString + "\n");
        }
        bufferedReader.close();
        String content = stringBuffer.toString();
        avgFileLength += content.length();
        fileLengArrayList.add((long) content.length());
        doc.add(new  Field( "content", content, Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.YES));
        doc.add(new  Field( "filename" , f.getName(), Field.Store.YES, Field.Index.NOT_ANALYZED));
        return  doc;  
    }  
      
    public   void  indexFilesFromPath(String path)  throws  Exception {  
        File folder = new  File(path);  
        if  (folder.isDirectory()) {  
            String[] files = folder.list();  
            for  ( int  i =  0 ; i < files.length; i++) { 
                File file = new  File(folder, files[i]);  
                Document doc = getDocument(file);  
                System.out.println("loaded "  + file +  " " );  
                writer.addDocument(doc);
            }
            
            Preprocessor.saveSettings();
        }else  {  
            System.out.println("-----folder.isDirectory():false." );  
        }  
    }
    
    static public void deleteIndexFiles(String path) {
    	File folder = new  File(path);  
        if  (folder.isDirectory()) {  
            String[] files = folder.list();  
            for  ( int  i =  0 ; i < files.length; i++) {  
                File file = new  File(folder, files[i]);  
                file.delete();
            } 
        }    
    }
    
    static public ArrayList<File> filesFromDirectory(String path) {
    	File folder = new  File(path);  
        if  (folder.isDirectory()) {  
        	ArrayList<File> resultList = new ArrayList<File>();
            String[] files = folder.list();  
            for  ( int  i =  0 ; i < files.length; i++) {  
                File file = new  File(folder, files[i]);  
                resultList.add(file);
            } 
            return resultList;
        }else  {  
            System.out.println("-----folder.isDirectory():false." );  
        }
        return null;
	}
    
    static public BufferedReader bufferReaderFromPath(String path)  throws  Exception{
    	File file = new  File(path);  
        if  (file.isFile()) {  
        	FileInputStream input = new  FileInputStream(file);  
            return new  BufferedReader( new  InputStreamReader(input));
        }else  {  
            System.out.println("-----file.isFile():false." );  
        }
        return null;
	}
    
    public void close()  throws  Exception {  
        writer.close();  
    }
    
    public static IndexReader preprocessClassifyFiles(String filePath, String indexPath, ArrayList<String> classNameArrayList, HashMap<String, Integer> classFileNumberHashMap) {
    	
    	SmartChineseAnalyzer analyzer = new SmartChineseAnalyzer(Version.LUCENE_36, true);
    	IndexWriterConfig indexWriterConfig = new IndexWriterConfig(    
                Version.LUCENE_36, analyzer);
    	indexWriterConfig.setOpenMode(OpenMode.CREATE);
        IndexWriter indexWriter = null;
        IndexReader indexReader = null;
        try {
        	Directory directory = FSDirectory.open(new File(indexPath));
			indexWriter = new  IndexWriter(directory, indexWriterConfig);
			File folder = new  File(filePath);//打开语料库文件夹
			if  (folder.isDirectory()) {
				System.out.println("索引建立...");
				
	            String[] classFiles = folder.list();
	            for (int classIndex = 0; classIndex < classFiles.length; classIndex++) {//遍历类别文件夹
	            	File classFile = new  File(folder, classFiles[classIndex]);
	            	if  (classFile.isDirectory()) {
	            		String className = classFiles[classIndex];//获得当前文件的类名
            			if (classNameArrayList != null)classNameArrayList.add(className);//算N用
            			
        	            String[] files = classFile.list();
        	            if (classFileNumberHashMap != null)classFileNumberHashMap.put(className, files.length);//保存当前类别的文件数,求N01用
        	            for  ( int  i =  0 ; i < files.length; i++) {//遍历该类别下的文件
        	                File file = new  File(classFile, files[i]);
        	                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "GBK"));
        	                
        	                System.out.print(".");
        	                StringBuffer contentStringBuffer = new StringBuffer();
        	                String readLineString = null;
        	                while ((readLineString = bufferedReader.readLine()) != null) {
        	                	contentStringBuffer.append(readLineString);
							}
        	                String content = contentStringBuffer.toString();
        	                
        	                Document doc = new  Document(); //生成一个document
        	                doc.add(new Field( "content", content, Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.YES));
        	                doc.add(new Field( "filename" , file.getName(), Field.Store.YES, Field.Index.NOT_ANALYZED));
        	                doc.add(new Field( "class", className, Field.Store.YES, Field.Index.NOT_ANALYZED));
        	                doc.add(new Field( "content-length", String.valueOf(content.length()), Field.Store.YES, Field.Index.NOT_ANALYZED));
        	                
        	                indexWriter.addDocument(doc);
        	                bufferedReader.close();
        	            } 
	            	}
				}
	            indexWriter.close();//写入索引
	            
	            System.out.println();
	            System.out.println("索引建立完毕");
	            indexReader = IndexReader.open(directory);
	            System.out.println(indexReader.numDocs() + "个文档");
			}else  {  
	            System.out.println("--preprocessClassifyFiles--:false." );  
	        }
        }catch (IOException e) {
			e.printStackTrace();
		} finally {
			
		}
        
        return indexReader;
    }

    public HashMap<String, ArrayList<FeatureTerm>>  processFilesToFeatureTermsHashMap(String targetFilePath) throws CorruptIndexException, IOException{
    	
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
					newTerm.setN11(pair.getOne());
					newTerm.setN01(pair.getTwo());
					newTerm.setN10(termEnum.docFreq() - newTerm.getN11());
					newTerm.setN00(indexReader.numDocs() - termEnum.docFreq() - newTerm.getN01());
					newTerm.setFiliterWeight(Scorer.getCHIValue(newTerm));
					
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

}
