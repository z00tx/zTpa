package devz00t.zTpa.Commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;
import devz00t.zTpa.ZTpa;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TPAHandler {
    private static final Map<UUID, UUID> requests = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> requestTypes = new ConcurrentHashMap<>();
    private static final Set<UUID> teleporting = Collections.synchronizedSet(new HashSet<>());
    private static final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private static final long COOLDOWN_TIME = 10_000; // 10 seconds

    public static void sendTPARequest(Player sender, Player target, boolean isTpa) {
        if (sender.equals(target)) {
            sender.sendMessage(ChatColor.RED + "You can't send a request to yourself!");
            return;
        }

        if (hasCooldown(sender)) {
            sender.sendMessage(ChatColor.RED + "Please wait before sending another request!");
            return;
        }

        requests.put(target.getUniqueId(), sender.getUniqueId());
        requestTypes.put(target.getUniqueId(), isTpa);

        showAcceptGUI(target, sender); // Added sender parameter here

        String requestType = isTpa ? "TPA" : "TPAHERE";
        sender.sendMessage(ChatColor.GREEN + requestType + " request sent to " + target.getName() + "!");
        target.sendMessage(ChatColor.GREEN + sender.getName() + " wants to teleport to you!");

        Bukkit.getScheduler().runTaskLater(ZTpa.getInstance(), () -> {
            if (requests.containsKey(target.getUniqueId())) {
                cancelRequest(target);
            }
        }, 1200L);
    }

    public static void showAcceptGUI(Player target, Player sender) {
        Inventory gui = Bukkit.createInventory(null, 27, "§aTeleport Request");

        // Create sender's head
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(sender);
        meta.setDisplayName(ChatColor.GOLD + sender.getName());
        head.setItemMeta(meta);

        ItemStack accept = createGuiItem(Material.LIME_STAINED_GLASS_PANE, ChatColor.GREEN + "Accept");
        ItemStack deny = createGuiItem(Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "Deny");

        // Fill GUI
        gui.setItem(13, head);
        gui.setItem(11, accept);
        gui.setItem(15, deny);

        target.openInventory(gui);
    }


    private static ItemStack createGuiItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    public static void handleRequestClick(Player player, String action) {
        if (!requests.containsKey(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "No pending teleport request!");
            return;
        }

        if (action.contains("Accept")) {
            acceptRequest(player);
        } else {
            cancelRequest(player);
        }
    }

    private static void acceptRequest(Player target) {
        UUID senderId = requests.get(target.getUniqueId());
        Player sender = Bukkit.getPlayer(senderId);

        if (sender == null || !sender.isOnline()) {
            target.sendMessage(ChatColor.RED + "Player is not online or not found!");
            requests.remove(target.getUniqueId());
            return;
        }

        boolean isTpa = requestTypes.get(target.getUniqueId());
        startTeleport(sender, target, isTpa);
        requests.remove(target.getUniqueId());
        requestTypes.remove(target.getUniqueId());
    }

    private static void startTeleport(Player player, Player target, boolean isTpa) {
        teleporting.add(player.getUniqueId());

        new BukkitRunnable() {
            int countdown = 3;

            @Override
            public void run() {
                if (!teleporting.contains(player.getUniqueId())) {
                    cancel();
                    return;
                }

                if (countdown > 0) {
                    player.sendTitle(
                            ChatColor.GREEN + "Teleporting in...",
                            ChatColor.YELLOW + String.valueOf(countdown),
                            0, 20, 0
                    );
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                    countdown--;
                } else {
                    if (isTpa) {
                        player.teleport(target.getLocation());
                    } else {
                        target.teleport(player.getLocation());
                    }
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                    teleporting.remove(player.getUniqueId());
                    cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
                    cancel();
                }
            }
        }.runTaskTimer(ZTpa.getInstance(), 0L, 20L);
    }

    public static void cancelRequest(Player target) {
        UUID senderId = requests.get(target.getUniqueId());
        if (senderId != null) {
            Player sender = Bukkit.getPlayer(senderId);
            if (sender != null && sender.isOnline()) {
                sender.sendMessage(ChatColor.RED + "Your teleport request has been denied.");
            }

            target.sendMessage(ChatColor.RED + "Teleport request denied.");
            requests.remove(target.getUniqueId());
            requestTypes.remove(target.getUniqueId());
        }
    }

    public static void handleMenuClick(Player player, ItemStack clicked, String title) {
        if (!clicked.hasItemMeta()) return;

        String targetName = title.replace("§4Teleport Menu: ", "");
        Player target = Bukkit.getPlayer(targetName);

        if (target == null || !target.isOnline()) {
            player.sendMessage(ChatColor.RED + "Player is not online or not found!");
            return;
        }

        String displayName = clicked.getItemMeta().getDisplayName();
        boolean isTpaHere = title.contains("TPAHere");

        if (displayName.contains("Confirm")) {
            sendTPARequest(player, target, !isTpaHere);
        } else if (displayName.contains("Cancel")) {
            player.sendMessage(ChatColor.RED + "Teleport request cancelled.");
        }
    }

    public static void openConfirmGUI(Player player, Player target, boolean isHere) {
        String title = "§4Teleport Menu: " + target.getName();
        Inventory gui = Bukkit.createInventory(null, 27, title);

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(target);
        meta.setDisplayName(ChatColor.GOLD + target.getName());
        head.setItemMeta(meta);

        ItemStack confirm = createGuiItem(Material.LIME_STAINED_GLASS_PANE,
                ChatColor.GREEN + "Confirm");
        ItemStack cancel = createGuiItem(Material.RED_STAINED_GLASS_PANE,
                ChatColor.RED + "Cancel");

        gui.setItem(13, head);
        gui.setItem(11, cancel);
        gui.setItem(15, confirm);

        player.openInventory(gui);
    }

    public static boolean hasCooldown(Player player) {
        if (!cooldowns.containsKey(player.getUniqueId())) return false;
        return System.currentTimeMillis() - cooldowns.get(player.getUniqueId()) < COOLDOWN_TIME;
    }

    public static void clearAllRequests() {
        requests.clear();
        requestTypes.clear();
        teleporting.clear();
        cooldowns.clear();
    }

    public static void handlePlayerQuit(Player player) {
        requests.remove(player.getUniqueId());
        teleporting.remove(player.getUniqueId());
        cooldowns.remove(player.getUniqueId());
    }

    public static boolean isTeleporting(Player player) {
        return teleporting.contains(player.getUniqueId());
    }

    public static void cancelTeleport(Player player, String reason) {
        if (teleporting.remove(player.getUniqueId())) {
            player.sendMessage(reason);
        }
    }

    public static boolean hasPendingRequest(Player sender, Player target) {
        return requests.containsKey(target.getUniqueId()) &&
                requests.get(target.getUniqueId()).equals(sender.getUniqueId());
    }
    public static UUID getRequestSender(UUID targetId) {
        return requests.get(targetId);
    }
}
