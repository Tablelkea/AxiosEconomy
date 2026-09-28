package fr.kilian.economy;

import fr.kilian.api.component.ComponentKey;
import fr.kilian.economy.profile.EconomyProfile;

public final class EconomyComponents {

    public static final ComponentKey<EconomyProfile> PROFILE =
            new ComponentKey<>(
                    "economy",
                    "profile",
                    EconomyProfile.class
            );

    private EconomyComponents(){



    }

}
