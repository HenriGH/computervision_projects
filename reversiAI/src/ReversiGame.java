import java.lang.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.time.Duration;
import java.time.Instant;

/**
 * This class contains the Reversi AI.
 */
public class ReversiGame {
    // Class managing the heuristic
    public Heuristic heuristic;
    // Our player number
    public int playerNumber;
    // The game phase, 1 for build phase, 2 for elimination phase
    public int gamePhase;
    // Class providing the map functions
    public MapModel modell;
    // Did the game end?
    public boolean gameEnd;
    // Number of the current move
    public int moveNumber;
    // Counter variables for Pruning/MiniMax
    public int MiniMaxStates;
    public int pruningStates;
    public int moveSortStates;
    public double pruningTotal;
    public double moveSortTotal;
    public double lastMax = 10;

    //Timer for iterative deepening search
    public long deadline;
    public long lastmovetimebuffer;
    public long possiblemovesamount;
    public long[] pastmovetimes;

    public boolean useOverwrites;
    /**
     * Constructs a new ReversiGame instance playing in the given map with the given
     * player number.
     * 
     * @param map
     *            the maps String representation
     * @param pN
     *            the players number
     */
    public ReversiGame(String map, int pN) {
        // Initializing with standard values
        playerNumber = pN;
        modell = new MapModel(map);
        gamePhase = 1;
        moveNumber = 0;
        heuristic = new Heuristic(0, modell, playerNumber);
        pruningTotal = 0;
        moveSortTotal = 0;
        lastmovetimebuffer = 0;
        possiblemovesamount = 0;
        pastmovetimes = new long[20];
        for(int i = 0; i < pastmovetimes.length; i++){
            pastmovetimes[i] = 0;
        }
        useOverwrites = false;
    }

    /**
     * Gets called by the main class when the games has ended, can be used to
     * display final statistics
     */
    public void gameEnd() {
        System.out.println(moveSortTotal + " " + pruningTotal);
    }

    /**
     * Gets called by the main class when the server requests a move, responds with
     * a move.
     * 
     * @param MessageBytes
     *            the bytes of the message send from the server
     * @param compare
     *            true if Pruning should be compared to MiniMax
     * @return an int[] decoding of the found move, index 0 for the y coordinate,
     *         index one for the x coordinate and index two for the special number
     */
    public int[] makeMove(byte[] MessageBytes, boolean compare) {
        int[] move = new int[2];
        if (compare) {
            // MiniMax and Pruning should be compared
            move = compare(MessageBytes);
        } else {
            // Just find the best move possible
            move = makeMoveHelper(MessageBytes);
        }
        System.gc();
        return move;
    }

    /**
     * Compares the returned moves of MiniMax and Pruning, prints a notice if there
     * is a difference
     * 
     * @param MessageBytes
     *            the bytes of the message send from the server
     * @return an int[] decoding of the found move, index 0 for the y coordinate,
     *         index one for the x coordinate and index two for the special number
     */
    public int[] compare(byte[] MessageBytes) {
        // setting of depthlimit, on a timelimit the depth is set to 2
        deadline = Long.MAX_VALUE;
        int depthLimit = MessageBytes[4];
        if (depthLimit == 0) {
            depthLimit = 3;
        }
        //testing
        moveNumber++;
        gameState curr = modell.getGameState();
        int[] move = null;
        if(moveNumber%10 == 0){
            gameEnd();
        }
        // retrieving results for Pruning and MiniMax
        //long timer = System.nanoTime();
        /*int moveInt = (int) pruningHelper(depthLimit, curr, playerNumber, gamePhase);
        System.out.println("Pruning States: " + pruningStates);
        pruningTotal += pruningStates;
        pruningStates = 0;*/
        /*int moveInt = (int) moveSortHelper(depthLimit, curr, playerNumber, gamePhase, Integer.MIN_VALUE, Integer.MAX_VALUE)[1];
        moveInt = (int) moveSortHelperZwei(depthLimit, curr, playerNumber, gamePhase, Integer.MIN_VALUE, Integer.MAX_VALUE)[1];
        moveInt = (int) pruningHelper(depthLimit, curr, playerNumber, gamePhase, Integer.MIN_VALUE, Integer.MAX_VALUE)[1];*/
        //System.out.println("Total time Pruning: "+ (System.nanoTime()-timer));
        //timer = System.nanoTime();
        /*moveSortHelper(depthLimit, curr, playerNumber, gamePhase);
        System.out.println("MoveSort States: " + moveSortStates);
        moveSortTotal += moveSortStates;
        moveSortStates = 0;
        System.out.println("Total moveSort: "+ moveSortTotal);
        System.out.println("Total pruning: "+pruningTotal);
        System.out.println("MoveSort result: "+ moveInt);*/
        //System.out.println((int)moveSort(2, curr, playerNumber, gamePhase,1000,-1000)[1]);
        //System.out.println("Zug moveSort: "+ moveInt);
        //System.out.println("Eval moveSort normal: " + (int)moveSort(depthLimit, curr, playerNumber, gamePhase,1000,0)[0]);
        //System.out.println("Total time moveSort: "+ (System.nanoTime()-timer));
        /*if (moveInt != MiniMaxZwei(depthLimit, curr, playerNumber, gamePhase)[1]) {
            // Error the moves are not the same
            System.out.println("we encounter a difference in resulting moves at move Number " + moveNumber);
        }*/
        //heuristic.initBonus(curr,modell);
        heuristic.initInversions(curr, modell);
        int moveInt = 0;
        if(gamePhase == 1) {
            moveInt = AspirationTest(depthLimit);
        }else{
            moveInt = easyBombing(curr,playerNumber);
        }
        // Filling up the move array
        move = new int[] { (int) ((moveInt / 100) % 100), (int) (moveInt % 100), (int) (moveInt / 10000) };
        return move;
    }

