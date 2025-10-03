    
    

public enum FighterType {
    ARQUERA("Arquera", 120, 20, 30),
    GUERRERO("Guerrero", 150, 15, 25),
    MAGO("Mago", 100, 25, 40);

    private final String displayName;
    private final int hp;
    private final int minAtk;
    private final int maxAtk;

    FighterType(String displayName, int hp, int minAtk, int maxAtk) {
        this.displayName = displayName;
        this.hp = hp;
        this.minAtk = minAtk;
        this.maxAtk = maxAtk;
    }

    public String getDisplayName() { return displayName; }
    public int getHp() { return hp; }
    public int getMinAtk() { return minAtk; }
    public int getMaxAtk() { return maxAtk; }
    
    public static Fighter createFighter(String clase, String name) {
        switch (clase) {
            case "ARQUERA":
                return new Fighter(name, ARQUERA.getHp(), ARQUERA.getMinAtk(), ARQUERA.getMaxAtk(), "ARQUERA");
            case "GUERRERO":
                return new Fighter(name, GUERRERO.getHp(), GUERRERO.getMinAtk(), GUERRERO.getMaxAtk(), "GUERRERO");
            case "MAGO":
                return new Fighter(name, MAGO.getHp(), MAGO.getMinAtk(), MAGO.getMaxAtk(), "MAGO");
            default:
                return null;
        }
    }
    
       
}