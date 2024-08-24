package org.obiz.sdtdbot.commands;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandOption;
import org.obiz.sdtdbot.ServerGameShell;

import java.util.NoSuchElementException;
import java.util.function.Consumer;

public class SayCommand extends Command {
    private static final Logger log = LogManager.getLogger(SayCommand.class);
    public static final String TEXT = "text";
    private ServerGameShell shell;

    public SayCommand(ServerGameShell shell) {
        super("say", "Say to in game chat");
        addOption(SlashCommandOption.createStringOption(TEXT, "Your message", true));
        this.shell = shell;
    }

    @Override
    void createResponseContent(SlashCommandInteraction interaction, Consumer<String> consumer) {
        try {
            String text  = interaction.getOptionByName(TEXT).get().getStringValue().get();
            shell.executeCommand("say \"" + encodeRus(text) + "\"")
                    .thenRun(() -> consumer.accept("Message sent!"));
        } catch (NoSuchElementException ok) {
            consumer.accept("No text found for send to game chat..");
        }
    }

    private static String encodeRus(String message) {
        String dictRuLetters = "абвгдеёжзийклмнопрстуфхцчшщъыьэюяАБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ";
        String[] dictTo = {"a","b","v","g","d","e","e","j","3","i","i'","k","l","m","n","o","p","r","s","t","u","f","x","c","4","sh","sh'","'","y","'","e","u","ya","A","B","V","G","D","E","E","J","3","I","I'","K","L","M","N","O","P","R","S","T","U","F","X","C","4","SH","SH'","'","Y","'","E","U","YA"};
        StringBuilder result = new StringBuilder();
        for (char c : message.toCharArray()) {
            int index = dictRuLetters.indexOf(c);
            if(index>-1)
                result.append(dictTo[index]);
            else
                result.append(c);
        }
        return result.toString();
    }
}
