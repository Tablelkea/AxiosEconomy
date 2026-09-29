package fr.kilian.economy.command;

import fr.kilian.economy.Main;
import fr.kilian.economy.money.MoneyFormatter;
import fr.kilian.economy.service.EconomyService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

public class BalanceCommand implements CommandExecutor {

    private final MoneyFormatter moneyFormatter = Main.getInstance().getMoneyFormatter();

    private final JavaPlugin javaPlugin;
    private final EconomyService economyService;

    public BalanceCommand(
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

        UUID uniqueId = player.getUniqueId();

        economyService.getBalance(uniqueId)
                .whenComplete(
                        (balance, throwable) -> {

                            javaPlugin.getServer()
                                    .getScheduler()
                                    .runTask(javaPlugin, () -> {
                                        if (throwable != null) {
                                            javaPlugin.getLogger().log(
                                                    Level.SEVERE,
                                                    "Failed to retrieve balance for " + uniqueId,
                                                    throwable
                                            );

                                            player.sendMessage("Failed to get your balance");
                                            return;
                                        }

                                        player.sendMessage("Balance: " + moneyFormatter.format(balance));
                                    });
                        }
                );
        return true;

    }
}
