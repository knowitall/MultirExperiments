package edu.washington.multirframework.argumentidentification;

import java.util.List;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import edu.washington.multirframework.corpus.TokenOffsetInformation.SentenceRelativeCharacterOffsetBeginAnnotation;
import edu.washington.multirframework.corpus.TokenOffsetInformation.SentenceRelativeCharacterOffsetEndAnnotation;
import edu.washington.multirframework.data.Argument;

public class TestArgumentIdentification implements ArgumentIdentification {

	private static TestArgumentIdentification instance = null;
	
	public static TestArgumentIdentification getInstance(){
		if(instance == null){
			instance = new TestArgumentIdentification();
		}
		return instance;
	}
	@Override
	public List<Argument> identifyArguments(Annotation d, CoreMap s) {
		List<Argument> arguments = NERArgumentIdentification.getInstance().identifyArguments(d, s);
		
		List<CoreLabel> tokens = s.get(CoreAnnotations.TokensAnnotation.class);
		for(CoreLabel token : tokens){
			String tokenString = token.get(CoreAnnotations.TextAnnotation.class);
			if(tokenString.toLowerCase().equals("he")){
				arguments.add(new Argument(tokenString,token.get(SentenceRelativeCharacterOffsetBeginAnnotation.class),
						token.get(SentenceRelativeCharacterOffsetEndAnnotation.class)));
			}
		}
		return arguments;
	}

}
