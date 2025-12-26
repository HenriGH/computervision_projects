import java.util.ArrayList;
import java.util.List;

public class Heuristic {
    int[][] values;
    int[][] oldvalues;
    ArrayList<Integer> edges;
    gameState holeView;
    Boolean sadSituation;
    int relativePlayernumber;
    int ourPlayer;

    public Heuristic(int heuristic, MapModel model, int player) {
        ourPlayer=player;
        if (heuristic == 0) {
            initiateCornerSide(model,1, 1.0);
        } else {
            initNewcornerSide(model, player);
        }
        sadSituation = false;
    }

    public static int countAllHeuristicBase(MapModel modell, int player, gameState base) {
        int counter = 0;
        for (int x = 0; x < modell.width; x++) {
            for (int y = 0; y < modell.height; y++) {
                if (base.getOwner(x, y) == player) {
                    counter++;
                }

            }
        }
        // System.out.println(counter);
        return counter;
    }

    /*
     * public void recalibrate(MapModel modell,int player, gameState base){
     * System.out.println("recall start");
     * ArrayList<Integer> remove=new ArrayList<>();
     * ArrayList<Integer> addd=new ArrayList<>();
     * for(int edge:edges){
     * System.out.print(edge+"");
     * if(base.getOwner(edge%10000)==player){
     * for (int direction = 0; direction < 8; direction++) {
     * if(modell.neighbourArrays[(edge/100)%100][edge%100][direction] != -1){
     * int hold=modell.neighbourArrays[(edge/100)%100][edge%100][direction]%10000;
     * values[hold/100][hold%100]+=10;
     * addd.add(hold);
     * remove.add(edge);
     * }
     * }
     * }
     * else{ if(base.getOwner(edge%10000)<9&&base.getOwner(edge%10000)>=1){
     * for (int direction = 0; direction < 8; direction++) {
     * if(modell.neighbourArrays[(edge/100)%100][edge%100][direction] != -1){
     * int hold=modell.neighbourArrays[(edge/100)%100][edge%100][direction]%10000;
     * values[hold/100][hold%100]-=10;
     * addd.add(hold);
     * remove.add(edge);
     * }
     * }
     * }
     * }}
     * System.out.println("recall mid");
     * for(Integer toRem:remove){
     * edges.removeAll(toRem);
     * }
     * for(Integer toAdd:addd){
     * if(!edges.contains(toAdd)){
     * edges.add(toAdd);}
     * }
     * System.out.println("recall end");
     * }
     */
    public void initiateCornerSide(MapModel modell, int factor, double decrease) {
    decrease = Math.max(decrease,0.0);
    decrease = Math.min(decrease,1.0);
        values = new int[modell.width][modell.height];
        oldvalues = new int[modell.width][modell.height];
        for (int x = 0; x < modell.width; x++) {
            for (int y = 0; y < modell.height; y++) {
                int capturables = 0;
                for (int direction = 0; direction < 4; direction++) {
                    if (modell.neighbourArrays[x][y][direction] != -1
                            && modell.neighbourArrays[x][y][direction + 4] != -1) {
                        capturables++;
                    }

                }
                if (capturables == 0) {
                    values[x][y] = 1+ (int)Math.floor((6 * factor*factor)*decrease);
                    oldvalues[x][y] = 1+ (int) Math.floor((6* factor*factor)*decrease);
                } else if (capturables == 1) {
                        values[x][y] = 1 + (int) Math.floor((3 * factor)* decrease);
                        oldvalues[x][y] = 1 +(int) Math.floor((3*factor) * decrease);
                } else if(capturables==2){
                        values[x][y] = 1 + (int) Math.floor((2)*decrease);
                        oldvalues[x][y] = 1 + (int) Math.floor((2)*decrease);
                    }
                else{
                    values[x][y] = 1;
                    oldvalues[x][y] = 1;
                }
            }
        }
        /*for (int x = 0; x < modell.width; x++) {
            for (int y = 0; y < modell.height; y++) {
                System.out.print(values[x][y] + "");
                }
            System.out.println(" ");
            }*/
    }
    /*public void initBonus(gameState curr,MapModel modell){
      for (int x = 0; x < modell.width; x++) {
            for (int y = 0; y < modell.height; y++) {
                values[x][y]=oldvalues[x][y];
                if(curr.getOwner(x,y)==12){values[x][y]+=6;}
                }
            }  
    }*/
    public void initBonus(gameState curr,MapModel modell){
        boolean neighbour = false;
        int tile = 0;
        for (int x = 0; x < modell.width; x++) {
            for (int y = 0; y < modell.height; y++) {
                neighbour = false;
                values[x][y]=oldvalues[x][y];
                for(int i = 0;i<8;i++){
                    tile = modell.neighbourArrays[x][y][i] % 10000;
                    if(tile != -1 && curr.getOwner(tile) == 12){
                        neighbour = true;
                    }
                }
                if(neighbour){
                    values[x][y] -= 10;
                }
            }
        }
    }
    public void initChoice(gameState curr,MapModel modell){
        boolean neighbour = false;
        int tile = 0;
        for (int x = 0; x < modell.width; x++) {
            for (int y = 0; y < modell.height; y++) {
                neighbour = false;
                values[x][y]=oldvalues[x][y];
                for(int i = 0;i<8;i++){
                    tile = modell.neighbourArrays[x][y][i] % 10000;
                    if(tile != -1 && curr.getOwner(tile) == 13){
                        neighbour = true;
                    }
                }
                if(neighbour){
                    values[x][y] -= 100;
                }
            }
        }
    }
    public void initInversions(gameState curr, MapModel modell){
        int inversionNumber=0;
        for (int x = 0; x < modell.width; x++) {
            for (int y = 0; y < modell.height; y++) {
                if(curr.getOwner(x,y)==13){inversionNumber++;}
                }
            } 
            relativePlayernumber=ourPlayer;
        while(inversionNumber>0){
            relativePlayernumber--;
            if(relativePlayernumber==0){relativePlayernumber=modell.getPlayers();}
            inversionNumber--;
        } 
    }

