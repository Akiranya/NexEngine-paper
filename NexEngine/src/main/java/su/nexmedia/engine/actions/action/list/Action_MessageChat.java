package su.nexmedia.engine.actions.action.list;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.actions.action.AbstractActionExecutor;
import su.nexmedia.engine.actions.action.ActionId;
import su.nexmedia.engine.actions.parameter.ParameterId;
import su.nexmedia.engine.actions.parameter.ParameterResult;
import su.nexmedia.engine.utils.MessageUtil;
import su.nexmedia.engine.utils.Placeholders;

public class Action_MessageChat extends AbstractActionExecutor {

    public Action_MessageChat() {
        super(ActionId.MESSAGE_CHAT);
        this.registerParameter(ParameterId.MESSAGE);
    }

    @Override
    protected void execute(@NotNull Player player, @NotNull ParameterResult result) {
        String text = (String) result.getValue(ParameterId.MESSAGE);
        if (text == null) return;

        text = Placeholders.Player.replacer(player).apply(text);

        MessageUtil.sendWithJson(player, text);
    }
}
