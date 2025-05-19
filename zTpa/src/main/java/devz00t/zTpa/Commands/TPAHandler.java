package devz00t.zTpa.Commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class TPAHandler {

    private static final Map<Player, Player> requests = new HashMap<>();
    private static final Map<UUID, UUID> sentRequests = new HashMap<>();
    private static final Map<Player, Boolean> requestTypes = new HashMap<>();
    private static final Set<Player> teleporting = new HashSet<>();
    private static final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final Map<Player, BukkitRunnable> expiryTasks = new HashMap<>();

    private static final long TELEPORT_COOLDOWN = 10_000;

    public static boolean hasPendingRequest(Player sender, Player target) {
        return sentRequests.containsKey(sender.getUniqueId()) && sentRequests.get(sender.getUniqueId()).equals(target.getUniqueId());
    }

    public static void sendTPARequest(Player from, Player to, boolean isTpa) {
        if (hasPendingRequest(from, to)) {
            from.sendMessage(ChatColor.RED + "You can only send one TPA request to this player.");
            return;
        }

        requests.put(to, from);
        sentRequests.put(from.getUniqueId(), to.getUniqueId());
        requestTypes.put(to, isTpa);

        String typeText = isTpa ? ChatColor.GRAY + "TPA" : ChatColor.GRAY + "TPAHere";
        String message = ChatColor.GRAY + "You received a " + typeText + ChatColor.GRAY + " request from " + ChatColor.AQUA + from.getName();

        to.sendMessage(message);
        to.sendMessage(ChatColor.GRAY + "Use " + ChatColor.GREEN + "/tpaccept" + ChatColor.GRAY + " to accept.");
        to.sendActionBar(message);

        from.sendMessage(ChatColor.GRAY + "Teleport request sent to " + ChatColor.AQUA + to.getName() + ChatColor.GRAY + ".");
        from.sendActionBar(ChatColor.GRAY + "TPA request sent to " + ChatColor.AQUA + to.getName());

        BukkitRunnable timeoutTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (requests.containsKey(to) && requests.get(to).equals(from)) {
                    requests.remove(to);
                    sentRequests.remove(from.getUniqueId());
                    requestTypes.remove(to);
                    expiryTasks.remove(to);

                    from.sendMessage(ChatColor.RED + "Your teleport request to " + ChatColor.AQUA + to.getName() + ChatColor.RED + " has expired.");
                    to.sendMessage(ChatColor.RED + "The teleport request from " + ChatColor.AQUA + from.getName() + ChatColor.RED + " has expired.");
                }
            }
        };
        timeoutTask.runTaskLater(Bukkit.getPluginManager().getPlugin("zTPA"), 60 * 20L);
        expiryTasks.put(to, timeoutTask);
    }

    public static void showAcceptGUI(Player target) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.GREEN + "Teleport Request");

        ItemStack accept = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta acceptMeta = accept.getItemMeta();
        acceptMeta.setDisplayName(ChatColor.GREEN + "Accept");
        accept.setItemMeta(acceptMeta);

        ItemStack decline = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta declineMeta = decline.getItemMeta();
        declineMeta.setDisplayName(ChatColor.RED + "Decline");
        decline.setItemMeta(declineMeta);

        ItemStack air = new ItemStack(Material.AIR);
        for (int i = 0; i < gui.getSize(); i++) {
            gui.setItem(i, air);
        }

        gui.setItem(11, decline);
        gui.setItem(15, accept);

        target.openInventory(gui);
    }

    public static void acceptRequest(Player target) {
        Player requester = requests.remove(target);
        Boolean isTpa = requestTypes.remove(target);

        BukkitRunnable task = expiryTasks.remove(target);
        if (task != null) {
            task.cancel();
        }

        if (requester == null || !requester.isOnline()) {
            target.sendMessage(ChatColor.RED + "The teleport requester is no longer online.");
            return;
        }

        sentRequests.remove(requester.getUniqueId());

        long now = System.currentTimeMillis();
        if (cooldowns.containsKey(requester.getUniqueId())) {
            long last = cooldowns.get(requester.getUniqueId());
            if (now - last < TELEPORT_COOLDOWN) {
                requester.sendMessage(ChatColor.RED + "You must wait before teleporting again.");
                return;
            }
        }

        teleporting.add(requester);
        requester.sendMessage(ChatColor.GREEN + "Teleporting in 5 seconds...");
        requester.playSound(requester.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);

        new BukkitRunnable() {
            int countdown = 5;

            @Override
            public void run() {
                if (!teleporting.contains(requester)) {
                    requester.sendMessage(ChatColor.RED + "Teleport canceled.");
                    cancel();
                    return;
                }

                if (countdown == 0) {
                    if (isTpa != null && !isTpa) {
                        target.teleport(requester.getLocation());
                    } else {
                        requester.teleport(target.getLocation());
                    }
                    requester.sendTitle(ChatColor.GREEN + "Teleported!", "", 10, 40, 10);
                    requester.playSound(requester.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                    cooldowns.put(requester.getUniqueId(), System.currentTimeMillis());
                    teleporting.remove(requester);
                    cancel();
                    return;
                }

                requester.sendActionBar(ChatColor.GRAY + "Teleporting in " + ChatColor.AQUA + countdown + "s...");
                countdown--;
            }
        }.runTaskTimer(Bukkit.getPluginManager().getPlugin("zTPA"), 0L, 20L);
    }

    public static void denyRequest(Player target) {
        Player requester = requests.remove(target);
        BukkitRunnable task = expiryTasks.remove(target);
        if (task != null) task.cancel();

        if (requester != null) {
            requester.sendMessage(ChatColor.RED + "Your teleport request was declined.");
            sentRequests.remove(requester.getUniqueId());
            requestTypes.remove(target);
        }
    }

    public static boolean isRequest(Player target) {
        return requests.containsKey(target);
    }

    public static boolean isTeleporting(Player player) {
        return teleporting.contains(player);
    }

    public static void cancelTeleport(Player player, String reason) {
        if (teleporting.remove(player)) {
            player.sendActionBar(reason);
            player.sendMessage(ChatColor.RED + "Teleport canceled due to movement.");
        }
    }

    public static void openConfirmGUI(Player player, Player target, boolean isHere) {
        Inventory gui = Bukkit.createInventory(null, 27,
                isHere ? ChatColor.GREEN + "Send TPAHere to " + target.getName() :
                        ChatColor.GREEN + "Send TPA to " + target.getName());

        ItemStack confirm = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta confirmMeta = confirm.getItemMeta();
        confirmMeta.setDisplayName(ChatColor.GREEN + "Confirm");
        confirmMeta.setLore(Collections.singletonList(target.getName()));
        confirm.setItemMeta(confirmMeta);

        ItemStack cancel = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta cancelMeta = cancel.getItemMeta();
        cancelMeta.setDisplayName(ChatColor.RED + "Cancel");
        cancel.setItemMeta(cancelMeta);

        ItemStack air = new ItemStack(Material.AIR);
        for (int i = 0; i < gui.getSize(); i++) {
            gui.setItem(i, air);
        }

        gui.setItem(11, cancel);
        gui.setItem(15, confirm);

        player.openInventory(gui);
    }

    public static void handleConfirmClick(Player player, String guiTitle, String targetName) {
        boolean isHere = guiTitle.contains("TPAHere");
        Player target = Bukkit.getPlayer(targetName);

        if (target != null && target.isOnline()) {
            sendTPARequest(player, target, !isHere);
            player.sendMessage(ChatColor.GREEN + "TPA request sent to " + target.getName() + ".");
        } else {
            player.sendMessage(ChatColor.RED + "Player not found or offline.");
        }
    }
    public boolean isLicenseValid(String key) {
        try {
            URL url = new URL("http://localhost:3000/verify");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);

            String jsonInput = "{\"key\":\"" + key + "\"}";
            try (OutputStream os = con.getOutputStream()) {
                byte[] input = jsonInput.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
            StringBuilder response = new StringBuilder();
            String responseLine;

            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }

            return response.toString().contains("\"valid\":true");

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
