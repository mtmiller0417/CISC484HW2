import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
//import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;

public class Main{
    /*ArrayList<String> trainingDataSpam = new ArrayList<String>();
    ArrayList<String> testingDataSpam = new ArrayList<String>();
    ArrayList<String> trainingDataHam = new ArrayList<String>();
    ArrayList<String> testingDataHam = new ArrayList<String>();*/

    //ArrayList of Words, alternative to bunches of Strings
    ArrayList<Word> spamTraining = new ArrayList<Word>();
    ArrayList<Word> hamTraining = new ArrayList<Word>();

    ArrayList<Word> spamTesting = new ArrayList<Word>();
    ArrayList<Word> hamTesting = new ArrayList<Word>();
    
    //Files(directories) that contain the data files
    File testSpam, testHam, trainSpam, trainHam;

    public static void main(String[] args){
        Main main = new Main(args);
    }

    public Main(String[] args){
        //Load in folders(directories) that hold all the data(txt files)
        trainSpam = new File("dataset 1/train/spam");
        trainHam = new File("dataset 1/train/ham");
        testSpam = new File("dataset 1/test/spam");
        testHam = new File("dataset 1/test/ham");

        //Convert all files to strings and store those stings into 
        /*loopDir(trainHam, trainingDataHam);
        loopDir(trainSpam, trainingDataSpam);
        loopDir(testHam, testingDataHam);
        loopDir(testSpam, testingDataSpam);*/

        loopDir(trainSpam, spamTraining);
        System.out.println("Number of unique words: " + spamTraining.size());
        Collections.sort(spamTraining);
        for(Word w : spamTraining){
            System.out.println(w.toString() + ": " + w.wordCount);
        }

        logReg(spamTraining);
    }

    //Loops through a directory convert all its files into Strings
    public void loopDir(File directory, ArrayList<Word> dataSet){
        File[] fileNames = directory.listFiles();
        System.out.println("Number of files in [" + directory.getPath() + "]: " + fileNames.length);
        for(File file : fileNames){
            try {
                //What to do for each file in the directory; Call concatFile on it and add the string to appropriate arraylist
                String str[] = concatFile(file.getPath()).split("\\s+");
                for(String s : str){
                    checkWord(s, dataSet);
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
    }

    // Checks to find the word, if it is found, increment the word count; If not found, add it to the arraylist
    public boolean checkWord(String word, ArrayList<Word> wordList){
        boolean flag = false;
        for(Word w : wordList){
            if(w.toString().equals(word)){
                flag = true;
                w.increment();//Increment the wordCount of the word
            }
        }
        //If the wors was not found in the list, add it to the list
        if(!flag)
            wordList.add(new Word(word));

        return flag;
    }

    public void logReg(ArrayList<Word> data){
        //Split the data 70 - 30
        ArrayList<Word> data70 = new ArrayList<Word>();
        ArrayList<Word> data30 = new ArrayList<Word>();
        for(int i = 0; i < data.size(); i++){
            if(i < data.size() * 0.3)
                data30.add(data.get(i));
            else    
                data70.add(data.get(i));
        }
        System.out.println("Size of original: " + data.size());
        System.out.println("Size of data30: " + data30.size() + "  Percentage = " + ((double)data30.size())/((double)data.size()));
        System.out.println("Size of data70: " + data70.size() + "  Percentage = " + ((double)data70.size())/((double)data.size()));

        //Learn parameters with data70

        //Use data30 to learn good lambda value(treat as validation data)
    }
}