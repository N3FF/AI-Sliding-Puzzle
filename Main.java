import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Stack;

//Justin Neff
//TCSS 435A
public class Main {

	// Goal States
	private static char goal1[];
	private static char goal2[];

	public static void main(String[] args) throws IOException {
		// User Input
		String input[] = input();
		char board[] = null;
		// If the input isn't empty turn the first segment of input into a char array
		if (input != null) {
			board = input[0].toCharArray();
			// Else invalid input
		} else {
			System.out.println("invalid input");
			return;
		}
		// Gets the index for the space character in the array
		int spaceIndex = 0;
		for (int i = 0; i < board.length; i++) {
			if (board[i] == ' ')
				spaceIndex = i;
		}
		// Set possible goals
		setGoals(board);
		// Solve puzzle using input method
		switch (input[1]) {
		case "bfs":
			bfs(board, spaceIndex);
			break;
		case "dfs":
			dfs(board, spaceIndex);
			break;
		case "gbfs":
			gbfs(board, input[2], spaceIndex);
			break;
		case "astar":
			astar(board, input[2], spaceIndex);
			break;
		case "dls":
			dls(board, input[2], spaceIndex);
			break;

		}

	}

	// Build the two goal states by sorting the input for one and swapping the last
	// two chars for the other
	private static void setGoals(char[] board) {
		char sorted[] = board.clone();
		Arrays.sort(sorted);
		for (int i = 1; i < sorted.length; i++) {
			sorted[i - 1] = sorted[i];
		}
		sorted[sorted.length - 1] = ' ';
		goal1 = sorted.clone();
		char temp = sorted[sorted.length - 3];
		sorted[sorted.length - 3] = sorted[sorted.length - 2];
		sorted[sorted.length - 2] = temp;
		goal2 = sorted.clone();
	}

	// Depth Limited search
	private static void dls(char[] board, String string, int spaceIndex) {
		// Puzzel (Puzzle ParentNode, char[] board array, int index of the space, int
		// mode of hueristics)
		Puzzle puzzle = new Puzzle(null, board, spaceIndex, 0);
		Stack<Puzzle> stack = new Stack<>();
		ArrayList<Puzzle> states = new ArrayList<>();
		// Print the starting puzzle
		System.out.println("Start\n" + puzzle);
		// R,D,L,U (Remember traversal ordering...)
		// Push first puzzle
		stack.push(puzzle);
		// Convert the string input to the number of the limit for searches.
		int limit = Integer.parseInt(string);
		// max size of fringe
		int max = 0;
		// number of nodes created
		int created = 1;
		// keep traversing while the stack isn't empty (root isn't popped)
		while (!stack.empty()) {
			// Exit if a solution is found
			if (solutionFound(puzzle)) {
				break;
			} else {
				// initiate all possible child nodes
				Puzzle right = null;
				Puzzle down = null;
				Puzzle left = null;
				Puzzle up = null;
				// if you can move right then move right and then move right and check if it's
				// already been visited
				// if it is visited then reset the value to null for later comparison.
				if (puzzle.right) {
					right = puzzle.moveRight();
					if (stateUsed(states, right))
						right = null;
				}
				if (puzzle.down) {
					down = puzzle.moveDown();
					if (stateUsed(states, down))
						down = null;
				}
				if (puzzle.left) {
					left = puzzle.moveLeft();
					if (stateUsed(states, left))
						left = null;
				}
				if (puzzle.up) {
					up = puzzle.moveUp();
					if (stateUsed(states, up))
						up = null;
				}
				// If all of the values are null or you have reached your depth limit then pop
				// one off
				if ((right == null && down == null && left == null && up == null) || puzzle.depth == limit) {
					puzzle = stack.pop();
					// Keep traversing through the tree if there are paths available
				} else {
					if (right != null)
						puzzle = puzzle.moveRight();
					else if (down != null)
						puzzle = puzzle.moveDown();
					else if (left != null)
						puzzle = puzzle.moveLeft();
					else if (up != null)
						puzzle = puzzle.moveUp();
					// add puzzle to states used, push it on the stack and increment the amount
					// created.
					states.add(puzzle);
					stack.push(puzzle);
					created++;
					// keep track of the maximum size
					if (max < stack.size() - 1) {
						max = stack.size() - 1;
					}
				}
			}
		}
		// print the end puzzle
		System.out.println("End\n" + puzzle);
		// print stats
		System.out.println(
				(stack.empty() ? -1 : puzzle.depth) + ", " + created + ", " + (created - stack.size()) + ", " + max);

	}

