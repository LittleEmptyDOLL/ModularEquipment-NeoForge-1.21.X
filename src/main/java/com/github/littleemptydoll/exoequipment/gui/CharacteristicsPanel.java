package com.github.littleemptydoll.exoequipment.gui;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.characteristics.Characteristic;
import com.github.littleemptydoll.exoequipment.characteristics.CharacteristicCategory;
import com.github.littleemptydoll.exoequipment.characteristics.CharacteristicType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import com.github.littleemptydoll.exoequipment.util.AttributeNameUtils;
import com.github.littleemptydoll.exoequipment.util.NameUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Supplier;

public final class CharacteristicsPanel {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    ExoEquipment.MODID,
                    "textures/gui/properties.png"
            );

    public static final int WIDTH = 156;
    public static final int HEIGHT = 166;
    private static final int HEADER_HEIGHT = 30;
    private static final int FOOTER_HEIGHT = 9;
    private static final int CONTENT_HEIGHT = 127;
    private static final int CONTENT_X = 9;
    private static final int CONTENT_Y = 30;
    private static final int CONTENT_WIDTH = 135;
    private static final int SCROLLBAR_X = 147;
    private static final int CLOSE_X = 140;
    private static final int CLOSE_Y = 10;
    private static final int CLOSE_SIZE = 5;
    private static final int ROW_HEIGHT = 11;
    private static final int CATEGORY_HEIGHT = 12;
    private static final int TEXT_COLOR = 0xFFD8EAF5;
    private static final int CATEGORY_COLOR = 0xFF7FC7E6;
    private static final int VALUE_COLOR = 0xFFEAF7FF;
    private static final int PANEL_BACKGROUND_U = 0;
    private static final int PANEL_BACKGROUND_V = 0;

    private final Supplier<List<Characteristic>> characteristicsSupplier;
    private final Supplier<?> revisionSupplier;
    private Object cachedRevision;
    private List<Row> cachedRows = List.of();
    private boolean cacheInitialized;
    private boolean open;
    private int left;
    private int top;
    private int scroll;
    private int maxScroll;
    private boolean draggingThumb;
    private int dragOffset;

    public CharacteristicsPanel(
            Supplier<List<Characteristic>> characteristicsSupplier
    ) {
        this(characteristicsSupplier, null);
    }

    public CharacteristicsPanel(
            Supplier<List<Characteristic>> characteristicsSupplier,
            Supplier<?> revisionSupplier
    ) {
        this.characteristicsSupplier = characteristicsSupplier;
        this.revisionSupplier = revisionSupplier;
    }

    public boolean isOpen() {
        return open;
    }

    public void toggle() {
        open = !open;
        if (!open) {
            scroll = 0;
            draggingThumb = false;
        }
    }

    public void close() {
        open = false;
        scroll = 0;
        draggingThumb = false;
    }

    public void updatePosition(int mainLeft, int mainTop, int mainWidth, int screenWidth) {
        int preferred = mainLeft + mainWidth + 4;
        left = preferred + WIDTH <= screenWidth
                ? preferred
                : mainLeft - WIDTH - 4;
        top = Math.max(2, mainTop);
    }

    public void render(GuiGraphics graphics, net.minecraft.client.gui.Font font, int mouseX, int mouseY) {
        if (!open) {
            return;
        }

        List<Row> rows = getRows();
        int contentHeight = rows.stream().mapToInt(Row::height).sum();
        maxScroll = Math.max(0, contentHeight - CONTENT_HEIGHT);
        scroll = Math.min(scroll, maxScroll);

        graphics.blit(
                TEXTURE,
                left,
                top,
                PANEL_BACKGROUND_U,
                PANEL_BACKGROUND_V,
                WIDTH,
                HEIGHT,
                256,
                256
        );

        Component title = Component.translatable("gui.exoequipment.characteristics");
        graphics.drawString(font, title, left + 9, top + 9, TEXT_COLOR, false);

        graphics.enableScissor(
                left + CONTENT_X,
                top + HEADER_HEIGHT,
                left + CONTENT_X + CONTENT_WIDTH,
                top + HEADER_HEIGHT + CONTENT_HEIGHT
        );

        int y = top + CONTENT_Y - scroll;
        for (Row row : rows) {
            if (row.category()) {
                graphics.drawString(font, row.text(), left + CONTENT_X, y, CATEGORY_COLOR, false);
            } else {
                drawCharacteristic(graphics, font, row.characteristic(), y);
            }
            y += row.height();
        }

        graphics.disableScissor();

        renderScrollbar(graphics, mouseX, mouseY);
        boolean closeHovered = isInside(mouseX, mouseY, left + CLOSE_X, top + CLOSE_Y, CLOSE_SIZE, CLOSE_SIZE);
        graphics.blit(
                TEXTURE,
                left + CLOSE_X,
                top + CLOSE_Y,
                closeHovered ? 170 : 165,
                0,
                CLOSE_SIZE,
                CLOSE_SIZE,
                256,
                256
        );
    }

    private void drawCharacteristic(
            GuiGraphics graphics,
            net.minecraft.client.gui.Font font,
            Characteristic characteristic,
            int y
    ) {
        String label = getLabel(characteristic.key());
        String value = formatValue(characteristic);

        int valueX = left + CONTENT_X + CONTENT_WIDTH - font.width(value);
        int labelRight = Math.max(left + CONTENT_X + 4, valueX - 4);
        int labelWidth = labelRight - (left + CONTENT_X + 4);
        label = truncate(font, label, labelWidth);

        graphics.drawString(
                font,
                label,
                left + CONTENT_X + 4,
                y,
                TEXT_COLOR,
                false
        );
        graphics.drawString(font, value, valueX, y, VALUE_COLOR, false);
    }

    private void renderScrollbar(GuiGraphics graphics, int mouseX, int mouseY) {
        int trackTop = top + 33;
        int trackHeight = 121;

        graphics.blit(
                TEXTURE,
                left + SCROLLBAR_X,
                trackTop,
                156,
                0,
                3,
                trackHeight,
                256,
                256
        );

        boolean upHovered = isInside(mouseX, mouseY, left + SCROLLBAR_X, top + 29, 3, 4);
        boolean downHovered = isInside(mouseX, mouseY, left + SCROLLBAR_X, top + 154, 3, 4);

        graphics.blit(TEXTURE, left + SCROLLBAR_X, top + 29,
                upHovered ? 159 : 159, upHovered ? 0 : 4,
                3, 4, 256, 256);
        graphics.blit(TEXTURE, left + SCROLLBAR_X, top + 154,
                downHovered ? 162 : 162, downHovered ? 0 : 4,
                3, 4, 256, 256);

        int thumbHeight = 24;
        int thumbRange = Math.max(0, trackHeight - thumbHeight);
        int thumbY = trackTop;
        if (maxScroll > 0) {
            thumbY += (int) Math.round((double) scroll / maxScroll * thumbRange);
        }

        boolean thumbHovered = isInside(
                mouseX,
                mouseY,
                left + SCROLLBAR_X,
                thumbY,
                3,
                thumbHeight
        );

        graphics.blit(
                TEXTURE,
                left + SCROLLBAR_X,
                thumbY,
                thumbHovered ? 159 : 162,
                8,
                3,
                thumbHeight,
                256,
                256
        );
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!open || button != 0 || !contains(mouseX, mouseY)) {
            return false;
        }

        if (isInside(mouseX, mouseY, left + CLOSE_X, top + CLOSE_Y, CLOSE_SIZE, CLOSE_SIZE)) {
            close();
            return true;
        }

        if (isInside(mouseX, mouseY, left + SCROLLBAR_X, top + 29, 3, 4)) {
            scrollBy(-ROW_HEIGHT);
            return true;
        }

        if (isInside(mouseX, mouseY, left + SCROLLBAR_X, top + 154, 3, 4)) {
            scrollBy(ROW_HEIGHT);
            return true;
        }

        int thumbY = getThumbY();
        if (isInside(mouseX, mouseY, left + SCROLLBAR_X, thumbY, 3, 24)) {
            draggingThumb = true;
            dragOffset = (int) mouseY - thumbY;
            return true;
        }

        return true;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button) {
        if (!open || !draggingThumb || button != 0) {
            return false;
        }

        int trackTop = top + 33;
        int thumbRange = Math.max(1, 121 - 24);
        int newThumbY = (int) mouseY - dragOffset;
        newThumbY = Math.max(trackTop, Math.min(trackTop + thumbRange, newThumbY));
        scroll = (int) Math.round(
                (double) (newThumbY - trackTop) / thumbRange * maxScroll
        );
        return true;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && draggingThumb) {
            draggingThumb = false;
            return true;
        }
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!open || !contains(mouseX, mouseY) || maxScroll <= 0) {
            return false;
        }

        scrollBy(-(int) Math.signum(scrollY) * ROW_HEIGHT * 3);
        return true;
    }

    public boolean contains(double mouseX, double mouseY) {
        return open && isInside(mouseX, mouseY, left, top, WIDTH, HEIGHT);
    }

    private void scrollBy(int amount) {
        scroll = Math.max(0, Math.min(maxScroll, scroll + amount));
    }

    private int getThumbY() {
        int trackTop = top + 33;
        int thumbRange = Math.max(0, 121 - 24);
        if (maxScroll <= 0) {
            return trackTop;
        }
        return trackTop + (int) Math.round((double) scroll / maxScroll * thumbRange);
    }

    private List<Row> getRows() {
        if (revisionSupplier == null) {
            return buildRows();
        }

        Object revision = revisionSupplier.get();

        if (!cacheInitialized
                || !Objects.equals(cachedRevision, revision)) {
            cachedRevision = revision;
            cachedRows = buildRows();
            cacheInitialized = true;
        }

        return cachedRows;
    }

    private List<Row> buildRows() {
        List<Characteristic> characteristics = characteristicsSupplier.get();
        List<Row> rows = new ArrayList<>();
        CharacteristicCategory previous = null;

        for (Characteristic characteristic : characteristics) {
            if (characteristic.category() != previous) {
                previous = characteristic.category();
                rows.add(Row.category(
                        Component.translatable(
                                "gui.exoequipment.characteristic.category." +
                                        characteristic.category().name().toLowerCase(Locale.ROOT)
                        )
                ));
            }
            rows.add(Row.characteristic(characteristic));
        }

        return rows;
    }

    private String getLabel(String key) {
        String translationKey = "gui.exoequipment.characteristic." + key.replace(':', '_');
        if (I18n.exists(translationKey)) {
            return I18n.get(translationKey);
        }

        String attributePrefix = "attribute.";
        String conditionalPrefix = "conditional.attribute.";
        if (key.startsWith(attributePrefix) || key.startsWith(conditionalPrefix)) {
            String prefix = key.startsWith(attributePrefix) ? attributePrefix : conditionalPrefix;
            String remainder = key.substring(prefix.length());
            int operationSeparator = remainder.lastIndexOf('.');
            if (operationSeparator > 0) {
                String attributeId = remainder.substring(0, operationSeparator);
                String operation = remainder.substring(operationSeparator + 1);
                ResourceLocation id = ResourceLocation.tryParse(attributeId);
                if (id != null) {
                    return (prefix.equals(conditionalPrefix) ? "Conditional " : "")
                            + AttributeNameUtils.getName(id).getString()
                            + " (" + NameUtils.toDisplayName(operation) + ")";
                }
            }
        }

        String fallback = key.replace(':', ' ').replace('.', ' ').replace('_', ' ');
        if (fallback.isEmpty()) {
            return key;
        }
        return Character.toUpperCase(fallback.charAt(0)) + fallback.substring(1);
    }

    private String formatValue(Characteristic characteristic) {
        String key = characteristic.key();
        double value = characteristic.value();

        if (key.endsWith("reduction") || key.endsWith("chance")) {
            return formatPercent(value);
        }
        if (key.contains("consumption") || key.contains("generation")
                || key.endsWith("max_input") || key.endsWith("max_output")
                || key.contains("energy")) {
            if (key.contains("temperature")) {
                return formatNumber(value) + " °C";
            }
            return formatNumber(value) + " FE";
        }
        if (key.contains("health_per_second")) {
            return formatNumber(value) + " HP/s";
        }
        if (key.contains("temperature") || key.contains("heat") || key.contains("cooling")) {
            return formatNumber(value) + " °C";
        }
        if (key.contains("distance") || key.contains("range") || key.contains("speed")
                || key.contains("thrust") || key.contains("acceleration")) {
            return formatNumber(value);
        }
        if (key.endsWith("cooldown")) {
            return formatNumber(value) + " t";
        }
        if (characteristic.type() == CharacteristicType.STATE) {
            return value != 0.0D
                    ? Component.translatable("gui.exoequipment.yes").getString()
                    : Component.translatable("gui.exoequipment.no").getString();
        }
        return formatNumber(value);
    }

    private String formatPercent(double value) {
        return formatNumber(value * 100.0D) + "%";
    }

    private String formatNumber(double value) {
        if (Math.rint(value) == value) {
            return Long.toString((long) value);
        }
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private String truncate(net.minecraft.client.gui.Font font, String text, int maxWidth) {
        if (maxWidth <= 0) {
            return "";
        }
        if (font.width(text) <= maxWidth) {
            return text;
        }
        String ellipsis = "...";
        while (!text.isEmpty() && font.width(text + ellipsis) > maxWidth) {
            text = text.substring(0, text.length() - 1);
        }
        return text + ellipsis;
    }

    private static boolean isInside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private record Row(boolean category, Component text, Characteristic characteristic, int height) {
        static Row category(Component text) {
            return new Row(true, text, null, CATEGORY_HEIGHT);
        }

        static Row characteristic(Characteristic characteristic) {
            return new Row(false, null, characteristic, ROW_HEIGHT);
        }
    }
}
