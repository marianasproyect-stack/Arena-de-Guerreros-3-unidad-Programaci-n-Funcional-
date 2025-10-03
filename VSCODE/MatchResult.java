public class MatchResult {
    private final String winner;
    private final String loser;
    private final int totalDamage;
    private final long duration;

    public MatchResult(String winner, String loser, int totalDamage, long duration) {
        this.winner = winner;
        this.loser = loser;
        this.totalDamage = totalDamage;
        this.duration = duration;
    }

    public String getWinner() { return winner; }
    public String getLoser() { return loser; }
    public int getTotalDamage() { return totalDamage; }
    public long getDuration() { return duration; }
}
