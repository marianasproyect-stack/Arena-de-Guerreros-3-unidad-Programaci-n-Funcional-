import java.util.Random;

public class Fighter {
    private final String name;
    private final int hp;
    private final int maxHp;
    private final int minAtk;
    private final int maxAtk;
    private final String type;  // clase del luchador
    private static final Random rand = new Random();

    // Constructor con ataque personalizado + tipo
    public Fighter(String name, int hp, int minAtk, int maxAtk, String type) {
        this(name, hp, hp, minAtk, maxAtk, type);
    }

    // Constructor completo para inmutabilidad ...
    public Fighter(String name, int hp, int maxHp, int minAtk, int maxAtk, String type) {
        this.name = name;
        this.hp = hp;
        this.maxHp = maxHp;
        this.minAtk = minAtk;
        this.maxAtk = maxAtk;
        this.type = type;
    }

    // Daño calculado entre min y max
    public int rollDamage() {
        return minAtk + rand.nextInt(maxAtk - minAtk + 1);
    }

    // Daño especial según tipo de luchador
    public int rollSpecialDamage() {
        int damage = 0;
        switch (type) {
            case "ARQUERA" -> damage = 40 + rand.nextInt(21);   // 40-60
            case "GUERRERO" -> damage = 50 + rand.nextInt(26);  // 50-75
            case "MAGO" -> damage = 60 + rand.nextInt(41);      // 60-100
            default -> damage = 30; // por si acaso
        }
        return damage;
    }


    // Devuelve un nuevo objeto Fighter con el HP actualizado tras recibir daño ...
    public Fighter takeDamage(int amount, String attacker) {
        if (!isAlive()) return this;
        int newHp = hp - amount;
        if (newHp < 0) newHp = 0;
        // System.out.println(attacker + " golpea a " + name + " por " + amount + " de daño.");
        return new Fighter(name, newHp, maxHp, minAtk, maxAtk, type);
    }


    // Devuelve un nuevo objeto Fighter con el HP actualizado tras curarse ...
    public Fighter heal(int amount) {
        if (!isAlive()) return this;
        int newHp = hp + amount;
        if (newHp > maxHp) newHp = maxHp;
        // System.out.println(name + " se curó " + amount + " puntos de vida.");
        return new Fighter(name, newHp, maxHp, minAtk, maxAtk, type);
    }

    public boolean isAlive() {
        return hp > 0;
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    //  alias para usar en ClientHandler
    public String getClase() {
        return type;
    }
}


