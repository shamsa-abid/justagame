/* Copyright (c) 2014 Peter Rimshnick
 * See the file license.txt for copying permission.
 */
package pmr.threes;

import java.util.Arrays;
import java.util.Map;

public class State {
	
	private final Board board;
	private final double probability;
	private final Map<HoleCard, Integer> cardStack;
	private final HoleCard holeCard;
	
	private static final double EPSILON = .00000001;
		
	public State(int[][] boardArray, Map<HoleCard, Integer> cardStack, HoleCard holeCard, double probability){
		this.board = new Board(boardArray);
		if (cardStack.isEmpty()) {
			this.cardStack = ThreesGame.generateCardStack(board);		
		}
		else this.cardStack = cardStack;
		this.holeCard = holeCard;
		this.probability = probability;
	}
	
	public State(int[][] boardArray, HoleCard holeCard){
		this.board = new Board(boardArray);		
		this.probability = 1.0;
		this.cardStack = ThreesGame.generateCardStack(board);
		this.holeCard = holeCard;
	}
	
	public State(Board board, Map<HoleCard, Integer> cardStack, HoleCard holeCard, double probability){
		this.board = board;
		if (cardStack.isEmpty()) {
			this.cardStack = ThreesGame.generateCardStack(board);		
		}
		else this.cardStack = cardStack;
		this.holeCard = holeCard;
		this.probability = probability;
	}
	
	public State(Board board, HoleCard holeCard){
		this.board = board;		
		this.probability = 1.0;
		this.cardStack = ThreesGame.generateCardStack(board);
		this.holeCard = holeCard;
	}
	
	public State(State newRoot){
		this.board = newRoot.getBoard();
		this.probability = 1.0;
		this.cardStack = newRoot.getCardStack();
		this.holeCard = newRoot.getHoleCard();
	}
	
	public Board getBoard(){ return board; };
	double getProbability() {return probability;}
	Map<HoleCard, Integer> getCardStack() { return cardStack; };
	HoleCard getHoleCard() { return holeCard; }
	
	@Override
	public boolean equals(Object o){
		if (!(o instanceof State)) return false;
		State s = (State)o;
		return s.board.equals(board) && s.cardStack.equals(cardStack) && s.holeCard.equals(holeCard) && Math.abs((s.probability-probability)/probability)<EPSILON;
	}
	
	@Override
	public int hashCode(){
		return Arrays.hashCode(board.getBoardArray());
	}
	
	@Override
	public String toString(){
		return board.toString() + "HoleCard: " + holeCard + " Prob: " + String.format("%3.2e\n", probability);
	}
	

}
