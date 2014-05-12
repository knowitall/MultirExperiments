package edu.washington.multir.development;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public class RunPreprocess {
	
	public static void main(String[] args) throws IOException{
		String featureFile = args[0];
		String multirDir = args[1];
		String collapse = args[2];
		String useMultiLabelsString = args[3];
		boolean collapseSentences = collapse.equals("true") ? true : false;
		boolean useMultiLabels = useMultiLabelsString.equals("true") ? true : false;
		Integer mentionThreshold = Integer.parseInt(args[4]);
		
		File multirDirFile = new File(multirDir);
		
		if(!multirDirFile.exists()){
			multirDirFile.mkdir();
		}
		
		Preprocess.run(featureFile,multirDir,new Random(1),collapseSentences,useMultiLabels,mentionThreshold);
	}

}
