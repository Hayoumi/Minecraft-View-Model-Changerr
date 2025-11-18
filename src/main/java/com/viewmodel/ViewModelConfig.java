package com.viewmodel;

/**
 * Mutable backing config queried by HeldItemRenderer mixins.
 * Values are kept in a singleton so they are easy to mutate from both Kotlin/Java UI pieces.
 */
public final class ViewModelConfig {
    public static final ViewModelConfig current = new ViewModelConfig();
    private static final ViewModelProfileManager PROFILE_MANAGER = new ViewModelProfileManager();

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
}
