package fr.kilian.economy;

import fr.kilian.api.AxiosApi;
import fr.kilian.economy.command.*;
import fr.kilian.economy.components.EconomyComponents;
import fr.kilian.economy.components.EconomyProfileCodec;
import fr.kilian.economy.menu.BankActionManager;
import fr.kilian.economy.menu.BankMenu;
import fr.kilian.economy.menu.BankMenuListener;
import fr.kilian.economy.money.MoneyFormatter;
import fr.kilian.economy.service.EconomyService;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.logging.Level;

public final class Main extends JavaPlugin {

    private BankMenu bankMenu;
    private MoneyFormatter moneyFormatter;
    private AxiosApi axiosApi;
    private EconomyService economyService;
    private static Main instance;
    private BankActionManager bankActionManager;

    @Override
    public void onEnable() {

        RegisteredServiceProvider<AxiosApi> provider =
                getServer()
                        .getServicesManager()
                        .getRegistration(AxiosApi.class);


        if(provider == null){
            getServer().getPluginManager().disablePlugin(this);
            getLogger().severe("AxiosAPI service not found, disabling plugin.");
            return;
        }

        axiosApi = provider.getProvider();

        axiosApi.components().register(
                EconomyComponents.PROFILE,
                new EconomyProfileCodec()
        );

        economyService = new EconomyService(axiosApi.players());

        moneyFormatter = new MoneyFormatter();
        bankMenu = new BankMenu(this, economyService, moneyFormatter);
        instance = this;
        bankActionManager = new BankActionManager();

        getServer().getPluginManager().registerEvents(new BankMenuListener(
                moneyFormatter,
                bankActionManager,
                economyService,
                this,
                bankMenu
        ), this);

        // Plugin startup logic

        if(!registerCommand(
                "balance",
                new BalanceCommand(this, economyService)
        )) {
            return;
        }

        if(!registerCommand(
                "pay",
                new PayCommand(this, economyService)
        )){
            return;
        }

        if(!registerCommand(
                "bank",
                new BankCommand(this, economyService, bankMenu)
        )){
            return;
        }

        if(!registerCommand(
                "setbalance",
                new SetBalanceCommand(this, economyService, moneyFormatter)
        )){
            return;
        }

        if(!registerCommand(
                "checkbalance",
                new CheckBalanceCommand(this, economyService, moneyFormatter)
        )){
            return;
        }

        if(!registerCommand(
                "addmoney",
                new AddMoneyCommand(this, economyService, moneyFormatter)
        ));

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private boolean registerCommand(String name, CommandExecutor commandExecutor){

        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(commandExecutor, "commandExecutor cannot be null");

        if(name.isBlank()){
            throw new IllegalArgumentException("name cannot be blank");
        }

        PluginCommand command = getCommand(name);

        if(command == null){
            getLogger().log(Level.SEVERE, "Command '"+name+"' is missing from plugin.yml");
            getServer().getPluginManager().disablePlugin(this);
            return false;
        }

        command.setExecutor(commandExecutor);

        return true;

    }

    public static Main getInstance(){

        return instance;

    }

    public MoneyFormatter getMoneyFormatter(){
        return moneyFormatter;
    }
}
