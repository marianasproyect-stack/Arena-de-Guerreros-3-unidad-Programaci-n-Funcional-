import java.io.*;
import java.net.*;
import java.util.Random;

public class ClientHandler extends Thread {
    // Tiempo de inicio de la partida para calcular duración
    private long matchStartTime = 0;

    public long getMatchStartTime() {
        return matchStartTime;
    }

    public void setMatchStartTime(long matchStartTime) {
        this.matchStartTime = matchStartTime;
    }
    // Validaciones funcionales extraídas
    private boolean validateFighterCreated() {
        if (fighter == null) {
            sendMessage("ERROR: aún no has creado tu personaje.");
            return false;
        }
        return true;
    }

    private boolean validateFighterAlive() {
        if (fighter != null && !fighter.isAlive()) {
            sendMessage("ERROR: ya has perdido. Solo puedes usar STATUS,EXIT o STATS.");
            return false;
        }
        return true;
    }

    private boolean validateMyTurn() {
        if (!myTurn) {
            sendMessage("NO_ES_TU_TURNO");
            return false;
        }
        return true;
    }
    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;
    private ClientHandler opponent;
    private Fighter fighter;

    private volatile boolean connected = true; // bandera de conexión
    private volatile boolean myTurn = false;   // Control de turno
    private int turnsSinceSpecial = 0;         // contador de turnos desde el último SPECIAL