    public void initNewcornerSide(MapModel modell, int player) {

        edges = new ArrayList<Integer>();
        values = new int[modell.width][modell.height];
        for (int x = 0; x < modell.width; x++) {
            for (int y = 0; y < modell.height; y++) {
                int capturables = 0;
                for (int direction = 0; direction < 4; direction++) {
                    if (modell.neighbourArrays[x][y][direction] != -1
                            && modell.neighbourArrays[x][y][direction + 4] != -1) {
                        capturables++;
                    }

                }
                if (capturables == 0) {
                    values[x][y] = 100;
                    edges.add(x * 100 + y);
                } else {
                    if (capturables == 1) {
                        values[x][y] = 1;
                    } else {
                        values[x][y] = 0;
                    }
                }
            }
        }
        for (int edge : edges) {
            for (int j = 0; j < 8; j++) {
                if (modell.neighbourArrays[edge / 100][edge % 100][j] != -1) {
                    int hold = modell.neighbourArrays[edge / 100][edge % 100][j] % 10000;
                    for (int i = 5; i > 0; i--) {
                        if (hold == -1) {
                            break;
                        }
                        values[hold / 100][hold % 100] -= i * (i % 2);
                        values[hold / 100][hold % 100] += i * ((i + 1) % 2);
                        hold = modell.neighbourArrays[hold / 100][hold % 100][j] % 10000;
                    }
                }
            }
        }
    }

