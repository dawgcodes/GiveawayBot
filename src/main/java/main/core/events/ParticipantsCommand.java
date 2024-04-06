package main.core.events;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import main.giveaway.Exceptions;
import main.jsonparser.JSONParsers;
import main.model.entity.ListUsers;
import main.model.repository.ListUsersRepository;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.Objects;

@Service
public class ParticipantsCommand {

    private final ListUsersRepository listUsersRepository;

    private static final JSONParsers jsonParsers = new JSONParsers();

    @Autowired
    public ParticipantsCommand(ListUsersRepository listUsersRepository) {
        this.listUsersRepository = listUsersRepository;
    }

    public void participants(@NotNull SlashCommandInteractionEvent event) {
        var userIdLong = event.getUser().getIdLong();
        var guildId = Objects.requireNonNull(event.getGuild()).getIdLong();

        event.deferReply().setEphemeral(true).queue();
        String id = event.getOption("giveaway_id", OptionMapping::getAsString);
        try {
            if (id != null) {
                File file = new File("participants.json");
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                List<ListUsers> listUsers = listUsersRepository.findAllByGiveawayIdAndCreatedUserId(Long.parseLong(id), userIdLong);
                if (listUsers.isEmpty()) {
                    String noAccessReroll = jsonParsers.getLocale("no_access_reroll", guildId);
                    event.getHook().sendMessage(noAccessReroll).setEphemeral(true).queue();
                    return;
                }
                String json = gson.toJson(listUsers);
                // Создание объекта FileWriter
                FileWriter writer = new FileWriter(file);
                // Запись содержимого в файл
                writer.write(json);
                writer.flush();
                writer.close();
                FileUpload fileUpload = FileUpload.fromData(file);
                event.getHook().sendFiles(fileUpload).setEphemeral(true).queue();
            } else {
                event.getHook().sendMessage("Options is null").setEphemeral(true).queue();
            }
        } catch (Exception exception) {
            Exceptions.handle(exception, event.getHook());
        }
    }
}