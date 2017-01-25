public class GameException extends Exception
{
    public String message;
    
    public GameException()
    {
        message = "GameException";
    }
    
    public GameException(String message)
    {
        this.message = "Error: " + message;
    }
    
    public String getMessage() {return message;}
}