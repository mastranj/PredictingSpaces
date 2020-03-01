import java.util.Scanner;
import java.io.*;

public class WordsRank {
	//public static int dictionarySize = 354985;* 2 is 709970, nearest prime: 709967
	public static String[] dic = new String[709967];// collection of dictionary words
	public static int best = -1;		// number of dictionary words in best segmentation found
	public static String bestSeg = null;	// best segmentation found
	public static int bestLikely = -1;
	public static int bestWeight = -1;
	public static boolean assigned = false;

	// support method that stores all words in dictionary.txt in dic

	private static void storeDic() {
		try {
			int wc = 0;
			Scanner filescan = new Scanner(new File("words.txt"));
			while (filescan.hasNext()) {
				//dic[wc] = filescan.nextLine();
				String word = filescan.nextLine();
				insert(word);
				wc++;
			}
			System.out.println(wc + " words stored");
		} catch (IOException e) {System.out.println(e);}
	}
	
	public static int hash(String key) {
		long value = 0;
		char letter;
		int power = key.length();
		for (int i = 0; i < key.length(); i++) {
			letter = key.charAt(i);
			value += ((int) letter)
					* Math.pow(27, --power); //decrement power then use it
		}
		return (int)(value % dic.length);
	}
	
	public static int doubleHash(String key) { //handle collision
		//Another near prime: 709963
		long value = 0;
		char letter;
		int power = key.length();
		int nearPrime = 7;//709963;
		for (int i = 0; i < key.length(); i++) {
			letter = key.charAt(i);
			value += ((int) letter - 96)  //Can subtract
					* Math.pow(27, --power); //decrement power then use it
		}
		return nearPrime - (int)(value % nearPrime);
		//return 1; //For testing purposes
		
	}
	
	public static void insert(String key) { //Assume array is not full...
		int hashVal = hash(key);
		int stepSize = doubleHash(key);
		
		while (dic[hashVal] != null) {
			hashVal += stepSize; //move... update this
			hashVal %= dic.length; //wrap around
		}
		
		dic[hashVal] = key;
	}
	
	public static boolean find(String key) {
	      int hashVal = hash(key);  // hash the key
		  int stepSize = doubleHash(key);

	      while(dic[hashVal] != null)  // until empty cell,
	         {                               // found the key?
	         if(dic[hashVal].equalsIgnoreCase(key))
	            return true;   // yes, return item
	         hashVal += stepSize;      // go to next cell
	         hashVal %= dic.length;    // wraparound if necessary
	         }
	      return false;                  // can't find item
	}
	
	/*private static void storeDic() {
		try {
			int wc = 0;
			Scanner filescan = new Scanner(new File("words.txt"));
			while (filescan.hasNext()) {
				dic[wc] = filescan.nextLine();
				wc++;
			}
			System.out.println(wc + " words stored");
		} catch (IOException e) {System.out.println(e);}
	}*/
	
