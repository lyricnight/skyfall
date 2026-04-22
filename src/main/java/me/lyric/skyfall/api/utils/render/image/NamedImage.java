package me.lyric.skyfall.api.utils.render.image;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

/**
 * gives a TextureStore a name.
 */
@Getter
@AllArgsConstructor
public class NamedImage {
    @Nullable
    private final TextureStore textureStore;
    private final String name;
}
