import java.util.Arrays;

//Justin Neff
//TCSS 435A
public class Puzzle implements Comparable<Puzzle> {
	//Parent Node
	Puzzle parent;
	//x and y coords of space char
	int x = 0, y = 0;
	// are movements in a direction possible
	boolean up = false, down = false, left = false, right = false;
	//space char current index
	int curIndex = 0;
	// depth of puzzle piece
	int depth = 0;
	// puzzle board state for this piece
	char board[];
	// size of the board (3x3 or 4x4)
	int size;
	// Manhatten and displacement hueristics
	int hMan = 999;
	int hDisp = 999;
	// hueristics mode
	int mode = 0;

	public Puzzle(Puzzle puzzle, char board[], int index, int depth) {
		//puzzle depth
		this.depth = depth;
		//set incoming puzzle to this parent puzzle
		this.parent = puzzle;
		// use the incoming board as this pieces board
		this.board = board;
		// calculate the size of the puzzle
		this.size = (int) Math.floor(Math.sqrt(board.length));
		// set the x and y coods of the space char
		setXY(index);
	}

	public Puzzle(Puzzle puzzle, char board[], int index, int depth, int mode) {
		this.mode = mode;
		this.depth = depth;
		this.parent = puzzle;
		this.board = board;
		this.size = (int) Math.floor(Math.sqrt(board.length));
		setXY(index);
		// if a search uses h1 or h2 then use heuristics
		if (mode > 0)
			setHueristic(depth);
	}

	//Set space index, calculate x and y position and check movement possibilities
	void setXY(int index) {
		curIndex = index;
		x = index % size;
		y = index / size;
		up = y > 0;				//Can move up?
		down = y < size - 1;	//Can move down?
		left = x > 0;  			//Can move left?
		right = x < size - 1;	//Can move right?
	}

	//move the space to the right and create a new puzzle piece with the changes
	Puzzle moveRight() {
		char[] newBoard = board.clone();
		newBoard[curIndex] = newBoard[curIndex + 1];
		newBoard[curIndex + 1] = ' ';
		return new Puzzle(this, newBoard, curIndex + 1, depth + 1, mode);
	}

	Puzzle moveLeft() {
		char[] newBoard = board.clone();
		newBoard[curIndex] = newBoard[curIndex - 1];
		newBoard[curIndex - 1] = ' ';
		return new Puzzle(this, newBoard, curIndex - 1, depth + 1, mode);
	}

	Puzzle moveUp() {
		char[] newBoard = board.clone();
		newBoard[curIndex] = newBoard[curIndex - size];
		newBoard[curIndex - size] = ' ';
		return new Puzzle(this, newBoard, curIndex - size, depth + 1, mode);
	}

	Puzzle moveDown() {
		char[] newBoard = board.clone();
		newBoard[curIndex] = newBoard[curIndex + size];
		newBoard[curIndex + size] = ' ';
		return new Puzzle(this, newBoard, curIndex + size, depth + 1, mode);
	}

	// take in d for the depth of the node
	void setHueristic(int d) {
		depth = d;
		// keep track of correct tile positions
		int correctPositions = 0;
		// keep track of total manhatten distance
		int distance = 0;
		//calculate how many tiles are in the right position
		for (int i = 0; i < board.length; i++)
			correctPositions += isTileInPos(board[i], i, 1) == 0 ? 1 : 0;
		// hueristics based on depth and tile displacement
		hDisp = correctPositions + depth;
		// calculate the manhatten distance for all of the pieces on the board
		for (int i = 0; i < board.length; i++) {
			distance += isTileInPos(board[i], i, 1);
		}
		hMan = distance;
	}

	// take the character and find it's position in relation to it's goal.
	private int isTileInPos(char tileChar, int index, int opt) {
		// turns the char into it's ascii value
		int val = (int) tileChar;
		// adjusts the character values to match the indecies of the array.
		if (val >= 49 && val <= 57) {
			val = val - 49;
		} else if (val >= 65 && val <= 70) {
			val = val - 56;
		} else if (val == 32) {
			val = 15;
		}
		
		// distplacement
		if (opt == 1) {
			return index == val ? 0 : 1;
		}
		// the manhattan distance between the current position to the goal position.
		return Math.abs((val % 4) - index % 4 + (val / 4) - index / 4);
	}

	// Override so puzzle's toString is nice
	@Override
	public String toString() {
		String state = "";
		for (int i = 0; i < board.length; i++) {
			state += "[" + board[i] + "]";
			if (i % size == size - 1) {
				state += "\r";
			}
		}
		return state;
	}

	// make it so you can compare equality based on the puzzle board state
	@Override
	public boolean equals(Object o) {
		Puzzle puzzle = (Puzzle) o;
		return Arrays.equals(puzzle.board, this.board);
	}

	@Override
	public int hashCode() {
		return this.board.hashCode();
	}

	// when using the priority queue sort by manhatten or displacement depending on mode.
	@Override
	public int compareTo(Puzzle puzzle) {
		return mode == 1 ? this.hDisp - puzzle.hDisp : this.hMan - puzzle.hMan;
	}
}
