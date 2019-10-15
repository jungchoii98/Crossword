package Server;

import java.util.ArrayList;

public class Board {
	int row = 16;
	int col = 16;
	private Square[][] squares = new Square[row][col];
	
	
	public boolean generateBoard(ArrayList<Word> allWords, ArrayList<Word> placedWords, ArrayList<Word> remainingWords, int requiredIntersections, int currentIntersections) {
		//base case
		if(allWords.size() == placedWords.size() && requiredIntersections <= currentIntersections) {
			//System.out.println("base case: ");
			return true;
		}
		//for(int i=0; i<allWords.size(); i++) {
		for(int i=0; i<remainingWords.size(); i++) {
			
//			System.out.print("remaining words: ");
//			for(int p=0; p<remainingWords.size(); p++) {
//				System.out.print(remainingWords.get(p).actualWord + " ");
//			}
//			System.out.println();
			
			//do not check other conditions if this is the first block placed
			Word temp;
			if(placedWords.size() == 0) {
				//System.out.println("place first: " + remainingWords.get(i).actualWord);
				temp = remainingWords.get(i);
				place(7, 7, remainingWords.get(i));
				placedWords.add(remainingWords.get(i));
				remainingWords.remove(remainingWords.get(i));
				if(generateBoard(allWords, placedWords, remainingWords, requiredIntersections, currentIntersections)) {
					//System.out.println("truth");
					return true;
				}
				else {
					//System.out.println("redact first : " + temp.actualWord);
					remainingWords.add(i, temp);
					redact(7, 7, temp);
					
					//remainingWords.add(allWords.get(i));
					placedWords.remove(temp);
				}
			}
			else {
				for(int j=0; j<row; j++) {
					for(int k=0; k<col; k++) {
						//for(int t=0; t<remainingWords.size(); t++) {
							//System.out.println("this is the value of t: " + t);
							//if you can place the word recurse, else try the next word
						//System.out.println("x: " + j + " y: " + k);
							if(remainingWords.get(i).isHorz && canHorzWordPlace(j, k, remainingWords.get(i)) ) {
								//System.out.println("place horz: " + remainingWords.get(i).actualWord + " x: " + j + " y: " + k);
								temp = remainingWords.get(i);
								if( place(j, k, remainingWords.get(i)) ) {
									currentIntersections++;
								}
								placedWords.add(temp);
								remainingWords.remove(i);
								//System.out.println("placedWords size " + placedWords.size());
								//System.out.println("allWords size " + allWords.size());
								//System.out.println("required intersections " + requiredIntersections);
								//System.out.println("current intersections " + currentIntersections);
								//printBoard();
								if(generateBoard(allWords, placedWords, remainingWords, requiredIntersections, currentIntersections)) {
									//System.out.println("truth");
									return true;
								}
								else {
									//System.out.println("horz redact: " + placedWords.get(placedWords.size()-1).actualWord);
									if( redact(j, k, placedWords.get(placedWords.size()-1)) ) {
										currentIntersections--;
									}
									remainingWords.add(i, temp);
									placedWords.remove(temp);
								}
							}
							if(remainingWords.get(i).isHorz == false && canVertWordPlace(j, k, remainingWords.get(i))) {
								//System.out.println("place vert: " + remainingWords.get(i).actualWord + " x: " + j + " y: " + k);
								temp = remainingWords.get(i);
								if( place(j, k, remainingWords.get(i)) ) {
									currentIntersections++;
									//System.out.println("Found an intersection");
								}
								placedWords.add(temp);
								remainingWords.remove(i);
								//System.out.println("placedWords size " + placedWords.size());
								//System.out.println("allWords size " + allWords.size());
								//System.out.println("required intersections " + requiredIntersections);
								//System.out.println("current intersections " + currentIntersections);
								//printBoard();
								if(generateBoard(allWords, placedWords, remainingWords, requiredIntersections, currentIntersections)) {
									//System.out.println("truth");
									return true;
								}
								else {
									//System.out.println("vert redact: " + placedWords.get(placedWords.size()-1).actualWord);
									if( redact(j, k, placedWords.get(placedWords.size()-1)) ) {
										//System.out.println("this spot im redacting x: " + j + " y: " + k);
										currentIntersections--;
									}
									remainingWords.add(i, temp);
									placedWords.remove(temp);
								}
							}
//								System.out.println("this is x: " + i + " this is y: " + j);
//								System.out.println("this is placed words size: " + placedWords.size());
						//}
					}
				}
			}
		}
		
		return false;
	}
	//places the word and returns if it placed on an intersection
	private boolean place(int x, int y, Word word) {
		boolean intersection = false;
		//System.out.println("placing: " + word.actualWord + " at x: " + x + " y: " + y);
		if(word.isHorz) {
			for(int i=0; i<word.size(); i++) {
				//checks if this square is an intersection of words
				if( !(squares[x][y+i].isEmpty()) ) {
					squares[x][y+i].isIntersection = true;
				}
				
				if(i==0) {
					squares[x][y].startSquare = true;
					if(word.actualWord.charAt(0) == squares[x][y].letter.charAt(0) && word.number.equals(squares[x][y].number) ) {
						//System.out.println("currIntersections++");
						intersection = true;
					}
				}
				//places the letter and updates the square
				if(!squares[x][y+i].isIntersection) {
					squares[x][y+i].letter = word.letterAt(i);
					squares[x][y+i].number = word.number;
				}
				
				//System.out.println("Placing "+word.actualWord+"-->"+x+","+(y+i));
				
				if(word.isHorz) {
					squares[x][y+i].isPlacedHorz = true;
				}
				else {
					squares[x][y+i].isPlacedVert = true;
				}
			}
		}
		else {
			for(int i=0; i<word.size(); i++) {
				//checks if this square is an intersection of words
				if(squares[x+i][y].isEmpty() == false) {
					squares[x+i][y].isIntersection = true;
				}
				
				if(i==0) {
					squares[x][y].startSquare = true;
					if(word.actualWord.charAt(0) == squares[x][y].letter.charAt(0) && word.number.equals(squares[x][y].number)) {
						//System.out.println("currIntersections++");
						intersection = true;
					}
				}
				//places the letter and updates the square
				if(!squares[x+i][y].isIntersection) {
					squares[x+i][y].letter = word.letterAt(i);
					squares[x+i][y].number = word.number;
				}
				
				//System.out.println("Placing "+word.actualWord+"-->"+(x+i)+","+(y));

				if(word.isHorz) {
					squares[x+i][y].isPlacedHorz = true;
				}
				else {
					squares[x+i][y].isPlacedVert = true;
				}
				
			}
		}
		if(squares[x][y].word == null) {
			squares[x][y].word = word;
		}
		else if(squares[x][y].word2 == null) {
			squares[x][y].word2 = word;
		}
		return intersection;
	}
	
