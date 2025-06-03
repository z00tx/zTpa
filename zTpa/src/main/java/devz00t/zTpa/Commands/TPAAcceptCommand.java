package devz00t.zTpa.Commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TPAAcceptCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;

        UUID senderId = TPAHandler.getRequestSender(player.getUniqueId());
        if (senderId == null) {
            player.sendMessage(ChatColor.RED + "You have no pending teleport requests!");
            return true;
        }

        Player requester = Bukkit.getPlayer(senderId);
        if (requester == null || !requester.isOnline()) {
            player.sendMessage(ChatColor.RED + "The request sender is no longer online!");
            return true;
        }

        TPAHandler.showAcceptGUI(player, requester);
        return true;
    }
}


