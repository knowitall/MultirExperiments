package edu.washington.multir.distantsupervision;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.time.SUTime;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.Triple;
import edu.washington.multir.data.TypeSignatureRelationMap;
import edu.washington.multir.util.FigerTypeUtils;
import edu.washington.multir.util.TypeConstraintUtils;
import edu.washington.multir.util.TypeConstraintUtils.GeneralType;
import edu.washington.multirframework.argumentidentification.ArgumentIdentification;
import edu.washington.multirframework.argumentidentification.FigerAndNERTypeSignatureORGDATESententialInstanceGeneration;
import edu.washington.multirframework.argumentidentification.FigerAndNERTypeSignatureORGLOCSententialInstanceGeneration;
import edu.washington.multirframework.argumentidentification.FigerAndNERTypeSignatureORGNUMSententialInstanceGeneration;
import edu.washington.multirframework.argumentidentification.FigerAndNERTypeSignatureORGORGSententialInstanceGeneration;
import edu.washington.multirframework.argumentidentification.FigerAndNERTypeSignatureORGOTHERSententialInstanceGeneration;
import edu.washington.multirframework.argumentidentification.FigerAndNERTypeSignatureORGPERSententialInstanceGeneration;
import edu.washington.multirframework.argumentidentification.FigerAndNERTypeSignaturePERDATESententialInstanceGeneration;
import edu.washington.multirframework.argumentidentification.FigerAndNERTypeSignaturePERLOCSententialInstanceGeneration;
import edu.washington.multirframework.argumentidentification.FigerAndNERTypeSignaturePERNUMSententialInstanceGeneration;
import edu.washington.multirframework.argumentidentification.FigerAndNERTypeSignaturePERORGSententialInstanceGeneration;
import edu.washington.multirframework.argumentidentification.FigerAndNERTypeSignaturePEROTHERSententialInstanceGeneration;
import edu.washington.multirframework.argumentidentification.FigerAndNERTypeSignaturePERPERSententialInstanceGeneration;
import edu.washington.multirframework.argumentidentification.KBP_NELAndNERArgumentIdentification;
import edu.washington.multirframework.argumentidentification.NELByTypeRelationMatching;
import edu.washington.multirframework.argumentidentification.RelationMatching;
import edu.washington.multirframework.argumentidentification.SententialInstanceGeneration;
import edu.washington.multirframework.corpus.Corpus;
import edu.washington.multirframework.corpus.CorpusInformationSpecification;
import edu.washington.multirframework.corpus.CorpusInformationSpecification.SentGlobalIDInformation.SentGlobalID;
import edu.washington.multirframework.corpus.CustomCorpusInformationSpecification;
import edu.washington.multirframework.corpus.DefaultCorpusInformationSpecification;
import edu.washington.multirframework.corpus.SentInformationI;
import edu.washington.multirframework.corpus.SentNamedEntityLinkingInformation;
import edu.washington.multirframework.data.Argument;
import edu.washington.multirframework.data.KBArgument;
import edu.washington.multirframework.data.NegativeAnnotation;
import edu.washington.multirframework.distantsupervision.DistantSupervision;
import edu.washington.multirframework.distantsupervision.NegativeExampleCollection;
import edu.washington.multirframework.distantsupervision.NegativeExampleCollectionByRatio;
import edu.washington.multirframework.knowledgebase.KnowledgeBase;
import edu.washington.multirframework.util.BufferedIOUtils;

