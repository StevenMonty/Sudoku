package a3;	

import java.util.List;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Sudoku {
	
	static boolean debuggingMode = false;	
	static int[][] givenBoard;	//Values given by sudoku file that are not to be changed
		
	/**
	 * Checks the board to determine if it is a full or partial solution
	 * @param board to be evaluated
	 * @return true if board is a full solution, false otherwise
	 */
    static boolean isFullSolution(int[][] board) {
 
    	debugPrint("IsFullSolution call");
    	
    	//If a zero is present, the board is not complete
    	for (int i = 0; i < board.length; i++)
    		for (int j = 0; j < board[i].length; j++)
    			if (board[i][j] == 0)
    				return false;
    	
    	//If board is a reject then it is also not a full solution
    	if (reject(board))
    		return false;

    	return true;
    }

	/**
	 * Checks to to if the partial solution passed in should be rejected
	 * for containing duplicate values in a row, column, or 3x3 region
	 * @param board a partial solution
	 * @return true if board is a reject, false if board is a valid partial solution
	 */
    static boolean reject(int[][] board) {
    
    	debugPrint("Reject call");
    	
    	//Check Rows for Dups
    	for (int r = 0; r < board.length; r++)
    	{
    		for (int c = 0; c < board.length; c++)
    		{
                int currentVal = board[r][c];	//Current value that is being checked for presence of dups
                
                if (currentVal != 0) //skip over null value
                { 
                    for (int otherCol = c + 1; otherCol < board[r].length; otherCol++) 
                    {
                        if (currentVal == board[r][otherCol]) //Duplicate value found in same row
                        	return true;
                    }
                }
    		}
    	}
    	
    	//Check Columns for Dups
    	for (int r = 0; r < board.length; r++)
    	{
    		for (int c = 0; c < board.length; c++)
    		{
                int currentVal = board[r][c];	//Current value that is being checked for presence of dups
                
                if (currentVal != 0) //skip over null value
                { 
                    for (int otherRow = r + 1; otherRow < board[r].length; otherRow++) 
                    {
                        if (currentVal == board[otherRow][c]) //Duplicate value found in same column
                        	return true;
                    }
                }
    		}
    	}
    	
    	//Check each of the 9 3x3 Regions for Duplicates
    	//Iterate through each region, new regions start every 3 indecies (0, 3, 6)
        for (int i = 0; i < board.length; i++) 
        {
            for (int j = 0; j < board.length; j++) 
            {

                if (i == 0) 
	                {
                	 	debugPrint("Row: " + i + " Column: " + j);
	                    if (j % 3 == 0)    
	                        if (checkRegion(board, i, j))	//Call a helper method
	                            return true;
	                   
	                } 
                else if (i != 1 && j != 1 && i % 3 == 0 && j % 3 == 0) 
	                {
	                	debugPrint("Row: " + i + " Column:" + j);
	                    if (checkRegion(board, i, j))		//Call a helper method
	                        return true;
	             
	                }
            }
        }
    	
        //if the previous loop completes without returning true, then a duplicate 
        //value is not present in each of the 9 regions of the game board
        return false;
    }
    
    /**
     * Helper method called by reject to check for duplicates inside the same
     * 3x3 region of a game board.
     * 
     * @param board to be checked
     * @param startIndex
     * @param column
     * @return true if the board contains a duplicate in a region and should 
     * be rejected, false if the board is valid and should not be rejected
     */
   static boolean checkRegion(int[][] board, int startIndex, int column) {

	   	int addAtIndex = 0;
	   	int[] temp = new int[9];
    
	    for (int i = startIndex; i < startIndex + 3; i++) 
	        for (int j = column; j < column + 3; j++) 
	            temp[addAtIndex++] = board[i][j];
           
        for (int i = 0; i < 9; i++) 
            for (int j = i + 1; j < 9; j++) 
                if (temp[i] != 0 && temp[i] == temp[j])
                    return true;
            
        return false;
    }

	/**
	 * Returns a copy of the partial solution with the addition of 
	 * (the default option for) one new decision. Returns null if 
	 * there are no more decisions to make.
	 * @param board to be extended
	 * @return partial solution board with one more choice added, null if no more choices
	 */
    static int[][] extend(int[][] board) {
    	
    	debugPrint("Extending call");

    	boolean extended = false;
    	int[][] tmp = new int[9][9];	//use of fixed size since sudoku is always 9x9
		
    	//Iterate over entire game board
    	for(int r = 0; r < board.length; r++)
		  {
			for(int c = 0; c < board[r].length; c++)
				{
				
				//Game board is a nonzero number or the board has alredy been extended and remaining indecies 
				//need to be copied to tmp
					if (board[r][c] != 0 || extended)
						{
							tmp[r][c] = board[r][c];
						}
				//The game board has not yet been extended and the current index is zero, meaning that
				//that index was not given when the board was read in. Extending is required. 
			  		else if (board[r][c] == 0)
				  		{
				  			tmp[r][c] = 1;
				  			extended = true;
				  		}
				}
		  }
		
	  if(extended)		//If board was extended, return it.
	  	return tmp;
	 
	  	return null;	//Board was not extended, return null. 
    }

    /**
     * Changes the most recent decision to the next available option (+1).
     * Returns null if there are no more options for the most recent 
     * decision (alters no other decisions).
     * @param board partial solution board to be incremented
     * @return partial solution with most recently changed choice, null if no more options
     */
    static int[][] next(int[][] board) {	
    	
    	//If the board is alredy is a full solution it cannot be extended, return the complete board back
    	if(isFullSolution(board))
    		return board;
    	
    	boolean modified = false;
    	int[][] tmp = new int[9][9]; 	//use of fixed size since sudoku is always 9x9
    		
    	//Iterate over the board backwards
    	for (int r = board.length-1; r >= 0; r--)
    	{
    		for (int c = board.length-1; c >= 0; c--)
    		{
    			//If the board has not been modified yet, keep searching through the board 
    			if (!modified)
    			{
    				//If the index at [r][c] of the board is the same as the givenBoard, then it cannot be changed,
    				//copy the value to the tmp board to be returned, continue to the next iteration of the loop
    				if(board[r][c] == givenBoard[r][c])
    				{
    					tmp[r][c] = board[r][c];
    					continue;
    				}
    				
    				//If the index of the given board is a nonzero number, add it to the tmp board
    				else if (givenBoard[r][c] != 0)
    					tmp[r][c] = givenBoard[r][c];
    				
    				//Case where the index now needs to be incremented by 1
    				else
    				{
    					//Index is not at the max value of 9, increment index and store in the tmp board
    					if (board[r][c] < 9)
    					{
    						tmp[r][c] = ++board[r][c];
    						modified = true;
    					}
    					else
    						return null;	//No more options for the index
    				}
    			}
    			//No increment needed, copy remaining values into tmp board
    			else
    				tmp[r][c] = board[r][c];
    		}
    	} 	
        return tmp;
    }

    static void testIsFullSolution() {	
    	
    	System.out.println("-------------------------------------------------");
    	System.out.println("Testing isFullSolution method:");
    	System.out.println("-------------------------------------------------");
    	    	
    	final int[][] fullSol  = readBoard("fullSolution.su");	
    	final int[][] notFullSol = readBoard("partialSolution.su");
    	final int[][] testBoard = readBoard("testBoard.su");
    	int[][] hardestBoard = readBoard("hardestBoardEver.su");	//"Worlds Hardest Sudoku Board"
    	givenBoard = readBoard("hardestBoardEver.su");
 
    	printBoard(fullSol);
    	System.out.println("fullSolution.su - should return true: " + isFullSolution(fullSol));
    	System.out.println();
    	printBoard(notFullSol);	//Board contains zero
    	System.out.println("partialSolution.su - should return false: " + isFullSolution(notFullSol)); 
    	
    	System.out.println();
    	printBoard(testBoard);
    	System.out.println("testBoard.su - should return false: " + isFullSolution(testBoard));
    	System.out.println();
    	System.out.println("hardestBoardEver.su:");
    	printBoard(hardestBoard);	
    	System.out.println();
    	System.out.println("Solving hardestBoardEver.su:");
    	hardestBoard = solve(hardestBoard);
    	printBoard(hardestBoard);
    	System.out.println("hardestBoardEver.su - should return true: " + isFullSolution(hardestBoard));
    	
    }

    static void testReject() {	
    	
    	System.out.println("-------------------------------------------------");
    	System.out.println("Testing reject method:");
    	System.out.println("-------------------------------------------------");
    	
    	int[][] badSol  = readBoard("badSolution.su");
    	int[][] fullSol  = readBoard("fullSolution.su");
        int[][] rowTest = readBoard("rejectRow.su");
        int[][] columnTest = readBoard("rejectColumn.su");
        int[][] regionTest = readBoard("rejectRegion.su");
        
    	printBoard(fullSol);
    	System.out.println("fullSolution.su - should return false: " + reject(fullSol));
    	System.out.println();
    	printBoard(badSol);
    	System.out.println("badSolution.su - should return true: " + reject(badSol));
    	System.out.println();
    	printBoard(rowTest);
    	System.out.println("rejectRow.su - should return true: " + reject(rowTest));
    	System.out.println();
    	printBoard(columnTest);
    	System.out.println("rejectColumn.su - should return true: " + reject(columnTest));
    	System.out.println();
    	printBoard(regionTest);
    	System.out.println("rejectRegion.su - should return true: " + reject(regionTest));
    }

    static void testExtend() {	
    	
    	System.out.println("-------------------------------------------------");
    	System.out.println("Testing extend method:");
    	System.out.println("-------------------------------------------------");

    	//givenBoard must be reinitialized each time a new board is read in for error checking
    	int[][] trivial = readBoard("1-trivial.su");
    	givenBoard = readBoard("1-trivial.su");	
        
    	int[][] endingBoard = extend(trivial);
 
    	System.out.println("Board before call to extend:");
    	printBoard(trivial);
        System.out.println();
    	System.out.println("Board after call to extend:");
    	printBoard(endingBoard);
    	System.out.println("The number at board[0][1] was incremented from 0 to 1 since the algorithm solves from left to right.");
        System.out.println();

    	System.out.println("Attempting to extend a complete board:");
    	int[][] fullSol = readBoard("fullSolution.su");
    	givenBoard = readBoard("fullSolution.su");	
    	printBoard(fullSol);
        System.out.println();
        System.out.println("No Assignment expected since a call to the extend method on a full solution will return a null.");
        printBoard(extend(fullSol));


    }

    static void testNext() {
       
    	System.out.println("-------------------------------------------------");
    	System.out.println("Testing next method:");
    	System.out.println("-------------------------------------------------");
        	
    	//givenBoard must be reinitialized each time a new board is read in for proper error checking
	
    	//Incomplete board test	
    	int[][] next = readBoard("2-easy.su");
    	givenBoard = readBoard("2-easy.su");
    	System.out.println("Original board from the 2-easy.su starter file");
    	printBoard(next);
    	System.out.println();
    	System.out.println("After first call to extend:");
    	next = extend(next);
    	printBoard(next);
    	
    	//Element at index [0][0] is incremented by 1 each call. 
        for (int i=1; i < 9; i++) {
            System.out.println();
            System.out.println("Call " + i + ":");
            next = next(next);
            printBoard(next);
        }
        
        //The last call results in an attempt to increment past 9 and returns "No Assignment"
    	System.out.println();
        System.out.println("One more call to next:");
        System.out.println("No Assignment expected since a call to the next method on a full solution returns a null");
        next = next(next);
        printBoard(next);
    	System.out.println();
    	    	
        //Complete board test
        givenBoard = readBoard("fullSolution.su");
        System.out.println("Testing a complete board using fullSolution.su");
        printBoard(givenBoard);
        next = next(givenBoard);
        System.out.println();
        System.out.println("Board after call to next:");
        printBoard(next);
        System.out.println("No changes should be seen, since the board was alredy a complete solution");
        System.out.println();
    }

    static void printBoard(int[][] board) {
        if (board == null) {
            System.out.println("No assignment");
            return;
        }
        for (int i = 0; i < 9; i++) {
            if (i == 3 || i == 6) {
                System.out.println("----+-----+----");
            }
            for (int j = 0; j < 9; j++) {
                if (j == 2 || j == 5) {
                    System.out.print(board[i][j] + " | ");
                } else {
                    System.out.print(board[i][j]);
                }
            }
            System.out.print("\n");
        }
    }

    static int[][] readBoard(String filename) {
        List<String> lines = null;
        try {
            lines = Files.readAllLines(Paths.get(filename), Charset.defaultCharset());
        } catch (IOException e) {
            return null;
        }
        int[][] board = new int[9][9];
        int val = 0;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                try {
                    val = Integer.parseInt(Character.toString(lines.get(i).charAt(j)));
                } catch (Exception e) {
                    val = 0;	//Null value if value not given in starter file
                }
                board[i][j] = val;
            }
        }
        return board;
    }

    static int[][] solve(int[][] board) {
        if (reject(board)) return null;
        if (isFullSolution(board)) return board;
        int[][] attempt = extend(board);
        while (attempt != null) {
            int[][] solution = solve(attempt);
            if (solution != null) return solution;
            attempt = next(attempt);
        }
        return null;
    }
    
    static void debugPrint(String e)
    {
    	if (debuggingMode) System.out.println(e);
    } 

    public static void main(String[] args) {
        if (args[0].equals("-t")) {				//Testing flag
        	        	
        	testIsFullSolution();
        	System.out.println();
            testReject();
        	System.out.println();
            testExtend();
        	System.out.println();
            testNext(); 
            
        } else {
            int[][] board = readBoard(args[0]);
            givenBoard = readBoard(args[0]);
            printBoard(board);
            System.out.println("Solution:");
            printBoard(solve(board));
        }
    }
}