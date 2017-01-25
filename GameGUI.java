import java.util.Date; //For figuring out time required for different tasks
import java.awt.event.*;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList; //For testing purposes only

public class GameGUI extends JPanel implements MouseListener
{
    //Game constants
    private static JFrame frame = new JFrame("Tic Tac Toe");
    private static Game game;
    private static final int NUM_ROWS = Game.BOARD_SIZE, NUM_COLS = Game.BOARD_SIZE;
    private static int numRounds;
    private static int numLosses;
    
    
    
    //Constants for drawing
    private static final int BOARD_SIZE_PIXELS = 700; //700 = the basis for all the current measurements below
    private static final int BUFFER_ZONE_MEASUREMENTS = 20; //Only used in row/col to x/y pixel conversions
    private static final int EXTRA_ROOM_X = 10; //Accounts for the frame screen's left border
    private static final int EXTRA_ROOM_Y = 30; //Accounts for the top bar (with the maximize, close, etc. buttons)
    private static final int SQUARE_SIZE_X = (BOARD_SIZE_PIXELS-2*BUFFER_ZONE_MEASUREMENTS-EXTRA_ROOM_X)/NUM_COLS; 
                                                                                            //assumes square board
    private static final int SQUARE_SIZE_Y = (BOARD_SIZE_PIXELS-2*BUFFER_ZONE_MEASUREMENTS-EXTRA_ROOM_Y)/NUM_ROWS;
    private static final double LETTER_PROPORTION = calculateLetterProportion(); 
                                                                           //how much of the square the letter takes
    private static final float LETTER_FONT_SIZE_PROPORTION = (float)(SQUARE_SIZE_X * LETTER_PROPORTION)/16;
                                                                 //16 = approximate pixel size of point size 12 font
    private static final int LETTER_SIZE_X = (int)(LETTER_PROPORTION * SQUARE_SIZE_X);
    private static final int LETTER_SIZE_Y = (int)(LETTER_PROPORTION * SQUARE_SIZE_Y);
    private static final int LETTER_BUFFER_ZONE_X = (int)((1 - LETTER_PROPORTION)/2 * SQUARE_SIZE_X);
    private static final int LETTER_BUFFER_ZONE_Y = (int)((1 - LETTER_PROPORTION)/2 * SQUARE_SIZE_Y) + 125;
    private static final Color CLICKED_SQUARE_COLOR = Color.YELLOW;
    private static final int NUMBER_BUFFER_ZONE_X = LETTER_BUFFER_ZONE_X + LETTER_SIZE_X/2 - 15;
    private static final int NUMBER_BUFFER_ZONE_Y = 13;
    
    
    public GameGUI()
    {
        frame.repaint();
    }
    
    public void play()
    {
        initialize(0); //begin the game as player 1
    }
    
    public void play(int offset)
    {
        if (NUM_ROWS < 3 || NUM_ROWS > 5)
        {
            System.out.println("Sorry, this tic-tac-toe board can only accomodate a 3x3, 4x4, or 5x5 board.");
            System.exit(0);
        }
        initialize(offset);
        if (game.getStartingPlayerNumber() == 2) //computer starts
            game.enemyMove();
    }
    
