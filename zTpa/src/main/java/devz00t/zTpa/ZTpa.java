package devz00t.zTpa;


import devz00t.zTpa.Commands.*;
import devz00t.zTpa.Listener.TPAListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class ZTpa extends JavaPlugin {
    private static ZTpa instance;

    @Override
    public void onEnable() {
        instance = this;

        // Commands registrieren
        getCommand("tpa").setExecutor(new TPACommand());
        getCommand("tpaccept").setExecutor(new TPAAcceptCommand());
        getCommand("tpahere").setExecutor(new TPAHereCommand());

        // Listener registrieren
        getServer().getPluginManager().registerEvents(new TPAListener(), this);
    }

    @Override
    public void onDisable() {
        TPAHandler.clearAllRequests();
    }

    public static ZTpa getInstance() {
        return instance;
    }
}

