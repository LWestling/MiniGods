package com.juse.minigods.rendering.model;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Generic Model converted aiScene (cpp) (assimp)
 */
public class Model {
    private static final int TOTAL_WEIGHTS = 4;
    public float[] vertices;
    public int[] indices;
    public String[] textures;

    public Bone[] bones;
    public Node rootNode;

    public Animation[] animations;

    public boolean useNormals, useAnimations;

    public HashMap<String, Integer> boneIndices, nodeIndices;
    public ArrayList<Node> nodes;

    public int boneIds[][];
    public float boneWeights[][];

    public Matrix4f boneFinalTransformation[];

    private Matrix4f rootInverse;

    public class Node {
        public String name;
        // transformation form nodeSpace to the nodeSpace of its parent
        public Matrix4f transformation;

        public Node parent;
        public Node children[];

        public int meshes[];
    }

    private void setBones(Bone[] bones) {
        this.bones = bones;

        boneFinalTransformation = new Matrix4f[50];
        for (int i = 0; i < boneFinalTransformation.length; i++) {
            boneFinalTransformation[i] = new Matrix4f();
        }

        boneIndices = new HashMap<>();
        boneIds = new int[vertices.length / 8][4];
        boneWeights = new float[vertices.length / 8][4];

        for (int i = 0; i < bones.length; i++) {
            Bone bone = bones[i];

            boneIndices.put(bone.name, i);
            boneFinalTransformation[i] = new Matrix4f(bone.transformation.transpose());

            for (int vertexWeightIndex = 0; vertexWeightIndex < bone.vertexWeights.length; vertexWeightIndex++) {
                VertexWeight weight = bone.vertexWeights[vertexWeightIndex];

                for (int j = 0; j < TOTAL_WEIGHTS; j++) {
                    if (boneWeights[weight.vertexId][j] == 0) {
                        boneIds[weight.vertexId][j] = i;
                        boneWeights[weight.vertexId][j] = weight.weight;
                        break;
                    }
                }
            }
        }
    }

    private void setRootNode(Node rootNode) {
        this.rootNode = rootNode;

        nodeIndices = new HashMap<>();
        nodes = new ArrayList<>();

        rootInverse = new Matrix4f();
        rootNode.transformation.invert(rootInverse);

        extractNodes(nodeIndices, rootNode);
    }

    private void extractNodes(HashMap<String, Integer> nodeIndices, Node node) {
        node.transformation.transpose();
        nodeIndices.put(node.name, nodes.size());
        nodes.add(node);

        for (Node child : node.children) {
            extractNodes(nodeIndices, child);
        }
    }

    public void updateBoneTransformations(Animation animation, double time) {
        double tick = (animation.getTicksPerSeconds() * time);
        tick %= animation.getDuration();

        updateNodeHierarchy(animation, rootNode, new Matrix4f(), tick);
    }

    private void updateNodeHierarchy(Animation animation, Node node, Matrix4f parentTransformation, double tick) {
        Matrix4f transformation = new Matrix4f(node.transformation);

        Animation.NodeChannel channel = animation.getNodeChannel(node.name);
        if (channel != null) {
            transformation = calculateMatrix(channel, tick);
        }
        parentTransformation.mul(transformation, transformation);

        int boneIndex = getBoneIndex(node.name);
        if (boneIndex >= 0) {
            // new matrix since transformation is also sent to children
            Matrix4f finalTrans = new Matrix4f();
            transformation.mul(bones[boneIndex].transformation, finalTrans);
            rootInverse.mul(finalTrans, finalTrans);
            boneFinalTransformation[boneIndex] = finalTrans;
        }

        for (Node child : node.children) {
            updateNodeHierarchy(animation, child, transformation, tick);
        }
    }

    private int getBoneIndex(String name) {
        return boneIndices.getOrDefault(name, -1);
    }

    private Matrix4f calculateMatrix(Animation.NodeChannel channel, double tick) {
        Vector3f animatedPosition = new Vector3f(), animatedScale = new Vector3f();
        Quaternionf animatedRotation = new Quaternionf();

        if (channel.getPositionKeys().length > 1) {
            int positionKey = getKeyFrameIndex(tick, channel.getPositionKeys());

            Animation.KeyValue<Vector3f> position = channel.getPositionKeys()[positionKey];
            Animation.KeyValue<Vector3f> nextPosition = channel.getPositionKeys()[positionKey + 1];

            double positionInterpolation = getInterpolation(position, nextPosition, tick);
            position.getValue().lerp(nextPosition.getValue(), (float) positionInterpolation, animatedPosition);
        } else {
            animatedPosition = channel.getPositionKeys()[0].getValue();
        }

        if (channel.getRotationKeys().length > 1) {
            int rotationKey = getKeyFrameIndex(tick, channel.getRotationKeys());

            Animation.KeyValue<Quaternionf> rotation = channel.getRotationKeys()[rotationKey];
            Animation.KeyValue<Quaternionf> nextRotation = channel.getRotationKeys()[rotationKey + 1];

            double rotationInterpolation = getInterpolation(rotation, nextRotation, tick);
            rotation.getValue().nlerp(rotation.getValue(), (float) rotationInterpolation, animatedRotation);
        } else {
            animatedRotation = channel.getRotationKeys()[0].getValue();
        }

        if (channel.getScalingKeys().length > 1) {
            int scaleKey = getKeyFrameIndex(tick, channel.getScalingKeys());

            Animation.KeyValue<Vector3f> scale = channel.getScalingKeys()[scaleKey];
            Animation.KeyValue<Vector3f> nextScale = channel.getScalingKeys()[scaleKey + 1];

            double scaleInterpolation = getInterpolation(scale, nextScale, tick);
            scale.getValue().lerp(nextScale.getValue(), (float) scaleInterpolation, animatedScale);
        } else {
            animatedScale = channel.getScalingKeys()[0].getValue();
        }

        Matrix4f transform = new Matrix4f().translate(animatedPosition);
        Matrix4f scale = new Matrix4f().scale(animatedScale);
        Matrix4f rotation = new Matrix4f().rotation(animatedRotation);

        return transform.mul(rotation.mul(scale));
    }

    private double getInterpolation(Animation.KeyValue current, Animation.KeyValue next, double tick) {
        double endTime = next.getTime() - current.getTime();
        double currentTime = tick - current.getTime();

        return currentTime / endTime;
    }

    private int getKeyFrameIndex(double tick, Animation.KeyValue values[]) {
        for (int i = 0; i < values.length - 1; i++) {
            if (tick < values[i + 1].getTime()) {
                return i;
            }
        }

        throw new InvalidParameterException("Invalid tick " + tick);
    }

    public int getAnimationIndex(String name) {
        for (int i = 0; i < animations.length; i++) {
            if (animations[i].getName().equals(name)) return i;
        }

        throw new InvalidParameterException("No animation with name");
    }
}
