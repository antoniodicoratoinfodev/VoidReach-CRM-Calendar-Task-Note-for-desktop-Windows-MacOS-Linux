package com.crm.service;

import com.crm.model.UserAccount;
import com.crm.repository.LocalUserRepository;
import com.crm.repository.UserRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.awt.image.BufferedImage;
import java.nio.file.StandardCopyOption;
import javax.imageio.ImageIO;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;

/** Local avatar storage. Each file is named with the immutable account ID. */
public class AvatarService {
    private final UserRepository users;
    private final Path avatars = LocalUserRepository.applicationDataDirectory().resolve("avatars");
    public AvatarService(UserRepository users) { this.users = users; }

    /** Stores the crop result, not the original photo, so the selected framing is permanent. */
    public void updateAvatar(UserAccount user, WritableImage croppedAvatar) {
        String nextFileName = user.getId() + ".png";
        Path target = avatars.resolve(nextFileName);
        try {
            Files.createDirectories(avatars);
            if (user.getAvatarFileName() != null && !user.getAvatarFileName().equals(nextFileName)) Files.deleteIfExists(avatars.resolve(user.getAvatarFileName()));
            BufferedImage image = SwingFXUtils.fromFXImage(croppedAvatar, null);
            ImageIO.write(image, "png", target.toFile());
            user.setAvatarFileName(nextFileName);
            users.save(user);
        } catch (IOException e) { throw new IllegalStateException("Impossibile salvare l'immagine profilo", e); }
    }

    public Path getAvatarPath(UserAccount user) { return user.getAvatarFileName() == null ? null : avatars.resolve(user.getAvatarFileName()); }
}
