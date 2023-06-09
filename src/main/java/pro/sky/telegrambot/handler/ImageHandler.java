package pro.sky.telegrambot.handler;

import pro.sky.telegrambot.entity.Owner;
import pro.sky.telegrambot.service.OwnerService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.File;
import com.pengrad.telegrambot.model.PhotoSize;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.GetFileResponse;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

import static java.nio.file.StandardOpenOption.CREATE_NEW;

/**
 * "Перехватчик" updates типа Image
 * Сохраняет фото в БД + в папку resources
 */

public class ImageHandler implements Handler {
    private final TelegramBot telegramBot;
    private final OwnerService ownerService;

    public ImageHandler(TelegramBot telegramBot,
                        OwnerService ownerService) {
        this.telegramBot = telegramBot;
        this.ownerService = ownerService;
    }

    @Override
    public void handle(Update update) throws IOException {
        Long chatId = update.message().chat().id();
        Owner owner = ownerService.findOwnerByChatId(chatId);
        List<PhotoSize> photos = List.of(update.message().photo());
        PhotoSize photo = photos.stream().max(Comparator.comparing(PhotoSize::fileSize)).orElse(null);
        GetFile request = new GetFile(photo.fileId());
        LocalDateTime dateOfLastRepost = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        GetFileResponse getFileResponse = telegramBot.execute(request);
        File file = getFileResponse.file();
        String filePath = telegramBot.getFullFilePath(file);
        owner.setPhotoReport(telegramBot.getFileContent(file));
        owner.setDateOfLastReport(dateOfLastRepost);
        ownerService.saveOwner(owner);

        try (
                InputStream is = new URL(filePath).openStream();
                OutputStream os = Files.newOutputStream(Path.of("src/main/resources/uploaded_files" + owner.getId()
                        + file.fileId() + ".jpg"), new StandardOpenOption[]{CREATE_NEW});
                BufferedInputStream bis = new BufferedInputStream(is, 1024);
                BufferedOutputStream bos = new BufferedOutputStream(os, 1024)
        ) {
            bis.transferTo(bos);
        }
        sendMessage(chatId, "Фото успешно загружено");

        if (owner.getStringReport() == null) {
            sendMessage(chatId, "Пожалуйста не забудьте предоставить текстовый отчет");
        }
    }

    private void sendMessage(Long chatId, String message) {
        SendMessage sendMessage = new SendMessage(chatId, message);
        telegramBot.execute(sendMessage);
    }
}
