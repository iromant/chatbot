//нынешнее состояние диалога (вкладка, категория, хз как еще назвать это)
package personalBanker.dialog.states;
import personalBanker.dialog.model.DialogContext;

public interface DialogState {
    String onEnter();

    String userRequest(DialogContext context);
    //тут надо бы поменять логику работы этих двух методов, что-то мне не нравится, но сначала дописать последнюю папку
    DialogState goNextState(DialogContext context);
}