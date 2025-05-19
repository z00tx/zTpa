package devz00t.zTpa.Commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

public class TPACommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /tpa <player>");
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

        openConfirmGUI(player, target);
        return true;
    }

    public void openConfirmGUI(Player player, Player target) {
        Inventory gui = Bukkit.createInventory(null, 27, "§aSend TPA to " + target.getName());

        ItemStack confirm = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta confirmMeta = confirm.getItemMeta();
        confirmMeta.setDisplayName("§aConfirm");
        confirmMeta.setLore(Collections.singletonList(target.getName()));
        confirm.setItemMeta(confirmMeta);

        ItemStack cancel = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta cancelMeta = cancel.getItemMeta();
        cancelMeta.setDisplayName("§cCancel");
        cancel.setItemMeta(cancelMeta);


        gui.setItem(11, cancel);
        gui.setItem(15, confirm);

        player.openInventory(gui);
    }
}
