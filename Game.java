/**
 * Stores information about the current game instance
 * 
 * Strategy: Uses the minimax algorithm to run through every possible move and response the computer can make
 *           to find the optimal one
 * Further improvements: - When played in a possible of certain defeat, the AI will assume optimal behavior
 *                         (aka will try to make moves that prolong the game)
 *                       - Prefers moves that give the computer a greater chance to win than the player, in case
 *                         the player does not play optimally
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Date;

public class Game
{
    //Game constants
    public static final int BOARD_SIZE = 3;
    public static final int NUM_SQUARES = BOARD_SIZE * BOARD_SIZE; //square board
    public static final char SYMBOLS[] = {'X', 'O'};
    private static final char EMPTY_SQUARE_FILLING = '_';
    private static final int MAX_SCORE = 1000;
    private static final int TIE_SCORE = 0;
    private static final int NO_WINNER_YET_SCORE = MAX_SCORE * 2;
    private static final int NO_WINNER_OR_TIE_YET_DEFAULT_VALUE = -1; //for the winner variable below
    private static final int MAX_DEPTH = 9; //for the minimax algorithm, depth <= MAX_DEPTH
                                            //Note: MAX_DEPTH * DEPTH_MULTIPLIER should be less than MAX_SCORE
    private static final int MAX_NUM_POSSIBLE_MOVES = 9; //Mainly for reference, this will affect the strategies
                                                         //for how to score moves most effectively
    private static final int DEPTH_MULTIPLIER = MAX_SCORE / (MAX_NUM_POSSIBLE_MOVES + 1); //Multiplied by depth
                                                                                          //in score calculations
                                                                                          //for minimax (priorizes
                                                                                          //depth over other 
                                                                                          //factors)
    
    //Game variables (changed as the game progresses)
    private char[][] field;
    private int turn;
    private int winner; //NO_WINNER_OR_TIE_YET_DEFAULT_VALUE if the game hasn't ended
    private final int offset; //When players want to start with 'O', offset = 1
    private final int startingPlayerNumber; //For player vs. computer games
                                    //1 if the player goes first, 2 if the computer goes first
    private HashMap<String, String> bestMoves; //stores the optimal moves previously found
                                               //fun facts: cut # of iterations from 39,172 -> 2821
                                               //           and time from 88 ms -> 25 ms for the first move
                                               //           (and removes the need for calculations for future moves)
    
    
    //Constructors
    //This constructor won't get called under normal circumstances
    public Game()
    {
        field = new char[BOARD_SIZE][BOARD_SIZE]; //default value for each element = '\0' or '\u0000'
        bestMoves = new HashMap<String, String>();
        turn = 0;
        winner = NO_WINNER_OR_TIE_YET_DEFAULT_VALUE;
        offset = 0;
        startingPlayerNumber = getPlayer();
        //fillField("_X___XOO_");
    }
    
    //When players want to start with 'O', offset should be an odd number
    public Game(int offset)
    {
        field = new char[BOARD_SIZE][BOARD_SIZE]; //default value = '\0' or '\u0000'
        bestMoves = new HashMap<String, String>();
        turn = 0;
        winner = NO_WINNER_OR_TIE_YET_DEFAULT_VALUE;
        this.offset = offset % 2;
        startingPlayerNumber = getPlayer();
        //fillField("_X___XOO_");
    }
    
    /**
     * Executes the move
     * @param r, c: the row and column of the square the current player has selected
     * 
     * @return true if the move if successful
     *         throws a GameException if the move is unsucessful
     */
    public boolean playerMove(int r, int c) throws GameException
    {
        validateMove(r, c); //throws GameException if the move is invalid
        placePiece(r, c);
        int score = checkWinner();
        if (score == TIE_SCORE)
            winner = 0;
        else if (score != NO_WINNER_YET_SCORE)
            winner = getPlayer();
        incrementTurn();
        return true;
    }
    
    /**
     * Performs the enemy's move, using the minimax algorithm
     * @return true if the move if successful
     *         throws a GameException if an error occurs (no errors exist currently)
     */
    public boolean enemyMove()
    {
        char[][] nextMove = stringToField(findBestMove());
        fillField(nextMove);
        int score = checkWinner();
        if (score == TIE_SCORE)
            winner = 0;
        else if (score != NO_WINNER_YET_SCORE)
            winner = getPlayer();
        incrementTurn();
        return true;
    }
    
    /**
     * Validates that the selected square can have a piece put in it
     * For tic-tac-toe, this checks that the square is empty and that r and c are in the field bounds
     * @return true if the move is valid
     *         throws GameException if the move is not valid
     */
    private boolean validateMove(int r, int c) throws GameException
    {
        if (r < 0 || r >= BOARD_SIZE ||
            c < 0 || c >= BOARD_SIZE)
        {
            throw new GameException("Selected square out of bounds. " + 
                                    "\n\tSelected square (" + (r+1) + ", " + (c+1) + "), but expected squares " + 
                                    "\n\twhere row/col are within 0->" + BOARD_SIZE);
        }
        if (field[r][c] != '\0')
        {
            throw new GameException("Selected square currently occupied. " + 
                                    "\n\tSelected square (" + (r+1) + ", " + (c+1) + "), but square currently " + 
                                    "\n\tcontains an " + field[r][c]);
        }
        return true;
    }
    
    /**
     * Places the current player's piece on the field in the selected spot
     * Assumes the move has been previously validated
     * @param r, c are the row and column of the new piece
     * @return 1 if the move is sucessful
     *         throws a GameException if an error occurs while adding the piece
     *              (currently, there are no such situations though)
     */
    private boolean placePiece(int r, int c)
    {
        field[r][c] = getCurrentTurnPiece();
        return true;
    }
    
    
    /**
     * Checked if the current player has won the game, and updates the winner to the current player's number
     * Only meant to be called after executing a move and before moving to the next turn
     * @return MAX_SCORE if player 1 won
     *         -MAX_SCORE if player 2 won
     *         TIE_SCORE if there's a tie
     *         NO_WINNER_YET_SCORE if no one has won yet (the game is still in progress)
     */
    private int checkWinner() {return checkWinner(field);}
    private int checkWinner(char[][] field)
    {
        int tempField[][] = new int[BOARD_SIZE][BOARD_SIZE];
        int numSquaresFilled = 0;
        for (int i = 0; i < BOARD_SIZE; i++)
        {
            for (int j = 0; j < BOARD_SIZE; j++)
            {
                switch(field[i][j])
                {
                    case 'X': tempField[i][j] = 1;
                              numSquaresFilled++;
                              break;
                    case 'O': tempField[i][j] = -1;
                              numSquaresFilled++;
                              break;
                    default: tempField[i][j] = 0;
                }
            }
        }
        
        int sumDiagDownRight = 0, sumDiagDownLeft = 0;
        for (int i = 0; i < BOARD_SIZE; i++)
        {
            int sumX = 0, sumY = 0;
            for (int j = 0; j < BOARD_SIZE; j++)
            {
                sumX += tempField[i][j];
                sumY += tempField[j][i];
            }
            if (Math.abs(sumX) == BOARD_SIZE || Math.abs(sumY) == BOARD_SIZE)
            {
                if (sumX == BOARD_SIZE || sumY == BOARD_SIZE) //if player 1 won
                    return MAX_SCORE;
                else
                    return -MAX_SCORE;
            }
            sumDiagDownRight += tempField[i][i];
            sumDiagDownLeft += tempField[i][BOARD_SIZE-i-1];
        }
        if (Math.abs(sumDiagDownRight) == BOARD_SIZE || Math.abs(sumDiagDownLeft) == BOARD_SIZE)
        {
            if (sumDiagDownRight == BOARD_SIZE || sumDiagDownLeft == BOARD_SIZE) //if player 1 won
                return MAX_SCORE;
            else
                return -MAX_SCORE;
        }
        else if (numSquaresFilled == BOARD_SIZE * BOARD_SIZE) //tie
        {
            return TIE_SCORE;
        }
        else
            return NO_WINNER_YET_SCORE;
    }
    
    /**
     * Scores the board for the minimax algorithm
     * @return MAX_SCORE if player 1 won
     *         -MAX_SCORE if player 2 won
     *         TIE_SCORE if there's a tie
     *         NO_WINNER_YET_SCORE if no one has won yet (the game is still in progress)
     */
    private int scoreBoard() {return scoreBoard(field);}
    private int scoreBoard(char[][] field)
    {
        int tempField[][] = new int[BOARD_SIZE][BOARD_SIZE];
        int numSquaresFilled = 0;
        for (int i = 0; i < BOARD_SIZE; i++)
        {
            for (int j = 0; j < BOARD_SIZE; j++)
            {
                switch(field[i][j])
                {
                    case 'X': tempField[i][j] = 1;
                              numSquaresFilled++;
                              break;
                    case 'O': tempField[i][j] = -1;
                              numSquaresFilled++;
                              break;
                    default: tempField[i][j] = 0;
                }
            }
        }
        
        int sumDiagDownRight = 0, sumDiagDownLeft = 0;
        for (int i = 0; i < BOARD_SIZE; i++)
        {
            int sumX = 0, sumY = 0;
            for (int j = 0; j < BOARD_SIZE; j++)
            {
                sumX += tempField[i][j];
                sumY += tempField[j][i];
            }
            if (Math.abs(sumX) == BOARD_SIZE || Math.abs(sumY) == BOARD_SIZE)
            {
                if (sumX == BOARD_SIZE || sumY == BOARD_SIZE) //if player 1 won
                    return MAX_SCORE;
                else
                    return -MAX_SCORE;
            }
            sumDiagDownRight += tempField[i][i];
            sumDiagDownLeft += tempField[i][BOARD_SIZE-i-1];
        }
        if (Math.abs(sumDiagDownRight) == BOARD_SIZE || Math.abs(sumDiagDownLeft) == BOARD_SIZE)
        {
            if (sumDiagDownRight == BOARD_SIZE || sumDiagDownLeft == BOARD_SIZE) //if player 1 won
                return MAX_SCORE;
            else
                return -MAX_SCORE;
        }
        else if (numSquaresFilled == BOARD_SIZE * BOARD_SIZE) //tie
        {
            return TIE_SCORE;
        }
        else
            return NO_WINNER_YET_SCORE;
    }
    
    //The minimiax algorithm functions
    /**
     * Returns the optimal move
     * Implements the minimax algorithm
     */
    private String findBestMove()
    {
        return findBestMove(fieldToString(field), turn, 1);
    }
    
    private String findBestMove(String curField, int curTurn, int depth)
    {
        //If you've previously run a simulation and found an optimal move for the current board, then use it
        if (bestMoves.containsKey(curField))
            return unpackField(bestMoves.get(curField));

        ArrayList<String> possibleMoves = findPossibleMoves(curField, curTurn);
        ArrayList<Integer> possibleScores = assignScoresToPossibleMoves(possibleMoves, depth);
        for (int i = 0; i < possibleMoves.size(); i++)
        {
            if (possibleScores.get(i) == NO_WINNER_YET_SCORE) //no winner has been reached yet -> apply minimax
            {
                //System.out.println("Entering findBestMoveScore from findBestMove: " + possibleMoves.get(i));
                possibleScores.set(i, findBestMoveScore(possibleMoves.get(i), curTurn + 1, depth + 1));
            }
        }
        /*
        System.out.println("\n\n\nInside findBestMove\n" + curField + "\t" + curTurn + "\t" + depth);
        for (int i = 0; i < possibleMoves.size(); i++)
        {
            System.out.println(possibleMoves.get(i) + " " + possibleScores.get(i));
        }
        System.out.println("\n\n\n");
        //*/
       
        if (getPlayer(curTurn) == startingPlayerNumber)
        {
            return possibleMoves.get(argmax(possibleScores));
        }
        else
        {
            return possibleMoves.get(argmin(possibleScores));
        }
    }
    
    private int findBestMoveScore(String curField, int curTurn, int depth)
    {
        if (bestMoves.containsKey(curField))
            return unpackScore(bestMoves.get(curField));
        ArrayList<String> possibleMoves = findPossibleMoves(curField, curTurn);
        ArrayList<Integer> possibleScores = assignScoresToPossibleMoves(possibleMoves, depth);
        int winsAndTies = 0, lossesAndTies = 0;
        for (int i = 0; i < possibleMoves.size(); i++)
        {
            if (possibleScores.get(i) == TIE_SCORE)
            {
                winsAndTies++;
                lossesAndTies++;
            }
            if (possibleScores.get(i) == NO_WINNER_YET_SCORE && depth < MAX_DEPTH)
            {
                //no winner has been reached yet -> apply minimax
                //Note: only resets score if depth != MAX_DEPTH yet
                //System.out.println("Entering findBestMoveScore from findBestMoveScore: " + possibleMoves.get(i));
                possibleScores.set(i, findBestMoveScore(possibleMoves.get(i), curTurn + 1, depth + 1));
            }
            else if (possibleScores.get(i) > 0 && possibleScores.get(i) != NO_WINNER_YET_SCORE
                     && possibleScores.get(i) != TIE_SCORE)
                winsAndTies++;
            else if (possibleScores.get(i) < 0 && possibleScores.get(i) != NO_WINNER_YET_SCORE
                     && possibleScores.get(i) != TIE_SCORE)
                lossesAndTies++;
        }
        /*
        for (int i = 0; i < possibleScores.size(); i++)
        {
            System.out.print(possibleScores.get(i) + " ");
            //if (possibleScores.get(i) == MAX_SCORE)
            //    System.out.println("HI");
        }
        System.out.println();
        */
        //System.out.println(winsAndTies + " " + lossesAndTies);
        if (getPlayer(curTurn) == startingPlayerNumber) //your turn
        {
            return possibleScores.get(argmax(possibleScores)) + winsAndTies;
        }   
        else //enemy's turn
        {
            int bestMovePosition = argmin(possibleScores);
            int numberOfVictories = lossesAndTies;
            bestMoves.put(curField, packageFieldAndScore(possibleMoves.get(bestMovePosition), 
                                                         possibleScores.get(bestMovePosition) - numberOfVictories));
            return possibleScores.get(bestMovePosition) - numberOfVictories;
        }
    }
    
    /**
     * Returns a list of all possible moves
     */
    public ArrayList<String> findPossibleMoves() {return findPossibleMoves(curFieldToString(), turn);}
    
    private ArrayList<String> findPossibleMoves(String curField, int curTurn)
    {
        char piece = getTurnPiece(curTurn);
        ArrayList<String> possibleMoves = new ArrayList<String>();
        for (int i = 0; i < curField.length(); i++)
        {
            if (curField.charAt(i) == EMPTY_SQUARE_FILLING)
            {
                possibleMoves.add(curField.substring(0, i) + piece + curField.substring(i+1));
            }
        }
        return possibleMoves;
    }
    
    /**
     * Scores all the boards in the given ArrayList using the scoreBoard function
     */
    public ArrayList<Integer> assignScoresToPossibleMoves(ArrayList<String> possibleMoves, int depth)
    {
        ArrayList<Integer> possibleMoveScores = new ArrayList<Integer>(possibleMoves.size());
        for (int i = 0; i < possibleMoves.size(); i++)
        {
            int tempScore = scoreBoard(stringToField(possibleMoves.get(i)));
            if (Math.abs(tempScore) == MAX_SCORE)
            {
                tempScore = (tempScore) / MAX_SCORE * (Math.abs(tempScore) - DEPTH_MULTIPLIER * depth);
                //depth 2 => 10 -> 8, -10 -> -8
                //depth 3 => 10 -> 7, -10 -> -7
                //even when placed in a position of certain defeat, assumes the current turn's player will
                //prefer the longer game (higher depth)
                //ex. when given the scores 80, 60, 80, and 80, the computer will prefer the 60 move
                //(prolonging the game will typically be seen as a "smarter" move)
                //ex2. when given the scores -80, -60, -80, -80, the assumed move for the player will be the -60 move
                //(the computer assumes that the player plays optimally [aka prolonging the game when possible])
            }
            possibleMoveScores.add(tempScore);
        }
        return possibleMoveScores;
    }
    
    /**
     * This returns the index of the largest value in a non-empty ArrayList
     * Excludes the noWinnerYetScore unless it's the only item in the list
     */
    private int argmax(ArrayList<Integer> lst)
    {
        int maxIndex = 0;
        for (int i = 1; i < lst.size(); i++)
        {
            if (lst.get(i) > lst.get(maxIndex))
                maxIndex = i;
        }
        return maxIndex;
    }
    
    /**
     * This returns the index of the smallest value in a non-empty ArrayList
     */
    private int argmin(ArrayList<Integer> lst)
    {
        int minIndex = 0;
        for (int i = 1; i < lst.size(); i++)
        {
            if (lst.get(i) < lst.get(minIndex))
                minIndex = i;
        }
        return minIndex;
    }
    
    /**
     * Counts the number of times a value appears in an ArrayList
     */
    private int count(ArrayList<Integer> lst, int value)
    {
        int numAppearances = 0;
        for (int i = 0; i < lst.size(); i++)
        {
            if (value == lst.get(i))
                numAppearances++;
        }
        return numAppearances;
    }
    
    /**
     * Counts the number of times a value above (>, not >=) the given parameter appears in an ArrayList
     */
    private int countAbove(ArrayList<Integer> lst, int value)
    {
        int numAppearances = 0;
        for (int i = 0; i < lst.size(); i++)
        {
            if (value < lst.get(i))
                numAppearances++;
        }
        return numAppearances;
    }
    
    /**
     * Counts the number of times a value satisfying the following two conditions appears in an ArrayList:
     *  1) The value >= the given parameter
     *  2) The value is a multiple of DEPTH_MULTIPLIER
     *     (in other words, it was a conclusive game in the current turn, since if the score had been
     *     NO_WINNER_YET_SCORE the minimax algorithm would have already changed it to something other than
     *     a multiple of DEPTH_MULTIPLIER)
     */
    private int countAboveAndNewMove(ArrayList<Integer> lst, int value)
    {
        int numAppearances = 0;
        for (int i = 0; i < lst.size(); i++)
        {
            if (value <= lst.get(i) && value % DEPTH_MULTIPLIER == 0)
                numAppearances++;
        }
        return numAppearances;
    }
    
    
    /**
     * Counts the number of times a value below (<, not <=) the given parameter appears in an ArrayList
     */
    private int countBelow(ArrayList<Integer> lst, int value)
    {
        int numAppearances = 0;
        for (int i = 0; i < lst.size(); i++)
        {
            if (value > lst.get(i))
                numAppearances++;
        }
        return numAppearances;
    }
    
    /**
     * Counts the number of times a value satisfying the following two conditions appears in an ArrayList:
     *  1) The value <= the given parameter
     *  2) The value is a multiple of DEPTH_MULTIPLIER
     *     (in other words, it was a conclusive game in the current turn, since if the score had been
     *     NO_WINNER_YET_SCORE the minimax algorithm would have already changed it to something other than
     *     a multiple of DEPTH_MULTIPLIER)
     */
    private int countBelowAndNewMove(ArrayList<Integer> lst, int value)
    {
        int numAppearances = 0;
        for (int i = 0; i < lst.size(); i++)
        {
            if (value >= lst.get(i) && value % DEPTH_MULTIPLIER == 0)
                numAppearances++;
        }
        return numAppearances;
    }
    
    
    /**
     * Packages a field and its respective score into a single string
     * The field length will take up the first BOARD_SIZE * BOARD_SIZE characters
     * The score will start at index BOARD_SIZE * BOARD_SIZE and take up the rest of the string
     * Used for storing optimal moves in the bestMoves HashMap
     * 
     * Right now, this assumes that every field will be length BOARD_SIZE * BOARD_SIZE
     * For example, if given the field "XO__" and 10, it'll return "XO__10"
     */
    private String packageFieldAndScore(String field, int score)
    {
        return field + score;
    }
    
    /**
     * Extracts the field as a String from a packaged field & score string
     */
    private String unpackField(String fieldAndScore)
    {
        return fieldAndScore.substring(0, NUM_SQUARES);
    }
    
    /**
     * Extracts the score as an int from a packaged field & score string
     */
    private int unpackScore(String fieldAndScore)
    {
        return Integer.parseInt(fieldAndScore.substring(NUM_SQUARES));
    }
    
    
    /**
     * Converts the current game's field to a string - for testing purposes
     */
    public String curFieldToString()
    {
        return fieldToString(field);
    }
    
    /**
     * Converts a field to a string - for the minimax algorithm
     */
    private String fieldToString(char[][] field)
    {
        String str = "";
        for (int i = 0; i < field.length; i++)
        {
            for (int j = 0; j < field.length; j++)
            {
                str += (field[i][j] == '\0') ? EMPTY_SQUARE_FILLING : field[i][j];
            }
        }
        return str;
    }
    
    private char[][] stringToField(String str)
    {
        char[][] newField = new char[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++)
        {
            for (int j = 0; j < BOARD_SIZE; j++)
            {
                newField[i][j] = (str.charAt(i * BOARD_SIZE + j) == EMPTY_SQUARE_FILLING) ? '\0' : 
                                 str.charAt(i * BOARD_SIZE + j);
            }
        }
        return newField;
    }
    
    
    
    /**
     * Prints the field to System.out
     */
    public void printField()
    {
        for (int i = 0; i < BOARD_SIZE; i++)
        {
            for (int j = 0; j < BOARD_SIZE; j++)
            {
                System.out.print((field[i][j] == '\0') ? EMPTY_SQUARE_FILLING : field[i][j]);
            }
            System.out.println();
        }
    }
    
    /**
     * Just for testing purposes - can be deleted with no issues to the code
     * Randomly fills field with values, with no regard for the rules of the game
     */
    private void fillField()
    {
        for (int i = 0; i < BOARD_SIZE; i++)
        {
            for (int j = 0; j < BOARD_SIZE; j++)
            {
               double temp = Math.random();
               if (temp > .67)
                   field[i][j] = 'X';
               else if (temp > .33)
                   field[i][j] = 'O';
               else
                   field[i][j] = '\0';
            }
        }
        printField();
    }
    
    public void fillField(String newField)
    {
        fillField(stringToField(newField));
    }
    
    public void fillField(char[][] newField)
    {
        for (int i = 0; i < field.length; i++)
        {
            for (int j = 0; j < field[0].length; j++)
            {
                field[i][j] = newField[i][j];
            }
        }
    }
    
    public void resetField()
    {
        String emptyField = "";
        for (int i = 0; i < NUM_SQUARES; i++)
        {
            emptyField += EMPTY_SQUARE_FILLING;
        }
        fillField(emptyField);
    }
    
    public void resetGame()
    {
        resetField();
        winner = NO_WINNER_OR_TIE_YET_DEFAULT_VALUE;
        turn = 0;
        if (startingPlayerNumber == 2) //computer starts
            enemyMove();
    }
    
    /**
     * Getters/setters
     */
    public char[][] getField() {return field;}
    public char getPiece(int n) {return field[n/BOARD_SIZE][n%BOARD_SIZE];}
    public char getPiece(int r, int c) {return field[r][c];}
    public int getTurn() {return turn;}
    public int getOffset() {return offset;}
    public int getWinner() {return winner;}
    public char getCurrentTurnPiece() {return getTurnPiece(turn);}
    public char getTurnPiece(int turn) {return SYMBOLS[(turn)%2];} //Returns the specified player's 
                                                                             //piece ('X' or 'O')
    public int getPlayer() {return getPlayer(turn);} //Returns the current player's index, 1 = player, 2 = computer
    public int getPlayer(int turn) {return (turn + offset)%2 + 1;} //Returns the given player's index
                                                                   //1 = player, 2 = computer
    public int getStartingPlayerNumber() {return startingPlayerNumber;}
    private void incrementTurn() {turn++;}
    
    
}