	// Astar search
	private static void astar(char[] board, String string, int spaceIndex) {
		// get hueristic mode
		int mode = Integer.parseInt(string.trim().substring(1));
		PriorityQueue<Puzzle> queue = new PriorityQueue<>();
		// create first board from user input data
		Puzzle puzzle = new Puzzle(null, board, spaceIndex, 0, mode);
		// temp node
		Puzzle newPuzzle = null;
		ArrayList<Puzzle> visited = new ArrayList<>();
		ArrayList<Puzzle> states = new ArrayList<>();
		// Print starting puzzle
		System.out.println("Start\n" + puzzle);
		// add the first puzzle to the queue
		queue.add(puzzle);
		// keep track of max size of queue
		int max = 0;
		while (true) {
			// see if you can move right
			if (puzzle.right) {
				// if you can move right create a new puzzle with the changes
				newPuzzle = puzzle.moveRight();
				// if it's the solution then break the while loop to exit the method
				if (solutionFound(newPuzzle))
					break;
				// check if the puzzle is in the queue, if it is update it's heuristic values
				// If it's in an already visited state then skip over
				if (!updatedQueue(queue, newPuzzle) && !stateUsed(states, newPuzzle)) {
					queue.add(newPuzzle);
					states.add(newPuzzle);
				}
			}
			// All of the below are the same as the if(puzzle.right)
			if (puzzle.down) {
				newPuzzle = puzzle.moveDown();
				if (solutionFound(newPuzzle))
					break;
				if (!updatedQueue(queue, newPuzzle) && !stateUsed(states, newPuzzle)) {
					queue.add(newPuzzle);
					states.add(newPuzzle);
				}
			}
			if (puzzle.left) {
				newPuzzle = puzzle.moveLeft();
				if (solutionFound(newPuzzle))
					break;
				if (!updatedQueue(queue, newPuzzle) && !stateUsed(states, newPuzzle)) {
					queue.add(newPuzzle);
					states.add(newPuzzle);
				}
			}
			if (puzzle.up) {
				newPuzzle = puzzle.moveUp();
				if (solutionFound(newPuzzle))
					break;
				if (!updatedQueue(queue, newPuzzle) && !stateUsed(states, newPuzzle)) {
					queue.add(newPuzzle);
					states.add(newPuzzle);
				}
			}
			// Keep track of the maximum size of the queue
			if (max < queue.size() - 1) {
				max = queue.size() - 1;
			}
			// poll the lowest huristic valued entry in the queue
			puzzle = queue.poll();
			visited.add(puzzle);
		}
		// print the end puzzle and stats
		System.out.println("End\n" + newPuzzle);
		System.out
				.println(newPuzzle.depth + ", " + (visited.size() + queue.size()) + ", " + visited.size() + ", " + max);
	}

	// Greedy breadth first search
	private static void gbfs(char[] board, String string, int spaceIndex) {
		// parse user input into a mode value
		int mode = Integer.parseInt(string.trim().substring(1));
		PriorityQueue<Puzzle> queue = new PriorityQueue<>();
		// Create first puzzle from user input
		Puzzle puzzle = new Puzzle(null, board, spaceIndex, 0, mode);
		// Temp node
		Puzzle newPuzzle = null;
		ArrayList<Puzzle> visited = new ArrayList<>();
		ArrayList<Puzzle> states = new ArrayList<>();
		// Print starting puzzle
		System.out.println("Start\n" + puzzle);
		queue.add(puzzle);
		// keep track of max queue size
		int max = 0;
		while (true) {
			// if the puzzle can move right then create a new puzzle piece with the
			// rightward move
			if (puzzle.right) {
				newPuzzle = puzzle.moveRight();
				// if the move is the solution break out of the while loop
				if (solutionFound(newPuzzle))
					break;
				// If the state hasn't been used before then
				if (!stateUsed(states, newPuzzle)) {
					// add the puzzle to the queue and the accessed puzzle states.
					queue.add(newPuzzle);
					states.add(newPuzzle);
				}
			}
			// repeat from the if(puzzle.right)
			if (puzzle.down) {
				newPuzzle = puzzle.moveDown();
				if (solutionFound(newPuzzle))
					break;
				if (!stateUsed(states, newPuzzle)) {
					queue.add(newPuzzle);
					states.add(newPuzzle);
				}
			}
			// repeat from the if(puzzle.right)
			if (puzzle.left) {
				newPuzzle = puzzle.moveLeft();
				if (solutionFound(newPuzzle))
					break;
				if (!stateUsed(states, newPuzzle)) {
					queue.add(newPuzzle);
					states.add(newPuzzle);
				}
			}
			// repeat from the if(puzzle.right)
			if (puzzle.up) {
				newPuzzle = puzzle.moveUp();
				if (solutionFound(newPuzzle))
					break;
				if (!stateUsed(states, newPuzzle)) {
					queue.add(newPuzzle);
					states.add(newPuzzle);
				}
			}
			// keeping maximum queue size
			if (max < queue.size() - 1) {
				max = queue.size() - 1;
			}
			puzzle = queue.poll();
			visited.add(puzzle);
		}
		// print end puzzle and stats
		System.out.println("End\n" + newPuzzle);
		System.out
				.println(newPuzzle.depth + ", " + (visited.size() + queue.size()) + ", " + visited.size() + ", " + max);
	}