    /**
     * Helper function for the makeMove method, tries to find the best possible move
     * 
     * @param MessageBytes
     *            the bytes of the message send from the server
     * @return an int[] decoding of the found move, index 0 for the y coordinate,
     *         index one for the x coordinate and index two for the special number
     */
    public void quickTest(gameState gs, int player){
        gameState curr = gs;
        ArrayList<Integer> moves = getListOfMoves(player,curr,1);
        int currEval = 0;
        int normal = 0;
        int quick = 0;
        curr.printMap();
        gameState nextState = null;
        for (int e : moves) {

            currEval = heuristic.cornerSideHeuristicInversion(modell, playerNumber,curr, playerNumber);
            nextState = modell.calculatePos((e/100)%100, e%100, player, e/10000, curr, 1);
            normal = heuristic.cornerSideHeuristicInversion(modell, playerNumber,nextState, playerNumber);
            quick = heuristic.EvaluateIfPossibleCornerSide(modell,(e/100)%100, e%100,e/10000, curr, player, 1 , playerNumber,currEval);
            System.out.println("Move: "+ e + " Eval: " + normal);
            if(quick != normal){
                System.out.println("Current: "+ currEval);
                System.out.println("Normal: "+ normal + " quick: "+ quick);
                System.out.println("Move:" + e);
                nextState.printMap();
            }
        }

    }
    public int[] makeMoveHelper(byte[] MessageBytes) {
        // Reading out the depth limit, is set to 2 if no depth limit is given
        int depthLimit = MessageBytes[4];
        if (depthLimit == 0) {
            depthLimit = 100;
        }
        //System.out.println("Test: "+ Byte.toUnsignedLong(MessageBytes[1]));
        byte byte0 = MessageBytes[0];
        byte byte1 = MessageBytes[1];
        byte byte2 = MessageBytes[2];
        byte byte3 = MessageBytes[3];
        // Reading out the time limit, not yet used, convert to nanoseconds
        long timeLimit = (Byte.toUnsignedLong(byte0) * 256*256*256 + Byte.toUnsignedLong(byte1) * 256*256+ Byte.toUnsignedLong(byte2) * 256 + Byte.toUnsignedLong(byte3))*1000000;
        System.out.println("Timelimit: " + timeLimit);
        moveNumber++;
        int[] move = null;
        // Uses the Pruning algorithm, asserts the found move is valid
        int moveInt=0;
        //quickTest();
        if(gamePhase==1){moveInt = aspirationWindowWithTimeEstimation(timeLimit,depthLimit,0);}
        else{
            gameState curr = modell.getGameState();
            moveInt=easyBombing(curr,playerNumber);
        }
        move = new int[] { (int) ((moveInt / 100) % 100), (int) (moveInt % 100), (int) (moveInt / 10000) };
        return move;
    }
    public int AspirationTest(int depthLimit){
        int move = 0;
        //double optimalEval = MiniMaxZwei(depthLimit,modell.getGameState(),playerNumber,gamePhase)[0];
        //int dataBucket = 0;
        /*for(int i = 0;i<=100;i+=5){
            //Measurement m1 = new Measurement(dataBucket);
            //m1.start();
            move = aspirationWindow(Long.MAX_VALUE/10, depthLimit,((double)i)/100);
            //m1.end();
            //Measurement m2 = new Measurement(dataBucket + 21,pruningStates + moveSortStates);
            pruningStates = 0;
            moveSortStates = 0;
            dataBucket +=1;
        }*/
        pruningStates = 0;
        moveSortStates = 0;
        aspirationWindow(Long.MAX_VALUE/10, depthLimit,0);
        pruningTotal = pruningTotal + (((double)(pruningStates + moveSortStates))-pruningTotal)*(1/((double) moveNumber));
        pruningStates = 0;
        moveSortStates = 0;
        move = aspirationWindow(Long.MAX_VALUE/10, depthLimit,0);
        moveSortTotal = moveSortTotal + (((double)(pruningStates + moveSortStates))- moveSortTotal)*(1/((double) moveNumber));
        return move;
    }
    public int deepeningSearch(long timeLimit,int depthLimit){
        return aspirationWindowWithTimeEstimation(timeLimit, depthLimit, 0);
    }
    public int countOverwrites(gameState gs){
        int ow = 0;
        for(int i = 1; i<= modell.players;i++){
            ow += gs.getOverwrites(i);
        }
        return ow+ countStone(gs, 12);
    }
    public int countStone(gameState gs, int stone){
        int sum = 0;
        for(int x = 0;x < modell.width;x++){
            for(int y = 0;y < modell.height;y++){
                if(gs.getOwner(x,y) == stone){
                    sum++;
                }
            }
        }
        return sum;
    }
    /*public int aspirationWindowTwo(long timeLimit, int depthLimit){
        deadline = System.nanoTime()+timeLimit-500000000;
        gameState curr = modell.getGameState();
        int[] moveEval = new int[]{100,0};
        //Assuming depth 1 always passes
        moveEval = MiniMaxZwei(1, curr, playerNumber, gamePhase,);
        int lastDeviation = 0;
        int[] buffer = moveEval;
        for(int i = 2;i<=depthLimit;i++){
            moveEval = moveSortHelper(i, curr, playerNumber, gamePhase, moveEval[0] - (int)Math.floor(Math.max(lastMax,lastDeviation)), moveEval[0] + 1 + (int)Math.floor((Math.max(lastMax,lastDeviation))));
            if(moveEval[1] == -1) {
                System.out.println("here on depth: " + i);
                System.out.println("Deviation was: "+ lastDeviation);
                System.out.println("Score was: "+ buffer[0]);
                moveEval = moveSortHelper(i, curr, playerNumber, gamePhase, Integer.MIN_VALUE, Integer.MAX_VALUE);
                System.out.println("Next score was: "+ moveEval[0]);
            }
            if(System.nanoTime()>deadline){
                System.out.println("Went to depth: " + (i-1));
                System.out.println("Took total time: " + (System.nanoTime()-deadline + timeLimit-500000000));
                lastMax = lastDeviation+2;
                return (int)buffer[1];
            }
            if(Math.abs(buffer[0]-moveEval[0])+2 > lastDeviation) {
                lastDeviation = Math.abs(buffer[0] - moveEval[0])+2;
            }
                buffer = moveEval;



        }
        lastMax = lastDeviation+1.1;
        return (int)buffer[1];

    }*/
    public int aspirationWindow(long timeLimit, int depthLimit, double AspirationSize){
        deadline = System.nanoTime()+timeLimit-500000000;
        gameState curr = modell.getGameState();
        int[] moveEval = new int[]{100,0};
        //Assuming depth 1 always passes
        moveEval = MiniMaxZwei(1, curr, playerNumber, gamePhase, heuristic.relativePlayernumber);

        int pufferMove = (int) moveEval[1];
        for(int i = 2;i<=depthLimit;i++){
            if(AspirationSize != 0) {
                moveEval = moveSortHelper(i, curr, playerNumber, gamePhase, moveEval[0] - (int)Math.floor(Math.abs(moveEval[0]) * AspirationSize), moveEval[0] + 1 + (int)Math.floor(Math.abs(moveEval[0]) * AspirationSize), heuristic.relativePlayernumber);
            }
            else{
                moveEval = moveSortHelper(i, curr, playerNumber, gamePhase, Integer.MIN_VALUE, Integer.MAX_VALUE, heuristic.relativePlayernumber);
            }
            if(moveEval[1] == -1) {
                moveEval = moveSortHelper(i, curr, playerNumber, gamePhase, Integer.MIN_VALUE, Integer.MAX_VALUE, heuristic.relativePlayernumber);
            }
            if(System.nanoTime()>deadline){
                System.out.println("Went to depth: " + (i-1));
                System.out.println("Took total time: " + (System.nanoTime()-deadline + timeLimit-500000000));
                return pufferMove;
            }
            pufferMove = (int) moveEval[1];

        }
        return pufferMove;

    }


