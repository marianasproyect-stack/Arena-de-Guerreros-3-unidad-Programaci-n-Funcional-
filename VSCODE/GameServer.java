
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameServer {
    private static final List<ClientHandler> waiting = new ArrayList<>();
    // Lista global de resultados de partidas
    public static final List<MatchResult> matchResults = new CopyOnWriteArrayList<>();

    // Método para mostrar estadísticas en consola
    public static void printStats() {
        System.out.println("\n--- ESTADÍSTICAS DE PARTIDAS ---");
        List<String> top = StatsProcessor.topPlayers(matchResults, 3);
        double avg = StatsProcessor.averageDamage(matchResults);
        Map<String, Long> ranking = StatsProcessor.playerRanking(matchResults);
        System.out.println("Top jugadores: " + top);
        System.out.println("Promedio de daño: " + avg);
        System.out.println("Ranking general: " + ranking);
        System.out.println("Total partidas: " + matchResults.size());
        // Mostrar duración de cada partida
        for (MatchResult result : matchResults) {
            System.out.println("Partida: " + result.getWinner() + " vs " + result.getLoser() +
                " | Duración: " + result.getDuration() + " segundos");
        }
        System.out.println("-------------------------------\n");
    }
    
    public static void main(String[] args) throws IOException {
        int port = 5000;
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Servidor iniciado en puerto " + port);

        try {
            while (true) {
                // Espera un cliente
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nuevo cliente conectado: " + clientSocket.getRemoteSocketAddress());

                // Crea handler
                ClientHandler handler = new ClientHandler(clientSocket);
                handler.start();

                // Sincronizamos acceso a la lista de espera
                synchronized (waiting) {
                    // Eliminamos de la lista los que ya no están conectados
                    waiting.removeIf(h -> !h.isConnected());

                    waiting.add(handler);
                    if (waiting.size() >= 2) {
                        ClientHandler a = waiting.remove(0);
                        ClientHandler b = waiting.remove(0);

                        if (a.isConnected() && b.isConnected()) {
                            a.setOpponent(b);
                            b.setOpponent(a);

                            a.sendMessage("MATCH_START");
                            b.sendMessage("MATCH_START");

                            // Esperar a que ambos jugadores creen su personaje
                            if (a.getFighter() != null && b.getFighter() != null) {
                                // Iniciar turnos y tiempo solo si ambos tienen personaje
                                a.startMatchTurn();
                                b.setMatchStartTime(a.getMatchStartTime()); // sincronizar tiempo
                                System.out.println("Emparejados: " +
                                    a.getFighter().getName() + " vs " +
                                    b.getFighter().getName());
                            } else {
                                System.out.println("Emparejados: esperando creación de personajes...");
                            }
                        }
                    }
                }
            }
        } finally {
            // Mostrar estadísticas solo al final, cuando el servidor se cierre
            printStats();
            serverSocket.close();
        }
    }
}