package com.boes.sage.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import io.leangen.geantyref.TypeToken;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.brigadier.CloudBrigadierManager;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.internal.CommandNode;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SageCommandManager {

    private final PaperCommandManager<CommandSender> manager;
    private final AnnotationParser<CommandSender> annotationParser;

    public SageCommandManager(JavaPlugin plugin) {
        this.manager = PaperCommandManager.builder(
                        SenderMapper.create(CommandSourceStack::getSender, this::toCommandSourceStack))
                .executionCoordinator(ExecutionCoordinator.simpleCoordinator())
                .buildOnEnable(plugin);

        this.annotationParser = new AnnotationParser<>(manager, CommandSender.class);

        if (manager.hasBrigadierManager()) {
            configureBrigadierHints(manager.brigadierManager());
        }
    }

    private void configureBrigadierHints(CloudBrigadierManager<CommandSender, ?> brigadierManager) {
        brigadierManager.setNativeNumberSuggestions(true);
        brigadierManager.registerMapping(new TypeToken<StringParser<CommandSender>>() {}, builder ->
            builder.to(parser -> switch (parser.stringMode()) {
                    case QUOTED -> StringArgumentType.string();
                    case GREEDY, GREEDY_FLAG_YIELDING -> StringArgumentType.greedyString();
                    default -> StringArgumentType.word();
                })
                .suggestedBy((parser, useCloud) -> hasCustomSuggestionProvider(parser) ? useCloud : null)
        );
    }

    private boolean hasCustomSuggestionProvider(StringParser<CommandSender> parser) {
        for (CommandNode<CommandSender> node : manager.commandTree().rootNodes()) {
            if (nodeHasCustomSuggestionProvider(node, parser)) {
                return true;
            }
        }
        return false;
    }

    private boolean nodeHasCustomSuggestionProvider(CommandNode<CommandSender> node, StringParser<CommandSender> parser) {
        CommandComponent<CommandSender> component = node.component();
        if (component.parser() == parser && component.suggestionProvider() != parser.suggestionProvider()) {
            return true;
        }
        for (CommandNode<CommandSender> child : node.children()) {
            if (nodeHasCustomSuggestionProvider(child, parser)) {
                return true;
            }
        }
        return false;
    }

    public void registerCommand(Object commandHolder) {
        annotationParser.parse(commandHolder);
    }

    public PaperCommandManager<CommandSender> raw() {
        return manager;
    }

    public <T> void registerParser(Class<T> type, ArgumentParser<CommandSender, T> parser) {
        manager.parserRegistry().registerParser(ParserDescriptor.of(parser, type));
    }

    public void registerNamedParser(String name, ParserDescriptor<CommandSender, ?> parser) {
        manager.parserRegistry().registerNamedParser(name, parser);
    }

    public void registerSuggestions(String name, Supplier<List<String>> values) {
        manager.parserRegistry().registerSuggestionProvider(name, staticSuggestions(values));
    }

    public void registerSuggestions(String name, SuggestionProvider<CommandSender> provider) {
        manager.parserRegistry().registerSuggestionProvider(name, provider);
    }

    public static SuggestionProvider<CommandSender> staticSuggestions(Supplier<List<String>> values) {
        return SuggestionProvider.blocking((ctx, input) ->
                values.get().stream().map(Suggestion::suggestion).collect(Collectors.toList()));
    }

    private CommandSourceStack toCommandSourceStack(CommandSender sender) {
        return new CommandSourceStack() {
            @Override
            public Location getLocation() {
                if (sender instanceof Entity entity) {
                    return entity.getLocation();
                }
                if (sender instanceof BlockCommandSender blockCommandSender) {
                    return blockCommandSender.getBlock().getLocation();
                }
                return Bukkit.getWorlds().get(0).getSpawnLocation();
            }

            @Override
            public CommandSender getSender() {
                return sender;
            }

            @Override
            public Entity getExecutor() {
                return sender instanceof Entity entity ? entity : null;
            }

            @Override
            public org.bukkit.entity.Player getPlayerOrThrow() {
                if (sender instanceof org.bukkit.entity.Player player) {
                    return player;
                }
                throw new IllegalStateException("Command source is not a player");
            }

            @Override
            public Entity getEntityOrThrow() {
                if (sender instanceof Entity entity) {
                    return entity;
                }
                throw new IllegalStateException("Command source is not an entity");
            }

            @Override
            public CommandSourceStack withLocation(Location location) {
                return this;
            }

            @Override
            public CommandSourceStack withExecutor(Entity executor) {
                return this;
            }
        };
    }
}