    public void reinitNewcornerSide(MapModel modell, gameState fakeBase, int player) {
        if (!sadSituation) {
            sadSituation = true;
            holeView = fakeBase.duplicate();
        }
        holeView = buildHoles(modell, fakeBase, player, holeView);
        gameState base = holeView;
        edges = new ArrayList<Integer>();
        values = new int[modell.width][modell.height];
        for (int x = 0; x < modell.width; x++) {
            for (int y = 0; y < modell.height; y++) {
                int capturables = 0;
                if (base.getOwner(x, y) != -1) {
                    for (int direction = 0; direction < 4; direction++) {
                        if ((modell.neighbourArrays[x][y][direction] != -1)
                                && (modell.neighbourArrays[x][y][direction + 4] != -1
                                        || base.getOwner(modell.neighbourArrays[x][y][direction] % 10000) == -1)) {
                            if (base.getOwner(modell.neighbourArrays[x][y][direction] % 10000) == -1
                                    || base.getOwner(modell.neighbourArrays[x][y][direction + 4] % 10000) == -1) {
                                capturables--;
                            }
                            capturables++;
                        }
                    }

                    if (capturables == 0) {
                        values[x][y] = 100;
                        edges.add(x * 100 + y);
                    } else {
                        if (capturables == 1) {
                            values[x][y] = 4;
                        } else {
                            values[x][y] = 0;
                        }
                    }
                }
            }
        }
        for (int edge : edges) {
            for (int j = 0; j < 8; j++) {
                if (modell.neighbourArrays[edge / 100][edge % 100][j] != -1) {
                    int hold = modell.neighbourArrays[edge / 100][edge % 100][j] % 10000;
                    for (int i = 3; i > 0; i--) {
                        if (hold == -1) {
                            i = 5;
                            break;
                        }
                        values[hold / 100][hold % 100] -= i * (i % 2) * i;
                        values[hold / 100][hold % 100] += i * ((i + 1) % 2) * i;
                        hold = modell.neighbourArrays[hold / 100][hold % 100][j] % 10000;
                    }
                }
            }
        }
    }

    public gameState buildHoles(MapModel modell, gameState base, int player, gameState fakeBase) {
        for (int edge : edges) {
            if (base.getOwner(edge % 10000) == player) {
                fakeBase.setOwner(edge % 10000, -1);
            }
        }
        return fakeBase;
    }

    public static int worstPossible(MapModel modell, int player, gameState base) {
        int counter = 1000;
        for (int x = 0; x < modell.width - 1; x++) {
            for (int y = 0; y < modell.height - 1; y++) {
                if (base.getOwner(x, y) == player) {
                    counter--;
                }
            }
        }
        return counter;

    }
    public int placementHeuristic(MapModel modell, int player, gameState base){
        int score = 0;
        int[] scores = new int[modell.players];
        int minScore = Integer.MAX_VALUE;
        int maxScore = Integer.MAX_VALUE;
        for(int i = 0; i < modell.players;i++){
            scores[i] = countAllHeuristicBase(modell, i+1, base);
        }
        for(int i = 0; i < modell.players;i++){
            if(scores[i] < scores[player-1]){
                score += 100000;
            }
            if(scores[i] - scores[player-1] < minScore && scores[i] > scores[player-1]){
                minScore = scores[i] - scores[player-1];
            }
            else if(scores[player-1] - scores[i] < maxScore && scores[i] < scores[player-1]){
                maxScore = scores[player-1] - scores[i];
            }
        }
        if(minScore == Integer.MAX_VALUE){
            minScore = 0;
        }
        if(maxScore == Integer.MAX_VALUE){
            maxScore = 0;
        }
        return score + minScore - maxScore;
    }
    public int smallestDeltaHeuristic(MapModel modell, int player, gameState base) {
        int ground = countAllHeuristicBase(modell, player, base);
        int delta = 10000;
        for (int i = 1; i <= modell.getPlayers(); i++) {
            if (i != player) {
                if (delta > ground - countAllHeuristicBase(modell, i, base)) {
                    delta = ground - countAllHeuristicBase(modell, i, base);
                }
            }
        }
        //System.out.println(delta+"Ergebnis Delta");
        return delta+1000;
    }

