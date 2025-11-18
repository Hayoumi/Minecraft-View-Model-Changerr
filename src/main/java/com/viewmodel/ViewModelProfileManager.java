package com.viewmodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Simple in-memory profile registry so the config screen can switch layouts quickly.
 * Persistence can hook into this later and hydrate the list from disk.
 */
public final class ViewModelProfileManager {
    private final List<ViewModelProfile> profiles = new ArrayList<>();
    private int activeIndex;

    public ViewModelProfileManager() {
        this.profiles.add(ViewModelProfile.defaults("Default"));
    }

    void bootstrap(ViewModelConfig config) {
        this.profiles.get(activeIndex).apply(config);
    }

    public List<ViewModelProfile> profiles() {
        return Collections.unmodifiableList(profiles);
    }

    public List<String> profileNames() {
        return profiles.stream().map(ViewModelProfile::name).toList();
    }

    public int getActiveIndex() {
        return activeIndex;
    }

    public ViewModelProfile getActiveProfile() {
        return profiles.get(activeIndex);
    }

    public void select(int index) {
        if (index < 0 || index >= profiles.size()) {
            return;
        }
        this.activeIndex = index;
        profiles.get(index).apply(ViewModelConfig.current);
    }

    public ViewModelProfile create(String requestedName) {
        String baseName = sanitizeName(requestedName);
        String uniqueName = makeUniqueName(baseName, -1);
        ViewModelProfile snapshot = ViewModelProfile.fromConfig(uniqueName, ViewModelConfig.current);
        profiles.add(snapshot);
        activeIndex = profiles.size() - 1;
        return snapshot;
    }

    public boolean renameActive(String newName) {
        if (profiles.isEmpty()) {
            return false;
        }
        String sanitized = sanitizeName(newName);
        if (nameExists(sanitized, activeIndex)) {
            return false;
        }
        ViewModelProfile renamed = profiles.get(activeIndex).withName(sanitized);
        profiles.set(activeIndex, renamed);
        return true;
    }

    public boolean deleteActive() {
        if (profiles.size() <= 1) {
            return false;
        }

        profiles.remove(activeIndex);
        activeIndex = Math.max(0, activeIndex - 1);
        profiles.get(activeIndex).apply(ViewModelConfig.current);
        return true;
    }

    public void updateActiveFromConfig() {
        if (profiles.isEmpty()) {
            return;
        }
        ViewModelProfile active = profiles.get(activeIndex);
        profiles.set(activeIndex, ViewModelProfile.fromConfig(active.name(), ViewModelConfig.current));
    }

    private static String sanitizeName(String value) {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.isEmpty()) {
            trimmed = "Profile";
        }
        return trimmed;
    }

    private boolean nameExists(String name, int ignoreIndex) {
        for (int i = 0; i < profiles.size(); i++) {
            if (i == ignoreIndex) {
                continue;
            }
            if (profiles.get(i).name().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    private String makeUniqueName(String base, int ignoreIndex) {
        if (!nameExists(base, ignoreIndex)) {
            return base;
        }

        int counter = 2;
        String candidate = base;
        while (nameExists(candidate, ignoreIndex)) {
            candidate = base + " " + counter++;
        }
        return candidate;
    }
}
