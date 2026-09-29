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

public class CheckBalanceCommand implements CommandExecutor {

    private final JavaPlugin javaPlugin;
    private final EconomyService economyService;
    private final MoneyFormatter moneyFormatter;

    public CheckBalanceCommand(
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

        if(!sender.hasPermission("economy.admin.checkbalance")){
            sender.sendMessage("You don't have permission");
            return true;
        }

        if(args.length != 1){
            sender.sendMessage("/checkbalance [player]");
            return true;
        }

        Player checked = Bukkit.getPlayerExact(args[0]);

        if(checked == null){
            sender.sendMessage("this player is Offline");
            return true;
        }

        UUID checkedId = checked.getUniqueId();
        String checkedName = checked.getName();

        economyService.getBalance(checkedId).whenComplete(
                (balance, throwable) -> {
                    javaPlugin.getServer().getScheduler().runTask(
                            javaPlugin, () -> {
                                if(throwable != null){
                                    sender.sendMessage("Failed to get " + checkedName + " balance");
                                    javaPlugin.getLogger().log(
                                            Level.SEVERE,
                                            "Failed to show balance for "
                                                    + checkedId,
                                            throwable
                                    );
                                    return;
                                }

                                sender.sendMessage("Actual balance for " +
                                        checkedName +
                                        ": " +
                                        moneyFormatter.format(balance)
                                );

                            });
                });


        return true;
    }
}