public class KBPDistantSupervision {
	private List<SententialInstanceGeneration> sigList;
	private List<String> outputPaths;
	private ArgumentIdentification ai;
	private RelationMatching rm;
	private NegativeExampleCollection nec;
	private List<PrintWriter> writers;

	
	public static void main(String[] args) throws SQLException, IOException{
		
		ArgumentIdentification ai = KBP_NELAndNERArgumentIdentification.getInstance();
		List<String> outputPaths = new ArrayList<>();
		List<SententialInstanceGeneration> sigList = new ArrayList<>();
		RelationMatching rm = NELByTypeRelationMatching.getInstance();
		NegativeExampleCollection nec = NegativeExampleCollectionByRatio.getInstance(1.0);
		KnowledgeBase kb = new KnowledgeBase("/homes/gws/jgilme1/KBPMultir/NewKnowledgeBase/FromFBDump/kbpMultirKB-Full.tsv.gz",
				"/homes/gws/jgilme1/KBPMultir/NewKnowledgeBase/FromFBDump/entities.tsv","/homes/gws/jgilme1/KBPMultir/NewKnowledgeBase/kbp-multir-relations");
		CustomCorpusInformationSpecification cis = new DefaultCorpusInformationSpecification();
		List<SentInformationI> sentInformationList = new ArrayList<>();
		sentInformationList.add(new SentNamedEntityLinkingInformation());
		cis.addSentenceInformation(sentInformationList);
		Corpus c = new Corpus("/scratch2/code/multir-reimplementation/MultirExtractor/FullCorpus-UIUCNotableTypes",cis,true);
		TypeSignatureRelationMap.init("/homes/gws/jgilme1/KBPMultir/DistantSupervision/partition-relation-map");
		
		
		
		outputPaths.add("/homes/gws/jgilme1/KBPMultir/DistantSupervision/100Sententces/PERPER-DS");
		outputPaths.add("/homes/gws/jgilme1/KBPMultir/DistantSupervision/100Sententces/PERDATE-DS");
		outputPaths.add("/homes/gws/jgilme1/KBPMultir/DistantSupervision/100Sententces/PERNUM-DS");
		outputPaths.add("/homes/gws/jgilme1/KBPMultir/DistantSupervision/100Sententces/PERLOC-DS");
		outputPaths.add("/homes/gws/jgilme1/KBPMultir/DistantSupervision/100Sententces/PEROTHER-DS");
		outputPaths.add("/homes/gws/jgilme1/KBPMultir/DistantSupervision/100Sententces/PERORG-DS");
		outputPaths.add("/homes/gws/jgilme1/KBPMultir/DistantSupervision/100Sententces/ORGORG-DS");
		outputPaths.add("/homes/gws/jgilme1/KBPMultir/DistantSupervision/100Sententces/ORGOTHER-DS");
		outputPaths.add("/homes/gws/jgilme1/KBPMultir/DistantSupervision/100Sententces/ORGPER-DS");
		outputPaths.add("/homes/gws/jgilme1/KBPMultir/DistantSupervision/100Sententces/ORGNUM-DS");
		outputPaths.add("/homes/gws/jgilme1/KBPMultir/DistantSupervision/100Sententces/ORGDATE-DS");
		outputPaths.add("/homes/gws/jgilme1/KBPMultir/DistantSupervision/100Sententces/ORGLOC-DS");

		sigList.add(FigerAndNERTypeSignaturePERPERSententialInstanceGeneration.getInstance());
		sigList.add(FigerAndNERTypeSignaturePERDATESententialInstanceGeneration.getInstance());
		sigList.add(FigerAndNERTypeSignaturePERNUMSententialInstanceGeneration.getInstance());
		sigList.add(FigerAndNERTypeSignaturePERLOCSententialInstanceGeneration.getInstance());
		sigList.add(FigerAndNERTypeSignaturePEROTHERSententialInstanceGeneration.getInstance());
		sigList.add(FigerAndNERTypeSignaturePERORGSententialInstanceGeneration.getInstance());
		sigList.add(FigerAndNERTypeSignatureORGORGSententialInstanceGeneration.getInstance());
		sigList.add(FigerAndNERTypeSignatureORGOTHERSententialInstanceGeneration.getInstance());
		sigList.add(FigerAndNERTypeSignatureORGPERSententialInstanceGeneration.getInstance());
		sigList.add(FigerAndNERTypeSignatureORGNUMSententialInstanceGeneration.getInstance());
		sigList.add(FigerAndNERTypeSignatureORGDATESententialInstanceGeneration.getInstance());
		sigList.add(FigerAndNERTypeSignatureORGLOCSententialInstanceGeneration.getInstance());
				
		//DateMap dm = new DateMap("/path");
		DateMap dm = null;
		
		KBPDistantSupervision ds = new KBPDistantSupervision(ai,outputPaths,sigList,rm,nec);
		ds.run(kb,dm,c);
	}
	
