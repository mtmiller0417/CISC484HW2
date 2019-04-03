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
    ArrayList<Word> trainingData = new ArrayList<Word>(); // Holds all of the words in both spam and ham data

    ArrayList<Word> spamTesting = new ArrayList<Word>();
    ArrayList<Word> hamTesting = new ArrayList<Word>();

    ArrayList<Double> trainingWeights = new ArrayList<Double>();

    ArrayList<Word> allWords = new ArrayList<Word>();
    ArrayList<Email> spamEmails = new ArrayList<Email>();
    ArrayList<Email> hamEmails = new ArrayList<Email>();
    double[] weights;
    ArrayList<Email> testing = new ArrayList<Email>(); 
    
    //Files(directories) that contain the data files
    File testSpam, testHam, trainSpam, trainHam;

    //Sizes
    int numtrDocs, numtrSpamDocs, numtrHamDocs;

    public static void main(String[] args){
        Main main = new Main(args);
    }

    public void processEmails(ArrayList<Email> emails,  File directory, int c){
        File[] files = directory.listFiles();
        for(File file : files)
            emails.add(new Email(file, c));
    }

    public Main(String[] args){
        //Load in folders(directories) that hold all the data(txt files)
        trainSpam = new File("dataset 1/train/spam");
        trainHam = new File("dataset 1/train/ham");
        testSpam = new File("dataset 1/test/spam");
        testHam = new File("dataset 1/test/ham");

    //Load training emails
    processEmails(spamEmails, trainSpam, 0);
    processEmails(hamEmails, trainHam, 1);
    for(int i = 0; i < spamEmails.size(); i++){
        for(int j = 0; j <spamEmails.get(i).words.size(); j++){
            allWords.add(spamEmails.get(i).words.get(j));
            spamEmails.get(i).words.get(j).weightIndex = (i*spamEmails.size()) + j;
        }
    }
    for(int i = 0; i < hamEmails.size(); i++){
        for(int j = 0; j <hamEmails.get(i).words.size(); j++){
            allWords.add(hamEmails.get(i).words.get(j));
            hamEmails.get(i).words.get(j).weightIndex = (i*hamEmails.size()) + j;
        }
    }
    weights = new double[allWords.size()];
    //Set all starting weights to 0
    for(int i = 0; i < weights.length; i++)
        weights[i] = 0.0;
    
    ArrayList<Email> allEmails = new ArrayList<Email>();
    for(Email email : hamEmails)
        allEmails.add(email);

    for(Email email : spamEmails)
        allEmails.add(email);

    System.out.println("Learning Weights..");
    learnWeights(allEmails, weights, 3, 0.0);
    /*System.out.println("Learning Ham Weights...");
    learnWeights(hamEmails, weights, 3, 0.0);
    System.out.println("Learning Spam Weights...");
    learnWeights(spamEmails, weights, 3, 0.0);*/
    
    
    System.out.println(logReg(spamEmails.get(0), weights));
    System.out.println(logReg(hamEmails.get(0), weights));

    /*for(Email e : hamEmails){
        if(logReg(e, weights) != 1)
            System.out.println("Got it!");
    }
    System.out.println();*/

    /*
	//getting sizes of data sets
	numtrSpamDocs = trainSpam.listFiles().length;
	numtrHamDocs = trainHam.listFiles().length;
	numtrDocs = numtrSpamDocs + numtrHamDocs;

    //Add the spam and ham words to the dataSet
    loopDir(trainSpam, trainingData, 0);    
    loopDir(trainHam, trainingData, 1);  
        
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
    
        logReg(trainingData);*/
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
                    checkWord(s, dataSet, c);
                }
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
    }

    // Checks to find the word, if it is found, increment the word count; If not found, add it to the arraylist
    public boolean checkWord(String word, ArrayList<Word> wordList, int c){
        boolean flag = false;
        for(Word w : wordList){
            if(w.toString().equals(word)){
                flag = true;
		    if (c == 0){
                w.incrementS();//Increment the wordCount of the word
                w.isSpam = true;
            }
		    else if (c == 1)
                w.incrementH();
                w.isHam = true;
            }
        }
        //If the words was not found in the list, add it to the list
        if(!flag) {
            if (c == 0)
                wordList.add(new Word(word, 1, 0));
	        else if (c == 1)
	            wordList.add(new Word(word, 0, 1));
	    }
        return flag;
    }

    public void findProbabilities(ArrayList<Word> data){
        int totalSpamCount = 0, totalHamCount = 0;
        for(Word w : data){
		    totalSpamCount += w.spamCount;
		    totalHamCount += w.hamCount;
	    }
	    for(Word w : data){
		    w.spamProb = w.spamCount/((double) totalSpamCount);
		    w.hamProb = w.hamCount/((double) totalHamCount);
	    }
    }

    public int logReg(Email email, double[] weights){
        double result1, result2;
        result1 = condProb(email, weights, 1);//Ham
        result2 = condProb(email, weights, 0);//Spam
        if(result1 > result2)
            return 1;
        else    
            return 0;
    }

    public void learnWeights(ArrayList<Email> emails, double[] weights, int maxIterations, double lambda){
        for(int i = 0; i < maxIterations; i++){
            System.out.println("Iteration " + i);
            for(int w = 0; w < weights.length; w++){
                double sum = 0.0;
                for(int x = 0; x < emails.size(); x++){
                    int yTrue = emails.get(x).trueClass; // Gets the true classification of y
                    //Only add to the sum if the email contains the word
                    for(Word wrd : emails.get(x).words){
                        if(wrd.toString().equals(allWords.get(w).toString())){
                            //Run sum
                            /*int freq = 0;
                            if(emails.get(x).trueClass == 0)
                                freq = wrd.spamCount;
                            else
                                freq = wrd.hamCount;*/
                            //One of these should be 0... but it doesn't have to?
                            sum += ((double)wrd.hamCount) * (yTrue - condProb(emails.get(x), weights, 1));
                            sum += ((double)wrd.spamCount) * (yTrue - condProb(emails.get(x), weights, 0));
                            break;
                        }
                    }
                }
                weights[w] += ((.02 * sum) - ((.02) * lambda * weights[w])); 
            }
        }
        /*System.out.println("***WEIGHTS***");
        for(int x = 0; x < weights.length; x++){
            System.out.println("Weight " + x + ": " +weights[x]);
        }*/
    }

    public double condProb(Email email, double[] weights, int c){
        if(c == 0){      // SPAM
            double sumW0 = 0.0;
            for(int i = 0; i < email.words.size(); i++){
                if(email.words.get(i).spamCount == 0){ // This word is not present in the ham data samples
                    //weights.add(0.0); // It's weight should already be 0
                }
                else{                          // This word is present in the ham data samples
                    sumW0 += weights[i] * ((double)(email.words.get(i).spamCount));
                }
            }
            return ((double)1) / ((double)1 + Math.exp(sumW0));
        }
        else if(c == 1){ // HAM
            double sumW1 = 0.0;
            for(int i = 0; i < email.words.size(); i++){
                if(email.words.get(i).hamCount == 0){ // This word is not present in the ham data samples
                    //weights.add(0.0); // It's weight should already be 0
                }
                else{                          // This word is present in the ham data samples
                    sumW1 += weights[i] * ((double)(email.words.get(i).hamCount));
                }
            }
            return Math.exp(sumW1) / ((double)1 + Math.exp(sumW1));
        }
        return 0;
    }
}