    private void initialize(int offset)
    {
        game = new Game(offset);
        frame.setSize(BOARD_SIZE_PIXELS, BOARD_SIZE_PIXELS);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new GameGUI());
        frame.setVisible(true);
        frame.addMouseListener(this);
        addMouseListener(this);
        numRounds = 1;
        numLosses = 0;
    }
    
    /**
     * Calculates the letter proportion based on NUM_ROWS
     * NUM_ROWS should be in the range [3, 5]
     */
    private static double calculateLetterProportion()
    {
        switch (NUM_ROWS)
        {
            case 3: return .75;
            case 4: return 1.0;
            case 5: return 1.25;
            default: return 1.0;
        }
    }
    
    /**
     * This close function is only called when the code breaks somewhere during execution, or if there is a tie
     * I'm not sure if this function will ever be needed, but it's here just in case
     */
    private void close()
    {
        if (displayYesNoNotification("Would you like to play again?", "Play Again?"))
        {
            game.resetGame();
            frame.repaint();
            numRounds++;
        }
        else
        {
            if (numLosses == 0)
                displayNotification("Woah, you lost none of your matches!\nGood job!", "Statistics");
            else if (numLosses == numRounds)
                displayNotification("I feel it's politically correct to say 'Good job!' and give you a pat on" + 
                                    " the back in these occasions, but...\n" + 
                                    "um...\nwell...\n...\nOkay, I'm gonna tell you this straight, so keep calm " + 
                                    "and don't lose your cool.\n" + 
                                    "Well, you kinda sorta lost all of your matches. Please don't hit me.",
                                    "Statistics");
            else if (numLosses > numRounds / 2)
                displayNotification("You only lost " + numLosses + " of your matches!\n" + 
                                    "Then again, you played " + numRounds + " matches, but who's counting?", 
                                    "Statistics");
            else
                displayNotification("You only lost " + numLosses + "/" + numRounds + " of your matches!", 
                                    "Statistics");
            System.exit(0);
        }
    }
    
    /**
     * This close function is called when the game ends with a winner
     */
    private void closeWinner()
    {
        if (game.getWinner() != 1) //if the non-computer player won
        {
            numLosses++;
        }
        if (game.getWinner() == game.getStartingPlayerNumber() && game.getStartingPlayerNumber() == 1)
            displayNotification("Congrats! You won!", "Winner!!!");
        else
            displayNotification("You lost. Nuff said.", "...");
        close();
    }
    
    /**
     * This close function is called when the game ends in a tie
     */
    private void closeTie()
    {
        displayNotification("There was a tie.", "Tie!!!");
        close();
    }
    
    
    /**
     * @returns r, the x-coordinate of the column's left boundary
     *          -1, if the x-coordinate is not within bounds
     */
    private int coltox(int col)
    {
        return (col < 0 || col > NUM_COLS) ? -1 : BUFFER_ZONE_MEASUREMENTS + col * SQUARE_SIZE_X;
    }
    
    /**
     * @returns r, the y-coordinate of the row's top boundary
     *         -1, if the y-coordinate is not within bounds
     */
    private int rowtoy(int row)
    {
        return (row < 0 || row > NUM_ROWS) ? -1 : BUFFER_ZONE_MEASUREMENTS + row * SQUARE_SIZE_Y;
    }
    
    /**
     * @returns c, the number of the col the x-coordinate is in
     *          -1, if the x-coordinate is not within bounds
     */
    private int xtocol(double x)
    {
        return (x < coltox(0) || x > coltox(NUM_COLS)) ? -1 : (int)((x-BUFFER_ZONE_MEASUREMENTS)/SQUARE_SIZE_X);
    }
    
    /**
     * @returns r, the number of the col the y-coordinate is in
     *          -1, if the y-coordinate is not within bounds
     */
    private int ytorow(double y)
    {
        return (y < rowtoy(0) || y > rowtoy(NUM_ROWS)) ? -1 : (int)((y-BUFFER_ZONE_MEASUREMENTS)/SQUARE_SIZE_Y);
    }
    
    /**
     * @param message - The main string displayed in the box
     * @param title - The name of the box (in the header)
     */
    private void displayNotification(String message, String title)
    {
        //Object[] options = {"OK"};
        //JOptionPane.showOptionDialog(null, message, title,
        //    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, 
        //    null, options, options[0]);
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * @param message - the main string displayed in the box
     * @param title - the name of the box (in the header)
     */
    private void displayErrorNotification(String message, String title)
    {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
    }
    
    
    /**
     * @param message - the main string displayed in the box
     * @param title - the name of the box (in the header)
     * Displays a box with only Yes and No as options
     * @return true  if yes is picked
     *         false if no is picked
     */
    private boolean displayYesNoNotification(String message, String title)
    {
        return JOptionPane.showConfirmDialog(null, message, title, JOptionPane.YES_NO_OPTION) == 0;
    }
    
    
    
    
    //Overridden functions (for drawing and mouse functions)
    @Override
    public void paintComponent(Graphics g)
    {
        Graphics2D pen = (Graphics2D)g;
        
        pen.setColor(Color.BLACK);
        for (int i = 0; i <= NUM_ROWS; i++)
        {
            pen.drawLine(coltox(i), rowtoy(0), coltox(i), rowtoy(NUM_COLS));
        }
        for (int i = 0; i <= NUM_COLS; i++)
        {
            pen.drawLine(coltox(0), rowtoy(i), coltox(NUM_ROWS), rowtoy(i));
        }
        
        for (int i = 0; i < NUM_ROWS; i++)
        {
            for (int j = 0; j < NUM_COLS; j++)
            {
                pen.setColor(Color.BLACK);
                pen.drawString("(" + (i+1) + ", " + (j+1) + ")", coltox(j) + NUMBER_BUFFER_ZONE_X,
                    rowtoy(i) + NUMBER_BUFFER_ZONE_Y);
            }
        }
        
        Font letterFont = pen.getFont().deriveFont(pen.getFont().getSize() * LETTER_FONT_SIZE_PROPORTION);
        pen.setFont(letterFont);
        //System.out.println(LETTER_BUFFER_ZONE_X + " " + LETTER_BUFFER_ZONE_Y);
        
        for (int r = 0; r < NUM_ROWS; r++)
        {
            for (int c = 0; c < NUM_COLS; c++)
            {
                char piece = game.getPiece(r, c);
                //pen.drawString("X", coltox(c) + LETTER_BUFFER_ZONE_X + 35, rowtoy(r) + LETTER_BUFFER_ZONE_Y);
                if (piece != '\0')
                {
                    pen.drawString(piece + "", coltox(c) + LETTER_BUFFER_ZONE_X + 35, 
                                   rowtoy(r) + LETTER_BUFFER_ZONE_Y);
                }
            }
        }
    }
    
    public void mouseClicked(MouseEvent me)
    {
        if (me != null && xtocol(me.getX()-EXTRA_ROOM_X) != -1 && ytorow(me.getY()-EXTRA_ROOM_Y) != -1)
        {
            try
            {
                game.playerMove(ytorow(me.getY()-EXTRA_ROOM_Y), xtocol(me.getX()-EXTRA_ROOM_X));
                frame.repaint();
                if (game.getWinner() == -1)
                    game.enemyMove();
                frame.repaint();
                //Old code (before having the computer's first move when the computer is player 1 got executed
                //in this class' play(int offset) method and the Game class' resetGame() method
                /*
                if (game.getOffset() == 0) //The user starts first
                {
                    game.playerMove(ytorow(me.getY()-EXTRA_ROOM_Y), xtocol(me.getX()-EXTRA_ROOM_X));
                    frame.repaint();
                    if (game.getWinner() == -1)
                        game.enemyMove();
                    frame.repaint();
                }
                else //The computer starts first
                {
                    game.enemyMove();
                    frame.repaint();
                    if (game.getWinner() == -1)
                        game.playerMove(ytorow(me.getY()-EXTRA_ROOM_Y), xtocol(me.getX()-EXTRA_ROOM_X));
                    frame.repaint();
                }
                */
                
                //Message if someone wins/if there was a tie
                if (game.getWinner() == 0)
                {
                    closeTie();
                }
                else if (game.getWinner() != -1)
                {
                    closeWinner();
                }
            }
            catch(GameException e)
            {
                displayNotification(e.getMessage(), "Error");
            }
        }
    }
    
    public void mouseEntered(MouseEvent me)
    {
        
    }
    
    public void mouseExited(MouseEvent me)
    {
        
    }
    
    public void mousePressed(MouseEvent me)
    {
        
    }
    
    public void mouseReleased(MouseEvent me)
    {
        
    }
}