	public boolean redact(int x, int y, Word word) {
		boolean intersection = false;
		if(word.isHorz) {
			for(int i=0; i<word.size(); i++) {
				if(i==0) {
					squares[x][y].startSquare = false;
					//if(word.actualWord.charAt(0) == squares[x][y].letter.charAt(0)) {
					if(squares[x][y].isIntersection == true && word.number.equals(squares[x][y].number)) {
						//System.out.println("currIntersection-- horz");
						intersection = true;
					}
				}
				if(squares[x][y+i].isIntersection == false) {
					squares[x][y+i].letter = "_";
					squares[x][y+i].number = "-1";
				}
				else {
					squares[x][y+i].isIntersection = false;
				}
				squares[x][y+i].isPlacedHorz = false;
			}
		}
		else {
			for(int i=0; i<word.size(); i++) {
				if(i==0) {
					squares[x][y].startSquare = false;
					//if(word.actualWord.charAt(0) == squares[x][y].letter.charAt(0)) {
					if(squares[x][y].isIntersection == true && word.number.equals(squares[x][y].number)) {
						//System.out.println("currIntersection-- vert");
						intersection = true;
					}
				}
				if(squares[x+i][y].isIntersection == false) {
					squares[x+i][y].letter = "_";
					squares[x+i][y].number = "-1";
				}
				else {
					squares[x+i][y].isIntersection = false;
				}
				squares[x+i][y].isPlacedVert = false;
			}
		}
		if(intersection == false) {squares[x][y].word = null;}
		else {
			if(squares[x][y].word == word) {
				squares[x][y].word = null;
			}
			else if(squares[x][y].word2 == word){
				squares[x][y].word2 = null;
			}
		}
		return intersection;
	}
	
	public void updateWordRowCol(ArrayList<Word> allWords, int index, int x, int y) {
		allWords.get(index).row = x;
		allWords.get(index).col = y;
	}
	
