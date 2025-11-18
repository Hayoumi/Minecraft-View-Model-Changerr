package com.viewmodel;

/**
 * Immutable snapshot of every tweakable viewmodel parameter.
 */
public record ViewModelProfile(
    String name,
    float size,
    float positionX,
    float positionY,
    float positionZ,
    float rotationYaw,
    float rotationPitch,
    float rotationRoll,
    boolean noSwing,
    boolean scaleSwing
) {
    private static final ViewModelProfile BASELINE = new ViewModelProfile(
        "Default",
        1.0f,
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        false,
        false
    );

    public static ViewModelProfile baseline() {
        return BASELINE;
    }

    public static ViewModelProfile defaults(String name) {
        return BASELINE.withName(name);
    }

    public static ViewModelProfile fromConfig(String name, ViewModelConfig config) {
        return new ViewModelProfile(
            name,
            config.getSize(),
            config.getPositionX(),
            config.getPositionY(),
            config.getPositionZ(),
            config.getRotationYaw(),
            config.getRotationPitch(),
            config.getRotationRoll(),
            config.getNoSwing(),
            config.getScaleSwing()
        );
    }

    public ViewModelProfile withName(String newName) {
        return new ViewModelProfile(
            newName,
            size,
            positionX,
            positionY,
            positionZ,
            rotationYaw,
            rotationPitch,
            rotationRoll,
            noSwing,
            scaleSwing
        );
    }

    public void apply(ViewModelConfig target) {
        target.setSize(size);
        target.setPositionX(positionX);
        target.setPositionY(positionY);
        target.setPositionZ(positionZ);
        target.setRotationYaw(rotationYaw);
        target.setRotationPitch(rotationPitch);
        target.setRotationRoll(rotationRoll);
        target.setNoSwing(noSwing);
        target.setScaleSwing(scaleSwing);
    }
}
