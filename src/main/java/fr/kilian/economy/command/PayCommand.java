package fr.kilian.economy.command;

import fr.kilian.economy.Main;
import fr.kilian.economy.exception.InsufficientBalanceException;
import fr.kilian.economy.money.MoneyFormatter;
import fr.kilian.economy.service.EconomyService;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

public class PayCommand implements CommandExecutor {

    private final MoneyFormatter moneyFormatter = Main.getInstance().getMoneyFormatter();

    private JavaPlugin javaPlugin;
    private EconomyService economyService;

    public PayCommand(
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

        if(!(sender instanceof Player player)){

            sender.sendMessage("Command reserved for player");
            return true;

        }

        if(args.length != 2){
            player.sendMessage("Wrong arguments. required 2 arguments");
            return true;
        }

        Player receiver = Bukkit.getPlayerExact(args[0]);
        long amount;

        if(receiver == null){
            player.sendMessage("This player is not Online");
            return true;
        }

        try{
            amount = moneyFormatter.parse(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage("invalid amount");
            return true;
        }

        UUID senderUniqueId = player.getUniqueId();
        UUID receiverUniqueId = receiver.getUniqueId();

        if(senderUniqueId.equals(receiverUniqueId)){
            player.sendMessage("the sender and the receiver cannot be the same player");
            return true;
        }

        if(amount <= 0){
            player.sendMessage("amount must be greater than 0");
            return true;
        }

        economyService.transfer(senderUniqueId, receiverUniqueId, amount).whenComplete(
                (unused, throwable) -> {
                    javaPlugin.getServer().getScheduler().runTask(javaPlugin, () -> {
                                if(throwable != null){

                                    if(throwable.getCause() instanceof InsufficientBalanceException){
                                        player.sendMessage("insufficient balance");
                                        return;
                                    }

                                    player.sendMessage("Transfer failed");
                                    javaPlugin.getLogger().log(
                                            Level.SEVERE,
                                            "Failed to transfer money from "
                                                    + senderUniqueId
                                                    + " to "
                                                    + receiverUniqueId,
                                            throwable
                                    );
                                    return;
                                }

                                player.sendMessage("You send " + moneyFormatter.format(amount) + " to " + receiver.getName());

                                if(receiver.isOnline()){
                                    receiver.sendMessage("You received " + moneyFormatter.format(amount) + " from " + player.getName());
                                }

                    });

                });

        return true;
    }
}
