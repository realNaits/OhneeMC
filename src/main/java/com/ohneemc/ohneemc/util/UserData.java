package com.ohneemc.ohneemc.util;

import com.ohneemc.ohneemc.OhneeMC;
import com.ohneemc.ohneemc.helpers.MessageHelper;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.logging.Level;

public class UserData {

    public static FileConfiguration users;
    private static File userFile;

    //<editor-fold desc="Home Section">

    /**
     * Is player afk or not
     *
     * @param player Player who sets their home.
     * @param name   Home name
     */
    public static boolean setHome(Player player, String[] name) {
        if (name.length == 1) {
            users = loadPlayerFile(player);

            if (users != null) {
                //Getting player group and their max allowed homes.
                String playerGroup = Vault.getGroup(player);

                if (Maps.getMaxHomes().get(playerGroup) == null){
                    OhneeMC.instance.getLogger().log(Level.SEVERE, "There are no home count defined for your group " + playerGroup + ", contact your administrator.");
                    MessageHelper.sendMessage(player,ChatColor.RED + "There are no home count defined for your group " + playerGroup + ", contact your administrator.");
                    return false;
                }

                int maxPlayerHomes = Maps.getMaxHomes().get(playerGroup);

                if (getHomeCount(player) == maxPlayerHomes){
                    MessageHelper.sendMessage(player, ChatColor.GREEN + "You've reached your max allowed homes. Count: " + maxPlayerHomes);
                    MessageHelper.sendMessage(player, ChatColor.GREEN + "Please delete one of your homes before setting a new one.");
                    return true;
                }

                Location location = getPlayerLocation(player);

                World world = location.getWorld();
                if (world == null) {
                    return false;
                }
                String worldName = world.getName();

                double x = location.getX();
                double y = location.getY();
                double z = location.getZ();
                float yaw = location.getYaw();
                float pitch = location.getPitch();

                String homeName = name[0].toLowerCase();

                if (users.contains("homes." + homeName)) {
                    MessageHelper.sendMessage(player, ChatColor.GREEN + "You already have a home with that name..");
                    return true;
                } else {
                    users.set("homes." + homeName + ".x", x);
                    users.set("homes." + homeName + ".y", y);
                    users.set("homes." + homeName + ".z", z);
                    users.set("homes." + homeName + ".yaw", yaw);
                    users.set("homes." + homeName + ".pitch", pitch);
                    users.set("homes." + homeName + ".world", worldName);

                    if (savePlayerFile(player)) {
                        MessageHelper.sendMessage(player, ChatColor.GREEN + "Home " + ChatColor.GOLD + homeName + ChatColor.GREEN + " has now been set!");
                        return true;
                    } else {
                        MessageHelper.sendMessage(player, ChatColor.RED + "Couldn't save your home..");
                        return true;
                    }
                }
            } else {
                return false;
            }
        } else {
            MessageHelper.sendMessage(player, ChatColor.GREEN + "You need to give a name for your new home!");
            return true;
        }
    }

    /**
     * Getting users
     *
     * @param player prints player users.
     */
    public static boolean getHomes(Player player) {
        users = loadPlayerFile(player);
        if (users != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(ChatColor.GREEN + "Homes: ");

            ConfigurationSection section = users.getConfigurationSection("homes");
            if (section != null) {
                for (String i : section.getKeys(false)) {
                    sb.append(ChatColor.GOLD + i);
                    sb.append(ChatColor.GREEN + ", ");
                }
            } else {
                MessageHelper.sendMessage(player, ChatColor.GREEN + "You've no homes yet. Use /sethome <name>");
                return true;
            }

            MessageHelper.sendMessage(player, sb.toString());
            return true;
        } else {
            return false;
        }
    }