    // "corners, meaning not capturable =5 pts side= only capturable on one way"
    public int cornerSideHeuristic(MapModel modell, int player, gameState base) {
        int counter = 0;
        for (int x = 0; x < modell.width; x++) {
            for (int y = 0; y < modell.height; y++) {
                if (base.getOwner(x, y) == player) {
                    counter += values[x][y];
                }
            }
        }
        // System.out.println(counter);
        return counter;
    }
    public int cornerSideHeuristicInversion(MapModel modell,int player, gameState base, int invertedPlayer) {
        int counter = 0;
        for (int x = 0; x < modell.width; x++) {
            for (int y = 0; y < modell.height; y++) {
                if (base.getOwner(x, y) == invertedPlayer) {
                    counter += values[x][y];
                }
            }
        }
        counter+=base.getOverwrites(ourPlayer)*100;
        // System.out.println(counter);
        //System.out.println(counter);
        return counter;
    }
    public int cornerSideHeuristicInversionHapp(MapModel modell,int player, gameState base) {
        int counter = 0;
        for (int x = 0; x < modell.width; x++) {
            for (int y = 0; y < modell.height; y++) {
                if(relativePlayernumber!=1){
                if (base.getOwner(x, y) == relativePlayernumber-1) {
                    counter += values[x][y];
                }}
                else{
                    if (base.getOwner(x, y) == modell.getPlayers()) {
                    counter += values[x][y];
                }
                }
            }
        }
        counter+=base.getOverwrites(player)*100;
        // System.out.println(counter);
        return counter;
    }

    public int deltaCornerSideHeuristic(MapModel modell, int player,
            gameState base) {

        int ground = cornerSideHeuristic(modell, player, base);
        int delta = 10000;
        for (int i = 1; i <= modell.getPlayers(); i++) {
            if (i != player) {
                if (delta < ground - cornerSideHeuristic(modell, i, base)) {
                    delta = ground - cornerSideHeuristic(modell, i, base);
                }
            }
        }
        return delta;
    }

    /*
     * public int fastHeuristic(MapModel modell, int player, gameState base) {
     * int min_y= 100;
     * int min_x = 100;
     * int max_y = 0;
     * int max_x = 0;
     * 
     * 
     * }
     */
    /**
     * Heuristic which evaluates a Game state based on how many and what items the
     * player has
     * 
     * @param modell
     *            current Map
     * @param player
     *            current player
     * @param base
     *            current Gamestate
     * @return rating of the current game state from the perspective of
     *         {@code player}
     */
    public static int itemsHeuristic(MapModel modell, int player,
            gameState base) {
        int overwrites = base.getOverwrites(player);
        int bombs = base.getBombs(player);
        // testing purposes
        int factor = 100;
        if (overwrites < 3) {
            factor = 2;
        }

        return bombs * modell.getBombStrength() + overwrites * factor * 2;
    }
    /**
     * Heuristic which evaluates a Game state based on its neighbours (we dont want
     * to be neighouring a bonus tile for example)
     * 
     * @param modell
     *            current Map
     * @param player
     *            current player
     * @param base
     *            current Gamestate
     * @return rating of the current game state from the perspective of
     *         {@code player}
     */
    public int neighbourHeuristic(MapModel modell, int player,
            gameState base) {
        // List<Integer> mytiles = modell.getMyTiles();
        List<Integer> mytiles = null;
        int rating = 0;
        for (int coords : mytiles) {
            for (int i = 0; i < 8; i++) {
                int neighbourcoords = modell.neighbourArrays[coords / 100][coords % 100][i];
                switch (base.getOwner(neighbourcoords % 10000)) {
                    case 11:
                        // choice
                        rating++;
                        break;
                    case 12:
                        // inversion
                        rating++;
                        break;
                    case 13:
                        // bonus
                        rating++;
                        break;
                    default:
                        rating += 10;
                        break;
                }
            }
        }
        return rating;
    }

