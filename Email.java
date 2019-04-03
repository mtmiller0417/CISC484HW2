import java.util.ArrayList;

public class Email{
	ArrayList<String> text;
	ArrayList<Integer> counts;
	ArrayList<Integer> index;
	int classification;

	public Email(String[] str, ArrayList<Word> trainingData, int c){
		this.classification = c; 
		this.text = new ArrayList<String>();
		this.counts = new ArrayList<Integer>();
		this.index = new ArrayList<Integer>();

		for(String s : str){ //for each word in the email
			int i = 0;
			boolean found = false;
			while(i < text.size()){ //search through text list to see if we have already encountered it
				if (text.get(i).equals(s)) { 
					int val = counts.get(i) + 1;
					counts.set(i, val); //increase count of that word
					i = text.size(); 
					found = true; 
				} else
					i++;
			}
			if (!found){ //have not encountered s in this email
				text.add(s);
				counts.add(1);
				//find the index of the s in text in the big list of all words
				boolean flag = false;
				int j = 0;
				while(!flag && j<trainingData.size()){ 
					if (s.equals(trainingData.get(j).word)){
						index.add(j);
						flag = true;
					} else
						j++;	
				}
				if (!flag) 
					index.add(-1);
			}
		}
			
	}
}