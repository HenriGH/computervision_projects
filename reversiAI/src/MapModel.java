import java.util.ArrayList;

/**
 * Class Providing several useful functions for gameStates and
 * managing the current gameState
 */
public class MapModel {
    // Array saving the coordinates of each neighbouring tile for every map tile
    int[][][] neighbourArrays;
    // Here the current gameState is saved
    gameState currPos;

    int temp = 0;
    // additional game info
    int bombStrength;
    int players;
    int height;
    int width;
    int gamePhase;

    /**
     * Initializes a new MapModel object, including a gameState class and the
     * neighbourArrays
     * 
     * @param map
     *            the String represantation of the map
     */
    public MapModel(String map) {

        // read players
        map = readOutInt(map);
        players = temp;
        int[] BombsOverwrites = new int[2 * players];
        // read overwrites
        map = readOutInt(map);
        // Fill overwrites array
        for (int i = 0; i < players; i++) {
            BombsOverwrites[players + i] = temp;
        }

        // read bombs
        map = readOutInt(map);
        for (int i = 0; i < players; i++) {
            // bombs[i] = temp;
            // hashmap.put(-8 - i, temp);
            BombsOverwrites[i] = temp;
        }
        // read bombStrength
        map = readOutInt(map);
        bombStrength = temp;
        // read heigth
        map = readOutInt(map);
        height = temp;
        // read Width
        map = readOutInt(map);
        width = temp;
        // read out the tiles
        boolean hold = false;
        neighbourArrays = new int[width][height][8];
        byte[][] ownerArray = new byte[width][height];
        for (int a = 0; a < height; a++) {
            for (int i = 0; i < width; i++) {
                hold = false;
                // catch all the special tiles
                if (map.substring(0, 1).equals("-")) {
                    // hole
                    ownerArray[i][a] = (byte) 10;
                    hold = true;
                }
                if (map.substring(0, 1).equals("c")) {
                    // change
                    ownerArray[i][a] = (byte) 11;
                    hold = true;
                }
                if (map.substring(0, 1).equals("b")) {
                    // bonus
                    ownerArray[i][a] = (byte) 12;
                    hold = true;
                }
                if (map.substring(0, 1).equals("i")) {
                    // inversion
                    ownerArray[i][a] = (byte) 13;
                    hold = true;
                }
                if (map.substring(0, 1).equals("x")) {
                    // expansion
                    ownerArray[i][a] = (byte) 14;
                    hold = true;
                }
                if (!hold) {
                    // normal
                    ownerArray[i][a] = (byte) Integer.parseInt(map.substring(0, 1));
                }
                map = map.substring(2);

                // fill the neighbour array for this tile
            }
        }
                for (int a = 0; a < height; a++) {
            for (int i = 0; i < width; i++) {
                for (int r = 0; r < 8; r++) {
                    neighbourArrays[i][a][r] = -1;
                }
                if (a > 0 && ownerArray[i][a - 1] != 10) {
                    neighbourArrays[i][a][0] = 10000 * (0) + i * 100 + a - 1;
                }
                if (a > 0 && i < width - 1 && ownerArray[i + 1][a - 1] != 10) {
                    neighbourArrays[i][a][1] = 10000 * (1) + (i + 1) * 100 + a - 1;
                }
                if (i < width - 1 && ownerArray[i + 1][a] != 10) {
                    neighbourArrays[i][a][2] = 10000 * (2) + (i + 1) * 100 + a;
                }
                if (i < width - 1 && a < height - 1 && ownerArray[i + 1][a + 1] != 10) {
                    neighbourArrays[i][a][3] = 10000 * (3) + (i + 1) * 100 + a + 1;
                }
                if (a < height - 1 && ownerArray[i][a + 1] != 10) {
                    neighbourArrays[i][a][4] = 10000 * (4) + i * 100 + a + 1;
                }
                if (i > 0 && a < height - 1 && ownerArray[i - 1][a + 1] != 10) {
                    neighbourArrays[i][a][5] = 10000 * (5) + (i - 1) * 100 + a + 1;
                }
                if (i > 0 && ownerArray[i - 1][a] != 10) {
                    neighbourArrays[i][a][6] = 10000 * (6) + (i - 1) * 100 + a;
                }
                if (i > 0 && a > 0 && ownerArray[i - 1][a - 1] != 10) {
                    neighbourArrays[i][a][7] = 10000 * (7) + (i - 1) * 100 + a - 1;
                }
            }
        }
        // Write the special transitions in the neighbour arrays
        while (map.length() > 2) {
            int[] transition = new int[6];
            for (int i = 0; i < 6; i++) {
                map = readOutInt(map);
                transition[i] = temp;
                if (i == 2) {
                    map = map.substring(4);
                }
            }
            // Neighbour arrays use following encoding: first to digits of int represent y
            // coordinates, next two the x coordinates and fifth digit represent the
            // direction the path must leave the tile
            neighbourArrays[transition[0]][transition[1]][transition[2]] = 10000 * ((transition[5] + 4) % 8)
                    + transition[3] * 100 + transition[4];
            neighbourArrays[transition[3]][transition[4]][transition[5]] = 10000 * ((transition[2] + 4) % 8)
                    + transition[0] * 100 + transition[1];

        }
        // game starts in build phase
        gamePhase = 1;

        // creation of gameState class
        currPos = new gameState(ownerArray, BombsOverwrites);

    }