    public static int modCornerSideHeuristic(MapModel modell, int player, gameState base) {
        int counter = 0;
        for (int x = 0; x < modell.width; x++) {
            for (int y = 0; y < modell.height; y++) {
                int capturables = 0;
                for (int direction = 0; direction < 4; direction++) {
                    if (modell.neighbourArrays[x][y][direction] != -1
                            && modell.neighbourArrays[x][y][direction + 4] != -1) {
                        capturables++;
                    }

                }
                int hold = base.getOwner(x, y);
                if (capturables == 0 && hold == player) {
                    counter += 5;
                }
                if (capturables == 0 && hold == 0) {
                    for (int direction = 0; direction < 8; direction++) {
                        if (modell.neighbourArrays[x][y][direction] != -1) {
                            int cords = modell.neighbourArrays[x][y][direction];
                            if (base.getOwner(cords % 10000) == player) {
                                counter = counter - 5;
                            }
                        }
                    }
                }
                if (capturables == 1 && hold == player) {
                    counter++;
                }
            }
        }
        return counter;
    }
    /**
     * Evaluates a move according to the total amount of stones of the player playerToEvaluate.
     * The return value is the change in heuristic score.
     * @param modell a mapModell instance
     * @param x the moves x- coordinates
     * @param y the moves y- coordinates
     * @param special the moves special number
     * @param curr the gameState to evaluate the move in
     * @param player the number of the player who executes the move
     * @param gamephase the current game phase
     * @param playerToEvaluate the player from whose perspective the move should be evaluated
     * @return the amount of captured/lost stones
     */
    public static double EvaluateIfpossible(MapModel modell, int x, int y, int special, gameState curr, int player,
            int gamephase, int playerToEvaluate) {
        int owner = curr.getOwner(x, y);
        int eval = 0;
        if (player == playerToEvaluate) {
            //trying to max, count all captured stones
            if (gamephase == 1) {
                if ((owner < 9) && (owner > 0)) {
                    // overwrite stone used
                    if ((curr.getOverwrites(player) <= 0)) {
                        return -1000;
                    }

                }
                if (owner == 14) {
                    if (curr.getOverwrites(player) <= 0) {
                        return -1000;
                    }
                    
                }
                //check captured stones in each direction
                int koords = x * 100 + y;
                for (int i = 0; i < 8; i++) {
                    eval += modell.tileWalk(koords, i, player, curr);
                }
                if (owner == 14 || eval > 0) {
                    eval++;
                }
            } else if ((curr.getBombs(player) > 0) && (owner != 10)) {
                // its bombing phase
                //not working yet
                eval++;
            }
        } else {
            //trying the min, count all lost stones
            boolean capture = false;
            if (gamephase == 1) {
                if ((owner < 9) && (owner > 0)) {
                    // overwrite stone used
                    // remove
                    if ((curr.getOverwrites(player) <= 0)) {
                        return -1000;
                    }

                }
                if (owner == 14) {
                    if (curr.getOverwrites(player) <= 0) {
                        return -1000;
                    }
                    capture = true;
                }
                if(owner == playerToEvaluate){
                    eval--;
                }
                //check replaced stone of enemy in each direction
                int koords = x * 100 + y;
                for (int i = 0; i < 8; i++) {
                    owner = modell.countCapturedTiles(koords, i, player, curr,playerToEvaluate);
                    if(owner>0){
                        //the move is valid
                        capture = true;
                         eval = eval - ((owner)) +1;
                    }
                }
                if(capture){
                    return eval;
                }
            } else if ((curr.getBombs(player) > 0) && (owner != 10)) {
                //not working yet
                // its bombing phase
                eval--;
            }
        }

        if (eval == 0) {
            return -1000;
        }
        return eval;
    }
    public int EvaluateIfPossibleCornerSide(MapModel modell, int x, int y, int special, gameState curr, int player,
            int gamephase, int playerToEvaluate, int evalBase) {
        int owner = curr.getOwner(x,y);
        if(gamephase == 2){

            gameState gs = modell.executeIfPossible(x, y, player, special, curr, gamephase);
            if(gs != null){
                evalBase =  cornerSideHeuristic(modell, playerToEvaluate, gs);
            }
            else {
                return Integer.MIN_VALUE;
            }

        }else if(owner == 11 && ((special != playerToEvaluate && player == playerToEvaluate) || (special == playerToEvaluate && player != playerToEvaluate))) {
            if(playerToEvaluate == player) {
                evalBase = cornerSideHeuristicInversion(modell, player, curr, special);
                evalBase = evalBase + MinCornerSide(modell, x, y, special, curr, player, gamephase, special, evalBase);
            }
            else{
                evalBase = cornerSideHeuristicInversion(modell, player, curr, player);
                evalBase = evalBase + MaxCornerSide(modell, x, y, special, curr, player, gamephase, player, evalBase) + values[x][y];

            }
            if(evalBase < Integer.MIN_VALUE/2) {
                return Integer.MIN_VALUE;
            }
        }else if(owner == 13) {
            //evalBase = cornerSideHeuristic(modell, (playerToEvaluate )% modell.players + 1, curr);
            if(player == playerToEvaluate){
                evalBase = evalBase + MaxCornerSide(modell, x, y, special, curr, player, gamephase, player, evalBase) + values[x][y];
            }
            else {
                evalBase = evalBase + MinCornerSide(modell, x, y, special, curr, player, gamephase, playerToEvaluate, evalBase);
            }
            if(evalBase < Integer.MIN_VALUE/2) {
                return Integer.MIN_VALUE;
            }
        }else if (playerToEvaluate == player) {
            if(owner == 14) {
                if(curr.getOverwrites(player) <= 0) {
                    return Integer.MIN_VALUE;
                }
                int captured = MaxCornerSide(modell, x, y, special, curr, player, gamephase, playerToEvaluate, evalBase);
                if(captured < Integer.MIN_VALUE/2) {
                    return evalBase + values[x][y]-100;
                }
                return evalBase+captured + values[x][y]-100;
            }
            else if(owner >0 && owner < 9){
                if(curr.getOverwrites(player) <= 0) {
                    return Integer.MIN_VALUE;
                }
                else{
                    evalBase += + MaxCornerSide(modell, x, y, special, curr, player, gamephase, playerToEvaluate, evalBase);
                    if(evalBase < Integer.MIN_VALUE/2) {
                        return Integer.MIN_VALUE;
                    }
                    else{
                        return evalBase - 100  + ((owner == playerToEvaluate) ? 0 : values[x][y]);
                    }
                }
            }
            if(owner == 12 && special == 21){
                evalBase += 100;
            }
            evalBase += + MaxCornerSide(modell, x, y, special, curr, player, gamephase, playerToEvaluate, evalBase)  + values[x][y];
            if(evalBase < Integer.MIN_VALUE/2) {
                return Integer.MIN_VALUE;
            }
        } else {
            if(owner == 14) {
                if(curr.getOverwrites(player) <= 0) {
                    return Integer.MIN_VALUE;
                }
                int captured = MinCornerSide(modell, x, y, special, curr, player, gamephase, playerToEvaluate, evalBase);
                if(captured < Integer.MIN_VALUE/2) {
                    return evalBase;
                }
                return evalBase+captured;

            }
            else if(owner >0 && owner < 9){
                if(curr.getOverwrites(player) <= 0) {
                    return Integer.MIN_VALUE;
                }
                else if(owner == playerToEvaluate){
                    evalBase -= values[x][y];
                }
            }
            evalBase += MinCornerSide(modell, x, y, special, curr, player, gamephase, playerToEvaluate, evalBase);
            if(evalBase < Integer.MIN_VALUE/2) {
                return Integer.MIN_VALUE;
            }
        }
        return evalBase;
    }