    public int aspirationWindowWithTimeEstimation(long timeLimit, int depthLimit, double AspirationSize){
        deadline = System.nanoTime()+timeLimit-1000000000;
        long harddeadline = System.nanoTime()+timeLimit;
        Instant start;
        Instant end;
        gameState curr = modell.getGameState();
        if(countOverwrites(curr) == 0){
            heuristic.initiateCornerSide(modell, 4,((double)((modell.width*modell.height)-moveNumber* modell.players))/((double)(modell.width*modell.height)));
        }else{
            heuristic.initiateCornerSide(modell, 1,((double)((modell.width*modell.height)-moveNumber* modell.players))/((double)(modell.width*modell.height)));
        }
        heuristic.initBonus(curr,modell);
        heuristic.initChoice(curr,modell);
        heuristic.initInversions(curr, modell);
        int pufferMove = firstMove(playerNumber, curr,gamePhase);
        int[] moveEval = new int[]{100,0};
        //Assuming depth 1 always passes
        start = Instant.now();
        moveEval = MiniMaxZwei(1, curr, playerNumber, gamePhase, heuristic.relativePlayernumber);
        end = Instant.now();
        if(moveEval[1] == -2) {
            return pufferMove;
        }
        if(moveEval[1] == -1) {
            start = Instant.now();
            useOverwrites = true;
            moveEval = MiniMaxZwei(1, curr, playerNumber, gamePhase, heuristic.relativePlayernumber);
            end = Instant.now();
        }
        if(pufferMove == 0 && !isMoveValidHelper(0,0,0,playerNumber, curr, gamePhase)){
            curr.printMap();
            System.out.println("gamePhase: " + gamePhase);
            System.out.println("Result MiniMax: " + MiniMaxZwei(1, curr, playerNumber, gamePhase, heuristic.relativePlayernumber)[1]);
            System.out.println("Result firstPossible: " + firstMove(playerNumber,curr,gamePhase));
            System.out.println("time: " + System.nanoTime());
            System.out.println("deadline: " + deadline);
            ArrayList<Integer> possibleM = getListOfMoves(playerNumber,curr,gamePhase);
            System.out.println(isMoveValidHelper(0,0,0,playerNumber, curr, gamePhase));
            for(int e : possibleM){
                System.out.println(e);
            }
            System.out.println("Overwrites: " + useOverwrites);
            useOverwrites = false;
            System.out.println("Result MiniMax: " + MiniMaxZwei(1, curr, playerNumber, gamePhase, heuristic.relativePlayernumber)[1]);
        }
        pufferMove = moveEval[1];
        if(pufferMove == 0 && !isMoveValidHelper(0,0,0,playerNumber, curr, gamePhase)){
            curr.printMap();
            System.out.println("gamePhase: " + gamePhase);
            System.out.println("Result MiniMax: " + MiniMaxZwei(1, curr, playerNumber, gamePhase, heuristic.relativePlayernumber)[1]);
            System.out.println("Result firstPossible: " + firstMove(playerNumber,curr,gamePhase));
            System.out.println("time: " + System.nanoTime());
            System.out.println("deadline: " + deadline);
            ArrayList<Integer> possibleM = getListOfMoves(playerNumber,curr,gamePhase);
            System.out.println(isMoveValidHelper(0,0,0,playerNumber, curr, gamePhase));
            for(int e : possibleM){
                System.out.println(e);
            }
            System.out.println("Overwrites: " + useOverwrites);
            useOverwrites = false;
            System.out.println("Result MiniMax: " + MiniMaxZwei(1, curr, playerNumber, gamePhase, heuristic.relativePlayernumber)[1]);
        }
        for(int i = 2;i<=depthLimit;i++){
            if(System.nanoTime() + estimatedTime(start, end, i) > harddeadline){
                if(pufferMove == 0){
                    curr.printMap();
                    System.out.println("Result MiniMax: " + MiniMaxZwei(1, curr, playerNumber, gamePhase, heuristic.relativePlayernumber)[1]);
                    System.out.println("Result firstPossible: " + firstMove(playerNumber,curr,gamePhase));
                    System.out.println("time: " + System.nanoTime());
                    System.out.println("deadline: " + deadline);
                    ArrayList<Integer> possibleM = getListOfMoves(playerNumber,curr,gamePhase);
                    for(int e : possibleM){
                        System.out.println(e);
                    }
                    System.out.println("Overwrites: " + useOverwrites);
                    useOverwrites = !useOverwrites;
                    System.out.println("Result MiniMax: " + MiniMaxZwei(1, curr, playerNumber, gamePhase, heuristic.relativePlayernumber)[1]);
                    possibleM = getListOfMoves(playerNumber,curr,gamePhase);
                    for(int e : possibleM){
                        System.out.println(e);
                    }
                }
                System.out.println("Went to depth: " + (i-1));
                System.out.println("Stopped by move estimation");
                useOverwrites = false;
                System.gc();
                return pufferMove;
            }
            //m0 = new Measurement(0, estimatedTime(start, end, i));
            start = Instant.now();
            if(AspirationSize != 0) {
                moveEval = moveSortHelper(i, curr, playerNumber, gamePhase, moveEval[0] - (int)Math.floor(Math.abs(moveEval[0]) * AspirationSize), moveEval[0] + 1+(int)Math.floor(Math.abs(moveEval[0]) * AspirationSize), heuristic.relativePlayernumber);
            }
            else{
                moveEval = moveSortHelper(i, curr, playerNumber, gamePhase, Integer.MIN_VALUE, Integer.MAX_VALUE, heuristic.relativePlayernumber);
            }
            if(moveEval[1] == -1) {
                useOverwrites = true;
                moveEval = moveSortHelper(i, curr, playerNumber, gamePhase, Integer.MIN_VALUE, Integer.MAX_VALUE, heuristic.relativePlayernumber);
            }
            end = Instant.now();
            //m1 = new Measurement(1,Duration.between(start, end).toNanos());
            if(System.nanoTime()>deadline){
                System.out.println("Went to depth: " + (i-1));
                System.out.println("Took total time: " + (System.nanoTime()-deadline + timeLimit-500000000));
                System.out.println("stopped by deadline expiration");
                return pufferMove;
            }
            if(i < pastmovetimes.length){
                pastmovetimes[i] = Duration.between(start, end).toNanos();
            }
            pufferMove = moveEval[1];

        }
        return pufferMove;

    }


    /**
     * Estimates the time the next calculation will take.
     * 
     * @param lastmovestart
     *              the start time of the last calculation
     * @param lastmoveend
     *              the end time of the last calculation
     * @param depth 
     *              the depth of the last calculation
     */
    public long estimatedTime(Instant lastmovestart, Instant lastmoveend, int depth){
        long lastmovedur = Duration.between(lastmovestart, lastmoveend).toNanos();
        /*long res;
        if(depth < pastmovetimes.length && pastmovetimes[depth] != 0){
            //res = pastmovetimes[depth]*2-pastmovetimes[depth]/2;
            res = pastmovetimes[depth]*10;
            return res;
        }*/
        return lastmovedur*possiblemovesamount*2;
    }

    /**
     * Performs the MiniMax algorithm on given gameState using the given depth
     * limit.
     * Simple implementation, does not use executeIfPossible function.
     * 
     * @param depthLimit
     *            the depth limit given by the server
     * @param start
     *            the postion to execute MiniMax on
     * @param player
     *            the player whose turn it is
     * @param gamephase
     *            the games current phase
     * @return the complete evaluation ist stored at index 0, the double encoding of
     *         the move in index one
     */
    public int[] MiniMaxZwei(int depthLimit, gameState start, int player, int gamephase, int invertedPlayer) {
        if(System.nanoTime()>deadline){
            return new int[]{-2,-2};
        }
        if (depthLimit == 0) {
            // we are not allowed to go deeper, evaluate heuristic score of leave
            MiniMaxStates++;
            int d = heuristic.cornerSideHeuristicInversion(modell, playerNumber, start, invertedPlayer);
            // move is 0 as it will not be used
            return new int[] { d, 0 };
        }
        // get list of possible move, initialize variables with standard values
        List<Integer> possibleMoves = getListOfMoves(player, start, gamephase);
        int eval = 0;
        int move = 0;
        int[] evals = null;

        if (possibleMoves.isEmpty()) {
            // checking for Elimination phase -> no player has a valid move
            /*int p = (player) % modell.players + 1;
            while (p != player) {
                possibleMoves = getListOfMoves(p, start, gamephase);
                if (!possibleMoves.isEmpty()) {
                    // a player still has a move
                    return MiniMaxZwei(depthLimit, start, p, gamephase,invertedPlayer);
                }
                p = p % modell.players + 1;
            }
            // in elimination phase/ the game ended
            if (gamephase == 2) {
                // Game came to an End
                MiniMaxStates++;
                int d = heuristic.cornerSideHeuristicInversion(modell, playerNumber, start, invertedPlayer);
                return new int[] { d, 0 };
            } else {
                // player can instead place a bomb
                return MiniMaxZwei(depthLimit, start, player, 2, playerNumber);
            }
            */
            return new int[]{-1,-1};
        } else {
            gameState gs = null;
            if (player == playerNumber) {
                // In a max state, eval set to -1000 so it will be overwritten
                eval = Integer.MIN_VALUE;
                // evaluating all possible moves
                for (int g : possibleMoves) {
                    // calculate the position after the move
                    gs = modell.calculatePos((g / 100) % 100, g % 100, player, g / 10000, start, gamephase);
                    // recursively calling MiniMax
                    evals = MiniMaxZwei(depthLimit - 1, gs,
                            (player) % modell.players + 1, gamephase,(start.getOwner((int)((g / 100) % 100),(int)(g % 100)) == 13 ? invertedPlayer%modell.players +1 : invertedPlayer));
                    if (evals[0] > eval) {
                        // found a new best move
                        eval = evals[0];
                        move = g;
                    }
                }
            } else {
                // In a min state, eval set to 1000 so it will be overwritten
                eval = Integer.MAX_VALUE;
                // evaluating all possible moves
                for (int g : possibleMoves) {
                    // calculate the position after the move
                    gs = modell.calculatePos((g / 100) % 100, g % 100, player, g / 10000, start, gamephase);
                    // recursively calling MiniMax
                    evals = MiniMaxZwei(depthLimit - 1, gs,
                            (player) % modell.players + 1, gamephase, (start.getOwner((int)((g / 100) % 100),(int)(g % 100)) == 13 ? invertedPlayer%modell.players +1 : invertedPlayer));
                    if (evals[0] < eval) {
                        // found a new worst move
                        eval = evals[0];
                        move = g;
                    }
                }
            }
        }
        return new int[] { eval, move };
    }