    /**
     * Creates a new gameState conatining the games postion after a given move is
     * played in the passed gameState.
     * 
     * @param x
     *            the moves x coordinates
     * @param y
     *            the moves y coordinates
     * @param playerNumber
     *            the player playing the move
     * @param special
     *            the moves special number
     * @param curr
     *            the gameState the move should be played in
     * @param phase
     *            the game phase
     * @return a new gameState object
     */
    public gameState calculatePos(int x, int y, int playerNumber, int special,
            gameState curr, int phase) {
        gameState next = curr.duplicate();
        executeMoveHelper(x, y, playerNumber, special, next, phase);
        return next;

    }

    /**
     * Creates a new gameState conatining the games postion after a given move is
     * played in the passed gameState, or returns null if the move is not possible.
     * 
     * @param x
     *            the moves x coordinates
     * @param y
     *            the moves y coordinates
     * @param playerNumber
     *            the player playing the move
     * @param special
     *            the moves special number
     * @param curr
     *            the gameState the move should be played in
     * @param phase
     *            the game phase
     * @return a new gameState object
     */
    public gameState executeIfPossible(int x, int y, int playerNumber, int special, gameState curr, int phase) {

        int owner = curr.getOwner(x, y);
        // Checking game phase
        if (phase == 1) {
            boolean captured = false;
            if ((owner < 9) && (owner > 0)) {
                // tries to use overwrite stone
                if ((curr.getOverwrites(playerNumber) <= 0)) {
                    return null;
                }

            }
            if (owner == 14) {
                // places stone on expansion tile
                if (curr.getOverwrites(playerNumber) <= 0) {
                    return null;
                } else {
                    // Implementation of expansion rule
                    captured = true;
                }
            }
            int[] lengths = new int[8];
            int koords = x * 100 + y;
            // testing how many stones can be captured in each direction
            for (int i = 0; i < 8; i++) {
                lengths[i] = tileWalk(koords, i, playerNumber, curr);
                if (lengths[i] > 0) {
                    // At least one stone is captured, move is legal
                    captured = true;
                }
            }
            if (captured) {
                // executing legal move
                gameState pos = curr.duplicate();
                pos.setOwner(x, y, playerNumber);
                // working through all special tiles
                for (int i = 0; i < 8; i++) {
                    tileWalkSwap(koords, i, playerNumber, pos, lengths[i]);
                }
                switch (owner) {
                    case 0 -> {
                    }
                    case 11 -> {
                        swapStones(playerNumber, special, pos);
                    }
                    case 12 -> {

                        if (special == 20) {
                            pos.increaseBombs(playerNumber);
                        } else {
                            pos.increaseOverwrites(playerNumber);
                        }
                    }
                    case 13 -> {
                        invertStones(pos);
                    }
                    case 14 -> {

                        pos.decreaseOverwrites(playerNumber);

                    }
                    default -> {
                        pos.decreaseOverwrites(playerNumber);
                    }
                }
                return pos;
            } else {
                // move is not legal, return null
                return null;
            }
        } else if ((curr.getBombs(playerNumber) > 0) && (owner != 10)) {
            // its elimination phase
            gameState pos = curr.duplicate();
            pos.decreaseBombs(playerNumber);
            fireBomb(x, y, pos);
            return pos;
        } else {
            // Illegal game phase or no bombs
            return null;
        }
    }

