/* Copyright (c) 2014 Peter Rimshnick
 * See the file license.txt for copying permission.
 */
package pmr.threes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ThreesGame {

	private final int depth;
	final double boardWeight;
	final double freeCellWeight;
	final double matchableWeight;
	private static final Random rand = new Random(0);
	private static final double EPSILON = .0000001;
	private static final int THREAD_DEPTH = 3;
	private ExecutorService threadPool;

	public ThreesGame(){
		depth = 5;
		boardWeight = .33;
		freeCellWeight = .33;
		matchableWeight = .33;
	}

	public ThreesGame(int depth){
		this.depth = depth;
		boardWeight = .33;
		freeCellWeight = .33;
		matchableWeight = .33;
	}

	public ThreesGame(int depth, double boardWeight, double freeCellWeight, double matchableWeight){
		this.depth = depth;
		this.boardWeight = boardWeight;
		this.freeCellWeight = freeCellWeight;
		this.matchableWeight = matchableWeight;
	}	

	public Choice findBestMove(State start){		
		threadPool = Executors.newCachedThreadPool();
		Choice bestMove = findBestMove(start, start, depth, THREAD_DEPTH); 
		threadPool.shutdown();
		return bestMove;		
	}
	
	private Choice findBestMove(final State root, final State s, final int depth, final int threadDepth) {		
		if (depth>0){
			Move[] options = {new Left(s), new Right(s), new Up(s), new Down(s)};
			Choice emptyChoice = Choice.getEmptyChoice();
			List<Choice> choices = new ArrayList<Choice>();
			if (threadDepth>0){
				List<Future<Choice>> futures = new ArrayList<Future<Choice>>();
				for (final Move m: options){
					Future<Choice> future = threadPool.submit(new Callable<Choice>(){
						@Override
						public Choice call() throws Exception {
							return evaluateMove(root, s, m, depth, threadDepth);
						}						
					});
					futures.add(future);
				}
				for (Future<Choice> future: futures){
					Choice c = null;
					try {
						c = future.get();
					} catch (InterruptedException | ExecutionException e) {
						throw new RuntimeException(e);
					}
					if (c!=null) choices.add(c);
				}
			}
			else {
				for (Move m: options){
					Choice c = null;					
					c = evaluateMove(root, s, m, depth,0);
					if (c!=null) choices.add(c);				
				}
			}			
			if (choices.isEmpty()) return emptyChoice;
			Collections.sort(choices);
			return choices.get(choices.size()-1);
		}
		else return new Choice(new NullMove(s), evaluateState(root, s));

	}

	private Choice evaluateMove(State root, State s, Move m, int depth, int threadDepth){
		double avg = 0;				
		State[] endStates = m.findEndStatesForSearch();
		if (endStates.length==1 && endStates[0]==s) return null;
		for (State mState: endStates){
			avg += findBestMove(root, mState, depth-1, threadDepth-1).getValue();					
		}
		return new Choice(m,avg);		
	}

	private double evaluateState(State root, State state){
		double boardScore = boardWeight<EPSILON? 0 : getBoardScore(root.getBoard())/getBoardScore(state.getBoard());
		int freeCellScore = freeCellWeight<EPSILON? 0 : getFreeCellScore(state.getBoard());
		int matchableScore = matchableWeight<EPSILON? 0 : getMatchableScore(state.getBoard());
		double score = (boardScore*boardWeight + freeCellScore*freeCellWeight + matchableScore*matchableWeight) * state.getProbability();
		return score;
	}

	public static double getBoardScore(Board board) {
		double score = 0;
		for (int[] row: board.getBoardArray()){
			for (int cell: row){
				if (cell>2) score += Math.pow(3, Math.log(cell/3)/Math.log(2)+1);  //3^(log_2(cell/3)+1) = 3^((log(cell/3)/log(2))+1)
			}
		}
		return score;
	}

	private int getFreeCellScore(Board board) {
		int freeCount = 0;
		for (int[] row: board.getBoardArray()){
			for (int cell: row){
				if (cell==0) freeCount ++;
			}
		}
		return freeCount;
	}

	private int getMatchableScore(Board board) {
		int[][] boardArray = board.getBoardArray();
		int score = 0;
		for (int i = 0; i < boardArray.length; i++){
			for (int j = 0; j < boardArray[0].length; j++){
				if (i>0 && canMatch(boardArray[i][j], boardArray[i-1][j])) score++;
				if (i<boardArray.length-1 && canMatch(boardArray[i][j], boardArray[i+1][j])) score++;
				if (j>0 && canMatch(boardArray[i][j], boardArray[i][j-1])) score++;
				if (j<boardArray.length-1 && canMatch(boardArray[i][j], boardArray[i][j+1])) score++;				
			}
		}
		return score;
	}

	static boolean canMatch(int int1, int int2) {
		return (int1==int2 && int1>=3) || oneAndTwo(int1, int2);
	}
	
	protected static boolean oneAndTwo(int a, int b){
		return (a==2 && b==1) || (a==1 && b==2);
	}	

	public static Map<HoleCard,Integer> generateCardStack(Board board) {
		//Just generate one at random, don't split state space
		Map<HoleCard, Integer> stack = new HashMap<HoleCard,Integer>();
		stack.put(new RegularHoleCard(3),4);
		stack.put(new RegularHoleCard(2),4);
		stack.put(new RegularHoleCard(1),4);

		if (rand.nextDouble()<.5){
			List<Integer> possibleAdditions = getPossibleAdditions(board.getBoardArray());
			if (possibleAdditions.size()>0) {
				stack.put(new AmbiguousHoleCard(possibleAdditions),1);			
			}
		}
		return stack;
	}
	
	public static List<Integer> getPossibleAdditions(int[][] boardArray){
		int maxCard = 1;
		for (int[] row: boardArray){
			for (int cell: row){
				if (cell>maxCard) maxCard = cell;
			}
		}
		List<Integer> possibleAdditions = new ArrayList<Integer>();
		for (int i = 3; i<=maxCard/8; i*=2){
			possibleAdditions.add(i);
		}
		return possibleAdditions;
	}

	public static State pickState(State[] endStates){
		double prob = 0;
		for (State s: endStates){
			prob += s.getProbability();
		}
		assert Math.abs(prob-1)<.001;
		List<State> stateList = Arrays.asList(endStates);
		Collections.sort(stateList, new Comparator<State>(){
			@Override
			public int compare(State s1, State s2){
				return Double.compare(s2.getProbability(), s1.getProbability()); //ascending
			}
		});
		double random = rand.nextDouble();
		double sum = 0;
		int i = -1;
		do {
			sum += stateList.get(++i).getProbability();
		}
		while ((random > sum) && i<stateList.size()-1);
		return stateList.get(i);
	}


}