	public KBPDistantSupervision(ArgumentIdentification ai, List<String> outputPaths, List<SententialInstanceGeneration> sigList, 
			RelationMatching rm, NegativeExampleCollection nec){
		this.sigList = sigList;
		this.ai = ai;
		this.rm =rm;
		this.nec = nec;
		this.outputPaths=outputPaths;
		if(outputPaths.size()!=sigList.size()){
			throw new IllegalArgumentException("Number of SentenceInstanceGeneration specifications must equal number of output paths");
		}
	}

	public void run(KnowledgeBase kb, DateMap dm, Corpus c) throws SQLException, IOException{
    	long start = System.currentTimeMillis();
    	
    	writers = new ArrayList<PrintWriter>();
    	for(int j =0; j < outputPaths.size(); j++){
    	  writers.add(new PrintWriter(BufferedIOUtils.getBufferedWriter(new File(outputPaths.get(j)))));
    	}
		Iterator<Annotation> di = c.getDocumentIterator();
		int count =0;
		long startms = System.currentTimeMillis();
		while(di.hasNext()){
			Annotation d = di.next();
			List<CoreMap> sentences = d.get(CoreAnnotations.SentencesAnnotation.class);
			List<List<Argument>> argumentList = new ArrayList<>();
			for(CoreMap sentence : sentences){
			  argumentList.add(ai.identifyArguments(d, sentence));
			}
			for(int j =0; j < sigList.size(); j++){
				SententialInstanceGeneration sig = sigList.get(j);
		    	PrintWriter dsWriter = writers.get(j);
				List<NegativeAnnotation> documentNegativeExamples = new ArrayList<>();
				List<Pair<Triple<KBArgument,KBArgument,String>,Integer>> documentPositiveExamples = new ArrayList<>();
				int sentIndex = 0;
				for(CoreMap sentence : sentences){
					int sentGlobalID = sentence.get(SentGlobalID.class);
					

									
					//argument identification
					List<Argument> arguments =  argumentList.get(sentIndex);
					//sentential instance generation
					List<Pair<Argument,Argument>> sententialInstances = sig.generateSententialInstances(arguments, sentence);
					if(sententialInstances.size() > 0){
						List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
						String arg1Type = TypeConstraintUtils.translateNERTypeToTypeString(TypeConstraintUtils.getNERType(sententialInstances.get(0).first, tokens));
						String arg2Type = TypeConstraintUtils.translateNERTypeToTypeString(TypeConstraintUtils.getNERType(sententialInstances.get(0).second,tokens));	
						
						//handle DATE
//						if(arg2Type.equals(GeneralType.DATE)){
//							List<Triple<KBArgument,KBArgument,String>> distantSupervisionAnnotations = 
//									getDateRelations(sententialInstances,dm,sentence,d);
//
//						}
						
						//handle NUM
						
						//handle WEBSITE
						
						//relation matching
						List<Triple<KBArgument,KBArgument,String>> distantSupervisionAnnotations = 
								rm.matchRelations(sententialInstances,kb,sentence,d);
													
						
						
						//adding sentence IDs
						List<Pair<Triple<KBArgument,KBArgument,String>,Integer>> dsAnnotationWithSentIDs = new ArrayList<>();
						for(Triple<KBArgument,KBArgument,String> trip : distantSupervisionAnnotations){
							Integer i = new Integer(sentGlobalID);
							Pair<Triple<KBArgument,KBArgument,String>,Integer> p = new Pair<>(trip,i);
							dsAnnotationWithSentIDs.add(p);
						}
						//negative example annotations
						List<NegativeAnnotation> negativeExampleAnnotations = null;
						negativeExampleAnnotations =
								  findNegativeExampleAnnotations(sententialInstances,distantSupervisionAnnotations,
										  kb,sentGlobalID, sentence, d);
						
						documentNegativeExamples.addAll(negativeExampleAnnotations);
						documentPositiveExamples.addAll(dsAnnotationWithSentIDs);
					}
					sentIndex++;
					
				}
				DistantSupervision.writeDistantSupervisionAnnotations(documentPositiveExamples,dsWriter);
				DistantSupervision.writeNegativeExampleAnnotations(nec.filter(documentNegativeExamples,documentPositiveExamples,kb,sentences),dsWriter);
			}
			count++;
			if( count % 1000 == 0){
				long endms = System.currentTimeMillis();
				System.out.println(count + " documents processed");
				System.out.println("Time took = " + (endms-startms));
			}
		}
		
		for(int j =0; j < writers.size(); j++){
			writers.get(j).close();
		}
    	long end = System.currentTimeMillis();
    	System.out.println("Distant Supervision took " + (end-start) + " millisseconds");
	}
	