    /**
     * Executes given move on the current gameState
     * 
     * @param x
     *            the moves x coordinates
     * @param y
     *            the moves y coordinates
     * @param playerNumber
     *            the player playing the move
     * @param special
     *            the moves special number
     * @param phase
     *            the game phase
     */
    public void executeMove(int x, int y, int playerNumber, int special, int phase) {
        executeMoveHelper(x, y, playerNumber, special, currPos, phase);
    }

    /**
     * Executes a move on a given gameState, which is directly changed
     * Assumes the given move is legal.
     * 
     * @param x
     *            the moves x coordinates
     * @param y
     *            the moves y coordinates
     * @param playerNumber
     *            the player playing the move
     * @param special
     *            the moves special number
     * @param toChange
     *            the gameState the move should be played in
     * @param phase
     *            the game phase
     */
    public void executeMoveHelper(int x, int y, int playerNumber, int special, gameState toChange, int phase) {
        if (phase == 1) {
            // build phase
            int owner = toChange.getOwner(x, y);
            toChange.setOwner(x, y, playerNumber);
            // working through all special tiles
            switch (owner) {
                case 0 ->

                        this.replaceStones(x, y, playerNumber, toChange);

                case 11 -> {

                    this.replaceStones(x, y, playerNumber, toChange);
                    this.swapStones(playerNumber, special, toChange);
                }
                case 12 -> {

                    this.replaceStones(x, y, playerNumber, toChange);
                    if (special == 20) {
                        toChange.increaseBombs(playerNumber);
                    } else {
                        toChange.increaseOverwrites(playerNumber);
                    }
                }
                case 13 -> {
                    this.replaceStones(x, y, playerNumber, toChange);
                    this.invertStones(toChange);
                }
                case 14 -> {

                    toChange.decreaseOverwrites(playerNumber);
                    this.replaceStones(x, y, playerNumber, toChange);
                }
                default -> {
                    toChange.decreaseOverwrites(playerNumber);
                    this.replaceStones(x, y, playerNumber, toChange);
                }
            }
        } else {
            // its elimination phase
            toChange.decreaseBombs(playerNumber);
            fireBomb(x, y, toChange);
        }
    }

    /**
     * places a bomb on a given tile, all tile reachable in |bomb strength| steps
     * are set to be holes
     * 
     * @param x
     *            x coordinates of the bomb
     * @param y
     *            y coordinates of the bomb
     * @param toChange
     *            gameState in which th bomb is placed
     */
    void fireBomb(int x, int y, gameState toChange) {
        // Arraylists used to store the tile reached in n and n+1 steps
        ArrayList<Integer> currentTiles = new ArrayList<>();
        ArrayList<Integer> nextTiles = new ArrayList<>();
        // add starting tile
        nextTiles.add((x * 100 + y));
        int candidate = 0;
        // set starting tile to hole
        toChange.setOwner(x, y, 10);
        for (int i = 0; i < bombStrength; i++) {
            // preparing for next iteration
            currentTiles = nextTiles;
            nextTiles = new ArrayList<>();
            // iterating over all tiles reached in i steps
            for (int tile : currentTiles) {
                // in all directions
                for (int dir = 0; dir < 8; dir++) {
                    candidate = (neighbourArrays[tile / 100][tile % 100][dir]) % 10000;
                    if ((candidate != -1) && toChange.getOwner(candidate) != 10) {
                        // neighbourtile in direction dir is a valid tile, add it to the nextTiles Array
                        // and set it to be a hole
                        nextTiles.add(candidate);
                        toChange.setOwner(candidate, 10);
                    }
                }
            }
        }
    }