    /**
     * Is player afk or not
     *
     * @param player Player to teleport.
     * @param args   Home name
     */
    public static boolean tpHome(Player player, String[] args) {
        users = loadPlayerFile(player);
        if (users != null) {
            String name = args[0].toLowerCase();

            if (!users.contains("homes." + name)) {
                MessageHelper.sendMessage(player, ChatColor.GREEN + "You don't have a home named " + name);
                return true;
            } else {
                String world = users.getString("homes." + name + ".world");
                double x = users.getInt("homes." + name + ".x");
                double y = users.getInt("homes." + name + ".y");
                double z = users.getInt("homes." + name + ".z");
                float yaw = users.getInt("homes." + name + ".yaw");
                float pitch = users.getInt("homes." + name + ".pitch");

                if (world == null) {
                    MessageHelper.sendMessage(player, ChatColor.RED + "Something went wrong while executing home.");
                    return false;
                }

                Location tp = new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
                player.teleport(tp);
                return true;
            }
        } else {
            return false;
        }
    }

    /**
     * Is player afk or not
     *
     * @param player Player who wants to delete their home.
     * @param args   Home name
     */
    public static boolean delHome(Player player, String[] args) {
        users = loadPlayerFile(player);
        if (users != null) {
            String name = args[0].toLowerCase();

            ConfigurationSection section = users.getConfigurationSection("homes");
            if (section != null) {
                if (section.contains(name)) {
                    users.set("homes." + name, null);
                    MessageHelper.sendMessage(player, ChatColor.GREEN + "Home " + ChatColor.GOLD + name + ChatColor.GREEN + " deleted.");

                    try {
                        users.save(OhneeMC.instance.getDataFolder() + "/userdata/" + player.getUniqueId() + ".yml");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    return true;
                } else {
                    MessageHelper.sendMessage(player, ChatColor.GREEN + "Couldn't find the home you want to delete.");
                    return true;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Is player afk or not
     *
     * @param player return players home count.
     */
    public static Integer getHomeCount(Player player) {
        users = loadPlayerFile(player);
        if (users != null) {
            int count = 0;

            ConfigurationSection section = users.getConfigurationSection("homes");
            if (section == null) {
                return 0;
            }
            for (String i : section.getKeys(false)) {
                count++;
            }

            return count;
        } else {
            return 0;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Gamemode">
    //GameMode

    /**
     * Is player afk or not
     *
     * @param player Returns their gamemode
     */
    public static GameMode getGamemode(Player player) {
        return GameMode.valueOf(Config.getString("gamemode"));
    }

    /**
     * Is player afk or not
     *
     * @param player   Which player to set gamemode for.
     * @param gameMode What gamemode you want to set.
     */
    public static void setGamemode(Player player, GameMode gameMode) {
        users = loadPlayerFile(player);
        player.setGameMode(gameMode);
        MessageHelper.sendMessage(player, ChatColor.GREEN + "Your gamemode changed to " + ChatColor.GOLD + gameMode.toString());
        if (users == null){
            return;
        }
        users.set("gamemode", gameMode.toString());
        savePlayerFile(player);
    }
    //</editor-fold>

    public static void setGlow(Player player, boolean glow) { player.setGlowing(glow); }

    //<editor-fold desc="Afk">

    /**
     * Is player afk or not
     *
     * @param player returns true or false if player is afk or not
     */
    public static boolean getAfk(Player player) {
        return Maps.isAfk.get(player.getUniqueId());
    }

    /**
     * Is player afk or not
     *
     * @param player Player you want to put afk.
     * @param afk    Set afk, true or false.
     */
    public static void setAfk(Player player, Boolean afk) {
        Maps.isAfk.put(player.getUniqueId(), afk);
    }
    //</editor-fold>

    //<editor-fold desc="Fly">
    /**
     * Is player afk or not
     *@param player Gets if player have fly enabled or not.
     */
    public static boolean getFly(Player player) {
        users = loadPlayerFile(player);
        if (users == null){
            return false;
        }
        return users.getBoolean("fly");
    }

    /**
     * Is player afk or not
     *
     * @param player Player to set fly for.
     * @param fly   Set fly true or false.
     */
    public static void setFly(Player player, boolean fly) {
        users = loadPlayerFile(player);
        if (users == null){
            return;
        }
        player.setAllowFlight(fly);
        player.setFlying(fly);
        users.set("fly", fly);
        savePlayerFile(player);
    }

    /**
     *
     * @param player who to set it for.
     * @param speed float speed. -1 to +1
     */
    public static void setFlySpeed(Player player, float speed){
        users = loadPlayerFile(player);
        if (users == null){
            return;
        }
        player.setFlySpeed(speed);
        users.set("flyspeed", speed);
        savePlayerFile(player);
    }

    /**
     *
     * @param player who
     * @return float flyspeed
     */
    public static Float getFlySpeed(Player player){
        users = loadPlayerFile(player);
        if (users == null){
            return 0.1f;
        }
        return (float) users.getDouble("flyspeed");
    }
    //</editor-fold>

    //<editor-fold desc="Admin section">
    /**
     *
     * @param player Player to vanish
     * @param enb Set vanish and not toggle
     * @return true on successful change, false if not.
     */
    public static boolean setVanish(Player player, Boolean enb){
        users = loadPlayerFile(player);
        if (users != null){

            //Set on login if enabled when left.
            if (enb != null){
                if (enb){
                    for (Player onlinePlayers : Bukkit.getOnlinePlayers()){
                        onlinePlayers.hidePlayer(OhneeMC.instance, player);
                    }
                    MessageHelper.sendMessage(player, ChatColor.GREEN + "You're now vanished.");
                    return true;
                }else{
                    for (Player onlinePlayers : Bukkit.getOnlinePlayers()){
                        onlinePlayers.showPlayer(OhneeMC.instance, player);
                    }
                    return true;
                }
            }

            //Sets vanished on command.
            boolean vanished = users.getBoolean("vanished");
            if (!vanished){
                for (Player onlinePlayers : Bukkit.getOnlinePlayers()){
                    onlinePlayers.hidePlayer(OhneeMC.instance, player);
                }
                MessageHelper.sendMessage(player, ChatColor.GREEN + "You're now vanished.");
                users.set("vanished", true);
                savePlayerFile(player);
                return true;
            }else{
                for (Player onlinePlayers : Bukkit.getOnlinePlayers()){
                    onlinePlayers.showPlayer(OhneeMC.instance, player);
                }
                MessageHelper.sendMessage(player, ChatColor.GREEN + "You're no longer vanished.");
                users.set("vanished", false);
                savePlayerFile(player);
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param player Player to check
     * @return true if vanished, false if not.
     */
    public static boolean isVanished(Player player){
        users = loadPlayerFile(player);
        if (users != null){
            return users.getBoolean("vanished");
        }
        return false;
    }

    public static Inventory getPlayerInventory(Player player){
        return player.getInventory();
    }

    public static boolean getImported(Player player) {
        users = loadPlayerFile(player);
        if (users != null) {
            return users.getBoolean("imported");
        }
        return false;
    }

    public static void setImported(Player player, Boolean set) {
        users = loadPlayerFile(player);
        if (users != null) {
            users.set("imported", set);
        }
        savePlayerFile(player);
    }
    //</editor-fold>

    public static String getPlaytime(Player player){
        users = loadPlayerFile(player);
        if (users != null){
            long firstJoined = users.getLong("Timestamps.firstJoined");
            long currentTime = System.currentTimeMillis();

            SimpleDateFormat format = new SimpleDateFormat("dd mm ss");
            //Date d1 = format.parse(String.valueOf(firstJoined));
        }

        return "";
    }

    public static String getGroup(Player player){
        if (player == null){
            return null;
        }

        return OhneeMC.perms.getPrimaryGroup(player);
    }

    //<editor-fold desc="File section">
    public static Location getPlayerLocation(Player player) {
        return player.getLocation();
    }

    public static FileConfiguration loadPlayerFile(Player player) {
        String path = OhneeMC.instance.getDataFolder() + "/userdata/" + player.getUniqueId() + ".yml";
        userFile = new File(path);

        users = YamlConfiguration.loadConfiguration(userFile);

        try {
            users.load(userFile);
            return users;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean savePlayerFile(Player player) {
        String path = OhneeMC.instance.getDataFolder() + "/userdata/" + player.getUniqueId() + ".yml";
        userFile = new File(path);

        //users = YamlConfiguration.loadConfiguration(userFile);

        try {
            users.save(userFile);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    //</editor-fold>
}
