package com.i5mc.chatcolor;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum ColorMapping {

    DARK_RED,
    RED,
    GOLD,
    YELLOW,
    DARK_GREEN,
    GREEN,
    AQUA,
    DARK_AQUA,
    DARK_BLUE,
    BLUE,
    LIGHT_PURPLE,
    DARK_PURPLE,
    WHITE,
    GRAY,
    DARK_GRAY,
    BLACK;

    public static final Map<Integer, ColorMapping> BY_VALUE = new HashMap<>();

    private int value = 1 << ordinal();
    private ChatColor chatColor = ChatColor.valueOf(name());

    static {
        for (ColorMapping mapping : values()) {
            BY_VALUE.put(mapping.getValue(), mapping);
        }
    }

    public int getValue() {
        return value;
    }

    public ChatColor getChatColor() {
        return chatColor;
    }

    public static List<ColorMapping> all(int all) {
        List<ColorMapping> out = new ArrayList<>();
        for (ColorMapping mapping : values()) {
            int value = mapping.getValue();
            if ((all & value) == value) out.add(mapping);
        }
        return out;
    }

}