    /**
     * Setter method for the game phase
     * 
     * @param gp
     *            the value it is set to
     */
    public void setGamePhase(int gp) {
        gamePhase = gp;
    }

    /**
     * Function responsible for updating the map after the server announces a move
     * 
     * @param bytes
     */
    public void moveMade(byte[] bytes) {
        // Splitting up the message

        int x = (int) bytes[1];
        int y = (int) bytes[3];
        int player = (int) bytes[5];
        int special = (int) bytes[4];

        // Update the Map
        modell.executeMove(x, y, player, special, gamePhase);

    }

    /**
     * Checks if the given move is a valid move for us on the current gameState.
     * 
     * @param x
     *            the moves x coordinates
     * @param y
     *            the moves y coordinates
     * @param special
     *            the special number of the move
     * @return true, if the move is valid
     */
    public boolean isMoveValid(int x, int y, int special) {
        // calling the helper function using the current gameState, game phase and
        // player number as additional parameters
        return isMoveValidHelper(x, y, special, playerNumber, modell.getGameState(), gamePhase);
    }

    /**
     * Checks if the given move by the given player is valid in the selected
     * gameState and game phase.
     * 
     * @param x
     *            the moves x coordinates
     * @param y
     *            the moves y coordinates
     * @param special
     *            the special number of the move
     * @param player
     *            the player playing the move
     * @param base
     *            the gameState the move is played in
     * @param gamephase
     *            the games current phase
     * @return true, if the move is valid
     */
    public boolean isMoveValidHelper(int x, int y, int special, int player, gameState base, int gamephase) {

        int koords = 100 * x + y;
        int owner = base.getOwner(koords);
        if (owner == 10) {
            // the given tile is a hole
            return false;
        }
        if (gamephase == 1) {
            // its build phase
            if ((owner < 9) && (owner > 0)) {
                // overwrite stone used
                if ((base.getOverwrites(player) <= 0)) {
                    // No overwrites left
                    return false;
                }

            } else if (owner == 11) {
                // choice tile
                // is a valid player to swap selected?
                if ((special < 1) | (special > modell.getPlayers())) {
                    return false;
                }

            } else if (owner == 12) {
                // bonus tile
                // is either a bomb or an overwrite stone selected?
                if ((special < 20) | (special > 21)) {
                    return false;
                }
            }
            if (owner == 14) {
                // placed on an expansion tile
                // Expansion rule is considered
                return (base.getOverwrites(player) > 0);
            }
            // checking for captured stones in each direction
            for (int i = 0; i < 8; i++) {
                if (modell.tileWalk(koords, i, player, base) > 0) {
                    // at least one stone is captured
                    return true;
                }

            }
        } else if ((base.getBombs(player) > 0) && (special == 0)) {
            // its bombing phase, player has a bomb
            return true;

        }
        return false;
    }

    /**
     * Returns a list of moves containing the moves valid for the given player in
     * the given gameState and game phase.
     * 
     * @param player
     *            the players number
     * @param base
     *            the gameState to find the moves in
     * @param gamephase
     *            the games current phase
     * @return a List of int move encodings
     */
    public ArrayList<Integer> getListOfMoves(int player, gameState base, int gamephase) {
        ArrayList<Integer> possibleMoves = new ArrayList<>();
        if (gamephase == 1) {
            // its build phase
            int owner = 0;
            // iterating over all tiles starting at the upper left corner
            for (int x = 0; x < modell.width; x++) {
                for (int y = 0; y < modell.height; y++) {
                    owner = base.getOwner(x, y);
                    if (owner != 10) {
                        // tile is not a hole
                        // covering all special tiles
                        switch (owner) {
                            case 12:
                                // bonus tile
                                if (isMoveValidHelper(x, y, 20, player, base, gamephase)) {
                                    // move is valid
                                    possibleMoves.add(200000 + x * 100 + y);
                                    possibleMoves.add(210000 + x * 100 + y);
                                }
                                break;
                            case 11:
                                // choice tile
                                if (isMoveValidHelper(x, y, 1, player, base, gamephase)) {
                                    for (int i = 1; i <= modell.players; i++) {
                                        // swapping with every player
                                        possibleMoves.add(i * 10000 + x * 100 + y);
                                    }

                                }
                                break;
                            default:
                                // normal tile
                                if(!((owner == 14 || (owner > 0 && owner < 9)) && !useOverwrites)) {
                                    if (isMoveValidHelper(x, y, 0, player, base, gamephase)) {
                                        // move is valid
                                        possibleMoves.add(x * 100 + y);

                                    }
                                }
                                break;
                        }
                    }
                }
            }
            return possibleMoves;
        } else {
            // gamePhase is 2
            // assuming server knows if we have bombs
            // iterating over all tiles starting at the upper left corner
            for (int x = 0; x < modell.width; x++) {
                for (int y = 0; y < modell.height; y++) {
                    // checking if the player has a bomb and the tile is not a hole
                    if (base.getOwner(x, y) != 10 && base.getBombs(player) > 0) {
                        possibleMoves.add(x * 100 + y);
                    }
                }
            }
            return possibleMoves;
        }
    }

