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
import java.util.logging.Level;

public class SetBalanceCommand implements CommandExecutor {

    private final JavaPlugin javaPlugin;
    private final EconomyService economyService;
    private final MoneyFormatter moneyFormatter;

    public SetBalanceCommand(
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

        if(!sender.hasPermission("economy.admin.setmoney")){
            sender.sendMessage("You don't have permission");
            return true;
        }

        if(args.length != 2){
            sender.sendMessage("/setmoney [player] [amount]");
            return true;
        }

        Player receiver = Bukkit.getPlayerExact(args[0]);

        if(receiver == null){
            sender.sendMessage("this player is offline");
            return true;
        }

        long amount;

        try{
            amount = moneyFormatter.parse(args[1]);
        } catch (NumberFormatException | ArithmeticException e) {
            sender.sendMessage("Invalid amount");
            return true;
        }

        if(amount < 0){
            sender.sendMessage("amount cannot be less than 0");
            return true;
        }

        economyService.setBalance(receiver.getUniqueId(), amount).whenComplete(
                (ignored, throwable) -> {
                    javaPlugin.getServer().getScheduler().runTask(
                            javaPlugin, () -> {

                                if(throwable != null){
                                    sender.sendMessage("Failed to set player balance");
                                    javaPlugin.getLogger().log(
                                            Level.SEVERE,
                                            "Failed to set balance for "
                                                    + receiver.getUniqueId(),
                                            throwable
                                    );
                                    return;
                                }

                                sender.sendMessage("actual balance for " + receiver.getName() +
                                        " is now: " +
                                        moneyFormatter.format(amount)
                                );

                            });
                });

        return true;
    }
}
