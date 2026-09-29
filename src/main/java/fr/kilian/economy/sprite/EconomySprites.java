package fr.kilian.economy.sprite;

import fr.kilian.api.sprite.Sprite;

public final class EconomySprites {

    public static final Sprite WALLET =
            Sprite.of("minecraft:item/gold_ingot");

    public static final Sprite BANK =
            Sprite.of("minecraft:item/diamond");

    public static final Sprite DEPOSIT =
            Sprite.of("minecraft:item/emerald");

    public static final Sprite WITHDRAW =
            Sprite.of("minecraft:item/redstone");

    public static final Sprite CLOSE =
            Sprite.of("minecraft:item/barrier");

    public static final Sprite COIN =
            Sprite.of("minecraft:item/raw_gold");

    private EconomySprites() {
    }
}