	public boolean canHorzWordPlace(int x, int y, Word word) {
		//System.out.println("checking horz: " + word.actualWord + " at x: " + x + " y: " + y);
		int neededSpace = y+word.size();
		if( neededSpace >= col ) {
			//System.out.println("this is y+word.size(): " + neededSpace);
			return false;
		}
		boolean flag = false;
		for(int i=0; i<word.size(); i++) {
			if(i==0) {
				if(canHorzPlace(x, y+i, true, false, word.actualWord.charAt(i)) == false) {
					return false;
				}
			}
			else if(i==word.size()-1) {
				if(canHorzPlace(x, y+i, false, true, word.actualWord.charAt(i)) == false) {
					return false;
				}
			}
			else if(canHorzPlace(x, y+i, false, false, word.actualWord.charAt(i)) == false) {
				return false;
			}
			if(squares[x][y+i].isEmpty() == false) {
				flag = true;
			}
		}
		return flag;
	}
	
	public boolean canVertWordPlace(int x, int y, Word word) {
		int neededSpace = x+word.size();
		//System.out.println("checking vert: " + word.actualWord + " at x: " + x + " y: " + y);
		if( neededSpace >= row ) {
			return false;
		}
		boolean flag = false;
		for(int i=0; i<word.size(); i++) {
			if(i==0) {
				if(canVertPlace(x+i, y, true, false, word.actualWord.charAt(i)) == false) {
					return false;
				}
			}
			else if(i==word.size()-1) {
				if(canVertPlace(x+i, y, false, true, word.actualWord.charAt(i)) == false) {
					return false;
				}
			}
			else if(canVertPlace(x+i, y, false, false, word.actualWord.charAt(i)) == false) {
				return false;
			}
			if(squares[x+i][y].isEmpty() == false) {
				flag = true;
			}
		}
		return flag;
	}
	
	public boolean canHorzPlace(int x, int y, boolean first, boolean last, char currLetter) {
		//System.out.println("this is x: " + x + " " + "this is y: " + y + " " + "this is letter: " + currLetter);
		//check if there exists a horizontal letter on the current spot
		if(squares[x][y].isPlacedHorz) {
			return false;
		}
		if(squares[x][y].isPlacedVert && currLetter == squares[x][y].letter.charAt(0)) {
			return true;
		}
		else if(squares[x][y].isPlacedVert && currLetter != squares[x][y].letter.charAt(0)) {
			return false;
		}
		//check the edge cases
		if(x == 0 || x == row-1 || y == 0 || y == col-1) {
			if(x == 0 && y == 0) {
				if(squares[x][y+1].isPlacedHorz) return false;
				if(squares[x][y+1].isPlacedVert && (last || first)) return false;
				if(squares[x+1][y].isPlacedHorz || squares[x+1][y].isPlacedVert) return false;
			}
			else if(x == row-1 && y == col-1) {
				if(squares[x][y-1].isPlacedHorz) return false;
				if(squares[x][y-1].isPlacedVert && (last || first)) return false;
				if(squares[x-1][y].isPlacedHorz || squares[x-1][y].isPlacedVert) return false;
			}
			else if(x == 0 && y == col-1) {
				if(squares[x][y-1].isPlacedHorz) return false;
				if(squares[x][y-1].isPlacedVert && (last || first)) return false;
				if(squares[x+1][y].isPlacedHorz || squares[x+1][y].isPlacedVert) return false;
			}
			else if(y == 0 && x == row-1) {
				if(squares[x][y+1].isPlacedHorz) return false;
				if(squares[x][y+1].isPlacedVert && (last || first)) return false;
				if(squares[x-1][y].isPlacedHorz || squares[x-1][y].isPlacedVert) return false;
			}
			else if(x == 0) {
				if(squares[x][y-1].isPlacedHorz || squares[x][y+1].isPlacedHorz) return false;
				if( (squares[x][y-1].isPlacedVert && first) || (squares[x][y+1].isPlacedVert && last) ) return false;
				if(squares[x+1][y].isPlacedHorz || squares[x+1][y].isPlacedVert) return false;
			}
			else if(y == 0) {
				if(squares[x][y+1].isPlacedHorz) return false;
				if(squares[x][y+1].isPlacedVert && (last || first) ) return false;
				if(squares[x+1][y].isPlacedHorz || squares[x+1][y].isPlacedVert || squares[x-1][y].isPlacedHorz || squares[x-1][y].isPlacedVert) return false;
			}
		}
		else {
			//check if there is a horizontal letter to the left or right
			if(squares[x][y-1].isPlacedHorz || squares[x][y+1].isPlacedHorz) {
				return false;
			}
			//check if there is a horizontal letter up or down
			if(squares[x-1][y].isPlacedHorz || squares[x+1][y].isPlacedHorz) {
				return false;
			}
			//check if there is a vertical letter up or down
			if(squares[x-1][y].isPlacedVert || squares[x+1][y].isPlacedVert) {
				return false;
			}
			//check if there is a vertical letter to the left or right
			if( (squares[x][y-1].isPlacedVert && first) || (squares[x][y+1].isPlacedVert && last) ) {
				return false;
			}
		}
		
		return true;
	}
	
