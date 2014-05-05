package edu.washington.multirframework.featuregeneration;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.Triple;
import edu.washington.multirframework.corpus.Corpus;
import edu.washington.multirframework.corpus.CustomCorpusInformationSpecification;
import edu.washington.multirframework.corpus.DefaultCorpusInformationSpecification;
import edu.washington.multirframework.corpus.SentFreebaseNotableTypeInformation;
import edu.washington.multirframework.corpus.SentInformationI;
import edu.washington.multirframework.corpus.SentNamedEntityLinkingInformation;
import edu.washington.multirframework.corpus.TokenChunkInformation;
import edu.washington.multirframework.corpus.TokenInformationI;
import edu.washington.multirframework.corpus.TokenOffsetInformation.SentenceRelativeCharacterOffsetBeginAnnotation;
import edu.washington.multirframework.featuregeneration.FeatureGeneratorDraft3.DependencyType;

public class DefaultFeatureGeneratorGeneralizedTwoMinusDirPath implements FeatureGenerator{

	private static final int WINDOW_SIZE = 2;
	private static final String MIDDLE_PREFIX = "m:";
	private static final String LEFT_PREFIX = "lw:";
	private static final String RIGHT_PREFIX = "rw:";
	private static final String INVERSE_TRUE_FEATURE = "INVERSE:true";
	private static final String INVERSE_FALSE_FEATURE = "INVERSE:false";
	private static final String GENERAL_FEATURE = "g:";
	private static final String BIGRAM_FEATURE = "b:";
	private static final String DISTANCE_FEATURE = "d:";
	private static final String TYPE_FEATURE = "t:";
	private static final String HEIGHT_FEATURE = "h:";
	private static final String DEP_Feature = "dep:";
        private static List<String> defaultBigram;
        private static DefaultFeatureGeneratorMinusDirPath OldFeatureGenerator = new DefaultFeatureGeneratorMinusDirPath();

	static{
		defaultBigram = new ArrayList<String>();
		defaultBigram.add("#PAD#");
		defaultBigram.add("#PAD#");
	}

	@Override
	public List<String> generateFeatures(Integer arg1StartOffset,
			Integer arg1EndOffset, Integer arg2StartOffset,
			Integer arg2EndOffset, String arg1ID, String arg2ID,
			CoreMap sentence, Annotation document) {

		List<String> features = new ArrayList<String>();
		List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);

		Pair<Integer,Integer> leftArgOffsets = FeatureGeneratorMethods.getLeftArgOffsets(arg1StartOffset,arg1EndOffset,arg2StartOffset,arg2EndOffset);
		Pair<Integer,Integer> rightArgOffsets =FeatureGeneratorMethods.getRightArgOffsets(arg1StartOffset,arg1EndOffset,arg2StartOffset,arg2EndOffset);
		String inverseFeature = INVERSE_FALSE_FEATURE;
		if(arg1StartOffset > arg2StartOffset){
			inverseFeature = INVERSE_TRUE_FEATURE;
		}

		String typeFeature = TYPE_FEATURE + getTypeFeature(leftArgOffsets,rightArgOffsets,tokens);

		//get exact middle token sequence
		List<CoreLabel> middleTokens = FeatureGeneratorMethods.getMiddleTokens(leftArgOffsets.second, rightArgOffsets.first, tokens);
		List<Triple<CoreLabel,DependencyType,CoreLabel>> dependencyPathMiddleTokens = FeatureGeneratorMethods.getDependencyPath(leftArgOffsets.second,rightArgOffsets.second,sentence);
		List<Triple<CoreLabel,DependencyType,CoreLabel>> dependencyPathLeftWindow = FeatureGeneratorMethods.getDependencyPathWindow(leftArgOffsets.second, sentence);
		List<Triple<CoreLabel,DependencyType,CoreLabel>> dependencyPathRightWindow = FeatureGeneratorMethods.getDependencyPathWindow(rightArgOffsets.second, sentence);
		
		//leftWindow
		System.out.println("Printing left dep windows");
		for(Triple<CoreLabel,DependencyType,CoreLabel> t: dependencyPathLeftWindow){
			System.out.println(t.first.get(CoreAnnotations.TextAnnotation.class) + "\t" + t.second + "\t" +t.third.get(CoreAnnotations.TextAnnotation.class));
		}
		
		
		System.out.println("Printing right dep windows");
		for(Triple<CoreLabel,DependencyType,CoreLabel> t: dependencyPathRightWindow){
			System.out.println(t.first.get(CoreAnnotations.TextAnnotation.class) + "\t" + t.second + "\t" +t.third.get(CoreAnnotations.TextAnnotation.class));
		}

		

