package edu.washington.multir.development;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import edu.washington.multirframework.multiralgorithm.MILDocument;
import edu.washington.multirframework.multiralgorithm.SparseBinaryVector;

public class BagManipulation {
	
	private static Random r = new Random(1);
	private static String newArgSuffix = "%";


	public static void main (String[] args) throws IOException{
		run(args[0],args[1]);
	}
	
	public static void run(String pathToTrain, String pathToNewTrain) throws IOException{
		MILDocument d = new MILDocument();
		List<MILDocument> l = new ArrayList<MILDocument>();
		DataInputStream dis = new DataInputStream(new BufferedInputStream
				(new FileInputStream(pathToTrain)));
		
		
		Map<Set<Integer>,Set<MILDocument>> relationMentionMap = new HashMap<>();
		List<MILDocument> splitBags = new ArrayList<>();
		
		while (d.read(dis)) {
			//if d num mentions > 15
			if(d.Y.length > 0 && d.numMentions > 15){
				splitBags.addAll(splitBag(d));
			}
			
			//if d num mentions < 5
			else if(d.Y.length > 0 && d.numMentions < 5){
				Set<Integer> relSet = new HashSet<>();
				for(Integer rel : d.Y){
					relSet.add(rel);
				}
				if(relationMentionMap.containsKey(relSet)){
					relationMentionMap.get(relSet).add(d);
				}
			}
			
			else{
				l.add(d);
			}
			d = new MILDocument();
		}
		dis.close();
		
		DataOutputStream dos = new DataOutputStream(new BufferedOutputStream( new FileOutputStream(pathToNewTrain)));
		
		//write docs whose mention count is between 5 and 15
		for(MILDocument md : l){
			md.write(dos);
		}
		
		//write split mildocs
		for(MILDocument md : splitBags){
			md.write(dos);
		}
		
		dos.close();
	}

	private static List<MILDocument> splitBag(
			MILDocument d) {
		List<MILDocument> newBags = new ArrayList<>();
		MILDocument bigBag = new MILDocument();
		//set big bag to be first 15, and last non-15 bag will be added to it
		bigBag.Y = d.Y;
		bigBag.arg1 = d.arg1;
		bigBag.arg2 = d.arg2;
		bigBag.features = Arrays.copyOf(d.features, 15, SparseBinaryVector[].class);
		bigBag.mentionIDs = Arrays.copyOf(d.mentionIDs,15);
		bigBag.numMentions = 15;
		bigBag.Z = Arrays.copyOf(d.Z, 15);
		
		MILDocument lastBag = null;
		int bagCount = 0;
		for(int j = 15; j < d.numMentions; j+=15){		
			if(lastBag != null){
				newBags.add(lastBag);
				bagCount++;
			}
			int k =0;
			for(k = 0; k < 15; k++){
				if( (k+j) == d.numMentions){
					break;
				}
			}
			int index = j+k;
			lastBag = new MILDocument();
			lastBag.Y = d.Y;
			lastBag.arg1 = d.arg1+newArgSuffix+bagCount;
			lastBag.arg2 = d.arg2+newArgSuffix+bagCount;
			lastBag.features = Arrays.copyOfRange(d.features, j, index, SparseBinaryVector[].class);
			lastBag.mentionIDs = Arrays.copyOfRange(d.mentionIDs,j,index);
			lastBag.numMentions = k +1;
			lastBag.Z = Arrays.copyOfRange(d.Z, j, index);
		}
		
		if(lastBag.numMentions == 15){
			newBags.add(lastBag);
		}
		//combine with bigBag
		else{
			List<SparseBinaryVector> newFeatures = Arrays.asList(bigBag.features);
			newFeatures.addAll(Arrays.asList(lastBag.features));
			bigBag.features = (SparseBinaryVector[]) newFeatures.toArray();

			int[] newMentionIds = new int[bigBag.numMentions+lastBag.numMentions];
			for(int i =0; i < bigBag.numMentions; i++){
				newMentionIds[i] = bigBag.mentionIDs[i];
			}
			for(int j =0; j <lastBag.numMentions; j++){
				newMentionIds[bigBag.numMentions+j] = lastBag.mentionIDs[j];
			}
			bigBag.mentionIDs = newMentionIds;
			
			int[] newZ = new int[bigBag.numMentions+lastBag.numMentions];
			for(int i =0; i < bigBag.numMentions; i++){
				newZ[i] = bigBag.Z[i];
			}
			for(int j =0; j <lastBag.numMentions; j++){
				newZ[bigBag.numMentions+j] = lastBag.Z[j];
			}
			bigBag.Z = newZ;
			
			bigBag.numMentions = bigBag.numMentions + lastBag.numMentions;
			newBags.add(bigBag);
		}
		return newBags;
	}
	
}
