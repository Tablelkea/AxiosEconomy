package fr.kilian.economy;

import fr.kilian.api.component.ComponentCodec;
import fr.kilian.economy.profile.EconomyProfile;

public final class EconomyProfileCodec
        implements ComponentCodec<EconomyProfile> {

    @Override
    public String encode(EconomyProfile value) {
        return value.getBalance()
                + ";"
                + value.getBankBalance();
    }

    @Override
    public EconomyProfile decode(String payload) {

        String[] parts = payload.split(";");

        long balance =
                Long.parseLong(parts[0]);

        long bankBalance =
                Long.parseLong(parts[1]);

        return new EconomyProfile(
                balance,
                bankBalance
        );
    }
}