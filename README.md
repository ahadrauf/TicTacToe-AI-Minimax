# TicTacToe-AI-Minimax
Implements the minimax algorithm to execute an optimal Tic-Tac-Toe AI algorithm.
The minimax algorithm evaluates each possible move the player and computer can make and assigns a score to each resulting board layout. It then analyzes those scores to determine which move the computer should make when facing a player playing optimally (with the least chance to lose).

Further improvements:
<ul>
<li>Store previous iterations in a HashMap to improve lookup time for subsequent moves</li>
<li>Analyze all possible moves to determine optimal move to cut player off (minimizes the chance of losses or draws, even if the computer hadn't been playing optimally after that move)</li>
<li>When placed into a position of certain defeat, chooses the best move (one that prolongs the game)</li>
</ul>
