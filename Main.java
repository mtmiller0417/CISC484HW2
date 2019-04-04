import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
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

    //Emails from the training sets and test set
    ArrayList<Email> spamEmails = new ArrayList<Email>();
    ArrayList<Email> hamEmails = new ArrayList<Email>();
    ArrayList<Email> testEmails = new ArrayList<Email>();

    //Sizes
    int numtrDocs, numtrSpamDocs, numtrHamDocs;

    double w0 = 50; //bias term for perceptron

    public static void main(String[] args){
        Main main = new Main(args);
    }

    public Main(String[] args){
        //Load in folders(directories) that hold all the data(txt files)
        trainSpam = new File("dataset 1/train/spam");
        trainHam = new File("dataset 1/train/ham");
        testSpam = new File("dataset 1/test/spam");
		testHam = new File("dataset 1/test/ham");
		//Read and use command line arguments if those are used instead
		if(args.length == 4){
			trainSpam = new File(args[0]);
			trainHam = new File(args[1]);
			testSpam = new File(args[2]);
			testHam = new File(args[3]);
		}

	//getting sizes of data sets
	numtrSpamDocs = trainSpam.listFiles().length;
	numtrHamDocs = trainHam.listFiles().length;
	numtrDocs = numtrSpamDocs + numtrHamDocs;

	//Read all words in spam and ham folders respectively and make them into one big ArrayList<Word> each
    loopDir(trainSpam, trainingData, 1);
	loopDir(trainHam, trainingData, -1);
	loopDir(testSpam, spamTesting, 1);
	loopDir(testHam, hamTesting, -1);

	//Using c = 1 for spam and c = -1 for ham
	trainMultinomial(trainingData);
	testNaiveBayes();	

	createEmailList(trainSpam, spamEmails, 1);
	createEmailList(trainHam, hamEmails, -1);
	trainWeightsLogReg();

	createEmailList(testSpam, testEmails, 1);
	createEmailList(testHam, testEmails, -1);
	testLogReg();

	createEmailList(trainSpam, spamEmails, 1);
	createEmailList(trainHam, hamEmails, -1);
	trainWeightsPerceptron();

	createEmailList(testSpam, testEmails, 1);
	createEmailList(testHam, testEmails, -1);
	testPerceptron();
	}
	
	public void testLogReg(){
		int correct = 0;
		for(Email email : testEmails){
			if(classifyLogReg(email) == email.classification)
				correct++;
		}
		System.out.println("Accuracy : " + ((double)correct) / ((double)testEmails.size()));
	}

    public void testNaiveBayes(){
		System.out.println("\nMULTINOMIAL NAIVE BAYES");
		int correctSpam = testNB(testSpam, 1);
		double ts = (double) testSpam.listFiles().length;
		double spamAccuracy = correctSpam/ts;
		System.out.println("Accuracy on Spam Test Set = " + spamAccuracy);
	
		int correctHam = testNB(testHam, -1);
		double th = (double) testHam.listFiles().length;
		double hamAccuracy = correctHam/th;
		System.out.println("Accuracy on Ham Test Set = " + hamAccuracy);

		int totalDocs = testSpam.listFiles().length + testHam.listFiles().length;
		double td = (double) totalDocs;
		double accuracy = (correctHam + correctSpam)/td;
		System.out.println("Accuracy = " + accuracy);
		System.out.println();
    }

    public void testPerceptron(){
		int count = 0;
		for(Email e: testEmails){
			int res = currentPrediction(e);
			if (res == e.classification)
				count++;
		}
		double s = (double) testEmails.size();
		double accuracy = count/s;
		System.out.println("\nPERCEPTRON ACCURACY = " + accuracy);
		}

	//Calculates P[y = (1 or -1) | Xi]; //Xi is an email
	public double condProb(Email email, ArrayList<Word> data, int classification){
        if(classification == 1){      // SPAM
            double sumW0 = 0.0;
            for(int i = 0; i < email.text.size(); i++){
				//System.out.println("i = " + i);
				int index = email.index.get(i);
				if(index == -1)//Does not belong to the list
					sumW0 += 0;
				else
					sumW0 += (data.get(index).weight) * ((double)email.counts.get(i)); 
			}
            return (((double)1) / ((double)1 + Math.exp(sumW0)));
        }
        else if(classification == -1){ // HAM
            double sumW1 = 0.0;
            for(int i = 0; i < email.text.size(); i++){
				int index = email.index.get(i);
				if(index == -1)//Does not belong to the list
					sumW1 += 0;
				else
					sumW1 += ((data.get(email.index.get(i)).weight) * ((double)email.counts.get(i)));
			}
            return ((Math.exp(sumW1)) / (((double)1 + Math.exp(sumW1))));
		}
        return 0;
	}
	
	public int classifyLogReg(Email email){
		double sum = 0.0;
		for(int x = 0; x < email.text.size(); x++){
			if(email.index.get(x) >= 0)
				sum += trainingData.get(email.index.get(x)).weight * email.counts.get(x);
		}	
		if(sum >= 0)
			return 1;
		return -1;
	}
	
	public void trainWeightsLogReg(){
		System.out.println("\nLOGISTIC REGRESSION");

		//Make all weights 0
		for(Word wrd : trainingData){
			wrd.weight = 0;
		}

		int MAXITERS = 5;
		double lambda = 0.01;
		double stepSize = 0.01;
		ArrayList<Email> eList = new ArrayList<Email>(); // list of all emails
		//altetween spam and ham emailsernating b
		int k = 0;
		while(k < spamEmails.size()){
			eList.add(spamEmails.get(k));
			if (k < hamEmails.size())
				eList.add(hamEmails.get(k));
			k++;
		}
		//If there were more hamEmails than spamEmails...
		if (k < hamEmails.size()){
			while(k < hamEmails.size()){
				eList.add(hamEmails.get(k));
				k++;
			}	
		}

		//Run for the numner of iterations
		for(int counter = 0; counter < MAXITERS; counter++){
			System.out.println("Iteration " + counter);
			//For each weight(weights are associated with the corresponding word in the wordclass)
			for(int w = 0; w < trainingData.size(); w++){ //Therefore loop through all unique words
				double sum = 0.0;
				for(int i = 0; i < eList.size(); i++){ // Loops through all emails
					int localIndex = findLocalIndex(eList.get(i), trainingData.get(w).toString());

					if(localIndex > 0)//If the word is in the email
						sum += (((double)eList.get(i).counts.get(localIndex)) * (eList.get(i).classification - condProb(eList.get(i), trainingData, 1))) 
							- (lambda * trainingData.get(w).weight);
				}
				trainingData.get(w).weight += (stepSize * (sum)) - (stepSize * lambda * trainingData.get(w).weight);
			}
		}
	}

	public double sumWeights(){
		double sum = 0.0;
		for(Word word : trainingData)
			sum += word.weight;
		return sum;
	}

	//Finds the index of the word INSIDE the email
	public int findLocalIndex(Email email, String word){
		for(int j = 0; j < email.text.size(); j++){
			if(email.text.get(j).equals(word)){
				//System.out.println(j);
				return j;
			}
		}
		return -1;
	}
	//Finds the index of the word in the total words ArrayList
	public int findGlobalIndex(Email email, String word){
		for(int j = 0; j < email.text.size(); j++){
			if(email.text.get(j).equals(word)){
				//System.out.println(j);
				return email.index.get(j);
			}
		}
		return -1;
	}

    public void trainWeightsPerceptron(){
		int countCorrect = 0;
		ArrayList<Email> eList = new ArrayList<Email>(); //list of all emails

		//altetween spam and ham emailsernating b
		int k = 0;
		for(; k < spamEmails.size(); k++){
			eList.add(spamEmails.get(k));
			if (k < hamEmails.size())
				eList.add(hamEmails.get(k));
		}
		if (k < hamEmails.size()){
			for(; k<hamEmails.size(); k++)
				eList.add(hamEmails.get(k));
		}

		boolean converge = false;	
		while(!converge){
		for(Email e : eList){
			int res = currentPrediction(e); //get prediction based on current weights
			if (res != e.classification){ //if not correct, update weights
				w0 = w0 + (e.classification - res);
				for(int i = 0; i < e.text.size(); i++){
					int x = e.counts.get(i);
					int ind = e.index.get(i);
					if (ind != -1) {
						Word w = trainingData.get(ind);
						w.weight = w.weight + (e.classification - res)*x;
					}
				}
			}	
		} 
		countCorrect = 0; //number of correctly classified emails based on current weights
		for(Email e : eList){
			int res = currentPrediction(e);
			if (res == e.classification)
				countCorrect++;
		}

		if (countCorrect == eList.size()) //if 100% accuracy on training set
			converge = true;
	}
    }

    public int currentPrediction(Email e){
		double sum = 0;
		for(int i = 0; i < e.text.size(); i++){
			int x = e.counts.get(i);
			int ind = e.index.get(i);
			double wi;
			if (ind != -1) //only -1 if it doesn't occur in training set
				wi = trainingData.get(ind).weight;
			else
				wi = 0;
			sum = sum + (x*wi);
		}
		sum += w0;
		int res;
		if (sum > 0)
			res = 1; //classify as spam
		else
			res = -1;
		return res;
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
			return 1; 
		else 
			return -1;
    }

    //Convert each file in a directory to an email, and add it to the passed in ArrayList<Email> 
    public void createEmailList(File directory, ArrayList<Email> elist, int c){
		File[] fileNames = directory.listFiles();
		for(File file : fileNames){
        	try {
        	    String str[] = concatFile(file.getPath()).split("\\s+");
				elist.add(new Email(str, trainingData, c));
	    	} catch (Exception e) {
                e.printStackTrace();
        	}
        }
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
		if (c == 1)
                	w.incrementS();//Increment the wordCount of the word
		else if (c == -1)
			w.incrementH();
            }
        }
        //If the wors was not found in the list, add it to the list
        if(!flag) {
           
	    	if (c == 1)
              	 wordList.add(new Word(word, 1, 0));
	    	else if (c == -1)
				wordList.add(new Word(word, 0, 1));
		}
        return flag;
    }
}