package fr.kilian.economy.menu;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class BankActionManager {

    private final ConcurrentMap<UUID, BankAction> pendingActions = new ConcurrentHashMap<>();

    public void setAction(UUID uniqueId, BankAction action){

        Objects.requireNonNull(uniqueId, "uniqueId cannot be null");
        Objects.requireNonNull(action, "action cannot be null");

        pendingActions.putIfAbsent(uniqueId, action);
    }

    public BankAction findAction(UUID uniqueId){

        Objects.requireNonNull(uniqueId, "uniqueId cannot be null");

        return pendingActions.get(uniqueId);

    }

    public void clearAction(UUID uniqueId){

        Objects.requireNonNull(uniqueId, "uniqueId cannot be null");

        pendingActions.remove(uniqueId);

    }


    public enum BankAction {
        DEPOSIT,
        WITHDRAW
    }

    public BankAction consumeAction(UUID uniqueId){

        Objects.requireNonNull(uniqueId, "uniqueId cannot be null");

        return pendingActions.remove(uniqueId);
    }

}
