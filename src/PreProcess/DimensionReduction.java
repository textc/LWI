package PreProcess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Iterator;

/**
 * Newsgroups�ĵ���Ԥ������
 */
public class DimensionReduction {


	public Map<Integer, Double> SlectWordByValue(
			Map<Integer, Double> IndexValueMap, double Slect)
			throws IOException {
		System.out.println("SlectWordByValue begin IndexValueMap.size:"
				+ IndexValueMap.size());
		int i = 0;
		int col = 0;
		int PrintDebug = 0;
		double MaxValue = 0.0;
		Map<Integer, Double> IndexValueMapNew = new TreeMap<Integer, Double>();

		col = IndexValueMap.size();
		int[] index = new int[col];
		double[] Value = new double[col];
		Set<Map.Entry<Integer, Double>> IndexValueMapSet = IndexValueMap
				.entrySet();
		for (Iterator<Map.Entry<Integer, Double>> pt = IndexValueMapSet
				.iterator(); pt.hasNext();) {
			Map.Entry<Integer, Double> pe = pt.next();
			if (i < col) {
				index[i] = pe.getKey();
				Value[i++] = pe.getValue();
			}
		}

		// ð������
		for (i = 0; i < col; i++) {
			for (int j = 0; j < col - i - 1; j++) {
				if (Value[j] > Value[j + 1]) {
					double dtemp = Value[j];
					int itemp = index[j];

					Value[j] = Value[j + 1];
					index[j] = index[j + 1];

					Value[j + 1] = dtemp;
					index[j + 1] = itemp;
				}
			}
		}

		double SlectNum = col * Slect;
		if (SlectNum > col)
			SlectNum = col;
		if ((PrintDebug & 0x01) == 0x01) {

			String strDir = "./DataMiningSample/0_outputfile/";

			String fileName = "IndexValueMapNewArry.txt";

			System.out.println("printIDMap:" + strDir + fileName);
			int countLine = 0;
			File outPutFile = new File(strDir + fileName);
			FileWriter outPutFileWriter;
			outPutFileWriter = new FileWriter(outPutFile);

			for (i = 0; i < col; i++) {
				outPutFileWriter.write(index[col - 1 - i] + " "
						+ Value[col - 1 - i] + "\n");
			}

			outPutFileWriter.flush();
			outPutFileWriter.close();
		}

		for (i = 0; i < SlectNum; i++) {
			IndexValueMapNew.put(index[col - 1 - i], Value[col - 1 - i]);
		}
		System.out.println("SlectWordByValue end IndexValueMap.size:"
				+ IndexValueMapNew.size());
		return IndexValueMapNew;
	}

	/***********************************************************************/
	/*
	 * IGDR(int** docClassTerm, class_index* classIndex) /* ���ã� /*
	 * ��Ϣ���棬����ÿ�����ʶ��ĵ����ֵ�ָ������Я������Ϣ�� /* ������ /* docClassTerm
	 * �����ĵ������㵥�ʳ��ֵ����ĵ����͵������๲�ֵ��ĵ��� /* classIndex ����βλ�ã��������С�����е��ĵ� /* ���أ� /*
	 * multimap< double, int> ÿ�����ʵ�IG�÷ֺ������꣬���� /*
	 * /**********************************************************************
	 */
	public Map<Integer, Double> IGDR(int[][] docClassTerm,
			int[] DocNumPerClass, int MaxTerm, int MaxDoc, int MaxLabel) {
		int row, col;
		Map<Integer, Double> mapIG = new TreeMap<Integer, Double>();
		for (row = 1; row < MaxTerm; ++row) // ��-����
		{
			int termDT = 0; // �����г��� row ���ĵ�����

			for (col = 1; col < MaxLabel; ++col) {
				if (docClassTerm[row][col] > 0) {
					termDT += docClassTerm[row][col];
				}
			}

			double pIG = 0; // MI
			double pCT = 0; // ������
			double pTermC = 0; // ���ָ���
			double pTermCn = 0; // �����ָ���
			double p = 0;

			for (col = 1; col < MaxLabel; ++col) // ��-���
			{
				if (DocNumPerClass[col] != 0) {
					// �����ܵ��ĵ���
					int classDN = DocNumPerClass[col];

					// p(c/t)
					double pC = (docClassTerm[row][col] + 0.0001)
							/ (double) (termDT + 0.0002);

					// p(c/-t)
					int t1 = (classDN - docClassTerm[row][col]);
					int t2 = (MaxDoc - termDT);
					double pCn = (t1 + 0.0001) / (double) (t2 + 0.0002);

					double pCC = classDN / (double) (MaxDoc);

					pCT -= pCC * Math.log(pCC);
					pTermC += pC * Math.log(pC);
					pTermCn += pCn * Math.log(pCn);

				}
			}
			int t3 = MaxDoc - termDT;
			pIG = pCT + pTermCn * (t3 + 0.0001) / (double) (MaxDoc) + pTermC
					* (termDT + 0.0001) / (double) (MaxDoc);

			if (Double.isNaN(pIG)) {
				pIG = 0.0001;
				System.out.println("IGDR:Find a NaN change to:" + pIG);
			}
			mapIG.put(row, pIG);
		}

		return mapIG;
	}

