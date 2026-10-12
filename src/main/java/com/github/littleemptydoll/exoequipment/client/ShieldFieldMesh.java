package com.github.littleemptydoll.exoequipment.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.joml.Vector3f;

/**
 * Dedicated shield field mesh.
 *
 * Architecture and shield-part proportions/UV layout are adapted from
 * Draconic Evolution 1.21's modular chestpiece shield mesh. Unlike the
 * PlayerModel skin geometry, these UVs exist solely for the shield shader,
 * which is essential to the DE-style radial wave remaining visually stable
 * as the camera moves around the player.
 *
 * Draconic Evolution assets are distributed under CC BY-NC-SA 4.0.
 * See THIRD_PARTY_NOTICES.md.
 */
public final class ShieldFieldMesh {
    private static final MeshPart BODY = new MeshPart(
            new float[] {
                    -0.300000F, -0.025000F, -0.175000F,
                    -0.300000F, -0.025000F, 0.175000F,
                    -0.300000F, 0.749999F, -0.175000F,
                    -0.300000F, 0.749999F, 0.175000F,
                    0.300000F, -0.025000F, -0.175000F,
                    0.300000F, 0.749999F, -0.175000F,
                    0.300000F, -0.025000F, 0.175000F,
                    0.300000F, 0.749999F, 0.175000F,
            },
            new float[] {
                    0.333401F, 0.770897F,
                    0.187500F, 0.770897F,
                    0.333401F, 0.447830F,
                    0.187500F, 0.447830F,
                    0.625019F, 0.770936F,
                    0.374903F, 0.770936F,
                    0.625019F, 0.447870F,
                    0.374903F, 0.447870F,
                    0.812497F, 0.770986F,
                    0.666596F, 0.770986F,
                    0.812497F, 0.447919F,
                    0.666596F, 0.447919F,
                    0.875028F, 0.770946F,
                    1.125144F, 0.447880F,
                    1.125144F, 0.770946F,
                    0.875028F, 0.447880F,
                    0.626697F, 0.902664F,
                    0.376581F, 0.756763F,
                    0.626697F, 0.756763F,
                    0.376581F, 0.902664F,
            },
            new int[] {
                    0, 0, 1, 1, 2, 2,
                    2, 2, 1, 1, 3, 3,
                    4, 4, 0, 5, 5, 6,
                    5, 6, 0, 5, 2, 7,
                    6, 8, 4, 9, 7, 10,
                    7, 10, 4, 9, 5, 11,
                    6, 12, 3, 13, 1, 14,
                    6, 12, 7, 15, 3, 13,
                    6, 16, 0, 17, 4, 18,
                    6, 16, 1, 19, 0, 17,
            }
    );

    private static final MeshPart RIGHT_LEG = new MeshPart(
            new float[] {
                    -0.181250F, -0.020001F, 0.175000F,
                    -0.181251F, 0.769999F, 0.175000F,
                    -0.181250F, -0.020001F, -0.175000F,
                    -0.181251F, 0.769999F, -0.175000F,
                    0.168750F, -0.020001F, 0.175000F,
                    0.168749F, 0.769999F, 0.175000F,
                    0.168750F, -0.020001F, -0.175000F,
                    0.168749F, 0.769999F, -0.175000F,
            },
            new float[] {
                    0.187528F, 0.456866F,
                    0.187528F, 0.127546F,
                    0.333429F, 0.456866F,
                    0.333429F, 0.127546F,
                    0.000026F, 0.456755F,
                    0.000025F, 0.127436F,
                    0.145927F, 0.456755F,
                    0.145927F, 0.127436F,
                    0.375004F, 0.456866F,
                    0.375004F, 0.127546F,
                    0.520905F, 0.456866F,
                    0.520905F, 0.127546F,
                    0.375015F, 0.456866F,
                    0.375015F, 0.127546F,
                    0.520916F, 0.456866F,
                    0.520916F, 0.127546F,
                    0.423143F, 0.176466F,
                    0.569044F, 0.176466F,
                    0.423143F, 0.030565F,
                    0.569044F, 0.030565F,
            },
            new int[] {
                    0, 0, 1, 1, 2, 2,
                    1, 1, 3, 3, 2, 2,
                    4, 4, 5, 5, 0, 6,
                    5, 5, 1, 7, 0, 6,
                    6, 8, 7, 9, 4, 10,
                    7, 9, 5, 11, 4, 10,
                    2, 12, 3, 13, 6, 14,
                    3, 13, 7, 15, 6, 14,
                    7, 16, 3, 17, 5, 18,
                    3, 17, 1, 19, 5, 18,
            }
    );

