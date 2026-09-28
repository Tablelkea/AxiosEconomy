package fr.kilian.economy.service;

import fr.kilian.api.component.ComponentContainer;
import fr.kilian.api.player.PlayerProfile;
import fr.kilian.api.player.PlayerService;
import fr.kilian.economy.EconomyComponents;
import fr.kilian.economy.profile.EconomyProfile;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static fr.kilian.economy.EconomyComponents.PROFILE;

public final class EconomyService {

    private final PlayerService playerService;

    public EconomyService(
            PlayerService playerService
    ) {

        this.playerService = Objects.requireNonNull(playerService, "playerService cannot be null");

    }

    private EconomyProfile getOrCreateEconomyProfile(
            PlayerProfile playerProfile
    ) {

        ComponentContainer components =
                playerProfile.getComponents();

        synchronized (components){

            Optional<EconomyProfile> existingProfile = components.find(PROFILE);

            if(existingProfile.isPresent()){
                return existingProfile.get();
            }

            EconomyProfile created = new EconomyProfile(0, 0);

            components.set(PROFILE, created);

            return created;
        }

    }

    public CompletableFuture<Long> getBalance(
            UUID uniqueId
    ) {

        Objects.requireNonNull(uniqueId, "uniqueId cannot be null");

        return playerService.find(uniqueId).thenApply(
            optionalProfile -> {
                if(optionalProfile.isEmpty()) {
                    throw new IllegalStateException(
                            "Player profile not found: " + uniqueId
                    );
                }

                PlayerProfile playerProfile = optionalProfile.get();
                EconomyProfile economyProfile = getOrCreateEconomyProfile(playerProfile);

                return economyProfile.getBalance();
            }
        );
    }

    public CompletableFuture<Void> deposit(
            UUID uniqueId,
            long amount
    ) {

        Objects.requireNonNull(uniqueId, "uniqueId cannot be null");

        if(amount <= 0){
            throw new IllegalArgumentException("amount must be greater than 0");
        }

        return playerService.find(uniqueId).thenAccept(
                optionalProfile -> {
                    if(optionalProfile.isEmpty()){
                        throw new IllegalStateException("Player profile not found: " + uniqueId);
                    }

                    PlayerProfile playerProfile = optionalProfile.get();
                    EconomyProfile economyProfile = getOrCreateEconomyProfile(playerProfile);
                    economyProfile.deposit(amount);
                }
        );

    }

    public CompletableFuture<Void> withdraw(
            UUID uniqueId,
            long amount
    ) {

        Objects.requireNonNull(uniqueId, "uniqueId cannot be null");

        if(amount <= 0){
            throw new IllegalArgumentException("amount must be greater than 0");
        }

        return playerService.find(uniqueId).thenAccept(
                optionalProfile -> {

                    if(optionalProfile.isEmpty()){
                        throw new IllegalStateException("Player profile not found: " + uniqueId);
                    }

                    PlayerProfile playerProfile = optionalProfile.get();
                    EconomyProfile economyProfile = getOrCreateEconomyProfile(playerProfile);
                    economyProfile.withdraw(amount);

                }
        );

    }

    public CompletableFuture<Void> depositToBank(
            UUID uniqueId,
            long amount
    ) {

        Objects.requireNonNull(uniqueId, "uniqueId cannot be null");

        if(amount <= 0){
            throw new IllegalArgumentException("amount must be greater than 0");
        }

        return playerService.find(uniqueId).thenAccept(
                optionalProfile -> {

                    if(optionalProfile.isEmpty()){
                        throw new IllegalStateException("Player profile not found: " + uniqueId);
                    }

                    PlayerProfile playerProfile = optionalProfile.get();
                    EconomyProfile economyProfile = getOrCreateEconomyProfile(playerProfile);
                    economyProfile.depositToBank(amount);

                }
        );

    }

    public CompletableFuture<Void> withdrawFromBank(
            UUID uniqueId,
            long amount
    ) {

        Objects.requireNonNull(uniqueId, "uniqueId cannot be null");

        if(amount <= 0){
            throw new IllegalArgumentException("amount must be greater than 0");
        }

        return playerService.find(uniqueId).thenAccept(
                optionalProfile -> {

                    if(optionalProfile.isEmpty()){
                        throw new IllegalStateException("Player profile not found: " + uniqueId);
                    }

                    PlayerProfile playerProfile = optionalProfile.get();
                    EconomyProfile economyProfile = getOrCreateEconomyProfile(playerProfile);
                    economyProfile.withdrawnFromBank(amount);

                }
        );

    }

    public CompletableFuture<Void> transfer(
            UUID senderId,
            UUID receiverId,
            long amount
    ) {

        Objects.requireNonNull(senderId, "senderId cannot be null");
        Objects.requireNonNull(receiverId, "receiverId cannot be null");

        if(amount <= 0){
            throw new IllegalArgumentException("amount must be greater than 0");
        }

        if(senderId.equals(receiverId)){
            throw new IllegalArgumentException("senderId and receiverId cannot be the same");
        }

        CompletableFuture<PlayerProfile> senderProfile = playerService.find(senderId).thenApply(
                optionalProfile -> {

                    if(optionalProfile.isEmpty()){
                        throw new IllegalStateException("Sender profile not found: " + senderId);
                    }

                    return optionalProfile.get();
                }
        );

        CompletableFuture<PlayerProfile> receiverProfile = playerService.find(receiverId).thenApply(
                optionalProfile -> {

                    if(optionalProfile.isEmpty()){
                        throw new IllegalStateException("Receiver profile not found: " + receiverId);
                    }

                    return optionalProfile.get();

                }
        );


        return senderProfile.thenAcceptBoth(receiverProfile,
                (sender, receiver) -> {
            EconomyProfile senderEconomy = getOrCreateEconomyProfile(sender);
            EconomyProfile receiverEconomy = getOrCreateEconomyProfile(receiver);

            senderEconomy.transferTo(receiverEconomy, amount);
        });

    }

    public CompletableFuture<Long> getBankBalance(
            UUID uniqueId
    ) {

        Objects.requireNonNull(uniqueId, "uniqueId cannot be null");

        return playerService.find(uniqueId).thenApply(
                optionalProfile -> {

                    if(optionalProfile.isEmpty()){
                        throw new IllegalStateException("Player profile not found: " + uniqueId);
                    }

                    PlayerProfile playerProfile = optionalProfile.get();
                    EconomyProfile economyProfile = getOrCreateEconomyProfile(playerProfile);

                    return economyProfile.getBankBalance();

                }
        );

    }

    public CompletableFuture<Void> setBalance(
            UUID uniqueId,
            long amount
    ) {

        Objects.requireNonNull(uniqueId, "uniqueId cannot be null");

        if(amount < 0){
            throw new IllegalArgumentException("amount cannot be less than 0");
        }

        return playerService.find(uniqueId).thenAccept(
                optionalProfile -> {

                    if(optionalProfile.isEmpty()){
                        throw new IllegalStateException("Player profile not found: " + uniqueId);
                    }

                    PlayerProfile playerProfile = optionalProfile.get();
                    EconomyProfile economyProfile = getOrCreateEconomyProfile(playerProfile);

                    economyProfile.setBalance(amount);

                }
        );

    }

}
