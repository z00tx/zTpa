package devz00t.zTpa.Listener;

import devz00t.zTpa.Commands.TPAHandler;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class TPAListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        if (e.getCurrentItem() == null) return;

        Player player = (Player) e.getWhoClicked();
        String title = e.getView().getTitle();
        ItemStack clicked = e.getCurrentItem();

        if (!clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;

        e.setCancelled(true);

        String displayName = clicked.getItemMeta().getDisplayName();


        if (title.startsWith("§4Teleport Menu:")) {
            TPAHandler.handleMenuClick(player, clicked, title);
        } else if (title.equals("§aTeleport Request")) {
            TPAHandler.handleRequestClick(player, displayName);
        }

        player.closeInventory();
    }


    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (e.getFrom().getBlockX() == e.getTo().getBlockX() &&
                e.getFrom().getBlockZ() == e.getTo().getBlockZ()) return;

        Player player = e.getPlayer();
        if (TPAHandler.isTeleporting(player)) {
            TPAHandler.cancelTeleport(player, ChatColor.RED + "Teleport cancelled due to movement");
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        TPAHandler.handlePlayerQuit(e.getPlayer());
    }
}
