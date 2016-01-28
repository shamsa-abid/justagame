/* Copyright (c) 2014 Peter Rimshnick
 * See the file license.txt for copying permission.
 */
package pmr.threes;

public class Choice implements Comparable<Choice> {
	private final Move move;
	private final double value;	

	private Choice(){
		move = null;
		value = Double.NEGATIVE_INFINITY;		
	};

	public Choice(Move move, double value){
		this.move = move;
		this.value = value;		
	}
	
	private static class EmptyChoice extends Choice {
		private EmptyChoice(){};
		
		@Override
		public boolean isEmptyChoice() {
			return true;
		}		
	}
	
	public static Choice getEmptyChoice(){
		return new EmptyChoice();
	}	

	public Move getMove() {
		return move;
	}

	public double getValue() {
		return value;
	}
	
	public boolean isEmptyChoice(){
		return false;
	}
	
	@Override
	public String toString(){
		return "(" + move + "," + value +")";
	}
	
	@Override
	public int compareTo(Choice c){
		return Double.compare(value, c.value);
	}
   
}	