    public int MinCornerSide(MapModel modell, int x, int y, int special, gameState curr, int player, int gamephase,
            int playerToEvaluate, double evalBase) {
        boolean capture = false;
        int temp = 0;
        int eval = 0;
        for (int i = 0; i < 8; i++) {
            temp = tileWalkLoss(x * 100 + y, i, player, curr,modell,playerToEvaluate);
            if (temp > 0) {
                        // the move is valid
                        capture = true;
                        eval = (eval - temp) + 1;
                    }
                }
                if (capture) {
                    return eval;
                }else {

            return Integer.MIN_VALUE;
        }
    }

    public int MaxCornerSide(MapModel modell, int x, int y, int special, gameState curr, int player, int gamephase,
            int playerToEvaluate, double evalBase) {
        boolean captured = false;
        int eval = 0;
        for (int i = 0; i < 8; i++) {
            eval += tileWalkWin(x * 100 + y, i, player, curr,modell);
        }
        if (eval > 0) {
            eval = eval%1000000;
            return (eval > 500000 ? eval-1000000 : eval);
        } else {

            return Integer.MIN_VALUE;
        }
    }

    public int tileWalkWin(int tileNumber, int direction, int owner, gameState base, MapModel modell) {
        // is the next tile a valid tile?
        if (modell.neighbourArrays[tileNumber / 100][tileNumber % 100][direction] == -1) {
            return 0;
        }
        int length = 0;
        int start = tileNumber;
        // skip first stone, change direction accordingly
        tileNumber = modell.neighbourArrays[tileNumber / 100][tileNumber % 100][direction];
        direction = tileNumber / 10000;
        tileNumber = tileNumber % 10000;
        // we need to skip at least one tile
        while (modell.isWalkable(tileNumber, owner, base)) {
            // the tile can be captured, added 1 to path length
            length += values[tileNumber / 100][tileNumber % 100] + 1000000;
            // check if next tile is a valid tile, avoid loops by checking if starttile is
            // revisited
            tileNumber = modell.neighbourArrays[tileNumber / 100][tileNumber % 100][direction];
            if ((tileNumber) == -1 || tileNumber % 10000 == start) {
                return 0;
            }
            // move to next tile, change direction accordingly
            direction = tileNumber / 10000;
            tileNumber = tileNumber % 10000;
        }
        // if the path end on our own tile and at least one capturable tile was skipped,
        // the amount of captured stones is returned
        if (base.getOwner(tileNumber) == owner && length != 0) {
            return length;
        }
        // does not capture any stones
        return 0;
    }

