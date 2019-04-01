import java.lang.Comparable;

public class Word implements Comparable<Word>{
    String word;
    int wordCount = 0;

    public Word(String word){
        this.word = word;
        this.wordCount = 1;
    }

    public void increment(){
        wordCount++;
    } 

    @Override
    public int compareTo(Word word){
        return this.wordCount - word.wordCount;
    }

    @Override
    public String toString(){
        return word;
    }
}