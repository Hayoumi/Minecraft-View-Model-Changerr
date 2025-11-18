package com.viewmodel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Mutable backing config queried by HeldItemRenderer mixins.
 * Values are kept in a singleton so they are easy to mutate from both Kotlin/Java UI pieces.
 */
public final class ViewModelConfig {
    public static final ViewModelConfig current = new ViewModelConfig();
    private static final ViewModelProfileManager PROFILE_MANAGER = new ViewModelProfileManager();
    private static final Logger LOGGER = LoggerFactory.getLogger("ViewmodelConfig");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
        .getConfigDir()
        .resolve("viewmodel-viewmodel.json");

    private float size = ViewModelProfile.baseline().size();
    private float positionX = ViewModelProfile.baseline().positionX();
    private float positionY = ViewModelProfile.baseline().positionY();
    private float positionZ = ViewModelProfile.baseline().positionZ();
    private float rotationYaw = ViewModelProfile.baseline().rotationYaw();
    private float rotationPitch = ViewModelProfile.baseline().rotationPitch();
    private float rotationRoll = ViewModelProfile.baseline().rotationRoll();
    private boolean noSwing = ViewModelProfile.baseline().noSwing();
    private boolean scaleSwing = ViewModelProfile.baseline().scaleSwing();

    private ViewModelConfig() {}

    public static ViewModelProfileManager profiles() {
        return PROFILE_MANAGER;
    }

    public static synchronized void load() {
        ConfigPayload payload = readPayload();
        PROFILE_MANAGER.loadProfiles(deserializeProfiles(payload.profiles()), payload.activeProfile());
    }

    public static synchronized void save() {
        ConfigPayload payload = snapshotPayload();
        writePayload(payload);
    }

    private static ConfigPayload snapshotPayload() {
        List<ViewModelProfile> profiles = PROFILE_MANAGER.snapshot();
        if (profiles.isEmpty()) {
            profiles.add(ViewModelProfile.defaults("Default"));
        }
        String activeName = PROFILE_MANAGER.profiles().isEmpty()
            ? "Default"
            : PROFILE_MANAGER.getActiveProfile().name();
        return new ConfigPayload(activeName, serializeProfiles(profiles));
    }

    private static ConfigPayload defaultPayload() {
        List<ViewModelProfile> defaults = new ArrayList<>();
        defaults.add(ViewModelProfile.defaults("Default"));
        return new ConfigPayload("Default", serializeProfiles(defaults));
    }

    private static ConfigPayload readPayload() {
        if (Files.notExists(CONFIG_PATH)) {
            ConfigPayload defaults = defaultPayload();
            writePayload(defaults);
            return defaults;
        }

        try (Reader reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
            ConfigPayload payload = GSON.fromJson(reader, ConfigPayload.class);
            if (payload == null || payload.profiles() == null || payload.profiles().isEmpty()) {
                throw new JsonParseException("Config missing profiles");
            }
            return payload;
        } catch (Exception e) {
            LOGGER.error("Failed to load viewmodel config, falling back to defaults", e);
            ConfigPayload defaults = defaultPayload();
            writePayload(defaults);
            return defaults;
        }
    }

    private static void writePayload(ConfigPayload payload) {
        try {
            Files.createDirectories(Objects.requireNonNull(CONFIG_PATH.getParent()));
            try (Writer writer = Files.newBufferedWriter(
                CONFIG_PATH,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
            )) {
                GSON.toJson(payload, writer);
            }
        } catch (IOException e) {
            LOGGER.error("Unable to write viewmodel config", e);
        }
    }

    private static List<ProfilePayload> serializeProfiles(List<ViewModelProfile> profiles) {
        List<ProfilePayload> serialized = new ArrayList<>(profiles.size());
        for (ViewModelProfile profile : profiles) {
            serialized.add(ProfilePayload.from(profile));
        }
        return serialized;
    }

    private static List<ViewModelProfile> deserializeProfiles(List<ProfilePayload> payloads) {
        List<ViewModelProfile> result = new ArrayList<>();
        if (payloads != null) {
            for (ProfilePayload payload : payloads) {
                result.add(payload.toProfile());
            }
        }
        return result;
    }

    public void resetToDefaults() {
        ViewModelProfile.baseline().apply(this);
    }

    public float getSize() {
        return size;
    }

    public void setSize(float size) {
        this.size = size;
    }

    public float getPositionX() {
        return positionX;
    }

    public void setPositionX(float positionX) {
        this.positionX = positionX;
    }

    public float getPositionY() {
        return positionY;
    }

    public void setPositionY(float positionY) {
        this.positionY = positionY;
    }

    public float getPositionZ() {
        return positionZ;
    }

    public void setPositionZ(float positionZ) {
        this.positionZ = positionZ;
    }

    public float getRotationYaw() {
        return rotationYaw;
    }

    public void setRotationYaw(float rotationYaw) {
        this.rotationYaw = rotationYaw;
    }

    public float getRotationPitch() {
        return rotationPitch;
    }

    public void setRotationPitch(float rotationPitch) {
        this.rotationPitch = rotationPitch;
    }

    public float getRotationRoll() {
        return rotationRoll;
    }

    public void setRotationRoll(float rotationRoll) {
        this.rotationRoll = rotationRoll;
    }

    public boolean getNoSwing() {
        return noSwing;
    }

    public void setNoSwing(boolean noSwing) {
        this.noSwing = noSwing;
    }

    public boolean getScaleSwing() {
        return scaleSwing;
    }

    public void setScaleSwing(boolean scaleSwing) {
        this.scaleSwing = scaleSwing;
    }

    static {
        PROFILE_MANAGER.bootstrap(current);
    }

    private record ConfigPayload(String activeProfile, List<ProfilePayload> profiles) {}

    private record ProfilePayload(
        String name,
        float size,
        float posX,
        float posY,
        float posZ,
        float yaw,
        float pitch,
        float roll,
        boolean noSwing,
        boolean scaleSwing
    ) {
        private static ProfilePayload from(ViewModelProfile profile) {
            return new ProfilePayload(
                profile.name(),
                profile.size(),
                profile.positionX(),
                profile.positionY(),
                profile.positionZ(),
                profile.rotationYaw(),
                profile.rotationPitch(),
                profile.rotationRoll(),
                profile.noSwing(),
                profile.scaleSwing()
            );
        }

        private ViewModelProfile toProfile() {
            return new ViewModelProfile(
                name,
                size,
                posX,
                posY,
                posZ,
                yaw,
                pitch,
                roll,
                noSwing,
                scaleSwing
            );
        }
    }
}
