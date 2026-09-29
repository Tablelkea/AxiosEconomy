package fr.kilian.economy.menu;

import fr.kilian.economy.exception.InsufficientBalanceException;
import fr.kilian.economy.exception.InsufficientBankBalanceException;
import fr.kilian.economy.money.MoneyFormatter;
import fr.kilian.economy.service.EconomyService;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletionException;
import java.util.logging.Level;

public final class BankMenuListener implements Listener {

    private MoneyFormatter moneyFormatter;
    private BankActionManager bankActionManager;
    private EconomyService economyService;
    private JavaPlugin javaPlugin;
    private BankMenu bankMenu;

    public BankMenuListener(
            MoneyFormatter moneyFormatter,
            BankActionManager bankActionManager,
            EconomyService economyService,
            JavaPlugin javaPlugin,
            BankMenu bankMenu
    ) {
        this.moneyFormatter = Objects.requireNonNull(moneyFormatter, "moneyFormatter cannot be null");
        this.bankActionManager = Objects.requireNonNull(bankActionManager, "bankActionManager cannot be null");
        this.economyService = Objects.requireNonNull(economyService, "economyService cannot be null");
        this.javaPlugin = Objects.requireNonNull(javaPlugin, "javaPlugin cannot be null");
        this.bankMenu = Objects.requireNonNull(bankMenu, "bankMenu cannot be null");
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event){

        if(!(event.getView().getTopInventory().getHolder() instanceof BankMenuHolder)){
            return;
        }

        event.setCancelled(true);

        int rawSlot = event.getRawSlot();

        if(!(event.getWhoClicked() instanceof Player player)){
            return;
        }

        if(rawSlot == BankMenu.CLOSE_SLOT){
            player.closeInventory();
            return;
        }

        if(rawSlot == BankMenu.DEPOSIT_SLOT){
            bankActionManager.setAction(player.getUniqueId(), BankActionManager.BankAction.DEPOSIT);
            player.closeInventory();
            player.sendMessage("Enter the amount in chat");
            return;
        }

        if(rawSlot == BankMenu.WITHDRAW_SLOT){
            bankActionManager.setAction(player.getUniqueId(), BankActionManager.BankAction.WITHDRAW);
            player.closeInventory();
            player.sendMessage("Enter the amount in chat");
            return;
        }

    }

    @EventHandler
    public void onChatMessage(AsyncChatEvent event){

        Player player = event.getPlayer();

        UUID uniqueId = player.getUniqueId();
        BankActionManager.BankAction action = bankActionManager.findAction(uniqueId);
        if(action == null){
            return;
        }

        event.setCancelled(true);
        Component message = event.message();

        long amount;
        String rawAmount = PlainTextComponentSerializer.plainText().serialize(message);

        try{
            amount = moneyFormatter.parse(rawAmount);
        }catch (IllegalArgumentException | ArithmeticException e){
            sendMessage(player, "Illegal amount");
            return;
        }

        javaPlugin.getLogger().info(
                "rawAmount=" + rawAmount
                        + ", parsed=" + amount
                        + ", formatted=" + moneyFormatter.format(amount)
        );

        if(amount <= 0){
            sendMessage(player, "amount must be greater than 0");
            return;
        }

        BankActionManager.BankAction consumedAction =
                bankActionManager.consumeAction(uniqueId);

        if (consumedAction == null) {
            return;
        }

        switch (consumedAction){

            case DEPOSIT -> {
                economyService.depositToBank(uniqueId, amount).whenComplete(
                        (ignored, throwable) -> {
                            javaPlugin.getServer().getScheduler().runTask(
                                    javaPlugin, () -> {

                                        if(throwable != null){
                                            Throwable cause = unwrapThrowable(throwable);
                                            if(cause instanceof InsufficientBalanceException){
                                                player.sendMessage("Insufficient balance");
                                                return;
                                            }

                                            player.sendMessage("Unable to deposit money into your bank");
                                            javaPlugin.getLogger().log(
                                                    Level.SEVERE,
                                                    "Failed to deposit to bank balance for " + uniqueId,
                                                    throwable
                                            );
                                            return;
                                        }

                                        if(player.isOnline()){
                                            player.sendMessage("Deposited " + moneyFormatter.format(amount) + " into your bank.");
                                            bankMenu.open(player);
                                        }
                                    });
                        });
            }
            case WITHDRAW -> {
                economyService.withdrawFromBank(uniqueId, amount).whenComplete(
                        (ignored, throwable) -> {
                            javaPlugin.getServer().getScheduler().runTask(
                                    javaPlugin, () -> {

                                        if(throwable != null){

                                            Throwable cause = unwrapThrowable(throwable);

                                            if(cause instanceof InsufficientBankBalanceException){

                                                player.sendMessage("Insufficient bank balance");
                                                return;
                                            }

                                            player.sendMessage("Unable to withdraw money from your bank");
                                            javaPlugin.getLogger().log(
                                                    Level.SEVERE,
                                                    "Failed to withdraw from bank balance for " + uniqueId,
                                                    throwable
                                            );
                                            return;
                                        }


                                        if(player.isOnline()){
                                            player.sendMessage("Withdrew " + moneyFormatter.format(amount) + " from your bank.");
                                            bankMenu.open(player);
                                        }

                                    });
                        });
            }

        }

    }

    private Throwable unwrapThrowable(Throwable throwable){

        while (
                throwable instanceof CompletionException &&
                        throwable.getCause() != null
        ) {

            throwable = throwable.getCause();

        }

        return throwable;

    }

    private void sendMessage(Player player, String message) {
        javaPlugin.getServer()
                .getScheduler()
                .runTask(
                        javaPlugin,
                        () -> player.sendMessage(message)
                );
    }



}
