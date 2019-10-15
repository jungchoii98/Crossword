package Server;

public class Word {
	boolean isHorz;
	String number;
	String actualWord;
	int row = 0;
	int col = 0;
	boolean placedByPlayer = false;
	boolean requiredIntersection = false;
	boolean placedByBoard = false;
	
	public Word(boolean isHorz, String number, String actualWord) {
		this.isHorz = isHorz;
		this.number = number;
		this.actualWord = actualWord;
	}
	public int size() {
		return actualWord.length();
	}
	public String letterAt(int index) {
		return actualWord.substring(index, index+1);
	}
}
