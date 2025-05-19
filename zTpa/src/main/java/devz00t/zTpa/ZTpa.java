package devz00t.zTpa;

import devz00t.zTpa.Commands.TPAAcceptCommand;
import devz00t.zTpa.Commands.TPACommand;
import devz00t.zTpa.Commands.TPAHereCommand;
import devz00t.zTpa.Listener.TPAListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class ZTpa extends JavaPlugin {

    @Override
    public void onEnable() {
        getCommand("tpa").setExecutor(new TPACommand());
        getCommand("tpaccept").setExecutor(new TPAAcceptCommand());
        getCommand("tpahere").setExecutor(new TPAHereCommand());
        getServer().getPluginManager().registerEvents(new TPAListener(), this);
        saveDefaultConfig();

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