	/***********************************************************************/
	/*
	 * CSDR(int** docClassTerm, class_index* classIndex) /* ���ã� /*
	 * ����ͳ�ƣ�����ÿ�������������ض� /* ������ /* docClassTerm �����ĵ������㵥�ʳ��ֵ����ĵ����͵������๲�ֵ��ĵ��� /*
	 * classIndex ����βλ�ã��������С�����е��ĵ� /* ���أ� /* multimap< double, int>
	 * ÿ�����ʵ�CS�÷ֺ������꣬���� /*
	 * /**********************************************************************
	 */
	public Map<Integer, Double> CSDR(int[][] docClassTerm,
			int[] DocNumPerClass, int MaxTerm, int MaxDoc, int MaxLabel) {
		Map<Integer, Double> mapCS = new TreeMap<Integer, Double>(); // Chi_Square

		int row, col;
		for (row = 1; row < MaxTerm; ++row) // ��-���ʣ��������е���
		{
			int termDT = 0;
			// �����г��� row������� ���ĵ�����
			for (col = 1; col < MaxLabel; ++col) {
				if (docClassTerm[row][col] > 0) {
					termDT += docClassTerm[row][col];
				}
			}

			double pCS = -1000; // MI
			double pCT = 0; // ������

			int da = 0;
			for (col = 1; col < MaxLabel; ++col) // ��-���
			{
				if (DocNumPerClass[col] != 0) {
					++da;
					// �����ܵ��ĵ���
					int classDN = DocNumPerClass[col];

					// ���뵥�ʹ��ִ���
					int A = docClassTerm[row][col];

					// ���뵥�ʶ������ִ���
					int D = (MaxDoc - classDN - termDT + docClassTerm[row][col]);

					// ���ʳ��ֶ��಻���ֵĴ���
					int B = termDT - docClassTerm[row][col];

					// ���ʲ����ֶ�����ֵĴ���
					int C = (classDN - docClassTerm[row][col]);

					double ttemp = Math.pow((double) (A * D - B * C), 2);
					double ttemp1 = ttemp / (double) ((A + C) * (C + D));
					double ttemp2 = MaxDoc * ttemp1;

					pCT = ttemp2 / (double) ((A + B) * (B + D));

				}
				// ȡ���ֵ
				pCS = pCS > pCT ? pCS : pCT;
			}

			if (Double.isNaN(pCS)) {
				pCS = 0.0001;
				System.out.println("CSDR:Find a NaN change to:" + pCS);
			}

			mapCS.put(row, pCS);
		}

		return mapCS;
	}

