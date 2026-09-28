package fr.kilian.economy;

import fr.kilian.api.AxiosApi;
import fr.kilian.economy.service.EconomyService;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    private AxiosApi axiosApi;
    private EconomyService economyService;

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

        // Plugin startup logic
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