    public boolean hasMove(int player, gameState base, int gamephase){

        if (gamephase == 1) {
            // its build phase
            int owner = 0;
            // iterating over all tiles starting at the upper left corner
            for (int x = 0; x < modell.width; x++) {
                for (int y = 0; y < modell.height; y++) {
                    owner = base.getOwner(x, y);
                    if (owner != 10) {
                        // tile is not a hole
                        // covering all special tiles
                        switch (owner) {
                            case 12:
                                // bonus tile
                                if (isMoveValidHelper(x, y, 20, player, base, gamephase)) {
                                    // move is valid
                                    return true;
                                }
                                break;
                            case 11:
                                // choice tile
                                if (isMoveValidHelper(x, y, 1, player, base, gamephase)) {
                                     return true;

                                }
                                break;
                            default:
                                // normal tile
                                if (isMoveValidHelper(x, y, 0, player, base, gamephase)) {
                                    // move is valid
                                    return true;

                                }
                                break;
                        }
                    }
                }
            }
            return false;
        } else {
            // gamePhase is 2
            // assuming server knows if we have bombs
            // iterating over all tiles starting at the upper left corner
            for (int x = 0; x < modell.width; x++) {
                for (int y = 0; y < modell.height; y++) {
                    // checking if the player has a bomb and the tile is not a hole
                    if (base.getOwner(x, y) != 10 && base.getBombs(player) > 0) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
    public int firstMove(int player, gameState base, int gamephase){

        if (gamephase == 1) {
            // its build phase
            int owner = 0;
            // iterating over all tiles starting at the upper left corner
            for (int x = 0; x < modell.width; x++) {
                for (int y = 0; y < modell.height; y++) {
                    owner = base.getOwner(x, y);
                    if (owner != 10) {
                        // tile is not a hole
                        // covering all special tiles
                        switch (owner) {
                            case 12:
                                // bonus tile
                                if (isMoveValidHelper(x, y, 20, player, base, gamephase)) {
                                    // move is valid
                                    return (x*100+y+200000);
                                }
                                break;
                            case 11:
                                // choice tile
                                if (isMoveValidHelper(x, y, 1, player, base, gamephase)) {
                                    return (x*100+y+10000);

                                }
                                break;
                            default:
                                // normal tile
                                if (isMoveValidHelper(x, y, 0, player, base, gamephase)) {
                                    // move is valid
                                    return (x*100+y);

                                }
                                break;
                        }
                    }
                }
            }
            return 0;
        } else {
            // gamePhase is 2
            // assuming server knows if we have bombs
            // iterating over all tiles starting at the upper left corner
            for (int x = 0; x < modell.width; x++) {
                for (int y = 0; y < modell.height; y++) {
                    // checking if the player has a bomb and the tile is not a hole
                    if (base.getOwner(x, y) != 10 && base.getBombs(player) > 0) {
                        return (x*100+y);
                    }
                }
            }
            return 0;
        }
    }
    /**
     * Helper function for the Pruning algorithm.
     * Executes alpha beta pruning on the given gameState with the passed depth
     * limit starting with the given player.
     * 
     * @param depthLimit
     *            the depth limit given by the server
     * @param start
     *            the postion to execute MiniMax on
     * @param player
     *            the player whose turn it is
     * @param gamephase
     *            the games current phase
     * @return the double evaluation of the position
     */
    public int[] pruningHelper(int depthLimit, gameState start, int player, int gamephase, int initialAlpha, int initialBeta, int invertedPlayer) {
        // initialize alpha and beta values at -100000 and 100000, these values will not
        // be surpassed
        // On the first depth, no cutoffs can be made
        int alpha = initialAlpha;
        int beta = initialBeta;
        int move = 0;
        int max = -100;
        gameState gs = null;
        int owner = 0;
        // Assuming the algorithm starts on a maximizing player
        // iterating over all tiles starting in the upper left corner
        for (int x = 0; x < modell.width; x++) {
            for (int y = 0; y < modell.height; y++) {
                owner = start.getOwner(x, y);
                if (owner != 10) {
                    // the tile is not a hole
                    // Covering all special tiles
                    switch (owner) {
                        case 12 -> {
                            // bonus tile
                            // choosing a bomb
                            gs = modell.executeIfPossible(x, y, player, 20, start, gamephase);
                            if (gs != null) {
                                // move is valid
                                // recursively calling pruningZwei with current alpha and beta values
                                max = pruningZwei(depthLimit - 1, gs, (player) % modell.players + 1, gamephase, alpha,
                                        beta, invertedPlayer);
                                if (max > alpha) {
                                    // new best move found, update alpha
                                    if(max >= beta){
                                        return new int[]{-1,-1};
                                    }
                                    alpha = max;
                                    move = x * 100 + y + 200000;
                                }
                            } // choosing a overite stone
                            gs = modell.executeIfPossible(x, y, player, 21, start, gamephase);
                            if (gs != null) {
                                // move is valid
                                max = pruningZwei(depthLimit - 1, gs, (player) % modell.players + 1, gamephase, alpha,
                                        beta, invertedPlayer);
                                if (max > alpha) {
                                    if(max >= beta){
                                        return new int[]{-1,-1};
                                    }
                                    // new best move found, update alpha
                                    alpha = max;
                                    move = x * 100 + y + 210000;
                                }
                            }
                        }
                        case 11 -> {
                            // choice tile
                            // swap with all players
                            for (int i = 1; i <= modell.players; i++) {

                                gs = modell.executeIfPossible(x, y, player, i, start, gamephase);

                                if (gs != null) {
                                    // move is valid
                                    // recursively calling pruningZwei with current alpha and beta values
                                    max = pruningZwei(depthLimit - 1, gs, (player) % modell.players + 1, gamephase,
                                            alpha, beta, invertedPlayer);
                                    if (max > alpha) {
                                        if(max >= beta){
                                            return new int[]{-1,-1};
                                        }
                                        // new best move found, update alpha
                                        alpha = max;
                                        move = x * 100 + y + i * 10000;
                                    }
                                }
                            }
                        }
                        default -> {
                            // normal tile
                            gs = modell.executeIfPossible(x, y, player, 0, start, gamephase);
                            if (gs != null) {
                                // move is valid
                                // recursively calling pruningZwei with current alpha and beta values
                                max = pruningZwei(depthLimit - 1, gs, (player) % modell.players + 1, gamephase, alpha,
                                        beta, invertedPlayer);
                                if (max > alpha) {
                                    if(max >= beta){
                                        return new int[]{-1,-1};
                                    }
                                    // new best move found, update alpha
                                    alpha = max;
                                    move = x * 100 + y;
                                }
                            }
                        }
                    }
                }
            }

        }
        System.out.println("Eval found best Pruning: "+ alpha);
        if(alpha == initialAlpha){
            return new int[]{-1,-1};
        }
        return new int[]{alpha,move};
    }

    /**
     * Function performing the alpha beta pruning on the given gameState with the
     * passed depth limit starting with the given player.
     * Uses the passed alpha and beta value to cutoff as much branches as possible.
     * 
     * @param depthLimit
     *            the depth limit given by the server
     * @param start
     *            the postion to execute MiniMax on
     * @param player
     *            the player whose turn it is
     * @param gamephase
     *            the games current phase
     * @param alpha
     *            the current alpha value
     * @param beta
     *            the current beta value
     * @return the double evaluation of the position
     */
    public int pruningZwei(int depthLimit, gameState start, int player, int gamephase, int alpha, int beta, int invertedPlayer) {
        if (System.nanoTime() > deadline) {
            return -1000;
        }
        if (depthLimit == 0) {
            // we are in a leaf, return the heuristic score
            pruningStates++;
            return heuristic.cornerSideHeuristicInversion(modell, playerNumber, start, invertedPlayer);
        }
        int max = Integer.MIN_VALUE;
        gameState gs = null;
        if (playerNumber == player) {
            // Its our turn, trying to max
            // Possibility for beta cutoff
            int owner = 0;
            // iterating over all tiles, starting in the upper left corner
            for (int x = 0; x < modell.width; x++) {
                for (int y = 0; y < modell.height; y++) {
                    owner = start.getOwner(x, y);
                    if (owner != 10) {
                        // the tile is not a hole
                        // cover all special tiles
                        switch (owner) {
                            case 12:
                                // bonus tile
                                // choosing a bomb
                                gs = modell.executeIfPossible(x, y, player, 20, start, gamephase);
                                if (gs != null) {
                                    // move is valid
                                    // recursively calling pruningZwei
                                    max = Math.max(pruningZwei(depthLimit - 1, gs, (player) % modell.players + 1,
                                            gamephase, alpha, beta, invertedPlayer), max);
                                    if (max >= beta) {
                                        // perform beta cutoff
                                        return max;
                                    }
                                    // update alpha
                                    alpha = Math.max(alpha, max);
                                } // choosing an overwrite stone
                                gs = modell.executeIfPossible(x, y, player, 21, start, gamephase);
                                if (gs != null) {
                                    // move is valid
                                    // recursively calling pruningZwei
                                    max = Math.max(pruningZwei(depthLimit - 1, gs, (player) % modell.players + 1,
                                            gamephase, alpha, beta, invertedPlayer), max);
                                    if (max >= beta) {
                                        // perform beta cutoff
                                        return max;
                                    }
                                    // update alpha
                                    alpha = Math.max(alpha, max);
                                }
                                break;
                            case 11:
                                // choice tile
                                // swap with all players
                                for (int i = 1; i <= modell.players; i++) {
                                    gs = modell.executeIfPossible(x, y, player, i, start, gamephase);
                                    if (gs != null) {
                                        // move is valid
                                        // recursively calling pruningZwei
                                        max = Math.max(pruningZwei(depthLimit - 1, gs, (player) % modell.players + 1,
                                                gamephase, alpha, beta, invertedPlayer), max);
                                        if (max >= beta) {
                                            // perform beta cutoff
                                            return max;
                                        }
                                        // update alpha
                                        alpha = Math.max(alpha, max);
                                    }
                                }
                                break;
                            case 13:
                                // normal tile
                                gs = modell.executeIfPossible(x, y, player, 0, start, gamephase);
                                if (gs != null) {
                                    // move is valid
                                    // recursively calling pruningZwei
                                    max = Math.max(pruningZwei(depthLimit - 1, gs, (player) % modell.players + 1,
                                            gamephase, alpha, beta, (invertedPlayer)%modell.players+1), max);
                                    if (max >= beta) {
                                        // perform beta cutoff
                                        return max;
                                    }
                                    // update alpha
                                    alpha = Math.max(alpha, max);
                                }
                                break;
                            default:
                                // normal tile
                                gs = modell.executeIfPossible(x, y, player, 0, start, gamephase);
                                if (gs != null) {
                                    // move is valid
                                    // recursively calling pruningZwei
                                    max = Math.max(pruningZwei(depthLimit - 1, gs, (player) % modell.players + 1,
                                            gamephase, alpha, beta, invertedPlayer), max);
                                    if (max >= beta) {
                                        // perform beta cutoff
                                        return max;
                                    }
                                    // update alpha
                                    alpha = Math.max(alpha, max);
                                }
                                break;
                        }
                    }
                } // }
            }
        } else {
            // its the enemies turn, trying to min
            // possibility for alpha cutoff
            int owner = 0;
            max = Integer.MAX_VALUE;
            // iterating over all tiles, starting in the upper left corner
            for (int x = 0; x < modell.width; x++) {
                for (int y = 0; y < modell.height; y++) {
                    owner = start.getOwner(x, y);
                    if (owner != 10) {
                        // tile is not a hole
                        // cover all special tiles
                        switch (owner) {
                            case 12:
                                // bonus tile
                                // choose a bomb
                                gs = modell.executeIfPossible(x, y, player, 20, start, gamephase);
                                if (gs != null) {
                                    // move is valid
                                    // recursively calling pruningZwei
                                    max = Math.min(pruningZwei(depthLimit - 1, gs, (player) % modell.players + 1,
                                            gamephase, alpha, beta, invertedPlayer), max);
                                    if (max <= alpha) {
                                        // alpha cutoff
                                        return max;
                                    }
                                    // update beta
                                    beta = Math.min(beta, max);
                                } // choose an overwrite stone
                                gs = modell.executeIfPossible(x, y, player, 21, start, gamephase);
                                if (gs != null) {
                                    // move is valid
                                    // recursively calling pruningZwei
                                    max = Math.min(pruningZwei(depthLimit - 1, gs, (player) % modell.players + 1,
                                            gamephase, alpha, beta,invertedPlayer), max);
                                    if (max <= alpha) {
                                        // alpha cutoff
                                        return max;
                                    }
                                    // update beta
                                    beta = Math.min(beta, max);
                                }
                                break;
                            case 11:
                                // choice tile
                                // swap with every player
                                for (int i = 1; i <= modell.players; i++) {
                                    gs = modell.executeIfPossible(x, y, player, i, start, gamephase);
                                    if (gs != null) {
                                        // move is valid
                                        // recursively calling pruningZwei
                                        max = Math.min(pruningZwei(depthLimit - 1, gs, (player) % modell.players + 1,
                                                gamephase, alpha, beta, invertedPlayer), max);
                                        if (max <= alpha) {
                                            // alpha cutoff
                                            return max;
                                        }
                                        // update beta
                                        beta = Math.min(beta, max);
                                    }
                                }
                                break;
                            case 13:
                                // normal tile
                                gs = modell.executeIfPossible(x, y, player, 0, start, gamephase);
                                if (gs != null) {
                                    // move is valid
                                    // recursively calling pruningZwei
                                    max = Math.min(pruningZwei(depthLimit - 1, gs, (player) % modell.players + 1,
                                            gamephase, alpha, beta, invertedPlayer%modell.players + 1), max);
                                    if (max <= alpha) {
                                        // alpha cutoff
                                        return max;
                                    }
                                    // update beta
                                    beta = Math.min(beta, max);
                                }
                                break;
                            default:
                                // normal tile
                                gs = modell.executeIfPossible(x, y, player, 0, start, gamephase);
                                if (gs != null) {
                                    // move is valid
                                    // recursively calling pruningZwei
                                    max = Math.min(pruningZwei(depthLimit - 1, gs, (player) % modell.players + 1,
                                            gamephase, alpha, beta, invertedPlayer), max);
                                    if (max <= alpha) {
                                        // alpha cutoff
                                        return max;
                                    }
                                    // update beta
                                    beta = Math.min(beta, max);
                                }
                                break;
                        }
                    }
                }
            }
        }
        if (max == Integer.MAX_VALUE || max == Integer.MIN_VALUE) {
            if (System.nanoTime() > deadline) {
                return -1000;
            }
            // max was not changed, no possible move was found
            // checking for Elimination phase
            int p = (player) % modell.players + 1;
            // check if not player has a move
            while (p != player) {
                if (hasMove(p, start, gamephase)) {
                    // a player still has a move
                    return pruningZwei(depthLimit, start, p, gamephase, alpha, beta, invertedPlayer);
                }
                p = p % modell.players + 1;
            }
            // in elimination phase or game ended
            if (gamephase == 2) {
                // Game came to an End
                pruningStates++;
                return Heuristic.countAllHeuristicBase(modell, playerNumber, start);
            } else {
                // player can now place a bomb
                return pruningZwei(depthLimit, start, player, 2, alpha, beta, playerNumber);
            }

        }

        return max;
    }
    public int moveSortLeavesZwei(gameState start, int player, int gamephase, int alpha,
            int beta, int invertedPlayer) {
        //System.out.println("Now in moveSOrtLeaves");
        if (System.nanoTime() > deadline) {
            return 0;
        }

        int eval = Integer.MIN_VALUE;
            gameState gs = null;
            int currEval = (int)heuristic.cornerSideHeuristicInversion(modell, invertedPlayer, start, invertedPlayer);
        int owner = 0;
            if (player == playerNumber) {
                // In a max state, eval set to -1000 so it will be overwritten

                // evaluating all possible moves
                // calculate the position after the move
                for (int x = 0; x < modell.width; x++) {
                    for (int y = 0; y < modell.height; y++) {
                        owner = start.getOwner(x, y);
                        if (owner != 10) {
                            // the tile is not a hole
                            // Covering all special tiles
                            switch (owner) {
                                case 12 -> {
                                    // bonus tile
                                    // choosing a bomb
                                    alpha = heuristic.EvaluateIfPossibleCornerSide(modell, x,
                                            y, 21, start, player, gamephase, invertedPlayer, currEval);
                                    if(alpha != Integer.MIN_VALUE){
                                        moveSortStates++;
                                        eval = Math.max(alpha,eval);
                                        //System.out.println("Calculated Eval: "+ eval);
                                        if (eval >= beta) {
                                            // found a new best move
                                            return eval;
                                        }
                                    }

                                }

                                case 11 -> {
                                    // choice tile
                                    // swap with all players

                                        for (int i = 1; i <= modell.players; i++) {
                                            // swapping with every player
                                            alpha = heuristic.EvaluateIfPossibleCornerSide(modell, x,
                                                    y, i, start, player, gamephase, invertedPlayer, currEval);
                                            if(alpha != Integer.MIN_VALUE){
                                                moveSortStates++;
                                                eval = Math.max(alpha,eval);
                                                //System.out.println("Calculated Eval: "+ eval);
                                                if (eval >= beta) {
                                                    // found a new best move
                                                    return eval;
                                                }
                                            }
                                        }


                                }
                                case 13 -> {
                                    // choice tile
                                    // swap with all players

                                    // normal tile
                                    alpha = heuristic.EvaluateIfPossibleCornerSide(modell, x,
                                            y, 0, start, player, gamephase, invertedPlayer%modell.players +1, currEval);
                                    if(alpha != Integer.MIN_VALUE){
                                        moveSortStates++;
                                        eval = Math.max(alpha,eval);
                                        //System.out.println("Calculated Eval: "+ eval);
                                        if (eval >= beta) {
                                            // found a new best move
                                            return eval;
                                        }
                                    }


                                }
                                default -> {
                                    // normal tile
                                    if(!((owner == 14 || (owner > 0 && owner < 9)) && !useOverwrites)) {
                                        alpha = heuristic.EvaluateIfPossibleCornerSide(modell, x,
                                                y, 0, start, player, gamephase, invertedPlayer, currEval);
                                        if (alpha != Integer.MIN_VALUE) {
                                            moveSortStates++;
                                            eval = Math.max(alpha, eval);
                                            //System.out.println("Calculated Eval: "+ eval);
                                            if (eval >= beta) {
                                                // found a new best move
                                                return eval;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                }
            } else {
                eval = Integer.MAX_VALUE;
                for (int x = 0; x < modell.width; x++) {
                    for (int y = 0; y < modell.height; y++) {
                        owner = start.getOwner(x, y);
                        if (owner != 10) {
                            // the tile is not a hole
                            // Covering all special tiles
                            switch (owner) {
                                case 12 -> {
                                    // bonus tile
                                    // choosing a bomb
                                    beta = heuristic.EvaluateIfPossibleCornerSide(modell, x,
                                            y, 21, start, player, gamephase, invertedPlayer, currEval);
                                    if(beta != Integer.MIN_VALUE){
                                        moveSortStates++;
                                        eval = Math.min(beta,eval);
                                        //System.out.println("Calculated Eval: "+ eval);
                                        if (eval <= alpha) {
                                            // found a new best move
                                            return eval;
                                        }
                                    }
                                }

                                case 11 -> {
                                    // choice tile
                                    // swap with all players

                                    for (int i = 1; i <= modell.players; i++) {
                                        // swapping with every player
                                        beta = heuristic.EvaluateIfPossibleCornerSide(modell, x,
                                                y, i, start, player, gamephase, invertedPlayer, currEval);
                                        if(beta != Integer.MIN_VALUE) {
                                            moveSortStates++;
                                            eval = Math.min(beta, eval);
                                            //System.out.println("Calculated Eval: "+ eval);
                                            if (eval <= alpha) {
                                                // found a new best move
                                                return eval;
                                            }
                                        }
                                    }


                                }
                                case 13 -> {
                                    // normal tile
                                    beta = heuristic.EvaluateIfPossibleCornerSide(modell, x,
                                            y, 0, start, player, gamephase, playerNumber%modell.players+1, currEval);
                                    if(beta != Integer.MIN_VALUE) {
                                        moveSortStates++;
                                        eval = Math.min(beta, eval);
                                        //System.out.println("Calculated Eval: "+ eval);
                                        if (eval <= alpha) {
                                            // found a new best move
                                            return eval;
                                        }
                                    }
                                }
                                default -> {
                                    // normal tile
                                    if(!((owner == 14 || (owner > 0 && owner < 9)) && !useOverwrites)) {
                                        beta = heuristic.EvaluateIfPossibleCornerSide(modell, x,
                                                y, 0, start, player, gamephase, playerNumber, currEval);
                                        if (beta != Integer.MIN_VALUE) {
                                            moveSortStates++;
                                            eval = Math.min(beta, eval);
                                            //System.out.println("Calculated Eval: "+ eval);
                                            if (eval <= alpha) {
                                                // found a new best move
                                                return eval;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                }

            }
            if(eval == Integer.MIN_VALUE || eval == Integer.MAX_VALUE){
                int p = player;
                while (p != player) {
                    if (hasMove(p,start,gamephase)) {
                        // a player still has a move
                        if(p == player){
                            useOverwrites = true;
                        }
                        return moveSortLeavesZwei(start,p,gamephase,alpha, beta, invertedPlayer);
                    }
                    p = p % modell.players + 1;
                }
                // in elimination phase/ the game ended
                if (gamephase == 2) {
                    // Game came to an End
                    moveSortStates++;
                    return currEval;
                } else {
                    // player can instead place a bomb
                    return moveSortLeavesZwei(start,p,2,alpha, beta, invertedPlayer);
                }
            }
        //System.out.println("Returned EVal Leaves: " + eval);

        return eval;
    }
    public int[] moveSortHelper(int depthLimit, gameState start, int player, int gamephase, int initialAlpha, int initialBeta, int invertedPlayer) {
        // initialize alpha and beta values at -100000 and 100000, these values will not
        // be surpassed
        // On the first depth, no cutoffs can be made
        int alpha = initialAlpha;
        int beta = initialBeta;
        int move = 0;
        int eval = -100;
        gameState gs = null;
        int owner = 0;
        // get list of possible move, initialize variables with standard values
        ArrayList<Long> possibleMoves = new ArrayList<>();
        if (player == playerNumber) {
            // In a max state, eval set to -1000 so it will be overwritten
            // evaluating all possible moves
            // calculate the position after the move
            for (int x = 0; x < modell.width; x++) {
                for (int y = 0; y < modell.height; y++) {
                    owner = start.getOwner(x, y);
                    if (owner != 10) {
                        // the tile is not a hole
                        // Covering all special tiles
                        switch (owner) {
                            case 12 -> {
                                // bonus tile
                                // choosing a bomb
                                if (isMoveValidHelper(x, y, 20, player, start, gamephase)) {
                                    gs = modell.calculatePos(x,y,player,21,start,gamephase);
                                    possibleMoves.add(0x3FD67BA0CECC0000L + (long) (heuristic.cornerSideHeuristicInversion(modell,playerNumber,gs, invertedPlayer))
                                            *1000000  + 200000 + x * 100 + y);
                                    gs = modell.calculatePos(x,y,player,20,start,gamephase);
                                    possibleMoves.add(0x3FD67BA0CECC0000L + (long)(heuristic.cornerSideHeuristicInversion(modell,playerNumber,gs, invertedPlayer))
                                            * 1000000 + 210000 + x * 100 + y);
                                }
                            }

                            case 11 -> {
                                // choice tile
                                // swap with all players
                                if (isMoveValidHelper(x, y, 1, player, start, gamephase)) {
                                    for (int i = 1; i <= modell.players; i++) {
                                        // swapping with every player
                                        gs = modell.calculatePos(x,y,player,i,start,gamephase);
                                        possibleMoves.add(0x3FD67BA0CECC0000L + (long) (heuristic.cornerSideHeuristicInversion(modell,playerNumber,gs, invertedPlayer))
                                                *1000000  + i *10000 + x * 100 + y);
                                    }
                                }

                            }
                            case 13 -> {
                                // inversion
                                if (isMoveValidHelper(x, y, 0, player, start, gamephase)) {
                                    gs = modell.calculatePos(x,y,player,0,start,gamephase);
                                    possibleMoves.add(0x3FD67BA0CECC0000L + (long) (heuristic.cornerSideHeuristicInversion(modell,playerNumber,gs, invertedPlayer %modell.players +1))
                                            *1000000  + x * 100 + y);
                                }
                            }
                            default -> {
                                // normal tile
                                if(!((owner == 14 || (owner > 0 && owner < 9)) && !useOverwrites)) {
                                    if (isMoveValidHelper(x, y, 0, player, start, gamephase)) {
                                        gs = modell.calculatePos(x, y, player, 0, start, gamephase);
                                        possibleMoves.add(0x3FD67BA0CECC0000L + (long) (heuristic.cornerSideHeuristicInversion(modell, playerNumber, gs, invertedPlayer))
                                                * 1000000 + x * 100 + y);
                                    }
                                }
                            }
                        }
                    }
                }

            }
            possiblemovesamount = possibleMoves.size();
            Collections.sort(possibleMoves,Collections.reverseOrder());
            for (long currMove : possibleMoves) {
                /*System.out.println("Now testing: " + (currMove % 1000000) + " with eval: " + (currMove / 1000000)
                        + " total: " + currMove);*/

                //System.out.println("Eval+move: "+ currMove);
                gs = modell.calculatePos((int)((currMove / 100) % 100), (int)(currMove % 100), player, (int)((currMove / 10000) % 100),
                        start,
                        gamephase);
                // recursively calling MiniMax
                eval = moveSort(depthLimit - 1, gs, (player) % modell.players + 1, gamephase, alpha, beta, (start.getOwner((int)((currMove / 100) % 100),(int)(currMove % 100)) == 13 ? invertedPlayer%modell.players +1 : invertedPlayer));
                // update alpha
                if (eval >= alpha) {
                    if(eval >= beta){
                        return new int[]{-1,-1};
                    }
                    alpha = eval;
                    move = (int) (currMove % 1000000);
                }
            }
        }
        // Assuming the algorithm starts on a maximizing player
        // iterating over all tiles starting in the upper left corner
        //System.out.println("Move found best moveSort: "+ alpha);
        if(initialAlpha >= alpha){
            return new int[]{-1,-1};
        }
        return new int[]{alpha,move};
    }

    public int moveSort(int depthLimit, gameState start, int player, int gamephase, int alpha,
                               int beta, int invertedPlayer) {
        // System.out.println("Entering this funtion on depth: "+ depthLimit);
        if (System.nanoTime() > deadline) {
            //System.out.println("timelimit reached");
            return 0;
        }
        if (depthLimit == 1) {
            // we are not allowed to go deeper, evaluate heuristic score of leaves
            // move is 0 as it will not be used
            //int a = pruningZwei(1,start, player, gamephase, alpha, beta, invertedPlayer);
            //int b = moveSortLeavesZwei(start, player, gamephase, alpha, beta, invertedPlayer);
            /*if(Math.abs(a - b)>50) {
                    System.out.println("Base Eval: " + heuristic.cornerSideHeuristicInversion(modell, player, start, invertedPlayer));
                System.out.println("pruning: " + a + " movesort: " + b + " to play: " + player + " to evaluate: " + invertedPlayer);
                quickTest(start,player);
            }*/
            //return pruningZwei(1,start, player, gamephase, alpha, beta, invertedPlayer);
            return moveSortLeavesZwei(start, player, gamephase, alpha, beta, invertedPlayer);
            //return a;
        }
        // get list of possible move, initialize variables with standard values
        ArrayList<Long> possibleMoves = new ArrayList<>();
        gameState gs = null;
        int owner = 0;
        for (int x = 0; x < modell.width; x++) {
            for (int y = 0; y < modell.height; y++) {
                owner = start.getOwner(x, y);
                if (owner != 10) {
                    // the tile is not a hole
                    // Covering all special tiles
                    switch (owner) {
                        case 12 -> {
                            // bonus tile
                            // choosing a bomb
                            if (isMoveValidHelper(x, y, 20, player, start, gamephase)) {
                                gs = modell.calculatePos(x,y,player,20,start,gamephase);
                                possibleMoves.add(0x3FD67BA0CECC0000L + (long) (heuristic.cornerSideHeuristicInversion(modell,playerNumber,gs, invertedPlayer))
                                        *1000000  + 200000 + x * 100 + y);
                                gs = modell.calculatePos(x,y,player,21,start,gamephase);
                                possibleMoves.add(0x3FD67BA0CECC0000L + (long) (heuristic.cornerSideHeuristicInversion(modell,playerNumber,gs, invertedPlayer))
                                        *1000000  + 210000 + x * 100 + y);
                            }

                        }

                        case 11 -> {
                            // choice tile
                            // swap with all players
                            if (isMoveValidHelper(x, y, 1, player, start, gamephase)) {
                                for (int i = 1; i <= modell.players; i++) {
                                    // swapping with every player
                                    gs = modell.calculatePos(x,y,player,i,start,gamephase);
                                    possibleMoves.add(0x3FD67BA0CECC0000L + (long) (heuristic.cornerSideHeuristicInversion(modell,playerNumber,gs, invertedPlayer))
                                            *1000000  + i * 10000 + x * 100 + y);
                                }
                            }

                        }
                        case 13 -> {
                                // inversion
                                if (isMoveValidHelper(x, y, 0, player, start, gamephase)) {
                                    gs = modell.calculatePos(x,y,player,0,start,gamephase);
                                    possibleMoves.add(0x3FD67BA0CECC0000L + (long) (heuristic.cornerSideHeuristicInversion(modell,playerNumber,gs, invertedPlayer%modell.players +1))
                                            *1000000  + x * 100 + y);
                                }
                            }
                        default -> {
                            // normal tile
                            if(!((owner == 14 || (owner > 0 && owner < 9)) && !useOverwrites)) {
                            if (isMoveValidHelper(x, y, 0, player, start, gamephase)) {
                                gs = modell.calculatePos(x, y, player, 0, start, gamephase);
                                possibleMoves.add(0x3FD67BA0CECC0000L + (long) (heuristic.cornerSideHeuristicInversion(modell, playerNumber, gs, invertedPlayer))
                                        * 1000000 + x * 100 + y);
                                }
                            }
                        }
                    }
                }
            }

        }
        int eval = Integer.MIN_VALUE;

        if (possibleMoves.isEmpty()) {
            // checking for Elimination phase -> no player has a valid move
            int p = player;
            while (p != player) {
                if (hasMove(p,start,gamephase)) {
                    // a player still has a move
                    if(p == player){
                        useOverwrites = true;
                    }
                    return moveSort(depthLimit, start, p, gamephase, alpha, beta, invertedPlayer);
                }
                p = p % modell.players + 1;
            }
            // in elimination phase/ the game ended
            if (gamephase == 2) {
                // Game came to an End
                moveSortStates++;
                return Heuristic.countAllHeuristicBase(modell, playerNumber, start);
            } else {
                // player can instead place a bomb
                return moveSort(1, start, player, 2, alpha, beta,playerNumber);
            }

        } else {
            if (player == playerNumber) {
                // In a max state, eval set to -1000 so it will be overwritten
                Collections.sort(possibleMoves,Collections.reverseOrder());
                for (Long currMove : possibleMoves) {
                    if(currMove<0){
                        System.out.println("Error here: " + currMove);
                    }
                    gs = modell.calculatePos((int)((currMove / 100) % 100), (int) (currMove % 100), player, (int) ((currMove / 10000) % 100),
                            start,
                            gamephase);
                    // recursively calling MiniMax
                    eval = Math.max(
                            moveSort(depthLimit - 1, gs, (player) % modell.players + 1, gamephase, alpha, beta,(start.getOwner((int)((currMove / 100) % 100),(int)(currMove % 100)) == 13 ? invertedPlayer%modell.players +1 : invertedPlayer)),
                            eval);
                    if (eval >= beta) {
                        // found a new best move
                        return eval;
                    }
                    // update alpha
                    alpha = Math.max(alpha, eval);
                }

            } else {
                eval = Integer.MAX_VALUE;
                Collections.sort(possibleMoves);
                for (long currMove : possibleMoves) {
                    gs = modell.calculatePos((int)((currMove / 100) % 100), (int)(currMove % 100), player, (int)((currMove / 10000) % 100),
                            start,
                            gamephase);
                    // recursively calling MiniMax
                    eval = Math.min(
                            moveSort(depthLimit - 1, gs, (player) % modell.players + 1, gamephase, alpha, beta,(start.getOwner((int)((currMove / 100) % 100),(int)(currMove % 100)) == 13 ? invertedPlayer%modell.players +1 : invertedPlayer)),
                            eval);
                    if (eval <= alpha) {
                        // found a new best move
                        return eval;
                    }
                    // update alpha
                    beta = Math.min(beta, eval);
                }

            }
        }
        //System.out.println("Returned EVal moveSort: " + eval);
        return eval;
    }
    public int easyBombing(gameState curr, int player){
        int bestx = 0;
        int besty = 0;
        int bestEval=Integer.MIN_VALUE;
        gameState gs = null;
        for (int x = 0; x < modell.width; x++) {
            for (int y = 0; y < modell.height; y++) {
                if(isMoveValidHelper(x, y, 0, player, curr, 2)){gs = modell.calculatePos(x,y,player,0,curr,2);
                  int hold = heuristic.placementHeuristic(modell, player, gs);
                  if(hold>=bestEval){
                    bestEval=hold;
                    bestx=x;
                    besty=y;}
                }
            
            }}
            //System.out.println("we get here");
            return (bestx*100+besty);
        }

}
