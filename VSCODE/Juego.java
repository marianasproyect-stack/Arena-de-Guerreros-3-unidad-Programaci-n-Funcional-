import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Juego {

    // Clase base para cualquier luchador 
    public static class Fighter {
        protected String name;   
        protected int hp;       
        protected int maxHp;    

        // Constructor
        public Fighter(String name, int hp) {
            this.name = name;
            this.hp = hp;
            this.maxHp = hp;
        }

        // Método sincronizado para recibir daño
        public synchronized void takeDamage(int amount, String attacker) {
            if (!isAlive()) return; // Si ya está muerto, no recibe daño

            hp -= amount; 
            if (hp < 0) hp = 0;

            // Crear barra de vida
            StringBuilder bar = new StringBuilder("[");
            int total = 20;
            int filled = (int) Math.round(((double) hp / maxHp) * total);
            for (int i = 0; i < total; i++) {
                bar.append(i < filled ? '=' : ' ');
            }
            bar.append("]");

            // Mostrar el ataque en pantalla
            System.out.println(attacker + " golpea a " + name + " por " + amount + " de daño.");
            System.out.println("-> Vida de " + name + ": " + hp + " " + bar);
        }
         
      
        public synchronized boolean isAlive() {
            return hp > 0;
        }

     
        public synchronized int getHp() {
            return hp;
        }
    }

    // Clase que representa un luchador que ataca en su propio hilo
    public static class Attacker extends Fighter implements Runnable {
        private final Fighter[] targets; // Lista de rivales (el equipo contrario)
        private final int attackPower;   
        private final int attacks;       

        // Constructor
        public Attacker(String name, int hp, Fighter[] targets, int attackPower, int attacks) {
            super(name, hp);
            this.targets = targets;
            this.attackPower = attackPower;
            this.attacks = attacks;
        }

        @Override
        public void run() {  
            Random rand = new Random(); 
            for (int i = 0; i < attacks; i++) {
                if (!isAlive()) {
                    System.out.println(name + " ha caído y no puede seguir atacando.");
                    break;
                }

                // Si ya no hay enemigos vivos, se detiene
                if (!anyAlive(targets)) {
                    System.out.println(name + " ve que ya no hay rivales vivos.");
                    break;
                }

                // Seleccionar un objetivo vivo al azar
                Fighter target = null;
                while (target == null) {
                    Fighter candidate = targets[rand.nextInt(targets.length)];
                    if (candidate.isAlive()) {
                        target = candidate;
                    }
                }
                
                // Atacar al objetivo
                target.takeDamage(attackPower, name);

                try {
                    TimeUnit.MILLISECONDS.sleep(600); // Esperar 0.6s entre ataques
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            System.out.println(name + " terminó sus ataques.");
        }

        //  verifica si al menos uno del equipo rival sigue vivo
        private boolean anyAlive(Fighter[] fighters) {
            for (Fighter f : fighters) {
                if (f.isAlive()) return true;
            }
            return false;
        }
    }

    // Método principal
    public static void main(String[] args) {
        System.out.println("=== Arena 2 vs 2 (Mari Tilina & Mariana VS Kaleth-chan & Rivi) ===");

        // Crear arrays para los equipos
        Fighter[] jugadores = new Fighter[2];
        Fighter[] enemigos = new Fighter[2];

        // Crear luchadores
        Attacker mariTilina = new Attacker("Mari Tilina", 60, enemigos, 12, 8);
        Attacker mariana = new Attacker("Mariana", 80, enemigos, 18, 6);

        Attacker kaleth = new Attacker("Kaleth-chan", 90, jugadores, 10, 7);
        Attacker rivi = new Attacker("Rivi", 110, jugadores, 15, 5);

        // Asignar a los arrays
        jugadores[0] = mariTilina;
        jugadores[1] = mariana;

        enemigos[0] = kaleth;
        enemigos[1] = rivi;

        // Crear hilos para cada luchador
        Thread tMariTilina = new Thread(mariTilina);
        Thread tMariana = new Thread(mariana);
        Thread tKaleth = new Thread(kaleth);
        Thread tRivi = new Thread(rivi);

        // Iniciar combate
        tMariTilina.start();
        tMariana.start();
        tKaleth.start();
        tRivi.start();

        try {
            // Esperar a que todos terminen
            tMariTilina.join();
            tMariana.join();
            tKaleth.join();
            tRivi.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Mostrar resultado final
        System.out.println("\n=== Resultado Final ===");
        System.out.println(mariTilina.name + " HP: " + mariTilina.getHp());
        System.out.println(mariana.name + " HP: " + mariana.getHp());
        System.out.println(kaleth.name + " HP: " + kaleth.getHp());
        System.out.println(rivi.name + " HP: " + rivi.getHp());

        // Contar vivos en cada equipo
        int vivosJugadores = (mariTilina.isAlive() ? 1 : 0) + (mariana.isAlive() ? 1 : 0);
        int vivosEnemigos = (kaleth.isAlive() ? 1 : 0) + (rivi.isAlive() ? 1 : 0);

        // Decidir el ganador
        if (vivosJugadores > 0 && vivosEnemigos == 0) {
            System.out.println("¡Los jugadores ganarón!");
        } else if (vivosEnemigos > 0 && vivosJugadores == 0) {
            System.out.println("¡Los enemigos ganarón!");
        } else if (vivosJugadores > 0 && vivosEnemigos > 0) {
            // Si ambos tienen sobrevivientes, gana el equipo con más HP total
            int totalHPJugadores = mariTilina.getHp() + mariana.getHp();
            int totalHPEnemigos = kaleth.getHp() + rivi.getHp();

            if (totalHPJugadores > totalHPEnemigos) {
                System.out.println("¡Los jugadores ganarón!");
            } else if (totalHPEnemigos > totalHPJugadores) {
                System.out.println("¡Los enemigos ganarón!");
            } else {
                System.out.println("¡Empate ! Ambos bandos quedaron igualados.");
            }
        } else {
            System.out.println("¡Empate!");
        }
    }
}