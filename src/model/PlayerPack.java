package model;

import constants.IConstants;

public class PlayerPack extends Pack {
    private String nickname;
    private int cups;
    private int botsKilled;
    private int viewedZone;
    private Flag flag;

    public PlayerPack(int id, AnimalSpecies species, String nickname) {
        super(id, species);
        this.nickname = nickname;
    }

    /** PLAYER_DAMAGE + cups (sobrescribe). */
    @Override
    public int getDamage() { return IConstants.PLAYER_DAMAGE + cups; }

    @Override
    public boolean isBot() { return false; }

    public void addCup() { cups++; }

    /** Si botsKilled % BOTS_PER_BONUS == 0, heal(1) a todos los vivos. */
    public void addBotKill() {
        botsKilled++;
        if (botsKilled % IConstants.BOTS_PER_BONUS == 0) {
            for (Animal animal : living()) {
                animal.heal(1);
            }
        }
    }

    public void setViewedZone(int zoneId) { this.viewedZone = zoneId; }

    public String getNickname() { return nickname; }

    public int getCups() { return cups; }

    public int getBotsKilled() { return botsKilled; }

    public int getViewedZone() { return viewedZone; }

    public Flag getFlag() { return flag; }

    public void setFlag(Flag flag) { this.flag = flag; }
}
