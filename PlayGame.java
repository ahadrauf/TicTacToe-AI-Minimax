import javax.swing.*;

public class PlayGame
{
    /**
     * Plays the game
     * Loops until the game ends
     */
    public static void playGame()
    {
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
                new GameGUI().play();
            }
        });
        /*
        while (game.getWinner() == -1) //while no player has won
        {
            try
            {
                System.out.flush();
                game.printField();
                System.out.println("Please enter the row and column # of your move");
                System.out.println("Please enter values from 1 to " + game.BOARD_SIZE + ":");
                int r = input.nextInt(), c = input.nextInt(); //r and c are from 1 -> game.BOARD_SIZE, inclusive
                game.move(r - 1, c - 1);
            }
            catch(InputMismatchException e)
            {
                System.out.println("You entered the wrong type of data: " + e.getMessage());
                input.next(); //clears the buffer
            }
            catch(GameException e)
            {
                System.out.println(e.getMessage());
            }
        }
        game.printField();
        System.out.println("Congrats! Player " + game.getWinner() + " won!");
        */
    }
    
    public static void playGameAsPlayer2()
    {
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
                new GameGUI().play(1);
            }
        });
    }
}