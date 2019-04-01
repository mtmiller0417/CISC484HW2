import java.lang.Comparable;

public class Word implements Comparable<Word>{
    String word;
    int spamCount = 0;
    int hamCount = 0;
    double spamProb;
    double hamProb;

    public Word(String word, int spam, int ham){
        this.word = word;
	this.spamCount += spam;
	this.hamCount += ham;
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