		List<CoreLabel> leftWindowTokens = FeatureGeneratorMethods.getLeftWindowTokens(leftArgOffsets.first, tokens, WINDOW_SIZE);
		List<CoreLabel> rightWindowTokens = FeatureGeneratorMethods.getRightWindowTokens(rightArgOffsets.second, tokens, WINDOW_SIZE);
		List<CoreLabel> leftTokens = FeatureGeneratorMethods.getLeftWindowTokens(leftArgOffsets.first, tokens);
		List<CoreLabel> rightTokens = FeatureGeneratorMethods.getRightWindowTokens(rightArgOffsets.second, tokens);

		// generalize tokens
		List<String> generalMiddleTokens = FeatureGeneratorMethods.getGeneralSequence(middleTokens);
		List<String> generalLeftTokens = FeatureGeneratorMethods.getGeneralSequence(leftTokens);
		List<String> generalRightTokens = FeatureGeneratorMethods.getGeneralSequence(rightTokens);

		String middleGeneralizedTokenSequenceFeature = makeSequenceFeature(generalMiddleTokens,inverseFeature,typeFeature,MIDDLE_PREFIX,GENERAL_FEATURE);
		String leftGeneralizedTokenSequenceFeature = makeSequenceFeature(generalLeftTokens.subList(Math.max(generalLeftTokens.size()-3,0), generalLeftTokens.size()),LEFT_PREFIX);
		String rightGeneralizedTokenSequenceFeature = makeSequenceFeature(generalRightTokens.subList(0,Math.min(generalRightTokens.size(), 3)),RIGHT_PREFIX);
		if(isGeneralized(middleGeneralizedTokenSequenceFeature)) features.add(middleGeneralizedTokenSequenceFeature);
		if(isGeneralized(middleGeneralizedTokenSequenceFeature+ " "+leftGeneralizedTokenSequenceFeature))features.add(middleGeneralizedTokenSequenceFeature+ " "+leftGeneralizedTokenSequenceFeature);
		if(isGeneralized(middleGeneralizedTokenSequenceFeature+ " "+ rightGeneralizedTokenSequenceFeature))features.add(middleGeneralizedTokenSequenceFeature+ " "+ rightGeneralizedTokenSequenceFeature);

		if(dependencyPathMiddleTokens.size() > 0){
			String middleDependencySequenceFeature = makeFeature(getDependencyPathMiddleSequenceFeature(dependencyPathMiddleTokens,leftArgOffsets,rightArgOffsets,GeneralizationClass.None)
					,inverseFeature,typeFeature,DEP_Feature);
			String generalizedMiddleDependencySequenceFeature = makeFeature(
					getDependencyPathMiddleSequenceFeature(dependencyPathMiddleTokens,leftArgOffsets,rightArgOffsets,GeneralizationClass.First),
					inverseFeature,typeFeature,DEP_Feature,GENERAL_FEATURE);
			String feature = generalizedMiddleDependencySequenceFeature;
			if(isGeneralized(feature)) features.add(feature);
			feature = generalizedMiddleDependencySequenceFeature+" "+leftGeneralizedTokenSequenceFeature;
			if(isGeneralized(feature)) features.add(feature);
			feature = generalizedMiddleDependencySequenceFeature+" "+rightGeneralizedTokenSequenceFeature;
			if(isGeneralized(feature)) features.add(feature);
		}

                features.addAll(OldFeatureGenerator.generateFeatures(arg1StartOffset, arg1EndOffset, arg2StartOffset, arg2EndOffset, arg1ID, arg2ID,
                                                                     sentence, document));

