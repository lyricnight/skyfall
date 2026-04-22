package me.lyric.skyfall.api.utils.interfaces.ducks;

/**
 * @author lyric
 * duck interface for IEntityRenderer
 */
public interface IEntityRenderer {
    @SuppressWarnings("EmptyMethod")
    void lightMapUpdate(boolean val);

    void skyfall$invokeRenderHand(float partialTicks, int pass);
}
