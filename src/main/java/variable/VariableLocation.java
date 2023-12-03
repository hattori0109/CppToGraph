package variable;

public class VariableLocation {
	private int line;
	private int column;
	private int id;

	public VariableLocation(int id, int l, int c) {
		this.id = id;
		line = l;
		column = c;
	}

	public int getId() {
		return id;
	}
	public int getLine() {
		return line;
	}
	public int getColumn() {
		return column;
	}
}
