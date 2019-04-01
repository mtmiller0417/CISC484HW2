import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
//import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;

public class Main{

    //ArrayList of Words, alternative to bunches of Strings
    ArrayList<Word> trainingData = new ArrayList<Word>();

    ArrayList<Word> spamTesting = new ArrayList<Word>();
    ArrayList<Word> hamTesting = new ArrayList<Word>();
    
    //Files(directories) that contain the data files
    File testSpam, testHam, trainSpam, trainHam;

    //Sizes
    int numtrDocs, numtrSpamDocs, numtrHamDocs;

    public static void main(String[] args){
        Main main = new Main(args);
    }

    public Main(String[] args){
        //Load in folders(directories) that hold all the data(txt files)
        trainSpam = new File("dataset 1/train/spam");
        trainHam = new File("dataset 1/train/ham");
        testSpam = new File("dataset 1/test/spam");
        testHam = new File("dataset 1/test/ham");

	//getting sizes of data sets
	numtrSpamDocs = trainSpam.listFiles().length;
	numtrHamDocs = trainHam.listFiles().length;
	numtrDocs = numtrSpamDocs + numtrHamDocs;

        loopDir(trainSpam, trainingData, 0);
	loopDir(trainHam, trainingData, 1);
        
	/*System.out.println("Number of unique words: " + spamTraining.size());
        Collections.sort(trainingData);
        for(Word w : spamTraining){
            System.out.println(w.toString() + ": " + w.wordCount);
        }*/

	//Using c = 0 for spam and c = 1 for ham
	trainMultinomial(trainingData);
	int correctSpam = testNB(testSpam, 0);
	double ts = (double) testSpam.listFiles().length;
	double spamAccuracy = correctSpam/ts;
	System.out.println("Accuracy on Spam Test Set " + spamAccuracy);
	
	int correctHam = testNB(testHam, 1);
	double th = (double) testHam.listFiles().length;
	double hamAccuracy = correctHam/th;
	System.out.println("Accuracy on Ham Test Set " + hamAccuracy);

	int totalDocs = testSpam.listFiles().length + testHam.listFiles().length;
	double td = (double) totalDocs;
	double accuracy = (correctHam + correctSpam)/td;
	System.out.println("Accuracy = " + accuracy);
	
    }

    public void trainMultinomial(ArrayList<Word> data){
	int totalSpamCount = data.size(); //because of Laplace smoothing
	int totalHamCount = data.size();
	for(Word w : data){
		totalSpamCount = totalSpamCount + w.spamCount;
		totalHamCount = totalHamCount + w.hamCount;
	}

	double tsc = (double) totalSpamCount;
	double thc = (double) totalHamCount;
	for(Word w : data){
		w.spamProb = (w.spamCount + 1)/tsc;
		w.hamProb = (w.hamCount + 1)/thc;
	}
    }

    public int testNB(File directory, int c){ // c is the class the data belongs to
	File[] fileNames = directory.listFiles();
	int correctlyClassified = 0;
	double priorS = Math.log(numtrSpamDocs) - Math.log(numtrDocs);
	double priorH = Math.log(numtrHamDocs) - Math.log(numtrDocs);
	for(File file : fileNames){
            try {
                String str[] = concatFile(file.getPath()).split("\\s+");
		int res = applyMultinomialNB(str, trainingData, priorS, priorH);
		if (res == c)
			correctlyClassified++;
	    } catch (Exception e) {
                e.printStackTrace();
            }
	}
	return correctlyClassified;
    }

    public int applyMultinomialNB(String str[], ArrayList<Word> trainData, double priorS, double priorH) {
	double scoreSpam = priorS;
	double scoreHam = priorH;

	for(String s : str){
		 boolean flag = false;
        	 for(Word w : trainData)
            		if(w.toString().equals(s)){
                		flag = true;
				scoreSpam += Math.log(w.spamProb);
				scoreHam += Math.log(w.hamProb);
			}
		 if (!flag){
			//do some smoothing idk
		 }
	}
	
	if (scoreSpam > scoreHam)
		return 0; 
	else 
		return 1;

    }
    //Loops through a directory convert all its files into Strings
    public void loopDir(File directory, ArrayList<Word> dataSet, int c){
        File[] fileNames = directory.listFiles();
        System.out.println("Number of files in [" + directory.getPath() + "]: " + fileNames.length); 
        for(File file : fileNames){
            try {
                //What to do for each file in the directory; Call concatFile on it and add the string to appropriate arraylist
                String str[] = concatFile(file.getPath()).split("\\s+");
                for(String s : str){
                    /*if(s.substring(s.length()-3, s.length()-1).equals("\n")){
                        s = s.substring(0, s.length() - 4);
                        System.out.println(s);
                    }*/
                    checkWord(s, dataSet, c);
                }
                //dataSet.add(concatFile(file.getPath()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //Collect all the lines of a single file and add it to the ArrayList
    public String concatFile(String filePath){
        String content = "";
        try{
            content = new String (Files.readAllBytes(Paths.get(filePath)));
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return content; // Return the concatonated String
        //dataSet.add(content);
    }

    // Checks to find the word, if it is found, increment the word count; If not found, add it to the arraylist
    public boolean checkWord(String word, ArrayList<Word> wordList, int c){
        boolean flag = false;
        for(Word w : wordList){
            if(w.toString().equals(word)){
                flag = true;
		if (c == 0)
                	w.incrementS();//Increment the wordCount of the word
		else if (c == 1)
			w.incrementH();
            }
        }
        //If the wors was not found in the list, add it to the list
        if(!flag) {
           
	    if (c == 0)
                	 wordList.add(new Word(word, 1, 0));
	    else if (c == 1)
			wordList.add(new Word(word, 0, 1));
	}
        return flag;
    }
}