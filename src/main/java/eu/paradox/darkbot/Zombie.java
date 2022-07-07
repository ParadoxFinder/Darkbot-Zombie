package eu.paradox.darkbot;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.types.Num;
import com.github.manolo8.darkbot.config.types.Option;
import com.github.manolo8.darkbot.core.entities.Player;
import com.github.manolo8.darkbot.core.itf.Configurable;
import com.github.manolo8.darkbot.core.itf.Module;
import com.github.manolo8.darkbot.core.utils.Location;
import com.github.manolo8.darkbot.extensions.features.Feature;
import com.github.manolo8.darkbot.modules.MapModule;

@Feature(name = "Zombie", description = "Makes the ship go to specific positions to kill or be killed.")
public class Zombie implements Module, Configurable<Zombie.Config> {
    /**
     * Configuration for the Zombie plugin.
     */
    public static class Config {
        @Option(value = "Coordinate X", description = "X position to travel to.")
        @Num(min = 0, max = 999)
        public int COORDINATE_X;

        @Option(value = "Coordinate Y", description = "Y position to travel to.")
        @Num(min = 0, max = 999)
        public int COORDINATE_Y;

        @Option(value = "Fire At Players", description = "If checked, the bot will attempt to target and fire at players.")
        public boolean FIRE_AT_PLAYERS;

        @Option(value = "Target Players Regex", description = "Regular expression to filter players. If empty, all players will be targetted.")
        public String TARGET_PLAYERS_REGEX;
    }

    /**
     * Main character of the bot.
     */
    private Main main;

    /**
     * Configuration of the module.
     */
    private Config config;

    /**
     * Installs the module.
     *
     * @param main Main character of the bot.
     */
    @Override
    public void install(Main main) {
        this.main = main;
    }

    /**
     * Returns whether the bot can refresh (reinstall).
     *
     * @return Whether the bot can refresh (reinstall).
     */
    @Override
    public boolean canRefresh() {
        return true;
    }

    /**
     * Sets the configuration of the module.
     *
     * @param config New configuration of the module.
     */
    @Override
    public void setConfig(Config config) {
        this.config = config;
    }

    /**
     * Runs a single tick of the module.
     */
    @Override
    public void tick() {
        if (checkMap()) {
            // Move the player.
            Location targetPosition = new Location(this.config.COORDINATE_X * 100d, this.config.COORDINATE_Y * 100d);
            if (this.main.hero.drive.getCurrentLocation().distance(targetPosition) > 500 || !this.main.hero.drive.isMoving()) {
                this.main.hero.drive.move(targetPosition);
            }

            // Fire at a player.
            if (this.config.FIRE_AT_PLAYERS && (this.main.hero.getTarget() == null || !this.main.hero.isAttacking())) {
                for (Player player : this.main.mapManager.entities.players) {
                    if (!this.config.TARGET_PLAYERS_REGEX.equals("") && !player.playerInfo.getUsername().matches(this.config.TARGET_PLAYERS_REGEX)) continue;
                    player.trySelect(true);
                    break;
                }
            }
        }
    }

    /**
     * Returns the status of the bot.
     *
     * @return The status of the bot.
     */
    @Override
    public String status() {
        return "Zombie Target: " + config.COORDINATE_X + "," + config.COORDINATE_Y;
    }

    /**
     * Checks if the map is on the working map. Instructs the bot to travel to the working map.
     *
     * @return Whether the map is correct.
     */
    protected boolean checkMap() {
        if (this.main.config.GENERAL.WORKING_MAP != this.main.hero.map.id && !main.mapManager.entities.portals.isEmpty()) {
            this.main.setModule(new MapModule()).setTarget(this.main.starManager.byId(this.main.config.GENERAL.WORKING_MAP));
            return false;
        }
        return true;
    }
}