	/***********************************************************************/
	/*
	 * MIDR(int** docClassTerm, class_index* classIndex) /* ���ã� /*
	 * ����Ϣ������ÿ����������������̶� /* ������ /* docClassTerm �����ĵ������㵥�ʳ��ֵ����ĵ����͵������๲�ֵ��ĵ��� /*
	 * classIndex ����βλ�ã��������С�����е��ĵ� /* ���أ� /* multimap< double, int>
	 * ÿ�����ʵ�MI�÷ֺ������꣬���� /*
	 * /**********************************************************************
	 */
	public Map<Integer, Double> MIDR(int[][] docClassTerm,
			int[] DocNumPerClass, int MaxTerm, int MaxDoc, int MaxLabel) {
		Map<Integer, Double> mapMI = new TreeMap<Integer, Double>();
		for (int row = 1; row < MaxTerm; ++row) // ��-����
		{
			int termDT = 0; // �����г��� axis ���ĵ�����

			for (int col1 = 1; col1 < MaxLabel; ++col1) {
				if (docClassTerm[row][col1] > 0) {
					termDT += docClassTerm[row][col1];
				}
			}

			double pMI = 0; // MI
			double pTermC = 0; // ���ָ���
			double pTermCn = 0; // �����ָ���

			for (int col = 1; col < MaxLabel; ++col) // ��-���
			{
				// int classDN = 0;
				// if (classIndex[col+1].last_index !=0)
				if (docClassTerm[row][col] > 0) {
					// ���е��ĵ���
					int classDN = DocNumPerClass[col];

					int termCT = docClassTerm[row][col];
					double ptc = termCT / (double) (classDN);

					pTermC += Math.log((termCT * MaxDoc)
									/ (double) (classDN + termDT));
					pTermCn += Math.log((MaxDoc - termCT + 0.0001)
									* MaxDoc
									/ (double) (classDN + MaxDoc - termDT + 0.0001));
				}
			}
			pMI = pTermCn + pTermC;

			if (Double.isNaN(pMI)) {
				pMI = 0.0001;
				System.out.println("MIDR:Find a NaN change to:" + pMI);
			}

			mapMI.put(row, pMI);
		}

		return mapMI;
	}

	/***********************************************************************/
	/*
	 * CEDR(int** docClassTerm, class_index* classIndex) /* ���ã� /*
	 * �����أ������ĵ��г��ֹ��ĵ��ʶ��ĵ����ֵ�ָ������Я������Ϣ�� /* ������ /* docClassTerm
	 * �����ĵ������㵥�ʳ��ֵ����ĵ����͵������๲�ֵ��ĵ��� /* classIndex ����βλ�ã��������С�����е��ĵ� /* ���أ� /*
	 * multimap< double, int> ÿ�����ʵ�CE�÷ֺ������꣬���� /*
	 * /**********************************************************************
	 */
	public Map<Integer, Double> CEDR(int[][] docClassTerm,
			int[] DocNumPerClass, int MaxTerm, int MaxDoc, int MaxLabel) {
		Map<Integer, Double> mapCE = new TreeMap<Integer, Double>();
		for (int row = 1; row < MaxTerm; ++row) // ��-����
		{
			int termDT = 0; // �����г��� axis ���ĵ�����

			for (int col1 = 1; col1 < MaxLabel; ++col1) {
				if (docClassTerm[row][col1] > 0) {
					termDT += docClassTerm[row][col1];
				}
			}

			double pCE = 0; // MI
			double pTermC = 0; // ���ָ���
			double pTermCn = 0; // �����ָ���

			for (int col = 1; col < MaxLabel; ++col) // ��-���
			{
				// int classDN = 0;
				// if (classIndex[col+1].last_index !=0)
				if (docClassTerm[row][col] > 0) {
					// ���е��ĵ���
					int classDN = DocNumPerClass[col];

					int termCT = docClassTerm[row][col];
					double ptc = termCT / (double) (classDN);

					pTermC += ptc
							* Math.log((termCT * MaxDoc)
									/ (double) (classDN + termDT));
					pTermCn += (1 - ptc + 0.0001)
							* Math.log((MaxDoc - termCT + 0.0001)
									* MaxDoc
									/ (double) (classDN + MaxDoc - termDT + 0.0001));
				}
			}
			pCE = pTermCn + pTermC;

			if (Double.isNaN(pCE)) {
				pCE = 0.0001;
				System.out.println("CEDR:Find a NaN change to:" + pCE);
			}

			mapCE.put(row, pCE);
		}

		return mapCE;
	}

