package com.viewmodel.mixin;

import com.viewmodel.ViewModelConfig;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {

    @Shadow
    protected abstract void renderItem(LivingEntity entity, ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light);

    /**
     * ГЛАВНАЯ ТРАНСФОРМАЦИЯ: позиция/масштаб/вращение из GUI
     */
    @Redirect(
        method = "renderFirstPersonItem",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"
        )
    )
    private void applyTransformsBeforeRender(
        HeldItemRenderer instance,
        LivingEntity entity,
        ItemStack stack,
        ModelTransformationMode renderMode,
        boolean leftHanded,
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        int light
    ) {
        if (!stack.isEmpty()) {
            ViewModelConfig config = ViewModelConfig.current;

            // Вращение
            float rotX = config.getRotationPitch();
            float rotY = config.getRotationYaw();
            float rotZ = config.getRotationRoll();

            if (rotX != 0 || rotY != 0 || rotZ != 0) {
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(rotX));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotY));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotZ));
            }

            // Масштаб
            float scale = config.getSize();
            if (scale != 1.0f) {
                matrices.scale(scale, scale, scale);
            }

            // Позиция
            float x = config.getPositionX();
            float y = config.getPositionY();
            float z = config.getPositionZ();

            if (x != 0 || y != 0 || z != 0) {
                matrices.translate(
                    (x / 100.0f) / scale,
                    (y / 100.0f) / scale,
                    (z / 100.0f) / scale
                );
            }
        }

        this.renderItem(entity, stack, renderMode, leftHanded, matrices, vertexConsumers, light);
    }

    /**
     * ОТКЛЮЧАЕМ АНИМАЦИЮ ДОСТАВАНИЯ ПРЕДМЕТА
     */
    @Inject(method = "applyEquipOffset", at = @At("HEAD"), cancellable = true)
    private void onApplyEquipOffset(MatrixStack matrices, Arm arm, float equipProgress, CallbackInfo ci) {
        ViewModelConfig config = ViewModelConfig.current;
        int dir = (arm == Arm.RIGHT) ? 1 : -1;
        
        // Базовая позиция руки
        matrices.translate(dir * 0.56F, -0.52F, -0.72F);
        
        // БЕЗ анимации опускания (equipProgress * -0.6F)
        
        // ДОБАВЛЯЕМ ПОВОРОТ -45° если включен NO SWING или SCALE SWING
        if (config.getNoSwing() || config.getScaleSwing()) {
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-dir * 0.0F));
        }
        
        ci.cancel();
    }

    /**
     * NO SWING - убираем смещение, оставляем только вращения
     * SCALE SWING - масштабируем смещение и вращения
     */
    @Inject(
        method = "applySwingOffset",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onApplySwingOffset(MatrixStack matrices, Arm arm, float swingProgress, CallbackInfo ci) {
        ViewModelConfig config = ViewModelConfig.current;

        boolean noSwing = config.getNoSwing();
        boolean scaleSwing = config.getScaleSwing();

        // Если ничего не включено — ванильная анимация
        if (!noSwing && !scaleSwing) {
            return;
        }

        int dir = (arm == Arm.RIGHT) ? 1 : -1;

        // Считаем ванильное смещение (которое было ДО applySwingOffset)
        float sqrtProgress = MathHelper.sqrt(swingProgress);
        float baseF = -0.4F * MathHelper.sin(sqrtProgress * (float) Math.PI);
        float baseG =  0.2F * MathHelper.sin(sqrtProgress * (float) Math.PI * 2.0F);
        float baseH = -0.2F * MathHelper.sin(swingProgress * (float) Math.PI);

        // Отменяем ванильное смещение
        matrices.translate(-dir * baseF, -baseG, -baseH);

        // Тригонометрия ванильного вращения
        float sinSq   = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
        float sinSqrt = MathHelper.sin(sqrtProgress * (float) Math.PI);

        if (noSwing) {
            // NO SWING - только вращения, без смещений
            // УБИРАЕМ +45° из ванильной формулы, так как мы уже повернули на -45° в applyEquipOffset
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(dir * (sinSq * 0.0F)));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(dir * sinSqrt * 20.0F));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(sinSqrt * -40.0F));

            ci.cancel();
            return;
        }

        if (scaleSwing) {
            // SCALE SWING - масштабируем и смещение, и вращение
            float scale = config.getSize();

            // Смещение с масштабом
            float f = baseF * scale;
            float g = baseG * scale;
            float h = baseH * scale;
            matrices.translate(dir * f, g, h);

            // Вращение с масштабом
            // УБИРАЕМ +45° из ванильной формулы
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(dir * (sinSq * -20.0F) * scale));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(dir * sinSqrt * -20.0F * scale));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(sinSqrt * -80.0F * scale));

            ci.cancel();
        }
    }
}
