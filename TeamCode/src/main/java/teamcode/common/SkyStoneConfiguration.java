package teamcode.common;

/**
 * Represents a way in which a pair of SkyStones can be placed. 1 means closest to the wall and 6
 * means farthest from the wall.
 */
public enum SkyStoneConfiguration {

    ONE_FOUR(1, 4), TWO_FIVE(2, 5), THREE_SIX(3, 6);

    private int firstStone;
    private int secondStone;

    private SkyStoneConfiguration(int firstStone, int secondStone) {
        this.firstStone = firstStone;
        this.secondStone = secondStone;
    }

    /**
     * Returns the position of the first stone of this configuration (the one closest to the wall).
     */
    public int getFirstStone() {
        return firstStone;
    }

    /**
     * Returns the position of the second stone of this configuration (the one farthest from the wall).
     */
    public int getSecondStone() {
        return secondStone;
    }

}