    public int tileWalkLoss(int tileNumber, int direction, int owner, gameState base, MapModel modell, int enemy) {
        // is the next tile a valid tile?
        if (modell.neighbourArrays[tileNumber / 100][tileNumber % 100][direction] == -1) {
            return 0;
        }
        int length = 0;
        int start = tileNumber;
        // skip first stone, change direction accordingly
        tileNumber = modell.neighbourArrays[tileNumber / 100][tileNumber % 100][direction];
        direction = tileNumber / 10000;
        tileNumber = tileNumber % 10000;
        // we need to skip at least one tile
        boolean captured = false;
        while (modell.isWalkable(tileNumber, owner, base)) {
            captured = true;
            // the tile can be captured, added 1 to path length
            if (base.getOwner(tileNumber) == enemy) {
                length += values[tileNumber / 100][tileNumber % 100];
            }
            // check if next tile is a valid tile, avoid loops by checking if starttile is
            // revisited
            tileNumber = modell.neighbourArrays[tileNumber / 100][tileNumber % 100][direction];
            if ((tileNumber) == -1 || tileNumber % 10000 == start) {
                return 0;
            }
            // move to next tile, change direction accordingly
            direction = tileNumber / 10000;
            tileNumber = tileNumber % 10000;
        }
        // if the path end on our own tile and at least one capturable tile was skipped,
        // the amount of captured stones is returned
        if (base.getOwner(tileNumber) == owner && captured) {
            return length + 1;
        }
        // does not capture any stones
        return 0;
    }
}