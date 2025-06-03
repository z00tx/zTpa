package devz00t.zTpa.Commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class TPAHereCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /tpahere <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            player.sendMessage(ChatColor.RED + "Player not found or offline.");
            return true;
        }

        if (TPAHandler.hasPendingRequest(player, target)) {
            player.sendMessage(ChatColor.RED + "You can only send one TPA request to this player.");
            return true;
        }

        TPAHandler.sendTPARequest(player, target, false);
        return true;
    }
}
