package com.norako.fracturia.entity.client.overworld.illagers;

import com.norako.fracturia.entity.custom.overworld.illagers.IllusionerEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import java.util.ArrayList;
import java.util.List;

public class BannerHeadLayerIllusioner extends GeoRenderLayer<IllusionerEntity>
{
    public BannerHeadLayerIllusioner(GeoRenderer<IllusionerEntity> entityRenderer)
    {
        super(entityRenderer);
    }

    @Override
    public void render(MatrixStack poseStack, IllusionerEntity animatable, BakedGeoModel bakedModel,
                       RenderLayer renderType, VertexConsumerProvider bufferSource,
                       VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay)
    {
        ItemStack headStack = animatable.getEquippedStack(EquipmentSlot.HEAD);
        if (headStack.isEmpty()) return;

        this.getGeoModel().getBone("bipedHead").ifPresent(headBone ->
                this.getGeoModel().getBone("bipedHeadBaseRotator").ifPresent(baseRotator ->
                {
                    poseStack.push();

                    // Body yaw — GeckoLib doesn't put it in the render layer poseStack
                    float bodyYaw = MathHelper.lerpAngleDegrees(partialTick, animatable.prevBodyYaw, animatable.bodyYaw);
                    poseStack.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(bodyYaw - 180.0f));

                    // Build chain from root to bipedHeadBaseRotator (root first)
                    // This captures all parent bone animations (body lean during attack, etc.)
                    // but excludes bipedHead which carries the look rotation
                    List<GeoBone> chain = new ArrayList<>();
                    GeoBone current = (GeoBone) baseRotator;
                    while (current != null)
                    {
                        chain.add(0, current);
                        current = (GeoBone) current.getParent();
                    }

                    // Apply each bone's transform (mirrors GeckoLib's prepMatrixForBone)
                    for (GeoBone bone : chain)
                    {
                        float px = bone.getPivotX() / 16f;
                        float py = bone.getPivotY() / 16f;
                        float pz = bone.getPivotZ() / 16f;
                        poseStack.translate(px, py, pz);
                        poseStack.multiply(RotationAxis.POSITIVE_Y.rotation(bone.getRotY()));
                        poseStack.multiply(RotationAxis.POSITIVE_X.rotation(bone.getRotX()));
                        poseStack.multiply(RotationAxis.POSITIVE_Z.rotation(bone.getRotZ()));
                        poseStack.translate(-px + bone.getPosX() / 16f,
                                -py + bone.getPosY() / 16f,
                                -pz + bone.getPosZ() / 16f);
                    }

                    // After chain, translate to head pivot + offset above
                    poseStack.translate(
                            headBone.getPivotX() / 16.0,
                            headBone.getPivotY() / 16.0 + 0.3,
                            headBone.getPivotZ() / 16.0
                    );

                    poseStack.scale(0.625f, 0.625f, 0.625f);

                    MinecraftClient.getInstance().getItemRenderer().renderItem(
                            headStack,
                            ModelTransformationMode.HEAD,
                            packedLight,
                            packedOverlay,
                            poseStack,
                            bufferSource,
                            animatable.getWorld(),
                            animatable.getId()
                    );

                    poseStack.pop();
                }));
    }
}
