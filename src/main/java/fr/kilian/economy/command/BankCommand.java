package fr.kilian.economy.command;

import fr.kilian.economy.Main;
import fr.kilian.economy.exception.InsufficientBalanceException;
import fr.kilian.economy.exception.InsufficientBankBalanceException;
import fr.kilian.economy.money.MoneyFormatter;
import fr.kilian.economy.service.EconomyService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.concurrent.CompletionException;
import java.util.logging.Level;

public final class BankCommand implements CommandExecutor {

    private final MoneyFormatter moneyFormatter = Main.getMoneyFormatter();

    private final JavaPlugin javaPlugin;
    private final EconomyService economyService;

    public BankCommand(
            JavaPlugin javaPlugin,
            EconomyService economyService
    ) {

        this.javaPlugin = Objects.requireNonNull(javaPlugin, "javaPlugin cannot be null");
        this.economyService = Objects.requireNonNull(economyService, "economyService cannot be null");

    }


    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String @NotNull [] args
    ) {

        if (!(sender instanceof Player player)) {

            sender.sendMessage("command reserved for player");
            return true;

        }

        if (args.length == 0) {
            handleBalance(player);
            return true;
        }

        if (args.length != 2) {
            sendUsage(player);
            return true;
        }


        if (args[0].equalsIgnoreCase("deposit")) {

            handleDeposit(player, args[1]);
            return true;
        }

        if(args[0].equalsIgnoreCase("withdraw")){

            handleWithdraw(player, args[1]);
            return true;
        }

        sendUsage(player);
        return true;
    }

    private OptionalLong parseAmount(
            Player player,
            String rawAmount
    ) {

        long amount;

        try {
            amount = moneyFormatter.parse(rawAmount);
        } catch (NumberFormatException e) {
            player.sendMessage("Invalid amount");
            return OptionalLong.empty();
        }

        if(amount <= 0){
            player.sendMessage("amount must be greater than 0");
            return OptionalLong.empty();
        }

        return OptionalLong.of(amount);

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

    private void handleBalance(Player player){

        UUID uniqueId = player.getUniqueId();

        economyService.getBankBalance(uniqueId).whenComplete(
                (balance, throwable) -> {
                    javaPlugin.getServer().getScheduler().runTask(
                            javaPlugin, () -> {

                                if (throwable != null) {
                                    player.sendMessage("Failed to get your bank balance");
                                    javaPlugin.getLogger().log(
                                            Level.SEVERE,
                                            "Failed to retrieve bank balance for " + uniqueId,
                                            throwable
                                    );
                                    return;
                                }

                                player.sendMessage("Bank balance: " + moneyFormatter.format(balance));

                            });
                });

    }

    private void handleDeposit(
            Player player,
            String rawAmount
    ) {

        OptionalLong parsedAmount = parseAmount(player, rawAmount);

        if(parsedAmount.isEmpty()){
            return;
        }

        long amount = parsedAmount.getAsLong();

        UUID uniqueId = player.getUniqueId();

        economyService.depositToBank(uniqueId, amount).whenComplete(

                (ignored, throwable) -> {
                    javaPlugin.getServer().getScheduler().runTask(
                            javaPlugin, () -> {

                                if (throwable != null) {

                                    Throwable cause = unwrapThrowable(throwable);

                                    if (cause instanceof InsufficientBalanceException) {
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

                                player.sendMessage("Deposited " + moneyFormatter.format(amount) + " into your bank");

                            });
                });

    }

    private void handleWithdraw(
            Player player,
            String rawAmount
    ) {

        OptionalLong parsedAmount = parseAmount(player, rawAmount);

        if(parsedAmount.isEmpty()){
            return;
        }

        long amount = parsedAmount.getAsLong();

        UUID uniqueId = player.getUniqueId();

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

                                player.sendMessage("You withdraw " + moneyFormatter.format(amount) + " from your bank");

                            });

                });

    }

    private void sendUsage(Player player){

        player.sendMessage("/bank\n" +
                "/bank deposit <amount>\n" +
                "/bank withdraw <amount>");

    }
}
