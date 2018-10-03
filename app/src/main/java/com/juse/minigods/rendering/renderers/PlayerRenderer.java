package com.juse.minigods.rendering.renderers;

import android.content.Context;
import android.content.res.AssetManager;

import com.juse.minigods.Utils.DataUtils;
import com.juse.minigods.game.Player;
import com.juse.minigods.rendering.Material.MaterialBuilder;
import com.juse.minigods.rendering.MaterialManager;
import com.juse.minigods.rendering.ShaderManager;
import com.juse.minigods.rendering.model.AnimatedModelMaterial;
import com.juse.minigods.rendering.model.Animation;
import com.juse.minigods.rendering.model.Model;
import com.juse.minigods.rendering.model.ModelLoader;
import com.juse.minigods.reporting.CrashManager;

import org.joml.Matrix4f;

import java.io.IOException;

import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_VERTEX_SHADER;

/**
 * Created by LukasW on 2018-03-22.
 * Renders the player but does not change any data
 */

public class PlayerRenderer implements RendererInterface {
    private final static String VS = "player/vertex", FS = "player/fragment";
    private final static int BASE_SHADER_UNIFORM_ID = 56;

    // before player model is done
    private static final float[] model = {
           0.f, 1.f, 0.f,   0.f, 0.f, 1.f,  0.f, 0.f,
           1.5f, 1.f, 0.f,   0.f, 0.f, 1.f,  1.f, 0.f,
           0.f, 0.f, 0.f,   0.f, 0.f, 1.f,  0.f, 1.f,

           1.5f, 1.f, 0.f,   0.f, 0.f, 1.f,  1.f, 0.f,
           1.5f, 0.f, 0.f,   0.f, 0.f, 1.f,  1.f, 1.f,
           0.f, 0.f, 0.f,   0.f, 0.f, 1.f,  0.f, 1.f,
    };

    private Player player;
    private int renderPass;

    private Model playerModel;
    private Animation playerRunAnimation, playerDeathAnimation;

    private AnimatedModelMaterial animatedModelMaterial;
    private ModelLoader modelLoader;

    public PlayerRenderer(Context context,  Player player) {
        String fileNames[] = {"models/dog/BeagleDefault.fbx", "models/dog/BeagleRun.fbx", "models/dog/BeagleDeath.fbx"};
        Model models[] = new ModelLoader().loadModels(fileNames, context, context.getAssets());
        assert models != null;

        playerModel = models[0];
        playerRunAnimation = models[1].animations[models[1].getAnimationIndex("Take 001")];
        playerDeathAnimation = models[2].animations[models[2].getAnimationIndex("Take 001")];
        animatedModelMaterial = new AnimatedModelMaterial(playerModel,"Take 001");
        animatedModelMaterial.setAnimationSpeed(0.85f);

        player.setObserver(() -> {
            if (!player.isOutOfBounds())
                animatedModelMaterial.resetAnimation();
        });
        this.player = player;
    }

    public void setup(ShaderManager shaderManager, MaterialManager materialManager, AssetManager assets) {
        modelLoader = new ModelLoader();

        int vs, fs;
        try {
            vs = loadShader(shaderManager, assets, GL_VERTEX_SHADER, VS);
            fs = loadShader(shaderManager, assets, GL_FRAGMENT_SHADER, FS);
        } catch (IOException e) {
            CrashManager.ReportCrash(CrashManager.CrashType.GRAPHICS, "Error loading: " +
                    "vs / fs in player renderer", e);
            return;
        }

        AnimatedModelMaterial.AnimatedShaderInfo info = new AnimatedModelMaterial.AnimatedShaderInfo();
        info.vertexLoc              = 0;
        info.normalLoc              = 1;
        info.uvLoc                  = 2;
        info.boneIdLocation         = 3;
        info.boneWeightLocation     = 4;
        info.boneUniformLocation    = 5;

        renderPass = materialManager.createRenderPass(vs, fs, shaderManager);

        MaterialBuilder builder = new MaterialBuilder();
        animatedModelMaterial.setLocation(BASE_SHADER_UNIFORM_ID, DataUtils.ToBuffer(new Matrix4f().translate(0.f, 0.f, 3.f)));
        animatedModelMaterial.buildMaterial(renderPass, assets, builder, info);
    }

    public void render(ShaderManager shaderManager, MaterialManager materialManager) {
        materialManager.render(renderPass, BASE_SHADER_UNIFORM_ID + 1, BASE_SHADER_UNIFORM_ID + 2, BASE_SHADER_UNIFORM_ID + 3, animatedModelMaterial);
    }

    public void update(MaterialManager materialManager) {
        animatedModelMaterial.updateLocation(
            DataUtils.ToBuffer(
                    new Matrix4f()
                            .translate(player.getPosition())
                            .scale(0.022f)
                            .rotateY(-(float) Math.PI / 2)
            )
        );

        if (player.isDead() && !player.isOutOfBounds()) {
            animatedModelMaterial.update(playerDeathAnimation, false);
        } else {
            animatedModelMaterial.update(playerRunAnimation, true);
        }
    }
}
