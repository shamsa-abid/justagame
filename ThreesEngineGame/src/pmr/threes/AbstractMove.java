/* Copyright (c) 2014 Peter Rimshnick
 * See the file license.txt for copying permission.
 */
package pmr.threes;

import static pmr.threes.ThreesGame.oneAndTwo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public abstract class AbstractMove implements Move {

	protected final State previousState;

	public AbstractMove(State previousState){
		this.previousState = previousState;
	}	

	@Override
	public State[] findEndStatesForSearch() {		
		List<State> step1States = getStep1States(previousState);
		List<State> newStates;
		if (step1States.size()!=1 || step1States.get(0)!=previousState) {
			newStates = updateHoleCardForSearch(step1States);
		}
		else newStates = new ArrayList<State>(Arrays.asList(new State[]{previousState}));		
		return newStates.toArray(new State[newStates.size()]);
	}

	@Override
	public State[] findEndStatesForSim() {		
		List<State> step1States = getStep1States(previousState);
		List<State> newStates;
		if (step1States.size()!=1 || step1States.get(0)!=previousState) {
			newStates = updateHoleCardForSim(step1States);
		}
		else newStates = new ArrayList<State>(Arrays.asList(new State[]{previousState}));		
		return newStates.toArray(new State[newStates.size()]);
	}

	protected static void copyInto(int[] fromArray, int[] toArray){
		for (int i = 0; i<fromArray.length; i++){
			toArray[i] = fromArray[i];
		}
	}

	protected static int[][] copyBoard(int[][] board){
		return Board.deepCopy(board);
	}

	protected static Map<HoleCard, Integer> subtractKey(Map<HoleCard, Integer> stack, HoleCard h){
		Map<HoleCard, Integer> newMap = new HashMap<HoleCard, Integer>(stack);
		int count = stack.get(h);
		assert count>0;
		if (count==1) {
			newMap.remove(h);			
		}
		else {
			newMap.put(h, count-1);		
		}
		return newMap;
	}

	protected static HoleCard getRandomCard(Map<HoleCard, Integer> stack){
		List<HoleCard> expanded = new ArrayList<HoleCard>();
		for (Entry<HoleCard, Integer> e: stack.entrySet()){
			for (int i = 0; i<e.getValue(); i++){
				expanded.add(e.getKey());
			}
		}
		Collections.shuffle(expanded);
		return expanded.get(0);
	}

	protected List<State> updateHoleCardForSearch(List<State> step1States){
		List<State> step2States = new ArrayList<State>();		
		for (State s: step1States){			
			HoleCard h = getRandomCard(s.getCardStack());
			step2States.add(new State(s.getBoard(), subtractKey(s.getCardStack(),h), h, s.getProbability()));

		}
		return step2States;
	}

	protected List<State> updateHoleCardForSim(List<State> step1States){
		List<State> step2States = new ArrayList<State>();		
		for (State s: step1States){
			int cardCount = 0;
			for (HoleCard h: s.getCardStack().keySet()) {
				cardCount+=s.getCardStack().get(h);
			}
			for (Entry<HoleCard, Integer> e: s.getCardStack().entrySet()){				
				HoleCard h = e.getKey();
				step2States.add(new State(s.getBoard(), subtractKey(s.getCardStack(),h), h, (s.getProbability() * e.getValue())/cardCount));
			}
		}
		return step2States;
	}

	protected static int[][] transpose(int[][] matrix){
		int[][] newMatrix = new int[matrix[0].length][matrix.length];
		for (int i = 0; i<matrix.length;i++){
			for (int j = 0; j<matrix[0].length; j++){
				newMatrix[j][i] = matrix[i][j];
			}
		}		
		return newMatrix;
	}

	protected static int[] combineLeft(int[] row) {
		if (row.length==1) return row;
		int[] newRow = new int[row.length];
		if (row[0]!=0 && row[0]==row[1] && row[0]>2) {
			newRow[0] = row[0]*2;
			shiftLeft(row, newRow,2);
			return newRow;
		}
		else if (row[0]!=0 && oneAndTwo(row[0],row[1])){
			newRow[0] = 3;
			shiftLeft(row, newRow,2);
			return newRow;
		}
		else if (row[0]==0){
			shiftLeft(row, newRow, 1);
			return newRow;
		}
		else {
			newRow[0] = row[0];
			int[] rest = combineLeft(Arrays.copyOfRange(row, 1, row.length));
			for (int i = 0; i < rest.length; i++){
				newRow[i+1] = rest[i];
			}
			return newRow;
		}
	}

	protected static void shiftLeft(int[] row, int[] newRow, int start){
		for (int i = start; i<row.length; i++){
			newRow[i-1]=row[i];
		}
		newRow[row.length-1] = 0;		
	}

	protected static int[] combineRight(int[] row) {		
		return reverse(combineLeft(reverse(row)));
	}

	protected static void shiftRight(int[] row, int[] newRow, int start){
		for (int i = start; i>0; i--){
			newRow[i+1]=row[i];
		}
		newRow[0] = 0;		
	}		

	protected static int[] reverse(int[] array){
		int[] copy = new int[array.length];
		for (int i = 0; i<array.length; i++){
			copy[array.length-1-i] = array[i];
		}
		return copy;
	}

	protected abstract List<State> getStep1States(State state);
	protected abstract Board getNewBoard(State state);




}
