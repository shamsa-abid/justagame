/* Copyright (c) 2014 Peter Rimshnick
 * See the file license.txt for copying permission.
 */
package pmr.threes;

import java.util.List;
import java.util.ArrayList;

public class AmbiguousHoleCard implements HoleCard {
	
	private final List<Integer> possibleAdditions;

	public AmbiguousHoleCard(List<Integer> possibleAdditions) {
		this.possibleAdditions = possibleAdditions;
	}
	
	public List<Integer> getPossibleAdditions(){
		return new ArrayList<Integer>(possibleAdditions);
	}
	
	@Override
	public String toString(){
		return "+";
	}

}
