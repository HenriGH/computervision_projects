/**
 * Class representing a reversi game position
 */
public class gameState {
    // Store the owners byte in a byte map
    public byte[][] map;
    // The amount of bombs and overwrites of every player
    public int[] bombsOverwrites;
    // total amount of players
    public int players;

    /**
     * Creates a new gameState object using the passed parameters.
     * 
     * @param pMap
     *            Contains a byte array representation of the map
     * @param pBO
     *            Array containing the bombs and overwrites of every player
     */
    public gameState(byte[][] pMap, int[] pBO) {
        bombsOverwrites = pBO;
        map = pMap;
        players = pBO.length / 2 - 1;
    }

    /**
     * Getter method for the game map
     * 
     * @param xxyy
     *            the int encoding of the tile, first two digits represent the y
     *            coordinates, third and fourth digits the x coordinates
     * @return the number representation of the tiles owner
     */
    public int getOwner(int xxyy) {
        return map[xxyy / 100][xxyy % 100];
    }

    /**
     * Getter method for the game map
     * 
     * @param x
     *            the x coordinates of the tile
     * @param y
     *            the y coordinates of the tile
     * @return the number representation of the tiles owner
     */
    public int getOwner(int x, int y) {
        return map[x][y];
    }

    /**
     * Setter method for the game map
     * 
     * @param xxyy
     *            the int encoding of the tile, first two digits represent the y
     *            coordinates, third and fourth digits the x coordinates
     * @param value
     *            the encoding of the owner as int
     */
    public void setOwner(int xxyy, int value) {
        map[xxyy / 100][xxyy % 100] = (byte) value;
    }

    /**
     * Setter method for the game map
     * 
     * @param x
     *            the x coordinates of the tile
     * @param y
     *            the y coordinates of the tile
     * @param value
     *            the encoding of the owner as int
     */
    public void setOwner(int x, int y, int value) {
        map[x][y] = (byte) value;
    }

    /**
     * 
     * @return the byte array representation of the map
     */
    public byte[][] getMap() {
        return map;
    }

    /**
     * 
     * @return the array representations of the current bomb and overwrite scores
     */
    public int[] getBombsOverwrites() {
        return bombsOverwrites;
    }

    /**
     * 
     * @param player
     *            the number of the player
     * @return the amount of bombs of the player
     */
    public int getBombs(int player) {
        return bombsOverwrites[player - 1];
    }

    /**
     * Sets a certain players bomb score to a certain number
     * 
     * @param player
     *            the number of the player
     * @param value
     *            the players new amount of bombs
     */
    public void setBombs(int player, int value) {
        bombsOverwrites[player - 1] = value;

    }

    /**
     * 
     * @param player
     *            the number of the player
     * @return the amount of overwrite stones of a player
     */
    public int getOverwrites(int player) {
        return bombsOverwrites[player + players];
    }

    /**
     * Sets a certain players overwrites score to a certain number
     * 
     * @param player
     *            the number of the player
     * @param value
     *            the players new amount of overwrtite stones
     */
    public void setOverwrites(int player, int value) {
        bombsOverwrites[player + players] = value;
    }

    /**
     * Sets all players bombs and overwrites score
     * 
     * @param pBO
     *            array containing the bombs and overwrites amount of every player
     */
    public void setBombsOverwrites(int[] pBO) {
        bombsOverwrites = pBO;
    }

    /**
     * drecreases the number of overwrite stones of a player by one
     * 
     * @param player
     *            the number of the player
     */
    public void decreaseOverwrites(int player) {
        bombsOverwrites[player + players]--;
    }

    /**
     * increases the number of overwrite stones of a player by one
     * 
     * @param player
     */
    public void increaseOverwrites(int player) {
        bombsOverwrites[player + players]++;
    }

    /**
     * drecreases the number of bombs of a player by one
     * 
     * @param player
     *            the number of the player
     */
    public void decreaseBombs(int player) {
        bombsOverwrites[player - 1]--;
    }

    /**
     * increases the number of bombs of a player by one
     * 
     * @param player
     *            the number of the player
     */
    public void increaseBombs(int player) {
        bombsOverwrites[player - 1]++;
    }

    /**
     * Duplicates itself, creates a new gameState object containing an identical map
     * and bomb/overwrite array,
     * side effects are avoided by only copying primitive data types.
     * 
     * @return A new gameState object
     */
    public gameState duplicate() {
        byte[][] owners = new byte[map.length][map[0].length];
        for (int i = 0; i < map.length; i++) {
            owners[i] = map[i].clone();
        }
        return new gameState(owners, bombsOverwrites.clone());
    }

    /**
     * Prints out the current amp in a human readable format
     * 
     */
    public void printMap() {
        // iterating over the whole map array
        for (int y = 0; y < map[0].length; y++) {
            for (int x = 0; x < map.length; x++) {
                // correcting the indentation
                if (map[x][y] < 10) {
                    System.out.print(" ");
                    System.out.print(map[x][y] + " ");
                } else {
                    System.out.print(map[x][y] + " ");
                }
            }
            System.out.println(" ");
        }
    }
}
