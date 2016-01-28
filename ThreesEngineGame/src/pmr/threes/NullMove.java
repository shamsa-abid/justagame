/* Copyright (c) 2014 Peter Rimshnick
 * See the file license.txt for copying permission.
 */
package pmr.threes;

public class NullMove implements Move {
	
	private State state;
	
	public NullMove(State state){
		this.state = state;
	}

	@Override
	public State[] findEndStatesForSearch() {
		return new State[]{state};
	}

	@Override
	public State[] findEndStatesForSim() {
		return new State[]{state};
	}	
}