		return features;
	}


	private boolean isGeneralized(String featureStr) {
		if (featureStr.contains("(") && featureStr.contains(")")) return true;
		return false;
	}


	private String getDependencyPathMiddleSequenceFeature(
			List<Triple<CoreLabel, DependencyType, CoreLabel>> dependencyPathMiddleTokens,
			Pair<Integer,Integer> leftArgOffsets, Pair<Integer,Integer> rightArgOffsets, GeneralizationClass gc) {
		StringBuilder featureBuilder = new StringBuilder();
		for(int i = 0; i < dependencyPathMiddleTokens.size(); i++){
			Triple<CoreLabel,DependencyType,CoreLabel> trip = dependencyPathMiddleTokens.get(i);
			featureBuilder.append(trip.second);
			if(i!=dependencyPathMiddleTokens.size()-1){
				String text = trip.third.get(CoreAnnotations.TextAnnotation.class);
				switch(gc){
				case None:
					break;
				case First:
					String lemma = FeatureGeneratorMethods.getWordnetStemFeature(trip.third);
					if(lemma != null){
						text = lemma;
					}
					break;
				default:
					break;
				}
				featureBuilder.append(text);
			}
			featureBuilder.append(" ");
		}
		return featureBuilder.toString().trim();
	}

	
	//private String getDependencyPath







	private String getTypeFeature(Pair<Integer, Integer> leftArgOffsets,
			Pair<Integer, Integer> rightArgOffsets, List<CoreLabel> tokens) {
		StringBuilder sb = new StringBuilder();
		String leftType = "O";
		String rightType = "O";
		
		for(CoreLabel t: tokens){
			Integer startOffset = t.get(SentenceRelativeCharacterOffsetBeginAnnotation.class);
			if(startOffset.equals(leftArgOffsets.first)){
				leftType = t.get(CoreAnnotations.NamedEntityTagAnnotation.class);
			}
			if(startOffset.equals(rightArgOffsets.first)){
				rightType = t.get(CoreAnnotations.NamedEntityTagAnnotation.class);
			}
		}
		
		sb.append(leftType);
		sb.append(" ");
		sb.append(rightType);
		return sb.toString().trim();
	}



	
	private List<List<String>> getRightBigrams(List<String> generalTokens, int numBigrams){
		List<List<String>> bigrams = new ArrayList<List<String>>();
		
		int i =0;
		while((i<generalTokens.size()-1) && (bigrams.size()<numBigrams)){
			
			List<String> bigram = new ArrayList<String>();
			bigram.add(generalTokens.get(i));
			bigram.add(generalTokens.get(i+1));
			
			if(!bigram.equals(defaultBigram)){
				bigrams.add(bigram);
			}	
			i++;
		}
		return bigrams;
	}
	
	private List<List<String>> getLeftBigrams(List<String> tokens, int numBigrams){
		List<List<String>> bigrams = new ArrayList<List<String>>();
		
		int i = tokens.size()-1;
		while((i>0) && (bigrams.size()<numBigrams)){
			
			List<String> bigram = new ArrayList<String>();
			bigram.add(tokens.get(i-1));
			bigram.add(tokens.get(i));
			
			if(!bigram.equals(defaultBigram)){
				bigrams.add(bigram);
			}	
			i--;
		}
		return bigrams;
	}


	private String makeSequenceFeature(List<String> generalTokens,
			String ... prefixes) {
		StringBuilder featureBuilder = new StringBuilder();
		
		//add prefixes
		for(int i =0; i < prefixes.length; i++){
			featureBuilder.append(prefixes[i]);
			featureBuilder.append(" ");
		}
		
		for(String s : generalTokens){
			featureBuilder.append(s);
			featureBuilder.append(" ");
		}
		
		return featureBuilder.toString().trim();
	}
	
	private String makeTokenFeature(List<CoreLabel> tokens,
			String ... prefixes) {
		StringBuilder featureBuilder = new StringBuilder();
		
		//add prefixes
		for(int i =0; i < prefixes.length; i++){
			featureBuilder.append(prefixes[i]);
			featureBuilder.append(" ");
		}
		
		for(CoreLabel t : tokens){
			featureBuilder.append(t.get(CoreAnnotations.TextAnnotation.class));
			featureBuilder.append(" ");
		}
		
		return featureBuilder.toString().trim();
	}
	
	private String makeFeature(String featureString, String ... prefixes){
		StringBuilder featureBuilder = new StringBuilder();

		//add prefixes
		for(int i =0; i < prefixes.length; i++){
			featureBuilder.append(prefixes[i]);
			featureBuilder.append(" ");
		}
		
		featureBuilder.append(featureString);
		return featureBuilder.toString().trim();

	}


	public static enum Direction{
		UP,DOWN
	}
	
	private static enum GeneralizationClass{
		None,First,Second
	}
	
	
	public static void main(String [] args) throws SQLException{
		
		CustomCorpusInformationSpecification cis = new DefaultCorpusInformationSpecification();
		List<SentInformationI> sentInfo = new ArrayList<>();
		sentInfo.add(new SentFreebaseNotableTypeInformation());
		sentInfo.add(new SentNamedEntityLinkingInformation());
		List<TokenInformationI> tokInfo = new ArrayList<>();
		tokInfo.add(new TokenChunkInformation());
		cis.addSentenceInformation(sentInfo);
		cis.addTokenInformation(tokInfo);
		Corpus c = new Corpus("jdbc:derby://rv-n14.cs.washington.edu:49152//scratch2/usr/jgilme1/FullCorpus",cis,true);
		c.setCorpusToTest("/projects/WebWare6/Multir/MultirSystem/files/testDocuments");
		
		Set<Integer> sentIds = new HashSet<>();
		sentIds.add(27342657);
		sentIds.add(27344192);
		Map<Integer,Pair<CoreMap,Annotation>> pairs =c.getAnnotationPairsForEachSentence(sentIds);
		Pair<CoreMap,Annotation> pair = pairs.get(27342657);
		
		FeatureGenerator fg = new DefaultFeatureGeneratorGeneralizedTwoMinusDirPath();
		List<String> features = fg.generateFeatures(41, 52, 28, 39, "/m/03wfx84", "/m/0t8v1", pair.first, pair.second);
		
		System.out.println("Feature set 1");
		for(String f: features){
			System.out.println(f);
		}
		pair = pairs.get(27344192);
		features = fg.generateFeatures(133, 145, 207, 219, "/m/035n85", "/m/0d8r0q", pair.first, pair.second);
		System.out.println("Feature set 2");
		for(String f: features){
			System.out.println(f);
		}
		
	}
	

}