    public ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.fighter = null; // aún no creado hasta que mande CREATE
    }

    public void setOpponent(ClientHandler opp) {
      this.opponent = opp;
     // Reiniciar el tiempo de inicio cada vez que se empareja un nuevo oponente
     this.matchStartTime = 0;
    }

    // Inicializa el turno al azar entre los dos jugadores
    public void startMatchTurn() {
        if (opponent == null) return;
        boolean first = new java.util.Random().nextBoolean();
        long startTime = System.currentTimeMillis();
        this.matchStartTime = startTime;
        opponent.setMatchStartTime(startTime); // sincronizar tiempo
        if (first) {
            this.myTurn = true;
            this.sendMessage("TU_TURNO");
            opponent.myTurn = false;
            opponent.sendMessage("ESPERA_TURNO");
        } else {
            this.myTurn = false;
            this.sendMessage("ESPERA_TURNO");
            opponent.myTurn = true;
            opponent.sendMessage("TU_TURNO");
        }
    }

    public void sendMessage(String msg) {
        out.println(msg);
    }

    public boolean isConnected() {
        return connected && !socket.isClosed();
    }

    public Fighter getFighter() {
        return this.fighter;
    }

    @Override
    public void run() {
        try {
            String line;
            Random rand = new Random();
            java.util.Map<String, Runnable> commandMap = new java.util.HashMap<>();

            commandMap.put("ATTACK", () -> {
                if (!validateFighterCreated() || !validateFighterAlive() || !validateMyTurn()) return;
                ClientHandler opponent1 = opponent;
                if (opponent1 != null && opponent1.isConnected() && opponent1.fighter != null) {
                    synchronized (opponent1) {
                        boolean evade = rand.nextDouble() < 0.2;
                        boolean crit = rand.nextDouble() < 0.15;
                        int damage = 0;
                        if (evade) {
                            opponent1.sendMessage("EVADE: esquivaste el ataque de " + fighter.getName());
                            sendMessage("EVADE: tu ataque fue esquivado por " + opponent1.fighter.getName());
                            System.out.println("[SERVER] EVADE: " + opponent1.fighter.getName() + " esquivó el ataque de " + fighter.getName());
                        } else {
                            damage = fighter.rollDamage();
                            if (crit) {
                                damage *= 2;
                                opponent1.sendMessage("CRITICO: recibiste " + damage + " de " + fighter.getName());
                                sendMessage("CRITICO: golpeaste a " + opponent1.fighter.getName() + " con " + damage);
                                System.out.println("[SERVER] CRITICO: " + fighter.getName() + " golpeó a " + opponent1.fighter.getName() + " con " + damage);
                            } else {
                                opponent1.sendMessage("DAMAGE: " + damage + " de " + fighter.getName());
                                sendMessage("ATACASTE a " + opponent1.fighter.getName() + " por " + damage);
                                System.out.println("[SERVER] ATAQUE: " + fighter.getName() + " hizo " + damage + " de daño a " + opponent1.fighter.getName());
                            }
                            opponent1.fighter = opponent1.fighter.takeDamage(damage, fighter.getName());
                            if (!opponent1.fighter.isAlive()) {
                                System.out.println("[DEBUG] Registrando resultado: ganador=" + fighter.getName() + ", perdedor=" + opponent1.fighter.getName());
                                sendMessage("YOU_WIN");
                                opponent1.sendMessage("YOU_LOSE");
                                
                                // Registrar resultado de la partida con duración
                                long matchEndTime = System.currentTimeMillis();
                                long durationSeconds = (matchEndTime - matchStartTime) / 1000;
                                GameServer.matchResults.add(new MatchResult(
                                    fighter.getName(),
                                    opponent1.fighter.getName(),
                                    damage,
                                    durationSeconds
                                ));
                                GameServer.printStats();
                            }
                        }
                        myTurn = false;
                        opponent1.myTurn = true;
                        turnsSinceSpecial++;
                        sendMessage("ESPERA_TURNO");
                        opponent1.sendMessage("TU_TURNO");
                    }
                } else {
                    sendMessage("No tienes un oponente disponible.");
                }
            });
            
            // Comando para mostrar estadísticas al jugador
          commandMap.put("STATS", () -> {
              sendMessage("--- ESTADÍSTICAS ---");
              String topPlayers = StatsProcessor.topPlayers(GameServer.matchResults, 3)
                .stream()
                .collect(java.util.stream.Collectors.joining(", "));
              double avg = StatsProcessor.averageDamage(GameServer.matchResults);
              String ranking = StatsProcessor.playerRanking(GameServer.matchResults)
               .entrySet()
               .stream()
               .map(e -> e.getKey() + ": " + e.getValue())
               .collect(java.util.stream.Collectors.joining(", "));
              sendMessage("Top jugadores: " + topPlayers);
              sendMessage("Promedio de daño: " + avg);
              sendMessage("Ranking general: " + ranking);
              sendMessage("Total partidas: " + GameServer.matchResults.size());

             // Mostrar duración de cada partida
              GameServer.matchResults.forEach(result ->
                 sendMessage("Partida: " + result.getWinner() + " vs " + result.getLoser() +
                    

                        " | Duración: " + result.getDuration() + " segundos")
                );
               sendMessage("-------------------");
            });

            commandMap.put("HEAL", () -> {
                if (!validateFighterCreated() || !validateFighterAlive() || !validateMyTurn()) return;
                int amount = 10 + rand.nextInt(11);
                fighter = fighter.heal(amount);
                sendMessage("Te curaste " + amount + " puntos. HP actual: " + fighter.getHp());
                myTurn = false;
                turnsSinceSpecial++;
                if (opponent != null && opponent.fighter != null) {
                    opponent.sendMessage(fighter.getName() + " se curó " + amount + " puntos. HP actual: " + fighter.getHp());
                }
                ClientHandler opponent1 = opponent;
                if (opponent1 != null) {
                    opponent1.myTurn = true;
                    opponent1.sendMessage("TU_TURNO");
                }
                sendMessage("ESPERA_TURNO");
                // Mostrar en consola quién se curó y cuánto
                System.out.println("[SERVER] HEAL: " + fighter.getName() + " se curó " + amount + " puntos. HP actual: " + fighter.getHp());
            });


            commandMap.put("SPECIAL", () -> {
                if (!validateFighterCreated() || !validateFighterAlive() || !validateMyTurn()) return;
                if (turnsSinceSpecial < 3) {
                    sendMessage("SPECIAL_NO_DISPONIBLE: Debes esperar " + (3 - turnsSinceSpecial) + " turnos más.");
                    return;
                }
                ClientHandler opponent1 = opponent;
                if (opponent1 != null && opponent1.isConnected() && opponent1.fighter != null) {
                    synchronized (opponent1) {
                        int damage = fighter.rollSpecialDamage();
                        opponent1.fighter = opponent1.fighter.takeDamage(damage, fighter.getName());
                        String specialMsg = switch (fighter.getClase()) {
                            case "ARQUERA" -> fighter.getName() + " desató una *Lluvia de Flechas*";
                            case "GUERRERO" -> fighter.getName() + " lanzó un *Golpe de Espada Colosal*";
                            case "MAGO" -> fighter.getName() + " invocó una *Explosión Arcana*";
                            default -> fighter.getName() + " usó un ataque especial";
                        };
                        sendMessage("SPECIAL: " + specialMsg + " causando " + damage + " de daño a " + opponent1.fighter.getName());
                        opponent1.sendMessage("SPECIAL: " + specialMsg + " y recibiste " + damage + " de daño");
                        System.out.println("[SERVER] SPECIAL: " + specialMsg + " causando " + damage + " de daño a " + opponent1.fighter.getName());
                        if (!opponent1.fighter.isAlive()) {
                            sendMessage("YOU_WIN");
                            opponent1.sendMessage("YOU_LOSE");
                            
                            // Registrar resultado de la partida con duración
                            long matchEndTime = System.currentTimeMillis();
                            long durationSeconds = (matchEndTime - matchStartTime) / 1000;
                            GameServer.matchResults.add(new MatchResult(
                                fighter.getName(),
                                opponent1.fighter.getName(),
                                damage,
                                durationSeconds
                            ));
                            GameServer.printStats();
                        }
                        turnsSinceSpecial = 0;
                        myTurn = false;
                        opponent1.myTurn = true;
                        sendMessage("ESPERA_TURNO");
                        opponent1.sendMessage("TU_TURNO");
                    }
                } else {
                    sendMessage("No tienes un oponente disponible.");
                }
            });
            commandMap.put("STATUS", () -> {
                if (!validateFighterCreated()) return;
                sendMessage("HP: " + fighter.getHp() + "/" + fighter.getMaxHp());
            });
            commandMap.put("EXIT", () -> {
                sendMessage("Saliendo del juego...");
                connected = false;
                ClientHandler opponent1 = opponent;
                if (opponent1 != null && opponent1.isConnected()) {
                    opponent1.sendMessage("Tu oponente se ha desconectado. Ganaste por abandono.");
                }
            });


            while ((line = in.readLine()) != null) {
                System.out.println("Recibido: " + line);
                if (line.startsWith("JUGADOR:")) {
                    String[] parts = line.split(":");
                    if (parts.length < 3) {
                        sendMessage("ERROR: formato inválido. Usa CREATE:CLASE:NOMBRE");
                        continue;
                    }
                    String clase = parts[1].toUpperCase();
                    String playerName = parts[2];
                    fighter = FighterType.createFighter(clase, playerName);
                    if (fighter == null) {
                        sendMessage("ERROR: clase inválida (" + clase + ")");
                        continue;
                    }
                    sendMessage("WELCOME " + fighter.getName() + " [" + clase + "]");
                    // Si ambos jugadores tienen personaje, iniciar el turno
                    if (opponent != null && opponent.getFighter() != null) {
                        // Solo uno debe iniciar el turno para ambos
                        if (!myTurn && !opponent.myTurn) {
                            startMatchTurn();
                        }
                    }
                    continue;
                }
                Runnable cmd = commandMap.get(line.toUpperCase());
                if (cmd != null) {
                    cmd.run();
                    if (line.equalsIgnoreCase("EXIT")) break;
                } else {
                    sendMessage("UNKNOWN_CMD");
                }
            }
        } catch (IOException e) {
            System.out.println("Error en handler: " + e.getMessage());
        } finally {
            connected = false;
            try { socket.close(); } catch (IOException ignored) {}
            System.out.println("Jugador desconectado: " + (fighter != null ? fighter.getName() : "SinNombre"));
        }
    }
}