	/*******************************************************************************/
	/*
	 * countWords /* ���ã� /* ͳ��ÿ���ʵ��ܵĳ��ִ��������س��ִ��� /* ������ /* @param 1:
	 * ����õ�newsgroup�ļ�Ŀ¼�ľ���·�� /* @param 2 /* /* ���أ�
	 * /****************************
	 * **************************************************
	 */
	public SortedMap<String, Double> countWords(String srcDir, int tfHold,
			Map<String, Double> wordMap) throws IOException {
		File sampleFile = new File(srcDir);
		File[] sample = sampleFile.listFiles();
		String word;
		for (int i = 0; i < sample.length; i++) {
			if (!sample[i].isDirectory()) {
				if (sample[i].getName().contains("stemed")) {
					FileReader samReader = new FileReader(sample[i]);
					BufferedReader samBR = new BufferedReader(samReader);
					while ((word = samBR.readLine()) != null) {
						if (!word.isEmpty() && wordMap.containsKey(word)) {
							double count = wordMap.get(word) + 1;
							wordMap.put(word, count);
						} else {
							wordMap.put(word, 1.0);
						}
					}
				}else{
					String fileFullName = sample[i].getCanonicalPath();
					File file=new File(fileFullName);  
					if(file.exists()){
						//file.delete();
						//System.out.println("delete:"+fileFullName);
					}
				}
			} else {
				countWords(sample[i].getCanonicalPath(), tfHold, wordMap);
			}
		}
		// ֻ���س��ִ�������3�ĵ���
		SortedMap<String, Double> newWordMap = new TreeMap<String, Double>();
		Set<Map.Entry<String, Double>> allWords = wordMap.entrySet();
		for (Iterator<Map.Entry<String, Double>> it = allWords.iterator(); it
				.hasNext();) {
			Map.Entry<String, Double> me = it.next();
			if (me.getValue() >= tfHold) {
				newWordMap.put(me.getKey(), me.getValue());
			}
		}

		return newWordMap;
	}

	/**
	 * ����IDF�������Դʵ���ÿ�����ڶ��ٸ��ĵ��г��ֹ�
	 * 
	 * @param testSampleDir
	 *            �����㷨������������Ŀ¼
	 * @return ���ʵ�IDFmap ��ʽΪSortedMap<String,Double> ��<���ʣ������õ��ʵ��ĵ���>
	 * @throws IOException
	 */
	public SortedMap<String, Double> computeIDF(String string,
			Map<String, Double> wordMap) throws IOException {
		// TODO Auto-generated method stub
		File fileDir = new File(string);
		Map<String, Double> IDFPerWordMap = new TreeMap<String, Double>();
		Set<String> alreadyCountWord = new HashSet<String>();// ���µ�ǰ�Ѿ��������ĸ��ĵ��еĴ�
		String word;
		Double TotalDocCout = 0.0;
		File[] sampleDir = fileDir.listFiles();
		for (int i = 0; i < sampleDir.length; i++) {
			File[] sample = sampleDir[i].listFiles();
			for (int j = 0; j < sample.length; j++) {
				TotalDocCout++;
				alreadyCountWord.clear();
				FileReader samReader = new FileReader(sample[j]);
				BufferedReader samBR = new BufferedReader(samReader);
				while ((word = samBR.readLine()) != null) {
					if (!alreadyCountWord.contains(word)) {
						if (IDFPerWordMap.containsKey(word)) {
							IDFPerWordMap.put(word,
									IDFPerWordMap.get(word) + 1.0);
						} else {
							IDFPerWordMap.put(word, 1.0);
						}
						alreadyCountWord.add(word);
					}
				}
			}
		}

		System.out.println("ComputeIDF: Total Document Count = " + TotalDocCout);
		Double IDF = 0.0;
		String dicWord;
		SortedMap<String, Double> IDFPerWordMapNew = new TreeMap<String, Double>();
		Set<Map.Entry<String, Double>> wordMapSet = wordMap.entrySet();
		for (Iterator<Map.Entry<String, Double>> pt = wordMapSet.iterator(); pt
				.hasNext();) {
			Map.Entry<String, Double> pe = pt.next();
			dicWord = pe.getKey();
			if (IDFPerWordMap.containsKey(dicWord)) {
				// ���㵥�ʵ�IDF
				IDF = Math.log((TotalDocCout / IDFPerWordMap.get(dicWord))+0.0001)
						/ Math.log(10);
				if(IDF == 0.0)
				{
		System.out.println("ComputeIDF: Math.log(TotalDocCout / IDFPerWordMap.get(dicWord)) = " + 
		Math.log(TotalDocCout / IDFPerWordMap.get(dicWord)));
		System.out.println("ComputeIDF: Math.log(10) = " + Math.log(10));
		System.out.println("ComputeIDF: TotalDocCout = " + TotalDocCout);
		System.out.println("ComputeIDF: IDFPerWordMap.get(dicWord) = " + IDFPerWordMap.get(dicWord));
				}
				IDFPerWordMapNew.put(dicWord, IDF);
			}
		}

		return IDFPerWordMapNew;
	}

