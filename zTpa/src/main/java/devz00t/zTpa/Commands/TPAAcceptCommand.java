package devz00t.zTpa.Commands;

import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class TPAAcceptCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) return false;
        TPAHandler.showAcceptGUI(p);
        return true;
    }
}