    private static final MeshPart LEFT_LEG = new MeshPart(
            new float[] {
                    0.181250F, -0.020001F, 0.175000F,
                    0.181250F, -0.020001F, -0.175000F,
                    0.181249F, 0.769999F, 0.175000F,
                    0.181249F, 0.769999F, -0.175000F,
                    -0.168750F, -0.020001F, 0.175000F,
                    -0.168751F, 0.769999F, 0.175000F,
                    -0.168750F, -0.020001F, -0.175000F,
                    -0.168751F, 0.769999F, -0.175000F,
            },
            new float[] {
                    0.812530F, 0.456939F,
                    0.666629F, 0.456939F,
                    0.812530F, 0.127620F,
                    0.666629F, 0.127620F,
                    1.000003F, 0.456887F,
                    0.854102F, 0.456887F,
                    1.000003F, 0.127568F,
                    0.854102F, 0.127568F,
                    0.625159F, 0.456939F,
                    0.479258F, 0.456939F,
                    0.625159F, 0.127620F,
                    0.479258F, 0.127620F,
                    0.625125F, 0.456939F,
                    0.479224F, 0.456939F,
                    0.625125F, 0.127620F,
                    0.479224F, 0.127620F,
                    0.417284F, 0.166701F,
                    0.417284F, 0.020800F,
                    0.563185F, 0.166701F,
                    0.563185F, 0.020800F,
            },
            new int[] {
                    0, 0, 1, 1, 2, 2,
                    2, 2, 1, 1, 3, 3,
                    4, 4, 0, 5, 5, 6,
                    5, 6, 0, 5, 2, 7,
                    6, 8, 4, 9, 7, 10,
                    7, 10, 4, 9, 5, 11,
                    1, 12, 6, 13, 3, 14,
                    3, 14, 6, 13, 7, 15,
                    7, 16, 5, 17, 3, 18,
                    3, 18, 5, 17, 2, 19,
            }
    );

    private static final MeshPart RIGHT_ARM = new MeshPart(
            new float[] {
                    0.087500F, -0.150000F, 0.150000F,
                    0.087500F, -0.150000F, -0.150000F,
                    0.087500F, 0.644999F, 0.150000F,
                    0.087500F, 0.644999F, -0.150000F,
                    -0.212500F, -0.150000F, 0.150000F,
                    -0.212500F, 0.644999F, 0.149999F,
                    -0.212499F, -0.150000F, -0.150000F,
                    -0.212499F, 0.644999F, -0.150000F,
            },
            new float[] {
                    0.331428F, 0.480105F,
                    0.331428F, 0.355047F,
                    0.000025F, 0.480105F,
                    0.000025F, 0.355046F,
                    0.331428F, 0.636748F,
                    0.331428F, 0.511690F,
                    0.000025F, 0.636748F,
                    0.000025F, 0.511690F,
                    0.331428F, 0.793129F,
                    0.331428F, 0.668071F,
                    0.000025F, 0.793129F,
                    0.000025F, 0.668071F,
                    0.331494F, 0.953576F,
                    0.331494F, 0.828518F,
                    0.000090F, 0.953576F,
                    0.000090F, 0.828518F,
                    0.292940F, 0.865263F,
                    0.417998F, 0.865263F,
                    0.292940F, 0.740205F,
                    0.417998F, 0.740205F,
                    0.093288F, 0.095252F,
                    0.093288F, 0.220310F,
                    0.218346F, 0.095252F,
                    0.218346F, 0.220310F,
            },
            new int[] {
                    0, 0, 1, 1, 2, 2,
                    2, 2, 1, 1, 3, 3,
                    4, 4, 0, 5, 5, 6,
                    5, 6, 0, 5, 2, 7,
                    6, 8, 4, 9, 7, 10,
                    7, 10, 4, 9, 5, 11,
                    1, 12, 6, 13, 3, 14,
                    3, 14, 6, 13, 7, 15,
                    6, 16, 1, 17, 4, 18,
                    4, 18, 1, 17, 0, 19,
                    7, 20, 5, 21, 3, 22,
                    3, 22, 5, 21, 2, 23,
            }
    );

