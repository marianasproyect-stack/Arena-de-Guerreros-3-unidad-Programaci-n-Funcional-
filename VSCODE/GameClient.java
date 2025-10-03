
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class GameClient {
    public static void main(String[] args) {
        String host = "localhost"; // cambiar si el server está en otra máquina
        int port = 5000;

        try (Socket socket = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner sc = new Scanner(System.in)) {

            // Hilo lector de mensajes del servidor
            Thread reader = new Thread(() -> {
                try {
                    String s;
                    while ((s = in.readLine()) != null) {
                        System.out.println("[SERVER] " + s);
                    }
                } catch (IOException e) {
                    System.out.println("Conexión cerrada por el servidor.");
                }
            });
            reader.start();

            // Elegir clase del personaje
            System.out.println("Elige tu clase:");
            System.out.println("1. Arquera (120 HP, 20-30 ATK)");
            System.out.println("2. Guerrero (150 HP, 15-25 ATK)");
            System.out.println("3. Mago    (100 HP, 25-40 ATK)");
            System.out.print("Opción(Coloque el número): ");
            int choice = Integer.parseInt(sc.nextLine());

            String type = "";
            switch (choice) {
                case 1 -> type = "ARQUERA";
                case 2 -> type = "GUERRERO";
                case 3 -> type = "MAGO";
                default -> {
                  System.out.println(" Opción inválida, intenta de nuevo.");
                }
            }

            // Nombre del jugador
            System.out.print("Ingresa tu nombre: ");
            String name = sc.nextLine();

            // Mandar al servidor el tipo y nombre
            out.println("JUGADOR:" + type + ":" + name);

            // Bucle principal de comandos
            while (true) {
                System.out.print("Comando (ATTACK/STATUS/HEAL/SPECIAL/STATS/EXIT): ");
                String cmd = sc.nextLine().trim();

                if (cmd.equalsIgnoreCase("EXIT")) {
                    out.println("EXIT");  //  avisamos al servidor
                    break;
                }

                out.println(cmd);
            }

            System.out.println("Saliendo del juego...");

        } catch (IOException e) {
            System.out.println("Error en cliente: " + e.getMessage());
        }
    }
}