	public static void split(String head, String in) {	
		// head + " " + in is a segmentation 
		String segment = head + " " + in;
		//System.out.println(segment);

		// count number of dictionary words within the segmentation
		// update best and bestSeg if new best segmentation has been found
		String[] tokens = segment.split(" ");
		int count = -1;
		int weight = 0;
		int length = 0;
		final int SUBTRACT_FOR_ONE = 5; //How many points we want to sub
										//if only one letter. Must put back
										//for important words like "a"
		for (int i=0; i<tokens.length; i++) {
			if (find(tokens[i].toLowerCase())) {
				length = tokens[i].length();
				count++;
				String word = tokens[i].toLowerCase(); //for comparison
				//Handle apostrophes
				if (!word.startsWith("'") && (word.contains("'"))
						&& !word.equalsIgnoreCase(("'"))) {
					weight += 1;
				}
				
				//Handle lengths
				if (length == 1 && !word.equalsIgnoreCase(" ")) {
					weight -= SUBTRACT_FOR_ONE; //Subtract if a single letter
				} else
				if (length == 3 || length == 4) { //Length is 3 or 4
					weight += 2;
				} else 
				if (length == 5) { //Length is 5
					weight += 6;
				} else
				if (length > 5) { //Larger than 5? That's impressive, better boost it
					weight += length+2;
				}
				//oddities:
				if (word.startsWith("a")) {
					if (word.contains("dd") ||
							word.contains("tt"))
						weight += 4; // Double letters, increase weight if begins with a
				}
				//Add weight to certain words
				//Most important/used words:
				if (isArticle(word)) {
					weight += 4;
					if (length == 1)
						weight += SUBTRACT_FOR_ONE; //add back subtraction
					if (i > 0) {
						if (tokens[i-1].toLowerCase().equalsIgnoreCase(("i")))
								weight -= 10;
					}
				} else //Second most used words
				
				if (isImportant(word)) {
					weight += 2;
					if (length == 1)
						weight += SUBTRACT_FOR_ONE; //add back subtraction
					if (i > 0) {
						if ((!tokens[i-1].equalsIgnoreCase("am") 
								|| !tokens[i-1].equalsIgnoreCase("do"))
								&& isImportant(tokens[i-1])) { //If we have two in a row, bad
							weight -= 10; //
						}
					}
				}
				
				//Prioritize sentences with ending in ing/er
				if ((word.endsWith("ing") || word.endsWith("er"))
						&& length > 3) {
					weight += 2;
					if (word.contains("do"))
						weight += 9;
					
				}
				
				//Prioritize sentences with most words that end with s
				 if (endsWithS(word) && length > 1) {
						weight += 3; //Greater than the weight for length = 4
						if (i < tokens.length - 1) {
							if ((tokens[i+1].toLowerCase().startsWith("s") ||
									tokens[i+1].toLowerCase().startsWith(" s"))
									&& find(tokens[i+1]))
								weight += 3;
						}
				 }
				 if (word.startsWith("s") &&
						 word.endsWith(("s")) &&
						 word.length() > 2) {
					 weight += 7; //Needs to surpass weight of ending with s
				 } else
				 if (word.startsWith("s")) {
					 weight += 2; //undo end with s
				 }

				//Words that usually go together/don't:
				if (i < tokens.length-1) {
					String next = tokens[i+1].toLowerCase();
					if (word.equals("i")) {
						if (next.equals("am"))
							weight += 20;
						if (next.equals("was"))
							weight += 20;
					}
					if (word.equals("we") || 
							word.equals("they")) {
						if (next.equals("are"))
							weight += 20;
						if (next.equals("were"))
							weight += 20;
					}
					if (word.equals("me")) {
						if (isArticle(next))
							weight -= 20;
					}
					if (word.equals(("an"))) {//next must start with vowel
						if(!beginsWithVowel(next))
							weight -= 10;
					}
				}
				
				//Words to end with
				if (i == tokens.length - 1) {
				if (word.equals("them") || 
						word.equals("him") ||
						word.equals("her"))
					weight += 5;
				if (isArticle(word)) {
					weight -= 15;//shouldn't end with these...
				}
				}
			} else {
				weight -= 20; //Not a word. Big punishment.
				if (tokens[i].length() > 3) {
					weight -= 100;
				}
			}
			
		}
		if (!assigned) {
			best = count;
			bestSeg = segment;
			bestWeight = weight;
			assigned = true;
			System.out.println("Assigned.");
		} else
		if (weight > bestWeight || (weight == bestWeight && count < best)) {
				best = count;
				bestSeg = segment;
				bestWeight = weight;
				//System.out.println("FOUND NEW BEST: "+weight);
		}

		// recursive calls
		for (int i=1; i<in.length(); i++) {
			split(head+" "+in.substring(0,i), in.substring(i,in.length()));
		}	
	}



	public static boolean isImportant(String tok) {
		String[] important = {"am", "i", "in",
		  "if", "he", "we", "do"}; //2 or less letters
		for (int i = 0; i < important.length; i++) {
			if (tok.equalsIgnoreCase(important[i])) {
				//System.out.println("Found important: "+tok);
				return true;
			}
		}
		return false;
	}

	public static boolean endsWithS(String tok) { //Probably could do without this method
		String[] end = {"s"};
		for (int i = 0; i < end.length; i++) {
			if (tok.toLowerCase().endsWith(end[i].toLowerCase())) {
				//System.out.println("Found ending with s: "+tok);
				return true;
			}
		}
		return false;
	}

	public static boolean beginsWithVowel(String tok) {
		String[] vow = {"a", "e", "i", "o", "u", "y"}; //Vowels to check for
		for (int i = 0; i < vow.length; i++) {
			if (tok.equalsIgnoreCase(vow[i])) {
				return true;
			}
		}
		return false;
		
	}
	public static boolean isArticle(String tok) { //Very important words
		String[] articles = {"a", "an", "the", "is", "are", "to", "in", "into"};
		for (int i = 0; i < articles.length; i++) {
			if (tok.equalsIgnoreCase(articles[i])) {
				//System.out.println("Found article: "+tok);
				return true;
			}
		}
		return false;
	}
	public static void main (String[] args) {
		// get input string from user
		Scanner scan = new Scanner(System.in);
		System.out.print("Enter a string: ");
		String input = scan.next();
		System.out.println();
		
		// preload all dictionary words into array
		storeDic();
		
		// recursively generate all segmentations of input
		long start = System.nanoTime();
		split("", input);
		long end = System.nanoTime();
		
		// print out best segmentation found including number of dictionary
		// words within that segmentation
		System.out.println("Best Segmentation: " + bestSeg + ", with "
							+ best + " words.");
		System.out.println("Time to Segment: " + (end-start));
	}
}