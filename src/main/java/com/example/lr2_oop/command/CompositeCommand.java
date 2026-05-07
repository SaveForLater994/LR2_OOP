package com.example.lr2_oop.command;

import java.util.ArrayList;
import java.util.List;

/**
 * Группа команд, выполняющихся вместе
 * Используется для операций над несколькими выделенными фигурами
 */
public class CompositeCommand implements Command {

    private final List<Command> commands;
    private final String description;

    public CompositeCommand(String description) {
        this.commands = new ArrayList<>();
        this.description = description;
    }

    public CompositeCommand(String description, List<Command> commands) {
        this.commands = new ArrayList<>(commands);
        this.description = description;
    }

    public void addCommand(Command command) {
        if (command != null) {
            commands.add(command);
        }
    }

    public void addCommands(List<Command> commands) {
        this.commands.addAll(commands);
    }

    public int getCommandCount() {
        return commands.size();
    }

    public boolean isEmpty() {
        return commands.isEmpty();
    }

    @Override
    public void execute() {
        for (Command command : commands) {
            command.execute();
        }
    }

    @Override
    public void undo() {
        // Выполняем в обратном порядке
        for (int i = commands.size() - 1; i >= 0; i--) {
            commands.get(i).undo();
        }
    }

    @Override
    public void redo() {
        for (Command command : commands) {
            command.redo();
        }
    }

    @Override
    public String getDescription() {
        return String.format("%s (%d операций)", description, commands.size());
    }

    @Override
    public boolean canMergeWith(Command other) {
        return false; // Composite команды не объединяются
    }
}