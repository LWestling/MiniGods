package com.juse.minigods.rendering.model;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashMap;

public class Animation {
    private String name;
    private double ticksPerSeconds, duration;

    private NodeChannel channels[];
    private HashMap<String, Integer> channelIndices;

    public Animation(String name, double ticksPerSeconds, double duration) {
        this.name = name;
        this.ticksPerSeconds = ticksPerSeconds;
        this.duration = duration;
    }

    private void setNodeChannels(NodeChannel channels[]) {
        this.channels = channels;
        channelIndices = new HashMap<>(channels.length);

        for (int i = 0; i < channels.length; i++) {
            channelIndices.put(channels[i].name, i);
        }
    }

    public NodeChannel getNodeChannel(String name) {
        int index = channelIndices.getOrDefault(name, -1);
        if (index == -1) return null;

        return channels[index];
    }

    public class NodeChannel {
        private String name;
        private int beforeState, afterState;

        private KeyValue<Vector3f> positionKeys[], scalingKeys[];
        private KeyValue<Quaternionf> rotationKeys[];

        public NodeChannel(String name, int beforeState, int afterState, KeyValue<Vector3f> positionKeys[],
                           KeyValue<Vector3f> scalingKeys[], KeyValue<Quaternionf> rotationKeys[]) {
            this.name = name;
            this.beforeState = beforeState;
            this.afterState = afterState;

            this.positionKeys = positionKeys;
            this.scalingKeys = scalingKeys;
            this.rotationKeys = rotationKeys;
        }

        public String getName() {
            return name;
        }

        public int getBeforeState() {
            return beforeState;
        }

        public int getAfterState() {
            return afterState;
        }

        public KeyValue<Vector3f>[] getPositionKeys() {
            return positionKeys;
        }

        public KeyValue<Vector3f>[] getScalingKeys() {
            return scalingKeys;
        }

        public KeyValue<Quaternionf>[] getRotationKeys() {
            return rotationKeys;
        }
    }

    // A Value in time, for example a rotation at 3 seconds.
    public class KeyValue<T> {
        private double time;
        private T value;

        public KeyValue(T value, double time) {
            this.value = value;
            this.time = time;
        }

        public double getTime() {
            return time;
        }

        public T getValue() {
            return value;
        }
    }

    public String getName() {
        return name;
    }

    public double getTicksPerSeconds() {
        return ticksPerSeconds;
    }

    public double getDuration() {
        return duration;
    }

    public NodeChannel[] getChannels() {
        return channels;
    }
}

/*
    // probably ununsed
    private class MeshChannel {
        private String name;
        private KeyValue<Integer> keys[];
    }

    // probably ununsed
    private class MorphMeshChannel {
        private String name; // move name as a key to hashmap instead ????? :o
        private MeshMorphKey keys[];

        private class MeshMorphKey {
            private double weights[];
            private int values[];
            private double time;
        }
    }
 */