    private static final MeshPart HEAD = new MeshPart(
            new float[] {
                    -0.300000F, -0.000000F, -0.300000F,
                    0.300000F, 0.000000F, -0.300000F,
                    -0.300000F, -0.549999F, -0.300000F,
                    0.300000F, -0.549999F, -0.300000F,
                    -0.300000F, -0.000000F, 0.300000F,
                    -0.300000F, -0.549999F, 0.300000F,
                    0.300000F, -0.549999F, 0.300000F,
                    0.300000F, 0.000000F, 0.300000F,
            },
            new float[] {
                    0.374942F, 0.770491F,
                    0.625058F, 0.770491F,
                    0.374942F, 0.999764F,
                    0.625058F, 0.999764F,
                    0.124826F, 0.770491F,
                    0.124826F, 0.999764F,
                    0.875097F, 0.999746F,
                    0.875097F, 0.770473F,
                    1.125213F, 0.770473F,
                    0.875174F, 0.770491F,
                    0.875174F, 0.999764F,
                    0.374942F, 0.312623F,
                    0.374942F, 0.062507F,
                    0.625058F, 0.312623F,
                    0.625058F, 0.062507F,
                    1.125213F, 0.999746F,
                    0.624066F, 0.762299F,
                    0.373950F, 0.512183F,
                    0.373950F, 0.762299F,
                    0.624066F, 0.512183F,
            },
            new int[] {
                    0, 0, 1, 1, 2, 2,
                    2, 2, 1, 1, 3, 3,
                    4, 4, 0, 0, 5, 5,
                    5, 5, 0, 0, 2, 2,
                    4, 6, 5, 7, 6, 8,
                    1, 1, 7, 9, 3, 3,
                    3, 3, 7, 9, 6, 10,
                    3, 11, 6, 12, 2, 13,
                    2, 13, 6, 12, 5, 14,
                    4, 6, 6, 8, 7, 15,
                    7, 16, 0, 17, 4, 18,
                    7, 16, 1, 19, 0, 17,
            }
    );

    private static final MeshPart LEFT_ARM = new MeshPart(
            new float[] {
                    -0.087500F, -0.150000F, -0.150000F,
                    -0.087500F, -0.150000F, 0.150000F,
                    -0.087500F, 0.644999F, -0.150000F,
                    -0.087500F, 0.644999F, 0.150000F,
                    0.212499F, -0.150000F, -0.150000F,
                    0.212499F, 0.644999F, -0.150000F,
                    0.212499F, -0.150000F, 0.150000F,
                    0.212499F, 0.644999F, 0.150000F,
            },
            new float[] {
                    0.668572F, 0.480105F,
                    0.668572F, 0.355047F,
                    0.999975F, 0.480105F,
                    0.999975F, 0.355046F,
                    0.668572F, 0.636748F,
                    0.668572F, 0.511690F,
                    0.999975F, 0.636748F,
                    0.999975F, 0.511690F,
                    0.668572F, 0.793129F,
                    0.668572F, 0.668071F,
                    0.999975F, 0.793129F,
                    0.999975F, 0.668071F,
                    0.668506F, 0.953576F,
                    0.668506F, 0.828518F,
                    0.999910F, 0.953576F,
                    0.999910F, 0.828518F,
                    0.707060F, 0.865263F,
                    0.582002F, 0.865263F,
                    0.707060F, 0.740205F,
                    0.582002F, 0.740205F,
                    0.906712F, 0.095252F,
                    0.906712F, 0.220310F,
                    0.781654F, 0.095252F,
                    0.781654F, 0.220310F,
            },
            new int[] {
                    0, 0, 1, 1, 2, 2,
                    2, 2, 1, 1, 3, 3,
                    4, 4, 0, 5, 5, 6,
                    5, 6, 0, 5, 2, 7,
                    6, 8, 4, 9, 7, 10,
                    7, 10, 4, 9, 5, 11,
                    1, 12, 6, 13, 3, 14,
                    3, 14, 6, 13, 7, 15,
                    6, 16, 1, 17, 4, 18,
                    4, 18, 1, 17, 0, 19,
                    7, 20, 5, 21, 3, 22,
                    3, 22, 5, 21, 2, 23,
            }
    );

