package variable;

import java.util.ArrayDeque;
import java.util.Deque;

import com.scitools.understand.Reference;

public class VariableData {
	public Deque<VariableLocation> data = new ArrayDeque<>();
	public int id;
	private int lastLexicalUse = -1;

	public VariableData(int id, Reference[] refs) {
		int length = refs.length;
		int line1 = 0;
		int line2 = 0;
		int column1 = 0;
		int column2 = 0;
		this.id = id;
		for(int i = 0; i<length; i++) {
			line1 = refs[i].line();
			column1 = refs[i].column();
			if(line1!=line2 || column1!=column2) {
				VariableLocation var = new VariableLocation(id, line1, column1);
				data.add(var);
			}
			line2 = line1;
			column2 = column1;
		}
	}

	public void setLastLexicalUse(int n) {
		lastLexicalUse = n;
	}
	public int getLastLexicalUse() {
		return lastLexicalUse;
	}

}
