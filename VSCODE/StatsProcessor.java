import java.util.*;
import java.util.stream.*;

public class StatsProcessor {
    // Top N jugadores con más victorias
    public static List<String> topPlayers(List<MatchResult> results, int n) {
        return results.stream()
            .collect(Collectors.groupingBy(MatchResult::getWinner, Collectors.counting()))
            .entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(n)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    // Promedio de daño por partida
    public static double averageDamage(List<MatchResult> results) {
        return results.stream()
            .mapToInt(MatchResult::getTotalDamage)
            .average()
            .orElse(0.0);
    }

    // Filtrar partidas por duración mínima
    public static List<MatchResult> filterByDuration(List<MatchResult> results, int minDuration) {
        return results.stream()
            .filter(r -> r.getDuration() >= minDuration)
            .collect(Collectors.toList());
    }

    // Ranking general de jugadores (victorias)
    public static Map<String, Long> playerRanking(List<MatchResult> results) {
        return results.parallelStream()
            .collect(Collectors.groupingBy(MatchResult::getWinner, Collectors.counting()));
    }
}
