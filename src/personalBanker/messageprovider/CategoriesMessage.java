package personalBanker.messageprovider;
import java.util.ArrayList;
import java.util.List;

public class CategoriesMessage extends AbstractMessageProvider {
    private final List<AbstractMessageProvider> categories;

    public CategoriesMessage() {
        this.categories = new ArrayList<>();
        initializeCategories();
        loadAllMessages();
    }

    private void initializeCategories() {
        categories.add(new HelpMessage());
        categories.add(new MenuMessage());
        categories.add(new FinanceMessage());
        //позже здесь будет другая реализация, пока мало категорий будет так
    }

    private void loadAllMessages() {
        for (AbstractMessageProvider category : categories) {
            messages.putAll(category.messages);
        }
    }

    @Override
    public String getCategoryName() {
        return "Composite Messages (All Categories)";
    }
}