	public boolean canVertPlace(int x, int y, boolean first, boolean last, char currLetter) {
		//check if there exists a vertical letter on the current spot
		if(squares[x][y].isPlacedVert) {
			return false;
		}
		//System.out.println("curr Letter = " + currLetter + " and this is square letter = " + squares[x][y].letter.charAt(0));
		if(squares[x][y].isPlacedHorz && currLetter == squares[x][y].letter.charAt(0)) {
			return true;
		}
		else if(squares[x][y].isPlacedHorz && currLetter != squares[x][y].letter.charAt(0)) {
			return false;
		}
		if(x == 0 || x == row-1 || y == 0 || y == col-1) {
			if(x == 0 && y == 0) {
				if(squares[x+1][y].isPlacedVert) return false;
				if(squares[x+1][y].isPlacedHorz && (last || first)) return false;
				if(squares[x][y+1].isPlacedVert || squares[x][y+1].isPlacedHorz) return false;
			}
			else if(x == row-1 && y == col-1) {
				if(squares[x-1][y].isPlacedVert) return false;
				if(squares[x-1][y].isPlacedHorz && (last || first)) return false;
				if(squares[x][y-1].isPlacedVert || squares[x][y-1].isPlacedHorz) return false;
			}
			else if(y == 0 && x == row-1) {
				if(squares[x-1][y].isPlacedVert) return false;
				if(squares[x-1][y].isPlacedHorz && (last || first)) return false;
				if(squares[x][y+1].isPlacedVert || squares[x][y+1].isPlacedHorz) return false;
			}
			else if(x == 0 && y == col-1) {
				if(squares[x+1][y].isPlacedVert) return false;
				if(squares[x+1][y].isPlacedHorz && (last || first)) return false;
				if(squares[x][y-1].isPlacedVert || squares[x][y-1].isPlacedHorz) return false;
			}
			else if(y == 0) {
				if(squares[x-1][y].isPlacedVert || squares[x+1][y].isPlacedVert) return false;
				if( (squares[x-1][y].isPlacedHorz && first) || (squares[x+1][y].isPlacedHorz && last) ) return false;
				if(squares[x][y+1].isPlacedVert || squares[x][y+1].isPlacedHorz) return false;
			}
			else if(x == 0) {
				if(squares[x+1][y].isPlacedVert) return false;
				if(squares[x+1][y].isPlacedHorz && (last || first)) return false;
				if(squares[x][y+1].isPlacedVert || squares[x][y+1].isPlacedHorz || squares[x][y-1].isPlacedVert || squares[x][y-1].isPlacedHorz) return false;
			}
		}
		else {
			//check if there is a vertical letter up or down
			if(squares[x-1][y].isPlacedVert || squares[x+1][y].isPlacedVert) {
				return false;
			}
			//check if there is a vertical letter left or right
			if(squares[x][y-1].isPlacedVert || squares[x][y+1].isPlacedVert) {
				return false;
			}
			//check if there is a horizontal letter left or right
			if(squares[x][y-1].isPlacedHorz || squares[x][y+1].isPlacedHorz) {
				return false;
			}
			//check if there is a horizontal letter up or down
			if( (squares[x-1][y].isPlacedHorz && first) || (squares[x+1][y].isPlacedHorz && last) ) {
				return false;
			}
		}
		
		return true;
	}
	
	public void placeWordOnBoard(String word) {
		for(int i=0; i<row; i++) {
			for(int j=0; j<col; j++) {
				if(squares[i][j].startSquare) {
					if(squares[i][j].word.actualWord.equals(word)) {
						squares[i][j].isPlacedGen = true;
					}
				}
			}
		}
	}
	
	public void printBoard() {
		for(int i=0; i<row; i++) {
			for(int j=0; j<col; j++) {
				if(squares[i][j].startSquare) {
					System.out.print(squares[i][j].number + squares[i][j].letter);
				}
				else {
					System.out.print(squares[i][j].letter + " ");
				}
			}
			System.out.println();
		}
	}
//	public void printBoard(char[][] board) {
//		for(int i=0; i<row; i++) {
//			for(int j=0; j<col*2; j++) {
//				System.out.print(board[i][j]);
//			}
//			System.out.println();
//		}
//	}
	public void initialize() {
		for(int i=0; i<row; i++) {
			for(int j=0; j<col; j++) {
				squares[i][j] = new Square();
			}
		}
	}
	
