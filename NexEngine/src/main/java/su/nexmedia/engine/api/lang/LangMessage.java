package su.nexmedia.engine.api.lang;

import com.google.common.collect.Sets;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.utils.*;
import su.nexmedia.engine.utils.message.NexParser;
import su.nexmedia.engine.utils.regex.RegexUtil;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LangMessage {

    @Deprecated private static final Pattern PATTERN_MESSAGE_FULL = Pattern.compile("(\\{message:)+(.+?)}+(.*)");
    @Deprecated private static final Map<String, Pattern> PATTERN_MESSAGE_PARAMS = new HashMap<>();

    private static final Pattern PATTERN_OPTIONS = Pattern.compile("<!(.*?)!>");

    static {
        for (String parameter : new String[]{"type", "prefix", "sound", "fadeIn", "stay", "fadeOut"}) {
            PATTERN_MESSAGE_PARAMS.put(parameter, Pattern.compile("~+(" + parameter + ")+?:+(.*?);"));
        }
    }

    enum Option {
        PREFIX("prefix"),
        SOUND("sound"),
        TYPE("type"),
        ;

        private final Pattern pattern;

        Option(@NotNull String name) {
            this.pattern = Pattern.compile(name + NexParser.OPTION_PATTERN);
        }

        @NotNull
        public Pattern getPattern() {
            return pattern;
        }
    }

    private final NexPlugin<?> plugin;

    // All the text data are stored as MiniMessage string
    private String msgRaw;
    private String msgLocalized;

    private OutputType type = OutputType.CHAT;
    private boolean hasPrefix = true;
    private Sound sound;
    private int[] titleTimes = new int[3];

    /**
     * Constructs a new instance with default settings.
     *
     * @param plugin the plugin
     * @param raw    the raw text
     */
    public LangMessage(@NotNull NexPlugin<?> plugin, @NotNull String raw) {
        this.plugin = plugin;
        this.setRaw(raw);
    }

    /**
     * Constructs a copy of this instance.
     *
     * @param other an instance
     */
    LangMessage(@NotNull LangMessage other) {
        this.plugin = other.plugin;
        this.msgRaw = other.getRaw();
        this.msgLocalized = other.getLocalized();
        this.type = other.type;
        this.hasPrefix = other.hasPrefix;
        this.sound = other.sound;
        this.titleTimes = Arrays.copyOf(other.titleTimes, other.titleTimes.length);
    }

    @Deprecated
    void setArguments(@NotNull String msg) {
        Matcher matcher = RegexUtil.getMatcher(PATTERN_MESSAGE_FULL, msg);
        if (!RegexUtil.matcherFind(matcher)) return;

        String msgRaw = matcher.group(3); // Extract all the text after `{params}`
        String msgParams = matcher.group(2).trim(); // Extract all the `params`
        this.msgLocalized = msgRaw;

        for (Map.Entry<String, Pattern> entryParams : PATTERN_MESSAGE_PARAMS.entrySet()) {
            Matcher matcherParam = RegexUtil.getMatcher(entryParams.getValue(), msgParams);
            if (!RegexUtil.matcherFind(matcherParam)) continue;

            String paramName = entryParams.getKey();
            String paramValue = matcherParam.group(2).stripLeading();
            switch (paramName) {
                case "type" -> this.type = CollectionsUtil.getEnum(paramValue, OutputType.class);
                case "prefix" -> this.hasPrefix = Boolean.parseBoolean(paramValue);
                case "sound" -> this.sound = CollectionsUtil.getEnum(paramValue, Sound.class);
                case "fadeIn" -> this.titleTimes[0] = StringUtil.getInteger(paramValue, -1);
                case "stay" -> {
                    this.titleTimes[1] = StringUtil.getInteger(paramValue, -1);
                    if (this.titleTimes[1] < 0) this.titleTimes[1] = Short.MAX_VALUE;
                }
                case "fadeOut" -> this.titleTimes[2] = StringUtil.getInteger(paramValue, -1);
            }
        }
    }

    void setOptions(@NotNull String msg) {
        Matcher matcher = RegexUtil.getMatcher(PATTERN_OPTIONS, msg);
        if (!RegexUtil.matcherFind(matcher)) return;

        // String with only args
        String matchFull = matcher.group(0);
        String matchOptions = matcher.group(1).trim();
        this.msgLocalized = msg.replace(matchFull, "");

        for (Option option : Option.values()) {
            Matcher matcherParam = RegexUtil.getMatcher(option.getPattern(), matchOptions);
            if (!RegexUtil.matcherFind(matcherParam)) continue;

            String optionValue = matcherParam.group(1).stripLeading();
            switch (option) {
                case TYPE -> {
                    String[] split = optionValue.split(":");
                    this.type = CollectionsUtil.getEnum(split[0], OutputType.class);
                    if (this.type == OutputType.TITLES) {
                        this.titleTimes[0] = split.length >= 2 ? StringUtil.getInteger(split[1], -1) : -1;
                        this.titleTimes[1] = split.length >= 3 ? StringUtil.getInteger(split[2], -1) : -1;
                        this.titleTimes[2] = split.length >= 4 ? StringUtil.getInteger(split[3], -1) : -1;
                    }
                }
                case PREFIX -> this.hasPrefix = Boolean.parseBoolean(optionValue);
                case SOUND -> this.sound = CollectionsUtil.getEnum(optionValue, Sound.class);
            }
        }
    }

    public @NotNull String getRaw() {
        return this.msgRaw;
    }

    public void setRaw(@NotNull String msgRaw) {
        this.msgRaw = msgRaw;
        this.setLocalized(this.replaceDefaults().apply(this.getRaw()));
        this.setArguments(this.getLocalized());
        this.setOptions(this.getLocalized());
    }

    public @NotNull String getLocalized() {
        return this.msgLocalized;
    }

    public @NotNull Component getLocalizedComponent() {
        return ComponentUtil.asComponent(this.msgLocalized);
    }

    private void setLocalized(@NotNull String msgLocalized) {
        this.msgLocalized = msgLocalized;
    }

    @SuppressWarnings("unchecked")
    public @NotNull LangMessage replace(@NotNull String var, @NotNull Object replacer) {
        if (this.isEmpty()) return this;
        if (replacer instanceof List) return this.replace(var, (List<Object>) replacer);
        return this.replace(str -> str.replace(var, String.valueOf(replacer)));
    }

    @Deprecated
    public @NotNull LangMessage replace(@NotNull String var, @NotNull List<Object> replacer) {
        if (this.isEmpty()) return this;
        return this.replace(str -> str.replace(var, String.join("\\n", replacer.stream().map(Object::toString).toList())));
    }

    public @NotNull LangMessage replace(@NotNull UnaryOperator<String> replacer) {
        if (this.isEmpty()) return this;
        LangMessage msgCopy = new LangMessage(this);
        msgCopy.setLocalized(replacer.apply(msgCopy.getLocalized()));
        return msgCopy;
    }

    @NotNull
    public LangMessage replace(@NotNull Predicate<String> predicate, @NotNull BiConsumer<String, List<String>> replacer) {
        if (this.isEmpty()) return this;

        LangMessage msgCopy = new LangMessage(this);
        List<String> replaced = new ArrayList<>();
        msgCopy.asList().forEach(line -> {
            if (predicate.test(line)) {
                replacer.accept(line, replaced);
                return;
            }
            replaced.add(line);
        });
        msgCopy.setLocalized(String.join("\\n", replaced));
        return msgCopy;
    }

    public boolean isEmpty() {
        return this.type == OutputType.NONE || this.getLocalized().isEmpty();
    }

    public void broadcast() {
        if (!this.isEmpty()) this.send(Bukkit.getServer());
    }

    public void broadcast(Audience exempt) {
        this.broadcast(Predicate.not(exempt::equals));
    }

    public void broadcast(Audience... exempt) {
        Set<Audience> exemptSet = Sets.newHashSet(exempt);
        this.broadcast(Predicate.not(exemptSet::contains));
    }

    public void broadcast(Predicate<Audience> filter) {
        if (!this.isEmpty()) this.send(Bukkit.getServer().filterAudience(filter));
    }

    public void send(@NotNull Audience audience) {
        if (this.isEmpty()) return;

        if (this.sound != null && audience instanceof Player player) {
            MessageUtil.playSound(player, this.sound);
        }

        if (this.type == OutputType.CHAT) {
            String prefix = hasPrefix ? plugin.getConfigManager().pluginPrefix : "";
            this.asList().forEach(line -> MessageUtil.sendMessage(audience, prefix + line));
            return;
        }

        if (audience instanceof Player player) {
            if (this.type == OutputType.ACTION_BAR) {
                MessageUtil.sendActionBar(player, this.getLocalized());
            } else if (this.type == OutputType.TITLES) {
                List<String> list = this.asList();
                String title = list.size() > 0 ? NexParser.toPlainText(list.get(0)) : "";
                String subtitle = list.size() > 1 ? NexParser.toPlainText(list.get(1)) : "";
                MessageUtil.showTitle(player, title, subtitle, this.titleTimes[0], this.titleTimes[1], this.titleTimes[2]);
            }
        }
    }

    public @NotNull List<String> asList() {
        if (this.isEmpty()) {
            return Collections.emptyList();
        } else {
            return Arrays.asList(this.getLocalized().split(Pattern.quote("\\n")));
        }
    }

    public @NotNull List<Component> asComponentList() {
        return ComponentUtil.asComponent(this.asList());
    }

    /**
     * Replaces plain '\n' line breaker with a system one.
     *
     * @return A string with a system lin breakers.
     */
    public @NotNull String normalizeLines() {
        return String.join("\n", this.asList());
    }

    @NotNull
    private UnaryOperator<String> replaceDefaults() {
        return str -> {
            for (Map.Entry<String, String> entry : this.plugin.getLangManager().getPlaceholders().entrySet()) {
                str = str.replace(entry.getKey(), entry.getValue());
            }
            return Placeholders.Plugin.replacer(plugin).apply(str);
        };
    }

    public enum OutputType {
        CHAT, ACTION_BAR, TITLES, NONE,
    }
}