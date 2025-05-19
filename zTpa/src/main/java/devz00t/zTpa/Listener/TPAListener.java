package devz00t.zTpa.Listener;

import devz00t.zTpa.Commands.TPAHandler;
import devz00t.zTpa.Commands.TPAHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class TPAListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;

        Player player = (Player) e.getWhoClicked();
        Inventory inv = e.getInventory();
        String title = e.getView().getTitle();

        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta() || clicked.getItemMeta().getDisplayName() == null) return;

        e.setCancelled(true);

        String displayName = clicked.getItemMeta().getDisplayName();

        // TPA GUI
        if (title.startsWith("§aSend TPA to ")) {
            if (displayName.contains("Confirm")) {
                List<String> lore = clicked.getItemMeta().getLore();
                if (lore == null || lore.isEmpty()) {
                    player.sendMessage(ChatColor.RED + "Something went wrong.");
                    return;
                }
                String targetName = lore.get(0);
                Player target = Bukkit.getPlayer(targetName);
                if (target != null && target.isOnline()) {
                    TPAHandler.sendTPARequest(player, target, true);
                } else {
                    player.sendMessage(ChatColor.RED + "Player not found or offline.");
                }
            } else if (displayName.contains("Cancel")) {
                player.sendMessage(ChatColor.RED + "TPA request cancelled.");
            }
            player.closeInventory();
            return;
        }


        if (title.startsWith("§aSend TPAHere to ")) {
            if (displayName.contains("Confirm")) {
                List<String> lore = clicked.getItemMeta().getLore();
                if (lore == null || lore.isEmpty()) {
                    player.sendMessage(ChatColor.RED + "Something went wrong.");
                    return;
                }

                String targetName = lore.get(0);
                Player target = Bukkit.getPlayer(targetName);
                if (target != null && target.isOnline()) {
                    TPAHandler.sendTPARequest(player, target, false); // false = /tpahere
                    player.sendMessage(ChatColor.GREEN + "TPAHERE request sent to " + target.getName() + ".");
                } else {
                    player.sendMessage(ChatColor.RED + "Player not found or offline.");
                }
            } else if (displayName.contains("Cancel")) {
                player.sendMessage(ChatColor.RED + "TPAHERE request cancelled.");
            }
            player.closeInventory();
            return;
        }

        // Accept/Decline GUI
        if (title.equals("§aTeleport Request")) {
            if (displayName.contains("Accept")) {
                TPAHandler.acceptRequest(player);
                player.sendMessage(ChatColor.GRAY + "Accepted the teleport request.");
            } else if (displayName.contains("Decline")) {
                TPAHandler.denyRequest(player);
                player.sendMessage(ChatColor.RED + "Declined the teleport request.");
            }
            player.closeInventory();
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();

        if (!TPAHandler.isTeleporting(player)) return;

        if (e.getFrom().getX() != e.getTo().getX() || e.getFrom().getZ() != e.getTo().getZ()) {
            TPAHandler.cancelTeleport(player, ChatColor.RED + "Teleport cancelled due to movement.");
        }
    }
}