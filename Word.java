import java.lang.Comparable;
import java.util.Random; 

public class Word implements Comparable<Word>{
    String word;
    int spamCount = 0;
    int hamCount = 0;
    double spamProb;
    double hamProb;
    double weight;

    public Word(String word, int spam, int ham){
        this.word = word;
	this.spamCount += spam;
	this.hamCount += ham;
	Random rand = new Random(); 
	this.weight = rand.nextInt(10) + 1;
    }

    public void incrementS(){
        spamCount++;
    }

    public void incrementH(){
        hamCount++;
    } 

    @Override
    public int compareTo(Word word){
        return this.spamCount - word.spamCount;
    }

    @Override
    public String toString(){
        return word;
    }
}