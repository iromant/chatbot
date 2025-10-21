package personalBanker.messageprovider;

public class SystemMessage extends AbstractMessageProvider {

    public SystemMessage() {
        messages.put("welcome", "Привет! Я твой финансовый помощник! Помогу тебе управлять бюджетом и отслеживать расходы!\n\n       *Как работать со мной*:\n  Используйте меню /menu для выбора действий\n  Отвечайте на мои вопросы по порядку\n  Если ошибётесь - я подскажу правильный формат\n  Используйте /back чтобы вернуться назад\n  В любой момент используйте /help для справки\n  Выберите действие из меню или введите команду!");
        messages.put("goodbye", "До свидания! Возвращайтесь для управления вашими финансами!");

        messages.put("error.invalid.amount", "Неверная сумма! Введите число, например: 1500 или 99.50");
        messages.put("error.unknown.command", "Неизвестная команда. Введите /help для списка команд");
        messages.put("error.operation.cancelled", "Операция отменена");
        messages.put("error.general", "Произошла ошибка. Попробуйте еще раз");

        messages.put("confirmation.required", "Подтвердите действие: {0}\n\nДа - подтвердить\nНет - отменить");
        messages.put("confirmation.yes", "Подтверждено");
        messages.put("confirmation.no", "Отменено");
    }

    @Override
    public String getCategoryName() {
        return "System Messages";
    }
}
