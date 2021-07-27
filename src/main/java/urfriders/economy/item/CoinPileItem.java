package urfriders.economy.item;

public class CoinPileItem extends CoinItem {

    public CoinPileItem(int tier) {
        super(tier);
    }

    @Override
    public long getValue() {
        return super.getValue() * 8;
    }
}
