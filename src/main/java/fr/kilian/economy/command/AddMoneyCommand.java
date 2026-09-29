package fr.kilian.economy.command;

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

public class AddMoneyCommand implements CommandExecutor {

    private final JavaPlugin javaPlugin;
    private final EconomyService economyService;
    private final MoneyFormatter moneyFormatter;

    public AddMoneyCommand(
            JavaPlugin javaPlugin,
            EconomyService economyService,
            MoneyFormatter moneyFormatter
    ) {

        this.javaPlugin = Objects.requireNonNull(javaPlugin, "javaPlugin cannot be null");
        this.economyService = Objects.requireNonNull(economyService, "economyService cannot be null");
        this.moneyFormatter = Objects.requireNonNull(moneyFormatter, "moneyFormatter cannot be null");

    }


    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String @NotNull [] args
    ) {


        if(!sender.hasPermission("economy.admin.addmoney")){
            sender.sendMessage("You don't have permission");
            return true;
        }

        if(args.length != 2){
            sender.sendMessage("/addmoney [player] [amount]");
            return true;
        }

        Player receiver = Bukkit.getPlayerExact(args[0]);

        if(receiver == null){
            sender.sendMessage("this player is Offline");
            return true;
        }

        UUID receiverId = receiver.getUniqueId();
        String receiverName = receiver.getName();

        long amount;

        try{
            amount = moneyFormatter.parse(args[1]);
        } catch (NumberFormatException | ArithmeticException e){
            sender.sendMessage("Invalid amount");
            return true;
        }

        if(amount <= 0){
            sender.sendMessage("amount must be greater than 0");
            return true;
        }

        economyService.deposit(receiverId, amount).whenComplete(
                (ignored, throwable) -> {
                    javaPlugin.getServer().getScheduler().runTask(
                            javaPlugin, () -> {
                                if(throwable != null){
                                    sender.sendMessage("Impossible to add money to " + receiverName + " balance");
                                    javaPlugin.getLogger().log(
                                            Level.SEVERE,
                                            "Failed to add to balance for "
                                                    + receiverId,
                                            throwable
                                    );
                                    return;
                                }

                                sender.sendMessage("Added " +
                                        moneyFormatter.format(amount) +
                                        " to " +
                                        receiverName +
                                        " balance"
                                );
                            });
                });

        return true;
    }
}