	// Breadth first search
	private static void bfs(char[] board, int spaceIndex) {
		// Create piece from user input
		Puzzle puzzle = new Puzzle(null, board, spaceIndex, 0);
		// temp piece
		Puzzle newPuzzle = null;
		Queue<Puzzle> queue = new LinkedList<>();
		// ejected into here from queue
		ArrayList<Puzzle> visited = new ArrayList<>();
		// visited puzzle states
		ArrayList<Puzzle> states = new ArrayList<>();
		// print start puzzle
		System.out.println("Start\n" + puzzle);
		// add first puzzle to the queue
		queue.add(puzzle);
		// set max queue size to 0
		int max = 0;

		while (true) {
			// if the puzzle can move right then create a new puzzle piece with the
			// rightward move
			if (puzzle.right) {
				newPuzzle = puzzle.moveRight();
				// if the move is the solution break out of the while loop
				if (solutionFound(newPuzzle))
					break;
				// If the state hasn't been used before then
				if (!stateUsed(states, newPuzzle))
					// add the puzzle to the queue and the accessed puzzle states.
					queue.add(newPuzzle);
				states.add(newPuzzle);
			}
			// repeat from the if(puzzle.right)
			if (puzzle.down) {
				newPuzzle = puzzle.moveDown();
				if (solutionFound(newPuzzle))
					break;
				if (!stateUsed(states, newPuzzle))
					queue.add(newPuzzle);
				states.add(newPuzzle);
			}
			// repeat from the if(puzzle.right)
			if (puzzle.left) {
				newPuzzle = puzzle.moveLeft();
				if (solutionFound(newPuzzle))
					break;
				if (!stateUsed(states, newPuzzle))
					queue.add(newPuzzle);
				states.add(newPuzzle);
			}
			// repeat from the if(puzzle.right)
			if (puzzle.up) {
				newPuzzle = puzzle.moveUp();
				if (solutionFound(newPuzzle))
					break;
				if (!stateUsed(states, newPuzzle))
					queue.add(newPuzzle);
				states.add(newPuzzle);
			}
			// keeping maximum queue size
			if (max < queue.size() - 1) {
				max = queue.size() - 1;
			}
			puzzle = queue.poll();
			visited.add(puzzle);
		}
		// print end puzzle and stats
		System.out.println("End\n" + newPuzzle);
		System.out
				.println(newPuzzle.depth + ", " + (visited.size() + queue.size()) + ", " + visited.size() + ", " + max);
	}