	public Square getSquare(int x, int y) {
		return squares[x][y];
	}
	
	public String stringBoard(int line) {
		String board = "";
		for(int j=0; j<col; j++) {
			if( squares[line][j].startSquare && ( (squares[line][j].word != null && squares[line][j].word.placedByPlayer) || (squares[line][j].word2 != null && squares[line][j].word2.placedByPlayer) ) ) {
				board += squares[line][j].number;
				board += squares[line][j].letter;
				if(squares[line][j].word != null && squares[line][j].word.placedByPlayer ) {
					if(squares[line][j].word.isHorz) {
						for(int k=1; k<squares[line][j].word.size(); k++) {
							squares[line][j+k].forcePlace = true;
						}
					}
					else {
						for(int k=1; k<squares[line][j].word.size(); k++) {
							squares[line+k][j].forcePlace = true;
						}
					}
				}
				if(squares[line][j].word2 != null && squares[line][j].word2.placedByPlayer) {
					if(squares[line][j].word2.isHorz) {
						for(int k=1; k<squares[line][j].word2.size(); k++) {
							squares[line][j+k].forcePlace = true;
						}
					}
					else {
						for(int k=1; k<squares[line][j].word2.size(); k++) {
							squares[line+k][j].forcePlace = true;
						}
					}
				}
			}
			else if(squares[line][j].startSquare && ( (squares[line][j].word != null && squares[line][j].word.placedByPlayer == false) || (squares[line][j].word2 != null && squares[line][j].word2.placedByPlayer == false) ) ) {
				board += squares[line][j].number;
				board += " ";
				if(squares[line][j].word != null && squares[line][j].word.placedByPlayer == false ) {
					if(squares[line][j].word.isHorz) {
						for(int k=1; k<squares[line][j].word.size(); k++) {
							squares[line][j+k].canPlace = true;
						}
					}
					else {
						for(int k=1; k<squares[line][j].word.size(); k++) {
							squares[line+k][j].canPlace = true;
						}
					}
				}
				if(squares[line][j].word2 != null && squares[line][j].word2.placedByPlayer == false) {
					if(squares[line][j].word2.isHorz) {
						for(int k=1; k<squares[line][j].word2.size(); k++) {
							squares[line][j+k].canPlace = true;
						}
					}
					else {
						for(int k=1; k<squares[line][j].word2.size(); k++) {
							squares[line+k][j].canPlace = true;
						}
					}
				}
			}
			else if(squares[line][j].forcePlace){
				board += squares[line][j].letter;
				board += " ";
			}
			else if(squares[line][j].canPlace) {
				board += "_";
				board += " ";
			}
//			else if( (squares[line][j].word != null && squares[line][j].word.placedByPlayer) || (squares[line][j].word2 != null && squares[line][j].word2.placedByPlayer) ) {
//				board += squares[line][j].letter;
//				board += " ";
//			}
			else {
				//board += "_";
				board += " ";
				board += " ";
			}
		}
		return board;
	}
	
	public static void main(String[] argc) {
		Board board = new Board();
		board.initialize();
		Word word1 = new Word(true, "1", "trojans");
		Word word2 = new Word(true, "2", "dodgers");
		Word word3 = new Word(true, "3", "csci");
		Word word4 = new Word(false, "1", "traveler");
		Word word5 = new Word(false, "4", "gold");
		Word word6 = new Word(false, "5", "marshall");
		ArrayList<Word> allWords = new ArrayList<Word>();
		ArrayList<Word> remainingWords = new ArrayList<Word>();
		ArrayList<Word> placedWords = new ArrayList<Word>();
		allWords.add(word1);
		allWords.add(word2);
		allWords.add(word3);
		allWords.add(word4);
		allWords.add(word5);
		allWords.add(word6);
		for(int i=0; i<allWords.size(); i++) {
			remainingWords.add(allWords.get(i));
		}
		board.generateBoard(allWords, placedWords, remainingWords, 1, 0);
		//board.printBoard();
	}
}

class Square {
	String letter = "_";
	String number = "-1";
	Word word = null;
	Word word2 = null;
	boolean startSquare = false;
	boolean isPlacedHorz = false;
	boolean isPlacedVert = false;
	boolean isIntersection = false;
	boolean isPlacedGen = false;
	boolean forcePlace = false;
	boolean canPlace = false;
	
	public boolean isEmpty() {
		if(letter.equals("_")) {
			return true;
		}
		else {
			return false;
		}
	}
}