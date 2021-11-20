package dev.bohush.economy.predicate;

import com.google.gson.JsonElement;
import net.minecraft.predicate.NumberRange;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

public class LongRange extends NumberRange<Long> {
    public static final LongRange ANY = new LongRange(null, null);

    public LongRange(@Nullable Long min, @Nullable Long max) {
        super(min, max);
    }

    public boolean test(long value) {
        if (this.min != null && value < this.min) {
            return false;
        }

        return this.max == null || value <= this.max;
    }

    public static LongRange fromJson(@Nullable JsonElement json) {
        return fromJson(json, ANY, JsonHelper::asLong, LongRange::new);
    }
}