	// Depth first search
	private static void dfs(char[] board, int spaceIndex) {
		// create puzzle from user input
		Puzzle puzzle = new Puzzle(null, board, spaceIndex, 0);
		// stack for DFS
		Stack<Puzzle> stack = new Stack<>();
		// Keep track of puzzle states
		ArrayList<Puzzle> states = new ArrayList<>();
		// print starting puzzle
		System.out.println("Start\n" + puzzle);

		// Put the first puzzle on the stack
		stack.push(puzzle);
		// keep track of the max size of the stack
		int max = 0;
		// keep track of the number of nodes created
		int created = 1;
		while (true) {
			// If the current puzzle is the solution then exit the while loop
			if (solutionFound(puzzle)) {
				break;

			} else {
				// initiate all possible chile nodes
				Puzzle right = null;
				Puzzle down = null;
				Puzzle left = null;
				Puzzle up = null;
				// if you can move right then move right and then move right and check if it's
				// already been visited
				// if it is visited then reset the value to null for later comparison.
				if (puzzle.right) {
					right = puzzle.moveRight();
					if (stateUsed(states, right))
						right = null;
				}
				// repeat from the if(puzzle.right)
				if (puzzle.down) {
					down = puzzle.moveDown();
					if (stateUsed(states, down))
						down = null;
				}
				// repeat from the if(puzzle.right)
				if (puzzle.left) {
					left = puzzle.moveLeft();
					if (stateUsed(states, left))
						left = null;
				}
				// repeat from the if(puzzle.right)
				if (puzzle.up) {
					up = puzzle.moveUp();
					if (stateUsed(states, up))
						up = null;
				}
				// If you can't move anywhere then move back up the tree and try another
				// direction
				if (right == null && down == null && left == null && up == null) {
					puzzle = stack.pop();
				} else {
					// If you can move in a direction then do it
					if (right != null)
						puzzle = puzzle.moveRight();
					else if (down != null)
						puzzle = puzzle.moveDown();
					else if (left != null)
						puzzle = puzzle.moveLeft();
					else if (up != null)
						puzzle = puzzle.moveUp();
					// new puzzle direction and increment created counter
					states.add(puzzle);
					stack.push(puzzle);
					created++;
					// keep track of the maximum size of the stack
					if (max < stack.size() - 1) {
						max = stack.size() - 1;
					}
				}
			}
		}
		// print the end puzzle and it's stats
		System.out.println("End\n" + puzzle);
		System.out.println(puzzle.depth + ", " + created + ", " + (created - stack.size()) + ", " + max);
	}

	// check if the puzzle board is equal to the goal board
	private static boolean solutionFound(Puzzle puzzle) {
		return Arrays.equals(puzzle.board, goal1) || Arrays.equals(puzzle.board, goal2);
	}

	// go through the list of states and see if a puzzle state is already in there
	private static boolean stateUsed(List<Puzzle> list, Puzzle puzzle) {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).equals(puzzle))
				return true;
		}
		return false;
	}

	// Update values for entries already in the priority queue
	private static boolean updatedQueue(PriorityQueue<Puzzle> queue, Puzzle puzzle) {
		// iterate through the puzzle queue
		Iterator<Puzzle> iter = queue.iterator();
		while (iter.hasNext()) {
			Puzzle temp = iter.next();
			// if a matching puzzle is found and the stats are better then update hueristic
			// data
			if (temp.equals(puzzle)) {
				if (puzzle.hMan < temp.hMan) {
					temp.hMan = puzzle.hMan;
				}
				if (puzzle.hDisp < temp.hDisp) {
					temp.hDisp = puzzle.hDisp;
				}
			}
			break;
		}
		return false;
	}

	// Get user input from the console
	private static String[] input() throws IOException {
		String finalArray[] = null;
		String searchMethod = null;
		System.out.println("Enter a puzzle of size 9 or 16 and a search method.\n\n  DFS = Depth-first Search \n  "
				+ "BFS = Breadth-first Search \n  DLS 50 = Depth-limited Search with 50 as the max depth \n  "
				+ "GBFS H1 = Greedy Breadth-first Search H1 or H2 are Heuristic options \n  AStar H2 = A* Search with H1 or H2 "
				+ "\r\rExample \"123456 78\" DFS or \"123456789ABC DEF\" Astar H2");
		System.out.print("Input: ");
		// Read in user input from console
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		// Separate commands
		String input[] = in.readLine().trim().split(" ");
		// Make sure the input had the correct number of inputs
		if (input.length > 2) {
			searchMethod = input[2].toLowerCase();
		}
		// Make sure the sorting method has the correct number of commands
		if (input.length == 4
				&& (searchMethod.equals("dls") || searchMethod.equals("gbfs") || searchMethod.equals("astar"))
				|| input.length == 3 && (searchMethod.equals("bfs") || searchMethod.equals("dfs"))) {
			// Create the array to be returned with the commands separated
			finalArray = new String[input.length - 1];
			finalArray[0] = (input[0] + " " + input[1]).replaceAll("\"", "").toUpperCase();
			finalArray[1] = searchMethod;
			if (searchMethod.equals("dls") || searchMethod.equals("gbfs") || searchMethod.equals("astar")) {
				finalArray[2] = input[3];
			}
		}
		return finalArray;
	}

}
