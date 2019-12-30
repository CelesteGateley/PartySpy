package xyz.fluxinc.partyspy;

import com.gmail.nossr50.api.PartyAPI;
import com.gmail.nossr50.events.chat.McMMOPartyChatEvent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class PartySpy extends JavaPlugin implements Listener, CommandExecutor {

    private YamlConfiguration storage;
    private File storageFile;
    private YamlConfiguration lang;

    @Override
    public void onEnable() {
        // Plugin startup logic
        storage = new YamlConfiguration();
        saveResource("storage.yml", false);
        storageFile = new File(getDataFolder(), "storage.yml");
        try { storage.load(storageFile); }
        catch (InvalidConfigurationException | IOException e) { e.printStackTrace();}

        lang = new YamlConfiguration();
        saveResource("lang.yml", false);

        try { lang.load(new File(getDataFolder(), "lang.yml")); }
        catch (InvalidConfigurationException | IOException e) { e.printStackTrace();}


        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void partyChatEvent(McMMOPartyChatEvent partyChatEvent) {
        Player sender = getServer().getPlayer(partyChatEvent.getSender());
        for (Player player : getServer().getOnlinePlayers()) {
            if (player.hasPermission("mcmmopartyspy.spy")) {
                if (!isToggled(player)) { continue; }
                String partyName = PartyAPI.getPartyName(player);
                if (partyName != null && partyName.equals(partyChatEvent.getParty())) { continue; }
                String message = "" + lang.getString("format");
                message = message.replace("%display%", sender.getDisplayName());
                message = message.replace("%player%", sender.getName());
                message = message.replace("%party%", partyChatEvent.getParty());
                message = message.replace("%message%", partyChatEvent.getMessage());
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));

            }

        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String alias, String[] arguments) {
        if (commandSender instanceof Player) {
            boolean isToggled = toggleSpy((Player) commandSender);
            if (isToggled) {
                commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.getString("prefix") + lang.getString("toggleOn")));
            } else {
                commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.getString("prefix") + lang.getString("toggleOff")));
            }
        }
        return true;
    }

    private boolean isToggled(Player player) {
        Object key = storage.get(String.valueOf(player.getUniqueId()));
        if (key == null) { return true; }
        return storage.getBoolean(String.valueOf(player.getUniqueId()));
    }

    private boolean toggleSpy(Player player) {
        boolean isToggled = isToggled(player);
        storage.set("" + player.getUniqueId(), !isToggled);
        try { storage.save(storageFile); } catch (IOException e) { e.printStackTrace(); }
        return !isToggled;
    }
}