	private List<String> getCandidateEntities(KnowledgeBase kb,String argumentName){
		Map<String,List<String>> entityMap = kb.getEntityMap();
		
		if(entityMap.containsKey(argumentName)){
			return entityMap.get(argumentName);
		}
		else{
			return new ArrayList<String>();
		}
	}
	
//	private List<Triple<KBArgument, KBArgument, String>> getDateRelations(
//			List<Pair<Argument, Argument>> sententialInstances, KnowledgeBase kb,
//			DateMap dm, CoreMap sentence, Annotation d) {
//		
//		for(Pair<Argument,Argument> p : sententialInstances){
//			if(p.first instanceof KBArgument){
//				
//			}
//			else{
//				List<String> candidateArg1Ids = getCandidateEntities(kb,p.first.getArgName());
//				//if(dm.matchesValue(arg1Id, rel, timexValue))
//				
//			}
//		}
//
//	}

	private  List<NegativeAnnotation> findNegativeExampleAnnotations(
			List<Pair<Argument, Argument>> sententialInstances,
			List<Triple<KBArgument, KBArgument, String>> distantSupervisionAnnotations,
			KnowledgeBase KB, Integer sentGlobalID, CoreMap sentence, Annotation doc) {
		
		Map<String,List<String>> entityMap = KB.getEntityMap();
		List<NegativeAnnotation> negativeExampleAnnotations = new ArrayList<>();
		
		
		
		String arg1Type = "OTHER";
		String arg2Type = "OTHER";
		List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
		if(sententialInstances.size() > 0){
			arg1Type = TypeConstraintUtils.translateNERTypeToTypeString(TypeConstraintUtils.getNERType(sententialInstances.get(0).first, tokens));
			arg2Type = TypeConstraintUtils.translateNERTypeToTypeString(TypeConstraintUtils.getNERType(sententialInstances.get(0).second,tokens));		
			
			List<String> typeAppropriateRelations = TypeSignatureRelationMap.getRelationsForTypeSignature(new Pair<String,String>(arg1Type,arg2Type));
			System.out.println("Arg 1 Type = " + arg1Type);
			System.out.println("Arg 2 Type = " + arg2Type);
			System.out.println("Type Appropriate Relations:");
			for(String rel : typeAppropriateRelations){
				System.out.println(rel);
			}
	
			for(Pair<Argument,Argument> p : sententialInstances){
				//check that at least one argument is not in distantSupervisionAnnotations
				Argument arg1 = p.first;
				Argument arg2 = p.second;
				boolean canBeNegativeExample = true;
				for(Triple<KBArgument,KBArgument,String> t : distantSupervisionAnnotations){
					Argument annotatedArg1 = t.first;
					Argument annotatedArg2 = t.second;
					
					//if sententialInstance is a distance supervision annotation
					//then it is not a negative example candidate
					if( (arg1.getStartOffset() == annotatedArg1.getStartOffset()) &&
						(arg1.getEndOffset() == annotatedArg1.getEndOffset()) &&
						(arg2.getStartOffset() == annotatedArg2.getStartOffset()) &&
						(arg2.getEndOffset() == annotatedArg2.getEndOffset())){
						canBeNegativeExample = false;
						break;
					}
				}
				if(canBeNegativeExample){
					//look for KBIDs, select a random pair
					List<String> arg1Ids = new ArrayList<>();
					if(arg1 instanceof KBArgument){
						   arg1Ids.add(((KBArgument) arg1).getKbId());
					}
					else{
						if(entityMap.containsKey(arg1.getArgName())){
							List<String> candidateArg1Ids = entityMap.get(arg1.getArgName());
						    arg1Ids = candidateArg1Ids;
						}
					}
	
					List<String> arg2Ids = new ArrayList<>();
					if(arg2 instanceof KBArgument){
						arg2Ids.add(((KBArgument) arg2).getKbId());
					}
					else{
						if(entityMap.containsKey(arg2.getArgName())){
							List<String> candidateArg2Ids = entityMap.get(arg2.getArgName());
							arg2Ids = candidateArg2Ids;
						}
					}
					if( (!arg1Ids.isEmpty()) && (!arg2Ids.isEmpty())){
						//check that no pair of entities represented by these
						//argument share a relation:
						if(participatesInTargetRelations(arg1Ids,KB,typeAppropriateRelations)){
							if(KB.noRelationsHold(arg1Ids,arg2Ids)){
								String arg1Id = arg1Ids.get(0);
								String arg2Id = arg2Ids.get(0);
								if((!arg1Id.equals("null")) && (!arg2Id.equals("null"))){
									KBArgument kbarg1 = new KBArgument(arg1,arg1Id);
									KBArgument kbarg2 = new KBArgument(arg2,arg2Id);
									List<String> annoRels = new ArrayList<String>();
									annoRels.add("NA");
									if(annoRels.size()>0){
										NegativeAnnotation negAnno = new NegativeAnnotation(kbarg1,kbarg2,sentGlobalID,annoRels);
										negativeExampleAnnotations.add(negAnno);
									}
								}
							}
						}
					}
				}
			}
		}
		return negativeExampleAnnotations;
	}

