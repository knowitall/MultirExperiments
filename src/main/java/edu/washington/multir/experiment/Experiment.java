package edu.washington.multir.experiment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import edu.washington.multir.development.TrainModel;
import edu.washington.multir.distantsupervision.FeedbackDistantSupervision;
import edu.washington.multir.distantsupervision.MultiModelDistantSupervision;
import edu.washington.multirframework.argumentidentification.ArgumentIdentification;
import edu.washington.multirframework.argumentidentification.RelationMatching;
import edu.washington.multirframework.argumentidentification.SententialInstanceGeneration;
import edu.washington.multirframework.corpus.Corpus;
import edu.washington.multirframework.corpus.CorpusInformationSpecification;
import edu.washington.multirframework.distantsupervision.DistantSupervision;
import edu.washington.multirframework.distantsupervision.NegativeExampleCollection;
import edu.washington.multirframework.featuregeneration.FeatureGeneration;
import edu.washington.multirframework.featuregeneration.FeatureGenerator;
import edu.washington.multirframework.knowledgebase.KnowledgeBase;

public class Experiment {
	
	private String corpusPath;
	private ArgumentIdentification ai;
	private FeatureGenerator fg;
	private List<SententialInstanceGeneration> sigs;
	private List<String> DSFiles;
	private List<String> oldFeatureFiles;
	private List<String> featureFiles;
	private List<String> multirDirs;
	private List<String> oldMultirDirs;
	private RelationMatching rm;
	private NegativeExampleCollection nec;
	private KnowledgeBase kb;
	private String testDocumentsFile;
	private CorpusInformationSpecification cis;
	private String evalOutputName;
	private boolean train = true;



	public void runExperiment() throws SQLException, IOException, InterruptedException, ExecutionException{
		
		Corpus corpus = new Corpus(corpusPath, cis, true);
		if(train){
		 corpus.setCorpusToTrain(testDocumentsFile);
		}
		else{
		  corpus.setCorpusToTest(testDocumentsFile);
		}
		
		if(!filesExist(multirDirs)){
			for(String s : multirDirs){
				File f = new File(s);
				f.mkdirs();
			}
		}
		
		if(oldFeatureFiles != null){
			runFeedbackExperiment(corpus);
		}
	
		boolean runDS = !filesExist(DSFiles);
		boolean runFG = false;
		
		//if distant supervision hasnt been run yet
		if(runDS){
			System.err.println("Running DS");
			runFG = true;
			if(DSFiles.size() > 1){
				MultiModelDistantSupervision mmds = new MultiModelDistantSupervision(ai, DSFiles, sigs, rm, nec, false);
				mmds.run(kb, corpus);
			}
			else{
				DistantSupervision ds = new DistantSupervision(ai, sigs.get(0), rm, nec);
				ds.run(DSFiles.get(0), kb, corpus);
			}
		}
		
		if(!runFG){
			runFG = !filesExist(featureFiles);
		}
		
		//if feature generation hasnt been run yet
		if(runFG){
			System.err.println("Running FG");

			FeatureGeneration fGeneration = new FeatureGeneration(fg);
			fGeneration.run(DSFiles, featureFiles, corpus, cis);
		}

		//do average training run
		TrainModel.run(featureFiles,multirDirs,10);
		
	}



	private void runFeedbackExperiment(Corpus corpus) throws SQLException, IOException, InterruptedException, ExecutionException {
		
		List<String> feedbackDSFiles = new ArrayList<>();
		int count = 1;
		for(String s : featureFiles){
			File f = new File(s);
			File dir = f.getParentFile();
			feedbackDSFiles.add(dir.toString()+"/FBDS"+count);
		}
		count++;
		List<String> feedbackFeatureFiles = new ArrayList<>();
		for(String s: feedbackDSFiles){
			feedbackFeatureFiles.add(s+".features");
		}
		
		FeedbackDistantSupervision.run(kb, oldMultirDirs, feedbackDSFiles, sigs, fg, ai, corpus);

		FeatureGeneration fGeneration = new FeatureGeneration(fg);
		fGeneration.run(feedbackDSFiles, feedbackFeatureFiles, corpus, cis);
		
		for(int i =0; i < featureFiles.size(); i++){
			//read feedbackFeatureFiles, find NA annotations and write them to feature file
			File feedbackFeatureFile = new File(feedbackFeatureFiles.get(i));
			File oldFeatureFile = new File(oldFeatureFiles.get(i));
			File newFeatureFile = new File(featureFiles.get(i));
			BufferedWriter bw = new BufferedWriter(new FileWriter(newFeatureFile));
			BufferedReader oldFeatureReader = new BufferedReader(new FileReader(oldFeatureFile));
			BufferedReader feedbackFeatureReader = new BufferedReader(new FileReader(feedbackFeatureFile));
			
			String nextLine;
			while((nextLine = feedbackFeatureReader.readLine())!=null){
				String[] values = nextLine.split("\t");
				String rel = values[3];
				if(rel.equals("NA")){
					bw.write(nextLine+"\n");
				}
			}
			
			String prevLine = null;
			while((nextLine=oldFeatureReader.readLine())!=null){
				if(prevLine!=null) bw.write(prevLine+"\n");
				prevLine = nextLine;
			}
			if(prevLine != null) bw.write(prevLine);
			
			
			bw.close();
			oldFeatureReader.close();
			feedbackFeatureReader.close();
		}
		
		
	}



