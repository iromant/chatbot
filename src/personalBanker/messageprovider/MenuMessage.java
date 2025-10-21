package personalBanker.messageprovider;

public class MenuMessage extends AbstractMessageProvider {

    public MenuMessage() {
        messages.put("menu.main", """
                **Главное меню**
            
            Выберите действие:
            1. Добавить расход
            2. Добавить доход
            3. Текущий баланс  
            4. Статистика
            5. Настройки бюджета
            6. Управление категориями
            
            Или введите команду, например /help""");
    }

    @Override
    public String getCategoryName() {
        return "Menu Messages";
    }
}
