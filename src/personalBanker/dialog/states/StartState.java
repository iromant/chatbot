package personalBanker.dialog.states;

import personalBanker.messageprovider.AggregatorMessage;
import personalBanker.dialog.model.DialogContext;

import java.util.Optional;

public class StartState implements DialogState {
    private AggregatorMessage messageProvider;

    public StartState() {
        this.messageProvider = new AggregatorMessage();
    }

    @Override
    public String onEnter() {
        return this.messageProvider.getMessage("welcome");
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