	/**
	 * ����RF�������Դʵ���ÿ�����ڶ��ٸ����г��ֹ�
	 * 
	 * @param testSampleDir
	 *            �����㷨������������Ŀ¼
	 * @return ���ʵ�IDFmap ��ʽΪSortedMap<String,Double> ��<���ʣ������õ��ʵ��ĵ���>
	 * @throws IOException
	 */
	public SortedMap<String, Double> computeRF(String string,
			Map<String, Double> wordMap) throws IOException {
		// TODO Auto-generated method stub
		File fileDir = new File(string);
		Map<String, Double> IDFPerWordMap = new TreeMap<String, Double>();
		Set<String> alreadyCountWord = new HashSet<String>();// ���µ�ǰ�Ѿ��������ĸ��ĵ��еĴ�
		String word;
		File[] sampleDir = fileDir.listFiles();
		double TotalClassCout = sampleDir.length;
				
		for (int i = 0; i < sampleDir.length; i++) {

			alreadyCountWord.clear();
			File[] sample = sampleDir[i].listFiles();
			for (int j = 0; j < sample.length; j++) {

				FileReader samReader = new FileReader(sample[j]);
				BufferedReader samBR = new BufferedReader(samReader);
				while ((word = samBR.readLine()) != null) {
					if (!alreadyCountWord.contains(word)) {
						if (IDFPerWordMap.containsKey(word)) {
							IDFPerWordMap.put(word,
									IDFPerWordMap.get(word) + 1.0);
						} else {
							IDFPerWordMap.put(word, 1.0);
						}
						alreadyCountWord.add(word);
					}
				}
			}
		}

		System.out.println("ComputeRF: Total Class Count = " + TotalClassCout);
		Double RF = 0.0;
		String dicWord;
		SortedMap<String, Double> IDFPerWordMapNew = new TreeMap<String, Double>();
		Set<Map.Entry<String, Double>> wordMapSet = wordMap.entrySet();
		for (Iterator<Map.Entry<String, Double>> pt = wordMapSet.iterator(); pt
				.hasNext();) {
			Map.Entry<String, Double> pe = pt.next();
			dicWord = pe.getKey();
			if (IDFPerWordMap.containsKey(dicWord)) {
				// ���㵥�ʵ�IDF
				RF = Math.log(TotalClassCout / IDFPerWordMap.get(dicWord))
						/ Math.log(10);
				if(RF == 0.0)
				{
					System.out.println("ComputeRF: RF = 0, Word = " + dicWord);
				}
				IDFPerWordMapNew.put(dicWord, RF);
			}
		}

		return IDFPerWordMapNew;
	}