	private boolean participatesInTargetRelations(List<String> arg1Ids,
			KnowledgeBase KB, List<String> typeAppropriateRelations) {

		for(String arg1Id: arg1Ids){
			for(String relation: typeAppropriateRelations){
				if(!KB.participatesInRelationAsArg1(arg1Id, relation)){
					return false;
				}
			}
		}
		return true;
	}

	public static class DistantSupervisionAnnotation{
		KBArgument arg1;
		KBArgument arg2;
		String rel;
		Integer sentID;
	}
	
	public static class DateMap {
		
		private Map<String,List<Pair<String,String>>> relMap;
		
		public DateMap(String file) throws IOException{
			BufferedReader br = new BufferedReader(new FileReader(new File(file)));
			String nextLine;
			while((nextLine = br.readLine())!=null){
				String[] values = nextLine.split("\t");
				String entityId = values[0];
				String rel = values[1];
				String timeString = values[2];
				String timexString = SUTime.parseDateTime(timeString).getTimexValue();
				
				if(relMap.containsKey(entityId)){
					relMap.get(entityId).add(new Pair<>(rel,timexString));
				}
				else{
					List<Pair<String,String>> timeRels = new ArrayList<>();
					timeRels.add(new Pair<>(rel,timexString));
					relMap.put(entityId,timeRels);
				}
			}
			
			br.close();
		}
		
		public boolean participatesInRelation(String arg1Id, String rel){
			List<Pair<String,String>> values = relMap.get(arg1Id);
			if(values != null){
				for(Pair<String,String> p : values){
					if(p.first.equals(rel)){
						return true;
					}
				}
			}
			return false;
		}
		
		public boolean matchesValue(String arg1Id, String rel, String timexValue){
			List<Pair<String,String>> values = relMap.get(arg1Id);
			if(values != null){
				for(Pair<String,String> p : values){
					if(p.first.equals(rel)){
						if(p.second.equals(timexValue)){
							return true;
						}
					}
				}
			}
			return false;
		}
		
		
	}
	
	
}