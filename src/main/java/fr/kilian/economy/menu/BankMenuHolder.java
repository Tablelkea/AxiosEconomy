package fr.kilian.economy.menu;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class BankMenuHolder implements InventoryHolder {

    private final Inventory inventory;

    public BankMenuHolder(){

        inventory = Bukkit.createInventory(this, 36, Component.text("Bank"));

    }

    @Override
    public Inventory getInventory(){

        return inventory;

    }

}