	public SortedMap<String, Double> computeRF(String string,
			Map<String, Double> wordMap, double trainSamplePercent, double indexOfSample) throws IOException {
		// TODO Auto-generated method stub
		File fileDir = new File(string);
		Map<String, Double> IDFPerWordMap = new TreeMap<String, Double>();
		Set<String> alreadyCountWord = new HashSet<String>();// ���µ�ǰ�Ѿ��������ĸ��ĵ��еĴ�
		String word;
		File[] sampleDir = fileDir.listFiles();
		double TotalClassCout = sampleDir.length;
				
		for (int i = 0; i < sampleDir.length; i++) {

			alreadyCountWord.clear();
			File[] sample = sampleDir[i].listFiles();
			for (int j = 0; j < sample.length; j++) {

				double TotalDocCout = sample.length;
				double testBeginIndex = indexOfSample*(TotalDocCout*trainSamplePercent);// ������������ʼ�ļ����
				double testEndIndex = (indexOfSample+1)*(TotalDocCout*trainSamplePercent);// �����������Ľ����ļ����
				if((j<testBeginIndex)||(j>testEndIndex)){
					FileReader samReader = new FileReader(sample[j]);
					BufferedReader samBR = new BufferedReader(samReader);
					while ((word = samBR.readLine()) != null) {
						if (!alreadyCountWord.contains(word)) {
							if (IDFPerWordMap.containsKey(word)) {
								IDFPerWordMap.put(word,
										IDFPerWordMap.get(word) + 1.0);
							} else {
								IDFPerWordMap.put(word, 1.0);
							}
							alreadyCountWord.add(word);
						}
					}
				}
			}
		}

		System.out.println("ComputeRF: Total Class Count = " + TotalClassCout);
		Double RF = 0.0;
		String dicWord;
		SortedMap<String, Double> IDFPerWordMapNew = new TreeMap<String, Double>();
		Set<Map.Entry<String, Double>> wordMapSet = wordMap.entrySet();
		for (Iterator<Map.Entry<String, Double>> pt = wordMapSet.iterator(); pt
				.hasNext();) {
			Map.Entry<String, Double> pe = pt.next();
			dicWord = pe.getKey();
			if (IDFPerWordMap.containsKey(dicWord)) {
				// ���㵥�ʵ�IDF
				RF = Math.log(TotalClassCout / IDFPerWordMap.get(dicWord))
						/ Math.log(10);
				if(RF == 0.0)
				{
					System.out.println("ComputeRF: RF = 0, Word = " + dicWord);
				}
				IDFPerWordMapNew.put(dicWord, RF);
			}
		}

		return IDFPerWordMapNew;
	}


	public SortedMap<String, Double> computeFirstFactor(String string, File ClassDir,
			Map<String, Double> wordMap, double b) throws IOException {
		// TODO Auto-generated method stub
		Map<String, Double> IDFPerWordMap = new TreeMap<String, Double>();
		Set<String> alreadyCountWord = new HashSet<String>();// ���µ�ǰ�Ѿ��������ĸ��ĵ��еĴ�
		String word;
	
		File[] sample = ClassDir.listFiles();
		double TotalDocCout = sample.length;
		for (int j = 0; j < sample.length; j++) {

			alreadyCountWord.clear();
			FileReader samReader = new FileReader(sample[j]);
			BufferedReader samBR = new BufferedReader(samReader);
			while ((word = samBR.readLine()) != null) {
				if (!alreadyCountWord.contains(word)) {
					if (IDFPerWordMap.containsKey(word)) {
						IDFPerWordMap.put(word,
								IDFPerWordMap.get(word) + 1.0);
					} else {
						IDFPerWordMap.put(word, 1.0);
					}
					alreadyCountWord.add(word);
				}
			}
		}

		//System.out.println("computeFirstFactor: Total DOC Count = " + TotalDocCout);
		Double DF = 0.0;
		String dicWord;
		SortedMap<String, Double> IDFPerWordMapNew = new TreeMap<String, Double>();
		Set<Map.Entry<String, Double>> wordMapSet = wordMap.entrySet();
		for (Iterator<Map.Entry<String, Double>> pt = wordMapSet.iterator(); pt
				.hasNext();) {
			Map.Entry<String, Double> pe = pt.next();
			dicWord = pe.getKey();
			if (IDFPerWordMap.containsKey(dicWord)) {
				// ���㵥�ʵ�IDF
				DF = IDFPerWordMap.get(dicWord)/TotalDocCout;
				//DF = DF*Math.exp(b);
				DF = DF*Math.log(b);
				DF = Math.exp(DF);
				IDFPerWordMapNew.put(dicWord, DF);
			}
		}

		return IDFPerWordMapNew;
	}

