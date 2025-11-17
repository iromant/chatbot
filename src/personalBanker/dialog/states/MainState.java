package personalBanker.dialog.states;

import personalBanker.dialog.model.DialogContext;
import personalBanker.messageprovider.AggregatorMessage;

import java.util.Optional;

public class MainState implements DialogState {
    private final AggregatorMessage messageProvider;

    public MainState() {
        this.messageProvider = new AggregatorMessage();
    }

    @Override
    public String onEnter() {
        return messageProvider.getMessage("menu.main");
    }

    @Override
    public String userRequest(DialogContext context) {
        String input = context.getUserInput().toLowerCase().trim();

        Optional<String> result = UniversalCommand.executeCommand(input, context, messageProvider);
        if (result.isPresent()) {
            return result.get();
        }

        return messageProvider.getMessage("finance.error.unknown");
    }

    @Override
    public DialogState goNextState(DialogContext context) {
        return context.hasNextState() ? context.getNextState() : this;
    }
}