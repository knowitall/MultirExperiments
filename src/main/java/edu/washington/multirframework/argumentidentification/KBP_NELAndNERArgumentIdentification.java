package edu.washington.multirframework.argumentidentification;

import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Interval;
import edu.washington.multirframework.data.Argument;

public class KBP_NELAndNERArgumentIdentification implements ArgumentIdentification{

	
	private static KBP_NELAndNERArgumentIdentification instance = null;
	
	private KBP_NELAndNERArgumentIdentification(){}
	public static KBP_NELAndNERArgumentIdentification getInstance(){
		if(instance == null) instance = new KBP_NELAndNERArgumentIdentification();
		return instance;
		}
	
	@Override
	public List<Argument> identifyArguments(Annotation d, CoreMap s) {
		List<Argument> nerArguments = ExtendedNERArgumentIdentification.getInstance().identifyArguments(d, s);
		List<Argument> nelArguments = NELArgumentIdentification.getInstance().identifyArguments(d, s);
		List<Argument> args = new ArrayList<Argument>();
		args.addAll(nelArguments);
		for(Argument nerArg : nerArguments){
			boolean intersects = false;
			for(Argument nelArg: nelArguments){
				Interval<Integer> nerArgInterval = Interval.toInterval(nerArg.getStartOffset(), nerArg.getEndOffset());
				Interval<Integer> nelArgInterval = Interval.toInterval(nelArg.getStartOffset(), nelArg.getEndOffset());
				if(nerArgInterval.intersect(nelArgInterval) !=null){
					intersects = true;
				}
			}
			if(!intersects){
				args.add(nerArg);
			}
		}
		return args;
	}
	
}
