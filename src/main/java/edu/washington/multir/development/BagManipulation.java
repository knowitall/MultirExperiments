package edu.washington.multir.development;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import edu.washington.multirframework.multiralgorithm.MILDocument;

public class BagManipulation {
	
	private static Random r = new Random(1);

	public static void main (String[] args) throws IOException{
		run(args[0],args[1]);
	}
	
	public static void run(String pathToTrain, String pathToNewTrain) throws IOException{
		MILDocument d = new MILDocument();
		List<MILDocument> l = new ArrayList<MILDocument>();
		DataInputStream dis = new DataInputStream(new BufferedInputStream
				(new FileInputStream(pathToTrain)));
		
		
		Map<Set<Integer>,Set<MILDocument>> relationMentionMap = new HashMap<>();
		
		
		while (d.read(dis)) {
			//if d num mentions > 15
			if(d.Y.length > 0 && d.numMentions > 15){
				int j = d.numMentions / 15;
				for(int i =1; i < j; i++){
					
				}
			}
			
			//if d num mentions < 5
			else if(d.Y.length > 0 && d.numMentions < 5){
				
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
		
		dos.close();
	}
	
}
