package org.obiz.sdtdbot.commands;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.permission.Role;

import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandInteraction;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;


public abstract class Command {

    private static final Logger log = LogManager.getLogger(Command.class);
//    private DiscordApi api;
    protected String command;
    protected String description;
    protected String roleName="";
    private SlashCommand slashCommand;
    private Predicate<SlashCommandInteraction> predicate; //additional condition for exec command

    public Command(String command, String description) {
        this.command = command;
        this.description = description;
        this.predicate = interaction -> true;
    }

    public Command(String command, String description, String roleName) {
        this(command, description);
        this.roleName = roleName;
    }

    /**
     *
     * @param command command name
     * @param description command description
     * @param predicate additional condition to check ber run command
     */
    public Command(String command, String description, Predicate<SlashCommandInteraction> predicate) {
        this(command, description);
        this.predicate = predicate;
    }

    /** Creates and register on server Discord slash command. Also register typical listener for the command with
     * role access checks.
     *
     * @param api
     */
    public void start(DiscordApi api) {
        slashCommand =  SlashCommand.with(command, description).createGlobal(api).join();
        log.debug("Command '" + command + "' accepted!");
        api.addSlashCommandCreateListener(event -> {
            SlashCommandInteraction slashCommandInteraction = event.getSlashCommandInteraction();
            if (slashCommandInteraction.getCommandName().equals(command)) {
                //role-based access control evaluations
                boolean userAllowedByRole = false;
                if(!roleName.isEmpty()) {
                    log.debug("Role is not empty, try to chek for command <"+command+">");
                    List<Role> roles = slashCommandInteraction.getServer().get().getRolesByName(roleName);
                    if(roles.isEmpty()) {
                        log.error("Role with name <" + roleName + "> not found on server!");
                    } else {
                        userAllowedByRole = roles.get(0).hasUser(slashCommandInteraction.getUser());
                    }
                } else {
                    userAllowedByRole = true;
                }
                //finally make decision based on role and additional logic
                if(userAllowedByRole && predicate.test(slashCommandInteraction)) {
                    log.debug("Command received: " + command);
                    consume(slashCommandInteraction);
                } else {
                    sendResponse(slashCommandInteraction, "Not allowed for you. Sorry :(");
                }
            }
        });
//        SlashCommand.with(command, description).createGlobal(api).thenAcceptAsync(slashCommand -> {
//        });
    }

    protected void consume(SlashCommandInteraction interaction) {
        createResponseContent(interaction, s -> {
            sendResponse(interaction, s);
        });
    }

    private void sendResponse(SlashCommandInteraction interaction, String response) {
        log.debug("Command simple response: " + response);
        interaction.createImmediateResponder()
                .setContent(response)
                .setFlags(MessageFlag.EPHEMERAL) // Only visible for the user which invoked the command
                .respond();
    }

    /**
     *
     * @param interaction - for get command params if needed
     * @param consumer - for accept command result asynchronously (from lambda)
     */
    abstract void createResponseContent (SlashCommandInteraction interaction, Consumer<String> consumer);

}