    private ShieldFieldMesh() {}

    public static void render(
            PoseStack poseStack,
            VertexConsumer buffer,
            PlayerModel<AbstractClientPlayer> source
    ) {
        renderPart(poseStack, buffer, source.head, HEAD);
        renderPart(poseStack, buffer, source.body, BODY);
        renderPart(poseStack, buffer, source.rightArm, RIGHT_ARM);
        renderPart(poseStack, buffer, source.leftArm, LEFT_ARM);
        renderPart(poseStack, buffer, source.rightLeg, RIGHT_LEG);
        renderPart(poseStack, buffer, source.leftLeg, LEFT_LEG);
    }

    private static void renderPart(
            PoseStack poseStack,
            VertexConsumer buffer,
            ModelPart sourcePart,
            MeshPart mesh
    ) {
        if (!sourcePart.visible || sourcePart.skipDraw) {
            return;
        }

        poseStack.pushPose();
        sourcePart.translateAndRotate(poseStack);
        mesh.render(poseStack.last(), buffer);
        poseStack.popPose();
    }

    private record MeshPart(
            float[] positions,
            float[] uvs,
            int[] triangles
    ) {
        private void render(PoseStack.Pose pose, VertexConsumer buffer) {
            Vector3f edgeA = new Vector3f();
            Vector3f edgeB = new Vector3f();
            Vector3f normal = new Vector3f();

            for (int i = 0; i < triangles.length; i += 6) {
                int p0 = triangles[i];
                int uv0 = triangles[i + 1];
                int p1 = triangles[i + 2];
                int uv1 = triangles[i + 3];
                int p2 = triangles[i + 4];
                int uv2 = triangles[i + 5];

                float x0 = positions[p0 * 3];
                float y0 = positions[p0 * 3 + 1];
                float z0 = positions[p0 * 3 + 2];
                float x1 = positions[p1 * 3];
                float y1 = positions[p1 * 3 + 1];
                float z1 = positions[p1 * 3 + 2];
                float x2 = positions[p2 * 3];
                float y2 = positions[p2 * 3 + 1];
                float z2 = positions[p2 * 3 + 2];

                edgeA.set(x1 - x0, y1 - y0, z1 - z0);
                edgeB.set(x2 - x0, y2 - y0, z2 - z0);
                edgeA.cross(edgeB, normal).normalize();

                emit(buffer, pose, x0, y0, z0, uv0, normal);
                emit(buffer, pose, x1, y1, z1, uv1, normal);
                emit(buffer, pose, x2, y2, z2, uv2, normal);
            }
        }

        private void emit(
                VertexConsumer buffer,
                PoseStack.Pose pose,
                float x,
                float y,
                float z,
                int uvIndex,
                Vector3f normal
        ) {
            buffer.addVertex(pose, x, y, z)
                    .setColor(0xFFFFFFFF)
                    .setUv(uvs[uvIndex * 2], uvs[uvIndex * 2 + 1])
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(LightTexture.FULL_BRIGHT)
                    .setNormal(pose, normal.x(), normal.y(), normal.z());
        }
    }
}