	public SortedMap<String, Double> computeFirstFactor(String string, File ClassDir,
			Map<String, Double> wordMap, double b, double trainSamplePercent, double indexOfSample) throws IOException {
		// TODO Auto-generated method stub
		Map<String, Double> IDFPerWordMap = new TreeMap<String, Double>();
		Set<String> alreadyCountWord = new HashSet<String>();// ���µ�ǰ�Ѿ��������ĸ��ĵ��еĴ�
		String word;
	
		File[] sample = ClassDir.listFiles();
		double TotalDocCout = sample.length;

		double testBeginIndex = indexOfSample*(TotalDocCout*trainSamplePercent);// ������������ʼ�ļ����
		double testEndIndex = (indexOfSample+1)*(TotalDocCout*trainSamplePercent);// �����������Ľ����ļ����
		TotalDocCout = TotalDocCout*(1-trainSamplePercent);
		
		for (int j = 0; j < sample.length; j++) {

			if((j<testBeginIndex)||(j>testEndIndex)){
				alreadyCountWord.clear();
				FileReader samReader = new FileReader(sample[j]);
				BufferedReader samBR = new BufferedReader(samReader);
				while ((word = samBR.readLine()) != null) {
					if (!alreadyCountWord.contains(word)) {
						if (IDFPerWordMap.containsKey(word)) {
							IDFPerWordMap.put(word,
									IDFPerWordMap.get(word) + 1.0);
						} else {
							IDFPerWordMap.put(word, 1.0);
						}
						alreadyCountWord.add(word);
					}
				}
			}
		}

		//System.out.println("computeFirstFactor: Total DOC Count = " + TotalDocCout);
		Double DF = 0.0;
		String dicWord;
		SortedMap<String, Double> IDFPerWordMapNew = new TreeMap<String, Double>();
		Set<Map.Entry<String, Double>> wordMapSet = wordMap.entrySet();
		for (Iterator<Map.Entry<String, Double>> pt = wordMapSet.iterator(); pt
				.hasNext();) {
			Map.Entry<String, Double> pe = pt.next();
			dicWord = pe.getKey();
			if (IDFPerWordMap.containsKey(dicWord)) {
				// ���㵥�ʵ�IDF
				DF = IDFPerWordMap.get(dicWord)/TotalDocCout;
				DF = DF*Math.log(b);
				DF = Math.exp(DF);

				IDFPerWordMapNew.put(dicWord, DF);
			}
		}

		return IDFPerWordMapNew;
	}

	public SortedMap<String, Double> computeTFPerDoc(File file,
			Map<String, Double> wordMap) throws IOException {

		String word;
		Double TF = 0.0;
		Double wordSumPerDoc = 0.0;// ����ÿƪ�ĵ����ܴ���
		SortedMap<String, Double> TFPerDocMap = new TreeMap<String, Double>();

		TFPerDocMap.clear();
		FileReader samReader = new FileReader(file);
		BufferedReader samBR = new BufferedReader(samReader);
		String fileShortName = file.getName();
		while ((word = samBR.readLine()) != null) {
			if (!word.isEmpty() && wordMap.containsKey(word)) {// ���������Դʵ�����Ĵʣ�ȥ���Ĵʲ�����
				wordSumPerDoc++;
				if (TFPerDocMap.containsKey(word)) {
					double count = TFPerDocMap.get(word);
					TFPerDocMap.put(word, count + 1);
				} else {
					TFPerDocMap.put(word, 1.0);
				}
			}
		}

		Set<Map.Entry<String, Double>> TFPerDocMapSet = TFPerDocMap.entrySet();
		for (Iterator<Map.Entry<String, Double>> pt = TFPerDocMapSet.iterator(); pt
				.hasNext();) {
			Map.Entry<String, Double> pe = pt.next();
			word = pe.getKey();
			// ���㵥�ʵ�TF
			TF = TFPerDocMap.get(word) / wordSumPerDoc;
			TFPerDocMap.put(word, TF);
		}

		return TFPerDocMap;
	}