    /**
     * Changes all stones owners captured by a move
     * 
     * @param x
     *            x coordinates of the move
     * @param y
     *            y coordinates of the move
     * @param owner
     *            the player playing the move
     * @param toChange
     *            the gameState the move is played in
     */
    void replaceStones(int x, int y, int owner, gameState toChange) {
        int koords = 100 * x + y;
        int[] lengths = new int[8];
        // checking the amounts of captured stones in all directions
        for (int i = 0; i < 8; i++) {
            lengths[i] = tileWalk(koords, i, owner, toChange);

        }
        // replacing the found number of stones in all directions
        for (int i = 0; i < 8; i++) {
            tileWalkSwap(koords, i, owner, toChange, lengths[i]);
        }
    }

    /**
     * Swaps the stones of two given players.
     * 
     * @param a
     *            the first participating player
     * @param b
     *            the second participating player
     * @param toChange
     *            the gameState the players swap in
     */
    void swapStones(int a, int b, gameState toChange) {
        // iterate over whole map
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // switch in case the tile owner is player a or b
                if (toChange.getOwner(x, y) == a) {
                    toChange.setOwner(x, y, b);
                } else if (toChange.getOwner(x, y) == b) {
                    toChange.setOwner(x, y, a);
                }
            }

        }
    }

    /**
     * Performs an inversion on a given gameState.
     * 
     * @param toChange
     *            the gameState the inversion should be performed
     */
    void invertStones(gameState toChange) {
        // switching all players
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // switch in case the tile owner is player a or b
                if (toChange.getOwner(x, y) >0 && toChange.getOwner(x,y)<=players) {
                    toChange.setOwner(x, y, (toChange.getOwner(x, y)) % players + 1);
                }
            }

        }
    }

    /**
     * Reads out an int of a string, the int is stored in the global variable temp,
     * while the rest string is returned
     * 
     * @param a
     *            the string to read
     * @return the string without the first int
     */
    String readOutInt(String a) {
        int rueckgabe = 0;
        while ("1234567890".contains(a.substring(0, 1))) {
            // the first symbol of the string is still a number
            rueckgabe = rueckgabe * 10 + Integer.parseInt(a.substring(0, 1));
            a = a.substring(1);
        }
        // skip to next number
        a = a.substring(1);
        temp = rueckgabe;
        return a;
    }

    /**
     * 
     * @return the current gameState
     */
    public gameState getGameState() {
        return currPos;
    }

    /**
     * Getter for the amout of players
     * 
     * @return the number of players
     */
    public int getPlayers() {
        return players;
    }

    /**
     * Getter for the bomb strength
     * 
     * @return the bomb strength
     */
    public int getBombStrength() {
        return bombStrength;
    }

    /**
     * Checks how many stones in the passed direction would be captured by a stone
     * placed on the passed tile number by the given player.
     * Avoids loops.
     * 
     * @param tileNumber
     *            the tile to start the tile walk
     * @param direction
     *            the direction to walk in
     * @param owner
     *            the player who walks
     * @param base
     *            the gameState to perform the tile walk on
     * @return the recoreded length
     */
    public int tileWalk(int tileNumber, int direction, int owner, gameState base) {
        // is the next tile a valid tile?
        if (neighbourArrays[tileNumber / 100][tileNumber % 100][direction] == -1) {
            return 0;
        }
        int length = 0;
        int start = tileNumber;
        int dirPuffer = direction;
        // skip first stone, change direction accordingly
        direction = neighbourArrays[tileNumber / 100][tileNumber % 100][direction] / 10000;
        tileNumber = neighbourArrays[tileNumber / 100][tileNumber % 100][dirPuffer] % 10000;
        dirPuffer = direction;
        // we need to skip at least one tile
        boolean captured = false;
        while (isWalkable(tileNumber, owner, base)) {
            // the tile can be captured, added 1 to path length
            length++;
            // check if next tile is a valid tile, avoid loops by checking if starttile is
            // revisited
            if (neighbourArrays[tileNumber / 100][tileNumber % 100][direction] == -1
                    || neighbourArrays[tileNumber / 100][tileNumber % 100][direction] % 10000 == start) {
                return 0;
            }
            // move to next tile, change direction accordingly
            direction = neighbourArrays[tileNumber / 100][tileNumber % 100][direction] / 10000;
            captured = true;

            tileNumber = neighbourArrays[tileNumber / 100][tileNumber % 100][dirPuffer] % 10000;

            dirPuffer = direction;
        }
        // if the path end on our own tile and at least one capturable tile was skipped,
        // the amount of captured stones is returned
        if (base.getOwner(tileNumber) == owner && captured) {
            return length;
        }
        // does not capture any stones
        return 0;
    }

    /**
     * This functions counts all stones of the player enemy which would be captured
     * in the given direction by the passed move.
     * 
     * @param tileNumber
     *            the tile to start the tile walk
     * @param direction
     *            the direction to walk in
     * @param owner
     *            the player who walks
     * @param base
     *            the gameState to perform the tile walk on
     * @param enemy
     *            the player number of the enemy
     * @return
     *         the number of captured stones
     */
    public int countCapturedTiles(int tileNumber, int direction, int owner, gameState base, int enemy) {
        // is the next tile a valid tile?
        if (neighbourArrays[tileNumber / 100][tileNumber % 100][direction] == -1) {
            return 0;
        }
        int length = 0;
        int start = tileNumber;
        int dirPuffer = direction;
        // skip first stone, change direction accordingly
        direction = neighbourArrays[tileNumber / 100][tileNumber % 100][direction] / 10000;
        tileNumber = neighbourArrays[tileNumber / 100][tileNumber % 100][dirPuffer] % 10000;
        dirPuffer = direction;
        // we need to skip at least one tile
        boolean captured = false;
        while (isWalkable(tileNumber, owner, base)) {
            // the tile can be captured
            // check if next tile is a valid tile, avoid loops by checking if starttile is
            // revisited

            // is a enemy tile captured?
            if (base.getOwner(tileNumber) == enemy) {
                length++;
            }
            if (neighbourArrays[tileNumber / 100][tileNumber % 100][direction] == -1
                    || neighbourArrays[tileNumber / 100][tileNumber % 100][direction] % 10000 == start) {
                return 0;
            }
            // move to next tile, change direction accordingly
            direction = neighbourArrays[tileNumber / 100][tileNumber % 100][direction] / 10000;
            captured = true;

            tileNumber = neighbourArrays[tileNumber / 100][tileNumber % 100][dirPuffer] % 10000;

            dirPuffer = direction;
        }
        // if the path end on our own tile and at least one capturable tile was skipped,
        // the amount of captured stones is returned
        if (base.getOwner(tileNumber) == owner && captured) {
            // length +1 to signal legal walk
            return length + 1;
        }
        // does not capture any stones
        return 0;
    }

    /**
     * Sets the tile owners of length tiles in the given direction to the specified
     * owner on the passed gameState.
     * Does not check if the tiles are valid/can be captured.
     * 
     * @param tileNumber
     *            the start tile, its owner number is not changed
     * @param direction
     *            the direction to walk in
     * @param owner
     *            the tiles new owner number
     * @param toChange
     *            the gameState to change
     * @param length
     *            number of tiles which owner shall be changed
     */
    public void tileWalkSwap(int tileNumber, int direction, int owner, gameState toChange, int length) {
        int dirPuffer = direction;
        for (int i = 0; i < length; i++) {
            direction = neighbourArrays[tileNumber / 100][tileNumber % 100][direction] / 10000;
            tileNumber = neighbourArrays[tileNumber / 100][tileNumber % 100][dirPuffer] % 10000;
            dirPuffer = direction;
            toChange.setOwner(tileNumber, owner);
        }
    }

    /**
     * Checks if a given tile can be captured by a given player.
     * 
     * @param tileNumber
     *            the tile to be ckecked
     * @param owner
     *            the players number who wants to capture the stone
     * @param base
     *            the gameState to work with
     * @return true if the stone can be captured, else false
     */
    public boolean isWalkable(int tileNumber, int owner, gameState base) {
        // either a tile of a different player, or an expansion Tile
        int pOwner = base.getOwner(tileNumber);
        return (tileNumber != -1 && ((pOwner > 0 && pOwner < 9 && owner != pOwner) || pOwner == 14));
    }

    /**
     * Prints out the given gameState
     * 
     * @param base
     *            a gameState instance
     */
    public void printMap(gameState base) {
        base.printMap();
    }
}
