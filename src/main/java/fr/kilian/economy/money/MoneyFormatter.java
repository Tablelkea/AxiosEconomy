package fr.kilian.economy.money;

import net.kyori.adventure.text.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class MoneyFormatter {

    public String format(long amount){

        BigDecimal decimal = BigDecimal.valueOf(amount, 2);

        return decimal.toPlainString();
    }

    public long parse(String rawAmount){

        Objects.requireNonNull(rawAmount, "rawAmount cannot be null");
        if(rawAmount.isBlank()){
            throw new IllegalArgumentException("rawAmount cannot be blank");
        }

        BigDecimal decimal = new BigDecimal(rawAmount).setScale(2, RoundingMode.UNNECESSARY);
        BigDecimal internalAmount = decimal.movePointRight(2);

        return internalAmount.longValueExact();

    }

}