	private boolean filesExist(List<String> dsFiles) {
		for(String s : dsFiles){
			File f = new File(s);
			if(!f.exists()){
				System.err.println(s + " File does not exist!Need To Generate it");
				return false;
			}
		}
		return true;
	}



	public ArgumentIdentification getAi() {
		return ai;
	}



	public void setAi(ArgumentIdentification ai) {
		this.ai = ai;
	}



	public FeatureGenerator getFg() {
		return fg;
	}



	public void setFg(FeatureGenerator fg) {
		this.fg = fg;
	}



	public List<SententialInstanceGeneration> getSigs() {
		return sigs;
	}



	public void setSigs(List<SententialInstanceGeneration> sigs) {
		this.sigs = sigs;
	}



	public List<String> getDSFiles() {
		return DSFiles;
	}



	public void setDSFiles(List<String> dSFiles) {
		DSFiles = dSFiles;
	}



	public List<String> getFeatureFiles() {
		return featureFiles;
	}



	public void setFeatureFiles(List<String> featureFiles) {
		this.featureFiles = featureFiles;
	}



	public List<String> getMultirDir() {
		return multirDirs;
	}



	public void setMultirDir(List<String> multirDirs) {
		this.multirDirs = multirDirs;
	}



	public List<String> getOldMultirDirs() {
		return oldMultirDirs;
	}



	public void setOldMultirDir(List<String> oldMultirDirs) {
		this.oldMultirDirs = oldMultirDirs;
	}



	public List<String> getOldDSFiles() {
		return oldFeatureFiles;
	}



	public void setOldDSFiles(List<String> oldDSFiles) {
		this.oldFeatureFiles = oldDSFiles;
	}



	public String getTestDocumentsFile() {
		return testDocumentsFile;
	}



	public void setTestDocumentsFile(String testDocumentsFile) {
		this.testDocumentsFile = testDocumentsFile;
	}
	
	
	
	public CorpusInformationSpecification getCis() {
		return cis;
	}



	public void setCis(CorpusInformationSpecification cis) {
		this.cis = cis;
	}
	
	public RelationMatching getRm() {
		return rm;
	}



	public void setRm(RelationMatching rm) {
		this.rm = rm;
	}
	
	public String getCorpusPath() {
		return corpusPath;
	}



	public void setCorpusPath(String corpusPath) {
		this.corpusPath = corpusPath;
	}
	
	public List<String> getOldFeatureFiles() {
		return oldFeatureFiles;
	}



	public void setOldFeatureFiles(List<String> oldFeatureFiles) {
		this.oldFeatureFiles = oldFeatureFiles;
	}



	public List<String> getMultirDirs() {
		return multirDirs;
	}



	public void setMultirDirs(List<String> multirDirs) {
		this.multirDirs = multirDirs;
	}



	public NegativeExampleCollection getNec() {
		return nec;
	}



	public void setNec(NegativeExampleCollection nec) {
		this.nec = nec;
	}



	public KnowledgeBase getKb() {
		return kb;
	}



	public void setKb(KnowledgeBase kb) {
		this.kb = kb;
	}



	public String getEvalOutputName() {
		return evalOutputName;
	}



	public void setEvalOutputName(String evalOutputName) {
		this.evalOutputName = evalOutputName;
	}



	public void setOldMultirDirs(List<String> oldMultirDirs) {
		this.oldMultirDirs = oldMultirDirs;
	}
	




	public boolean isTrain() {
		return train;
	}



	public void setTrain(boolean train) {
		this.train = train;
	}


}
