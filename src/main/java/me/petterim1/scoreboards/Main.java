package me.petterim1.scoreboards;

import cn.nukkit.Nukkit;
import cn.nukkit.Player;
import cn.nukkit.event.Listener;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;

import com.creeperface.nukkit.placeholderapi.api.PlaceholderAPI;

import de.theamychan.scoreboard.network.Scoreboard;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Main extends PluginBase implements Listener {

    private static final int currentConfig = 3;

    private static Class<?> placeholderAPI;

    static boolean incompatibleJava;

    static String scoreboardTitle;
    static List<String>  scoreboardText;
    static List<String> noScoreboardWorlds;

    static final Map<Player, Scoreboard> scoreboards = new ConcurrentHashMap<>();

    @Override
    public void onEnable() {
        APIDownloader.checkAndRun(this);

        try {
            placeholderAPI = Class.forName("com.creeperface.nukkit.placeholderapi.api.PlaceholderAPI");
        } catch (Exception e) {
            getLogger().critical("Error with PlaceholderAPI" , e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        saveDefaultConfig();
        Config config = getConfig();

        if (config.getInt("version") < currentConfig) {
            getLogger().warning("The config file of SimpleScoreboards plugin is outdated. Please delete the old config.yml file.");
        }

        scoreboardTitle = config.getString("title");
        scoreboardText = config.getStringList("text");
        noScoreboardWorlds = config.getStringList("noScoreboardWorlds");

        try {
            if (Integer.parseInt(System.getProperty("java.version").split("\\.")[0]) > 11) {
                getLogger().warning("The plugin cannot be guaranteed to work on this Java version. For best compatibility and performance, use Java 8 or 11.");
                incompatibleJava = true;
            }
        } catch (Exception e) {
            getLogger().warning("Failed to check Java version. For best compatibility and performance, use Java 8 or 11.");
            if (Nukkit.DEBUG > 1) {
                e.printStackTrace();
            }
        }

        getServer().getPluginManager().registerEvents(new Listeners(), this);

        if (config.getInt("update") > 0) {
            getServer().getScheduler().scheduleDelayedRepeatingTask(this, new ScoreboardUpdater(this), config.getInt("update"), config.getInt("update"), config.getBoolean("async", true));
        } else {
            getLogger().notice("Scoreboard updating is not enabled (update <= 0)");
        }
    }

    static String getScoreboardString(Player p, String text) {
        try {
            return (String) placeholderAPI.getDeclaredMethod("translateString", String.class, Player.class).invoke(PlaceholderAPI.getInstance(), getKDRStats(p, text), p);
        } catch (Exception e) {
            e.printStackTrace();
            return "PlaceholderAPI error!";
        }
    }

    private static String getKDRStats(Player p, String textToReplace) {
        try {
            Class.forName("kdr.Main");

            return textToReplace.replace("%kdr_kdr%", String.format("%.2f", kdr.Main.plugin.getKDR(p)))
                    .replace("%kdr_kills%", String.valueOf(kdr.Main.plugin.getKills(p)))
                    .replace("%kdr_deaths%", String.valueOf(kdr.Main.plugin.getDeaths(p)))
                    .replace("%kdr_topkdr%", String.format("%.2f", kdr.Main.plugin.getTopKDRScore()))
                    .replace("%kdr_topkdrplayer%", kdr.Main.plugin.getTopKDRPlayer())
                    .replace("%kdr_topkills%", String.valueOf(kdr.Main.plugin.getTopKills()))
                    .replace("%kdr_topdeaths%", String.valueOf(kdr.Main.plugin.getTopDeaths()))
                    .replace("%kdr_topkillsplayer%", kdr.Main.plugin.getTopKillsPlayer())
                    .replace("%kdr_topdeathsplayer%", kdr.Main.plugin.getTopDeathsPlayer());
        } catch (Exception e) {
            return textToReplace;
        }
    }
}
