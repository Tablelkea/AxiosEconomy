package fr.kilian.economy.menu;

import fr.kilian.economy.money.MoneyFormatter;
import fr.kilian.economy.service.EconomyService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class BankMenu {

    private final JavaPlugin javaPlugin;
    private final EconomyService economyService;
    private final MoneyFormatter moneyFormatter;

    public static final int BANK_BALANCE_SLOT = 15;
    public static final int WALLET_BALANCE_SLOT = 11;
    public static final int DEPOSIT_SLOT = 20;
    public static final int WITHDRAW_SLOT = 24;
    public static final int INFO_SLOT = 13;
    public static final int CLOSE_SLOT = 31;


    public BankMenu(
            JavaPlugin javaPlugin,
            EconomyService economyService,
            MoneyFormatter moneyFormatter
    ) {

        this.javaPlugin = Objects.requireNonNull(javaPlugin, "javaPlugin cannot be null");
        this.economyService = Objects.requireNonNull(economyService, "economyService cannot be null");
        this.moneyFormatter = Objects.requireNonNull(moneyFormatter, "moneyFormatter cannot be null");

    }

    public void open(Player player){

        Objects.requireNonNull(player, "player cannot be null");

        BankMenuHolder menuHolder = new BankMenuHolder();
        Inventory menu = menuHolder.getInventory();

        player.openInventory(menu);

        UUID uniqueId = player.getUniqueId();


        CompletableFuture<Long> bankFuture = economyService.getBankBalance(uniqueId);
        CompletableFuture<Long> walletFuture = economyService.getBalance(uniqueId);

        CompletableFuture<BankMenuData> dataFuture = bankFuture.thenCombine(walletFuture, BankMenuData::new);

        dataFuture.whenComplete((data, throwable) -> {

            javaPlugin.getServer().getScheduler().runTask(
                    javaPlugin, () -> {

                        if(throwable != null){
                            return;
                        }

                        if(!player.isOnline() || player.getOpenInventory().getTopInventory().getHolder() != menuHolder){
                            return;
                        }

                        menu.setItem(
                                WALLET_BALANCE_SLOT,
                                createWalletBalanceItem(data.walletBalance())
                        );

                        menu.setItem(
                                INFO_SLOT,
                                createInfoItem(
                                        data.walletBalance(),
                                        data.bankBalance()
                                )
                        );

                        menu.setItem(
                                BANK_BALANCE_SLOT,
                                createBankBalanceItem(data.bankBalance())
                        );

                        menu.setItem(
                                DEPOSIT_SLOT,
                                createDepositItem()
                        );

                        menu.setItem(
                                WITHDRAW_SLOT,
                                createWithdrawItem()
                        );

                        fillBackground(menu);

                    });

        });

    }

    private ItemStack createBankBalanceItem(long balance) {

        ItemStack item = new ItemStack(Material.CHEST);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(
                Component.text("Bank", NamedTextColor.AQUA)
                        .decoration(TextDecoration.ITALIC, false)
        );

        meta.lore(List.of(
                Component.empty(),
                Component.text("Stored balance", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),

                Component.text(
                                moneyFormatter.format(balance),
                                NamedTextColor.AQUA
                        )
                        .decoration(TextDecoration.ITALIC, false)
        ));

        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createWalletBalanceItem(long balance) {

        ItemStack item = new ItemStack(Material.GOLD_INGOT);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(
                Component.text("Wallet", NamedTextColor.GOLD)
                        .decoration(TextDecoration.ITALIC, false)
        );

        meta.lore(List.of(
                Component.empty(),
                Component.text("Available balance", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),

                Component.text(
                                moneyFormatter.format(balance),
                                NamedTextColor.YELLOW
                        )
                        .decoration(TextDecoration.ITALIC, false)
        ));

        item.setItemMeta(meta);

        return item;
    }

    private record BankMenuData(
            long bankBalance,
            long walletBalance
    ) {}

    private ItemStack createDepositItem() {

        ItemStack item = new ItemStack(Material.EMERALD);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(
                Component.text("Deposit", NamedTextColor.GREEN)
                        .decoration(TextDecoration.ITALIC, false)
        );

        meta.lore(List.of(
                Component.empty(),
                Component.text(
                                "Move money from your wallet",
                                NamedTextColor.GRAY
                        )
                        .decoration(TextDecoration.ITALIC, false),

                Component.text(
                                "to your bank account.",
                                NamedTextColor.GRAY
                        )
                        .decoration(TextDecoration.ITALIC, false),

                Component.empty(),

                Component.text(
                                "Click to deposit",
                                NamedTextColor.GREEN
                        )
                        .decoration(TextDecoration.ITALIC, false)
        ));

        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createWithdrawItem() {

        ItemStack item = new ItemStack(Material.REDSTONE);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(
                Component.text("Withdraw", NamedTextColor.RED)
                        .decoration(TextDecoration.ITALIC, false)
        );

        meta.lore(List.of(
                Component.empty(),
                Component.text(
                                "Move money from your bank",
                                NamedTextColor.GRAY
                        )
                        .decoration(TextDecoration.ITALIC, false),

                Component.text(
                                "to your wallet.",
                                NamedTextColor.GRAY
                        )
                        .decoration(TextDecoration.ITALIC, false),

                Component.empty(),

                Component.text(
                                "Click to withdraw",
                                NamedTextColor.RED
                        )
                        .decoration(TextDecoration.ITALIC, false)
        ));

        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createFillerItem() {

        ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(
                Component.text(" ")
                        .decoration(TextDecoration.ITALIC, false)
        );

        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createInfoItem(
            long walletBalance,
            long bankBalance
    ) {

        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();

        long totalBalance;

        try {
            totalBalance = Math.addExact(
                    walletBalance,
                    bankBalance
            );
        } catch (ArithmeticException exception) {
            totalBalance = Long.MAX_VALUE;
        }

        meta.displayName(
                Component.text("AXIOS Bank", NamedTextColor.YELLOW)
                        .decoration(TextDecoration.BOLD, true)
                        .decoration(TextDecoration.ITALIC, false)
        );

        meta.lore(List.of(
                Component.empty(),

                Component.text("Wallet: ", NamedTextColor.GRAY)
                        .append(
                                Component.text(
                                        moneyFormatter.format(walletBalance),
                                        NamedTextColor.GOLD
                                )
                        )
                        .decoration(TextDecoration.ITALIC, false),

                Component.text("Bank: ", NamedTextColor.GRAY)
                        .append(
                                Component.text(
                                        moneyFormatter.format(bankBalance),
                                        NamedTextColor.AQUA
                                )
                        )
                        .decoration(TextDecoration.ITALIC, false),

                Component.empty(),

                Component.text("Total wealth", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),

                Component.text(
                                moneyFormatter.format(totalBalance),
                                NamedTextColor.YELLOW
                        )
                        .decoration(TextDecoration.ITALIC, false)
        ));

        item.setItemMeta(meta);

        return item;
    }

    private void fillBackground(Inventory inventory) {

        ItemStack filler = createFillerItem();

        for (int slot = 0; slot < inventory.getSize(); slot++) {

            if (inventory.getItem(slot) == null) {
                inventory.setItem(slot, filler);
            }
        }
    }

}
