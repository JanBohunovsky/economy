package urfriders.economy.shop;

public class OwnerShopInventory extends ShopInventory {

    public OwnerShopInventory(Shop shop) {
        super(shop);
    }

    @Override
    public void markDirty() {
    }
}