	public SortedMap<String, Integer> computeTermPerDoc(File file,
			Map<String, Double> wordMap) throws IOException {

		String word;
		Double TF = 0.0;
		SortedMap<String, Integer> TFPerDocMap = new TreeMap<String, Integer>();

		TFPerDocMap.clear();
		FileReader samReader = new FileReader(file);
		BufferedReader samBR = new BufferedReader(samReader);
		String fileShortName = file.getName();
		while ((word = samBR.readLine()) != null) {
			if (TFPerDocMap.containsKey(word)) {
				int count = TFPerDocMap.get(word);
				TFPerDocMap.put(word, count + 1);
			} else {
				TFPerDocMap.put(word, 1);
			}
		}

		return TFPerDocMap;
	}

	public Map<Integer, Double> SlectWordByRandom(
			Map<Integer, Double> IndexValueMap, double Slect)
			throws IOException {
		System.out.println("SlectWordByRandom begin IndexValueMap.size:"
				+ IndexValueMap.size());
		int i = 0;
		int col = 0;
		int PrintDebug = 0;
		double MaxValue = 0.0;
		Map<Integer, Double> IndexValueMapNew = new TreeMap<Integer, Double>();

		col = IndexValueMap.size();
		int[] index = new int[col];
		int[] flag  = new int[col];
		Set<Map.Entry<Integer, Double>> IndexValueMapSet = IndexValueMap
				.entrySet();
		for (Iterator<Map.Entry<Integer, Double>> pt = IndexValueMapSet
				.iterator(); pt.hasNext();) {
			Map.Entry<Integer, Double> pe = pt.next();
			if (i < col) {
				index[i] = pe.getKey();
				flag[i++] = 0;
			}
		}

		double SlectNum = col * Slect;
		if (SlectNum > col){
			SlectNum = col;
		}

		for(int m=0; m<SlectNum; m++)
		{
			int k = (int)Math.floor(Math.random() * col);
			if(flag[k] == 1){
				while(true)
				{
					k = k+(int)Math.floor(Math.random() * 10);
					if(k>=col)
					{
						k = 0;
					}
					if(flag[k] == 0)
					{
						flag[k] = 1;
						break;
					}
				}
			}else{
				flag[k] = 1;
			}
		}
		
		if ((PrintDebug & 0x01) == 0x01) {

			String strDir = "./DataMiningSample/0_outputfile/";

			String fileName = "IndexValueMapNewArry.txt";

			System.out.println("printIDMap:" + strDir + fileName);
			int countLine = 0;
			File outPutFile = new File(strDir + fileName);
			FileWriter outPutFileWriter;
			outPutFileWriter = new FileWriter(outPutFile);

			for (i = 0; i < col; i++) {
				outPutFileWriter.write(index[col - 1 - i] + " "+ "\n");
			}

			outPutFileWriter.flush();
			outPutFileWriter.close();
		}

		for (i = 0; i < col; i++) {
			if(flag[i] == 1){
				IndexValueMapNew.put(index[i], IndexValueMap.get(index[i]));
			}
		}
		System.out.println("SlectWordByValue end IndexValueMap.size:"
				+ IndexValueMapNew.size());
		return IndexValueMapNew;
	}

	public static int getIDMapMaxIndex(Map<Integer, Double> IndexValueMap)throws IOException {
		int MaxIndex = 0;
		Set<Map.Entry<Integer, Double>> IndexValueMapSet = IndexValueMap.entrySet();
		for (Iterator<Map.Entry<Integer, Double>> it = IndexValueMapSet.iterator(); it
			    .hasNext();) {
			Map.Entry<Integer, Double> me = it.next();
			if(me.getKey() > MaxIndex)
				MaxIndex = me.getKey();
		}

		return MaxIndex;
	}

	// ��HashMap����value������
	static class ByValueComparator implements Comparator<Object> {
		HashMap<Integer, Double> base_map;

		public ByValueComparator(HashMap<Integer, Double> disMap) {
			this.base_map = disMap;
		}

		@Override
		public int compare(Object o1, Object o2) {
			// TODO Auto-generated method stub
			String arg0 = o1.toString();
			String arg1 = o2.toString();
			if (!base_map.containsKey(arg0) || !base_map.containsKey(arg1)) {
				return 0;
			}
			if (base_map.get(arg0) < base_map.get(arg1)) {
				return 1;
			} else if (base_map.get(arg0) == base_map.get(arg1)) {
				return 0;
			} else {
				return -1;
			}
		}
	}

}
