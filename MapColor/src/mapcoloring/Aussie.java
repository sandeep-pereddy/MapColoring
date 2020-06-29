package mapcoloring;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import org.supercsv.*;
import org.supercsv.io.CsvListWriter;
import org.supercsv.io.ICsvListWriter;
import org.supercsv.prefs.CsvPreference;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Aussie  
{	
	static String folderPath = "C:\\study\\workplace\\visualization\\";
	static ConcurrentHashMap<String, ArrayList<String>> domain = new ConcurrentHashMap<String, ArrayList<String>>();
	static HashMap<String, ArrayList<String>> constraints = new HashMap<String, ArrayList<String>>();
	static ConcurrentHashMap<String,String> mapColor = new ConcurrentHashMap<String,String>();
	static ConcurrentHashMap<String, ArrayList<String>> output = new ConcurrentHashMap<String, ArrayList<String>>();
	static Stack<ConcurrentHashMap<String,ArrayList<String>>> colorStack =new Stack<ConcurrentHashMap<String,ArrayList<String>>>();
	static ConcurrentHashMap<String,ArrayList<String>> failureList = new ConcurrentHashMap<String,ArrayList<String>>();
	static Input input = new Input();

	static String[] colors;
	static int minDomain;
	static String keyMin;
	static String previousKeyMin = "";
	static boolean isBacktracked = false;
	static boolean isBacktrackedHeuristics = false;
	static String tempColor = "";
	static String [] states;
	static boolean isAustralia = false;
	
	static ArrayList<String> domainList = new ArrayList<String>();
	static int k = 0;
	static int loop = 0;
	static int backTrackCount = 0;
	static ConcurrentHashMap<String,ArrayList<String>> heuristicList=new ConcurrentHashMap<String,ArrayList<String>>();
	
	
	public static void main (String [] args) throws FileNotFoundException, IOException, ParseException
	{
		Scanner ss = new Scanner(System.in);
		System.out.println("Choose the country :\n1)United states of America\n2)Australia \nEnter 1 or 2 :");		
		JSONParser parser = new JSONParser();
		JSONObject jsonObject;
		if(ss.nextInt() == 1)
		{
//			Object obj = parser.parse(new FileReader("C:\\Users\\deepa\\OneDrive\\Desktop\\USmap.json"));
			Object obj = parser.parse(input.usaConstraints);
			jsonObject = (JSONObject) obj;
			String[] colors_temp = {"red", "green", "blue","yellow"};
			colors = colors_temp;
		}
		else
		{
//			Object obj = parser.parse(new FileReader("C:\\Users\\deepa\\OneDrive\\Desktop\\ausMap.json"));
			Object obj = parser.parse(input.ausConstaints);
			jsonObject = (JSONObject) obj;
			String[] colors_temp = {"red", "green", "blue"};
			colors = colors_temp;
			isAustralia = true;
		}
		minDomain = colors.length;
						
		/*
		 * get keySet of the jsonObject
		 * to loop through the json, iterator is required (moving to each key using hasNext())
		 * 		take each key as String
		 * 		take each value as jsonArray (convert jsonArray to list)
		 * add key,value pair to new list
		 * */
		
		Set<String> keys = jsonObject.keySet();
//		String [] states = new String[keys.size()];	//to store list of all states
		states = new String[keys.size()];	//to store list of all states
	    int x = 0;	        
	    
        Iterator<String> keysItr = keys.iterator();
        while(keysItr.hasNext())
        {
        	String key = keysItr.next();
            JSONArray value = (JSONArray)jsonObject.get(key);
            states[x++] = key;            
            ArrayList<String> valueList = new ArrayList<String>(); 

    	   for (int i=0; i<value.size(); i++){ 
       	    valueList.add(value.get(i).toString());
       	   }             
            constraints.put(key, valueList);		//hashmap for constraints
        }

        System.out.println("CONSTRAINTS : "+constraints);
        
        //assigning colors to all states (creating domain)
        for(int i=0; i<states.length; i++)
        {
          colors = copyArray(colors);          
          ArrayList<String> domainColors = new ArrayList<String>();        
          for (String c : colors)
        	  domainColors.add(c);
          domain.put(states[i],domainColors);		//hashMap for states and its available colors
        }
        
        System.out.println("DOMAINS : "+domain);
        keyMin = states[0];
        
        System.out.println("\nChoose the type of algorithm :\nWITH HEURISTICS\nEnter 1 Depth First Search\nEnter 2 Depth First Search with forward checking\nEnter 3 Depth First Search with forward checking and singleton propagation");
        System.out.println("WITHOUT HEURISTICS\nEnter 4 Depth First Search\nEnter 5 Depth First Search with forward checking\nEnter 6 Depth First Search with forward checking and singleton propagation \n\nEnter a number : ");
        
        switch(ss.nextInt())
        {
        	case 1:
        		DFS(true);
        		break;
        	case 2:
        		DFSForwardCheck(true);
        		break;
        	case 3:
        		singleton(true);
        		break;
        	case 4:
        		DFS(false);
        		break;
        	case 5:
        		DFSForwardCheck(false);
        		break;
        	case 6:
        		singleton(false);
        		break;
        }
	}
	
	static void singleton(boolean heuristics) throws IOException
	{
		int i = 0;
		while(i < 2)
		{
			mapColor.clear();
			String [] states1 = input.states4;
			if(isAustralia)
			{
				String [] states2 = input.states5;
				states1 = states2;
			}	
		
			if(!heuristics)		//for not using heuristics
			{
				ArrayList<String> tempStateList = shuffleState(states1);
				
				for(String s : tempStateList)
		        	domainList.add(s);
			}
			
			while(mapColor.size() <= states.length-1)
	        {
	        	String key = (heuristics)? heuristics(constraints, domain) : domainList.get(k);
	        	assignColor(key);
	            k++;
	            minDomain = colors.length;
	        }
			i++;
			domainList.clear();
			
			k = 0;
			//assigning colors to all states (creating domain)
	        for(int j=0; j<states.length; j++)
	        {
	          colors = copyArray(colors);          
	          ArrayList<String> domainColors = new ArrayList<String>();        
	          for (String c : colors)
	        	  domainColors.add(c);
	          domain.put(states[j],domainColors);		//hashMap for states and its available colors
	        }
		}
		
		 System.out.println("FINAl OUTPUT  "+mapColor);
		 converttoCsv(mapColor);
		 browseroutput();
	}
	
	static public void fwdCheckWithBacktrack(String minKey)
	{		
		for(Map.Entry<String,ArrayList<String>> domainEntry: domain.entrySet() )
		{	
			String previousState;
			
			if(domainEntry.getKey().equals(minKey))		//finding domain with key=minKey
			{
				if(isBacktracked)
				{
					
					if(domainEntry.getValue().size() == 1)
//					if(tempColor == colors[colors.length-1])
					{
						k--;
						previousState = domainList.get(k);
//						System.out.println("***BACKTRACKED FOR STATE: "+ previousState);
						tempColor = mapColor.get(previousState);
						isBacktracked = true;
//						domain.get(previousState).add(tempColor);
						reinitialiseColor(previousState, tempColor);						
						mapColor.remove(previousState);
						
						fwdCheckWithBacktrack(previousState);
						isBacktracked = false;
						backTrackCount++;
						break;
					}
				}
				
				if(domainEntry.getValue().size() == 0)
				{
					
					k--;
					previousState = domainList.get(k);
//					System.out.println("***BACKTRACKED FOR STATE: "+ previousState);
					tempColor = mapColor.get(previousState);
					isBacktracked = true;				
//					domain.get(previousState).add(tempColor);					
					reinitialiseColor(previousState, tempColor);					
					mapColor.remove(previousState);
					
					ArrayList<String> tempColorList = new ArrayList<String>();
					tempColorList.add(tempColor);
					if(!failureList.containsKey(previousState))
					{
						failureList.put(previousState,tempColorList);

					}
					else
					{
						
						for(Map.Entry<String, ArrayList<String>> failureEntry : failureList.entrySet())
						{
							if(failureEntry.getKey().equals(previousState))
							{
								failureEntry.getValue().add(tempColor);
							}
			
						}
					}
					
//					System.out.println("FAILURE LIST2 : " + failureList);
					
					fwdCheckWithBacktrack(previousState);
					isBacktracked = false;
					backTrackCount++;
					break;
				}
//				System.out.println("K VALUE =  " + k);
				String removedColor = domainEntry.getValue().get(0);
				String currentState = domainEntry.getKey();		
				
				for(Map.Entry<String, ArrayList<String>> failureEntry : failureList.entrySet())
				{
					if(failureEntry.getKey().equals(currentState) && failureEntry.getValue().contains(removedColor))
					{
						k--;
						previousState = domainList.get(k);
//						System.out.println("***BACKTRACKED FOR FAILURE STATE: "+ previousState);
						tempColor = mapColor.get(previousState);
						isBacktracked = true;
	//					domain.get(previousState).add(tempColor);
						reinitialiseColor(previousState, tempColor);						
						mapColor.remove(previousState);
						
				
							
						fwdCheckWithBacktrack(previousState);
						isBacktracked = false;
						backTrackCount++;
						break;
					}
	
				}
				
						
				domainEntry.getValue().remove(removedColor);		//removing the color from domain
				mapColor.put(currentState, removedColor);
				if(isBacktrackedHeuristics)
					heuristicList.remove(currentState);
				
//				System.out.println("NEWLY ADDED STATE AND COLOR : "+ currentState + "    "+ removedColor);
//				System.out.println("\nMAP COLOR : "+mapColor+"\n");
				
				for(Map.Entry<String, ArrayList<String>> constraintEntry : constraints.entrySet())	//finding all constraints for minKey
				{
					if(constraintEntry.getKey().equals(currentState))
					{
						ArrayList <String> constaraintList =  constraintEntry.getValue();		//storing constraints in list
						int i = 0;
						while(i < constaraintList.size())
						{
							for(Map.Entry<String,ArrayList<String>> ouputEntry : domain.entrySet())	//removing color from each constraint
							{
								if(constaraintList.get(i).equals(ouputEntry.getKey()))		//comparing constraint list with each key in domain
									ouputEntry.getValue().remove(removedColor);		
							}
							i++; 
						}	
//						System.out.println("----after removing ----"+domain+"\n");
					}
				}
			}
		}
	}
	
	
	static public void reinitialiseColor(String previousState, String tempColor)
	{
		for(Map.Entry<String, ArrayList<String>> constraintEntry : constraints.entrySet())	//finding all constraints for minKey
		{
			if(constraintEntry.getKey().equals(previousState))
			{
				ArrayList <String> constraintList =  constraintEntry.getValue();
				int j = 0;
				while(j < constraintList.size())
				{
					for(Map.Entry<String,ArrayList<String>> ouputEntry : domain.entrySet())	//removing color from each constraint
					{
						if(constraintList.get(j).equals(ouputEntry.getKey()))		//comparing constraint list with each key in domain
							ouputEntry.getValue().add(tempColor);
					}
					j++; 
				}	
//				System.out.println("STATE : " + previousState+"  COLOR ADDED: "+ tempColor);
//				System.out.println("AFTER ADDING "+domain+"\n");
			}
		}
	}
	
	static void DFS(boolean heuristics) throws IOException
	{
		int i = 0;
		while(i < 1)
		{
			mapColor.clear();
			String [] states1 = input.states3;	
			
			if(heuristics)
				isBacktrackedHeuristics = true;
			
			if(isAustralia)
			{
				String [] states2 = input.states5;
				states1 = states2;
			}	
			 for(String s : states1)
		        	domainList.add(s);
			
			while((!isAustralia)? (mapColor.size() != states.length) : (output.size() != states.length))
	        {
				if(k == 0 && heuristics)
					heuristicList = domain;
					
	        	String key = (heuristics)? heuristics(constraints, heuristicList) : domainList.get(k);
	        	if(!heuristics || isAustralia)
	        		backtracking(constraints,domain,key);
	        	else 
	        		heuWithBacktrack(key);
		        k++;
	        }       
			
			i++;
			domainList.clear();
			heuristicList.clear();
			colorStack.clear();
			output.clear();
			
			k = 0;
			//assigning colors to all states (creating domain)
	        for(int j=0; j<states.length; j++)
	        {
	          colors = copyArray(colors);          
	          ArrayList<String> domainColors = new ArrayList<String>();        
	          for (String c : colors)
	        	  domainColors.add(c);
	          domain.put(states[j],domainColors);		//hashMap for states and its available colors
	        }
		}

//        System.out.println("Total no. of loops : " + k);        
        System.out.println("output : " + mapColor);
        System.out.println("backTrackCount : " + backTrackCount); 
		        
        converttoCsv(mapColor);
        browseroutput();
	}
	
	static void DFSForwardCheck(boolean heuristics) throws IOException
	{
		int i = 0;
		backTrackCount = 0;
		while(i < 1)
		{
			mapColor.clear();
			String [] states1 = input.states3;
			
			if(heuristics)		//if heursitics not used
				isBacktrackedHeuristics = true;
			
			if(isAustralia)
			{
				String [] states2 = input.states5;
				states1 = states2;
			}	
			 for(String s : states1)
		        	domainList.add(s);
			
			while(mapColor.size() <= states.length-1)
	        {
				if(k==0 && heuristics)
					heuristicList = domain;
				String key = (heuristics)? heuristics(constraints, heuristicList) : domainList.get(k);
	        	fwdCheckWithBacktrack(key);
	            k++;
	        }
			i++;
			domainList.clear();
			failureList.clear();
			heuristicList.clear();
			k = 0;
			//assigning colors to all states (creating domain)
	        for(int j=0; j<states.length; j++)
	        {
	          colors = copyArray(colors);          
	          ArrayList<String> domainColors = new ArrayList<String>();        
	          for (String c : colors)
	        	  domainColors.add(c);
	          domain.put(states[j],domainColors);		//hashMap for states and its available colors
	        }
	        System.out.println("========================================================");
		}
		
        System.out.println("FINAl OUTPUT  "+mapColor);
        System.out.println("backTrackCount : " + backTrackCount); 
        converttoCsv(mapColor);
        browseroutput();
	}
	
	public static ArrayList<String> shuffleState(String[]states)
	{
		 ArrayList<String> statesList = new ArrayList<String>(Arrays.asList(states));
         Collections.shuffle(statesList);
         System.out.println(statesList);
         return statesList;
	}
	
	static String[] copyArray(String[] colors)
	{
		String [] newArray = colors;
		return newArray;
	}
	
	static public String heuristics(HashMap<String,ArrayList<String>> constraints, ConcurrentHashMap<String,ArrayList<String>> domain) 
	{
		for (Map.Entry<String,ArrayList<String>> entry : domain.entrySet()) 
		{
			 if(domain.size() == 1)
			 {
				 keyMin = entry.getKey();
			 }
			 else
			 {
				 if(entry.getValue().size() < minDomain)		//choosing the domain with least legal values
				   {
					   minDomain = entry.getValue().size();
					   keyMin = entry.getKey();
				   }
				   else if(entry.getValue().size() == minDomain) //when more than 1 legal values are available
				   {	 
					   int currentConstraint = constraints.get(entry.getKey()).size();
					   if((currentConstraint == 0)? (true) : (currentConstraint > constraints.get(keyMin).size()) )
						   keyMin = entry.getKey();
				   }
				   if(!domain.containsKey(keyMin) && isBacktrackedHeuristics)
				   {
					   keyMin = heuristicList.keys().nextElement();
				   }
			 }				   
		}	
		
//		System.out.println("heuristics keyMin  :  "+keyMin);
		return keyMin;		
	}
	
	
	
	static public void assignColor(String minKey)
	{
		for(Map.Entry<String,ArrayList<String>> entry: domain.entrySet() )
		{
			if(entry.getKey().equals(minKey))		//finding domain with key=minKey
			{
				String removedColor = entry.getValue().get(0);
				String currentState = entry.getKey();				
				entry.getValue().remove(removedColor);		//removing the color from minKey
				mapColor.put(currentState, removedColor);
				domain.remove(currentState);				//removing the assigned state from domain list
//				System.out.println("\nAssigned map color : "+mapColor);
				
				for(Map.Entry<String, ArrayList<String>> constraintEntry : constraints.entrySet())	//finding all constraints for minKey
				{
					if(constraintEntry.getKey().equals(currentState))
					{
						ArrayList <String> constaraintList =  constraintEntry.getValue();		//storing constraints in list
						int i = 0;
						while(i < constaraintList.size())
						{
							for(Map.Entry<String,ArrayList<String>> ouputEntry : domain.entrySet())	//removing color from each constraint
							{
//								singleDomain(domain);
								if(constaraintList.get(i).equals(ouputEntry.getKey()))		//comparing constraint list with each key in domain
									ouputEntry.getValue().remove(removedColor);		
								
								singleDomain(domain);
							}
							i++; 
						}	
//						System.out.println("after removing "+domain+"\n");
					}
				}
			}
		}
	}
	
	public static void singleDomain(ConcurrentHashMap<String,ArrayList<String>> domain)
	{
		for (Map.Entry<String,ArrayList<String>> entry : domain.entrySet()) 
		{
			if(entry.getValue().size() == 1)
			{
				assignColor(entry.getKey());
//				System.out.println("single domain removed : "+ entry.getKey());
			}
		}
	}
	
	public static void backtracking(HashMap<String,ArrayList<String>> constraints, ConcurrentHashMap<String,ArrayList<String>> domain,String minKey)
	{
		int flag = 0;
		
		for(Map.Entry<String,ArrayList<String>> domainEntry : domain.entrySet())  //choosing each state from domain hashMap
		{
			if(flag == 1)
			{
				if(colorStack.peek().get(minKey) != null)
			    output.put(minKey, colorStack.peek().get(minKey));
				mapColor.put(minKey, colorStack.peek().get(minKey).get(0));
				heuristicList.remove(minKey);
//				System.out.println("Removed 1 : " + minKey);
			    break;
			}
			if(domainEntry.getKey().equals(minKey)) 
			{
				int limit = 0;
				ArrayList<String> tempColorList = new ArrayList<String>();
				for(int j=0; j<colors.length; j++)
				{   
					if(flag == 1)
					{
						if(colorStack.peek().get(minKey) != null)
						output.put(minKey, colorStack.peek().get(minKey));
						mapColor.put(minKey, colorStack.peek().get(minKey).get(0));
						heuristicList.remove(minKey);
//						System.out.println("Removed 2 : " + minKey);
						break;
					}
					tempColorList.clear();
					ConcurrentHashMap<String,ArrayList<String>> domainCopy =  new ConcurrentHashMap<String,ArrayList<String>>();

					if(!isBacktracked)
					{	
						tempColorList.add(colors[j]);
						domainCopy.put(minKey,tempColorList);
						colorStack.push(domainCopy);		//storing new available color in the stack
					}
					else			//when backtracking is done
					{
						ConcurrentHashMap<String, ArrayList<String>> tempHashmap = colorStack.peek();
						String currentKey = tempHashmap.keys().nextElement();
						String currentValue = tempHashmap.get(currentKey).get(0);
						
						output.remove(currentKey);		//removing the wrongly assigned value
						colorStack.pop();
						
						if(currentValue == colors[colors.length-1]) //if the last color is tried,backtrack again
//						if(currentKey == minKey && currentValue == colors[colors.length-1]) //if the last color is tried,backtrack again
						{	
							tempHashmap = colorStack.peek();
							currentKey = tempHashmap.keys().nextElement();
							k--;
							backtracking(constraints, domain, currentKey);
							backTrackCount++;
							break;
						}
						else			//else assign the next available color
						{
							int colorIndex = 0;
							for(String x : colors)
							{	
								colorIndex++;
								if(x == currentValue)
									break;
							}
							limit = colorIndex;
														
							tempColorList.add(colors[colorIndex]);
							domainCopy.put(minKey,tempColorList);
							colorStack.push(domainCopy);		//storing new available color in the stack
							j = colorIndex;			//check this****************
						}
						isBacktracked = false;
					}
					
					for(Map.Entry<String, ArrayList<String>> constraintEntry : constraints.entrySet()) //checking if any of its constraints have the assigned color 
					{
						if(constraintEntry.getKey().equals(minKey))
						{
							if(!output.isEmpty())
							{
								for(Map.Entry<String, ArrayList<String>> ouputEntry : output.entrySet())
								{
									//if there is conflict for the new color assigned with already assigned color, remove from stack
									if(constraintEntry.getValue().contains(ouputEntry.getKey())  && ouputEntry.getValue().equals(tempColorList))
									{
										colorStack.pop();
										flag = 0;
										limit++;
										if(limit != colors.length)
											break;
									}
									else if(limit == colors.length)		//when there are no more colors to assign for that state -> backtrack
									{
										String keyName = colorStack.peek().keys().nextElement();
										isBacktracked = true;
										k--;							
										backtracking(constraints, domain, keyName);	
										limit = 0;
										backTrackCount++;
										break;										
									}
									else
										flag = 1;
								}
							}
							else
								flag = 1;
						}
					}
				}
			}
		}
		if(!colorStack.isEmpty() && (colorStack.peek().get(minKey) != null))		//adding the last color in output list
		{
			output.put(minKey, colorStack.peek().get(minKey));		
			mapColor.put(minKey, colorStack.peek().get(minKey).get(0));
			heuristicList.remove(minKey);
//			System.out.println("Removed 3 : " + minKey+"\n");
		}			
	}

	static public void heuWithBacktrack(String minKey)
	{		
		for(Map.Entry<String,ArrayList<String>> domainEntry: domain.entrySet() )
		{	
			String previousState;
			
			if(domainEntry.getKey().equals(minKey))		//finding domain with key=minKey
			{
				if(isBacktracked)
				{
					
					if(domainEntry.getValue().size() == 1)
//					if(tempColor == colors[colors.length-1])
					{
						k--;
						previousState = domainList.get(k);
//						System.out.println("***BACKTRACKED FOR STATE: "+ previousState);
						tempColor = mapColor.get(previousState);
						isBacktracked = true;
//						domain.get(previousState).add(tempColor);
						reinitialiseColor(previousState, tempColor);						
						mapColor.remove(previousState);
						
						fwdCheckWithBacktrack(previousState);
						isBacktracked = false;
						break;
					}
				}
				
				if(domainEntry.getValue().size() == 0)
				{
					
					k--;
					previousState = domainList.get(k);
//					System.out.println("***BACKTRACKED FOR STATE: "+ previousState);
					tempColor = mapColor.get(previousState);
					isBacktracked = true;				
//					domain.get(previousState).add(tempColor);					
					reinitialiseColor(previousState, tempColor);					
					mapColor.remove(previousState);
					
					ArrayList<String> tempColorList = new ArrayList<String>();
					tempColorList.add(tempColor);
					if(!failureList.containsKey(previousState))
					{
						failureList.put(previousState,tempColorList);

					}
					else
					{
						
						for(Map.Entry<String, ArrayList<String>> failureEntry : failureList.entrySet())
						{
							if(failureEntry.getKey().equals(previousState))
							{
								failureEntry.getValue().add(tempColor);
							}
			
						}
					}
					
//					System.out.println("FAILURE LIST2 : " + failureList);
					
					fwdCheckWithBacktrack(previousState);
					isBacktracked = false;
					break;
				}
//				System.out.println("K VALUE =  " + k);
				String removedColor = domainEntry.getValue().get(0);
				String currentState = domainEntry.getKey();		
				
				for(Map.Entry<String, ArrayList<String>> failureEntry : failureList.entrySet())
				{
					if(failureEntry.getKey().equals(currentState) && failureEntry.getValue().contains(removedColor))
					{
						k--;
						previousState = domainList.get(k);
//						System.out.println("***BACKTRACKED FOR FAILURE STATE: "+ previousState);
						tempColor = mapColor.get(previousState);
						isBacktracked = true;
	//					domain.get(previousState).add(tempColor);
						reinitialiseColor(previousState, tempColor);						
						mapColor.remove(previousState);
						
				
							
						fwdCheckWithBacktrack(previousState);
						isBacktracked = false;
						break;
					}
	
				}
				
						
				domainEntry.getValue().remove(removedColor);		//removing the color from domain
				mapColor.put(currentState, removedColor);
				if(isBacktrackedHeuristics)
					heuristicList.remove(currentState);
				
//				System.out.println("NEWLY ADDED STATE AND COLOR : "+ currentState + "    "+ removedColor);
//				System.out.println("\nMAP COLOR : "+mapColor+"\n");
				
				for(Map.Entry<String, ArrayList<String>> constraintEntry : constraints.entrySet())	//finding all constraints for minKey
				{
					if(constraintEntry.getKey().equals(currentState))
					{
						ArrayList <String> constaraintList =  constraintEntry.getValue();		//storing constraints in list
						int i = 0;
						while(i < constaraintList.size())
						{
							for(Map.Entry<String,ArrayList<String>> ouputEntry : domain.entrySet())	//removing color from each constraint
							{
								if(constaraintList.get(i).equals(ouputEntry.getKey()))		//comparing constraint list with each key in domain
									ouputEntry.getValue().remove(removedColor);		
							}
							i++; 
						}	
//						System.out.println("----after removing ----"+domain+"\n");
					}
				}
			}
		}
	}
	
	public static void converttoCsv( ConcurrentHashMap<String,String>hash_output) throws IOException
	{	
		 StringWriter csv_output = new StringWriter();
		 final String[] header = new String[] { "states", "Colour"};		
		 
		  ICsvListWriter listWriter = null;
	        try {
//                listWriter = new CsvListWriter(new FileWriter("C:\\Users\\deepa\\OneDrive\\Desktop\\mapColoring\\output.csv"), CsvPreference.STANDARD_PREFERENCE);
	        	listWriter = new CsvListWriter(new FileWriter(folderPath+"output.csv"), CsvPreference.STANDARD_PREFERENCE);
                
                listWriter.writeHeader(header);
		        for (Map.Entry<String, String> entry :hash_output.entrySet())
		            listWriter.write(entry.getKey(), entry.getValue());
		    }
	        finally {
                if( listWriter != null )
                    listWriter.close();
	        }	  
	}
	
	public static void  browseroutput()  throws IOException
	{
//		File htmlFile = new File("C:\\Users\\deepa\\OneDrive\\Desktop\\mapColoring\\isproj.html");
		File htmlFile = new File(folderPath + "isproj.html");
		File browserLocation = new File("C:\\Program Files\\Mozilla Firefox\\firefox.exe");
		if (!browserLocation.exists()) 
			System.out.println("User needs to install Mozilla Firefox to view the output");
		else 
		{
			try {
				Runtime.getRuntime().exec(new String[] { browserLocation.toString(), htmlFile.toString() });
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
}
