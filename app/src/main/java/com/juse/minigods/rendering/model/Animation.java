package com.juse.minigods.rendering.model;

import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Animation {
    private String name;
    private double ticksPerSeconds, duration;

    private class MeshChannel {
        private String name;
        private KeyValue<Integer> keys[];
    }

    private class MorphMeshChannel {
        private String name; // move name as a key to hashmap instead ????? :o
        private MeshMorphKey keys[];

        private class MeshMorphKey {
            private double weights[];
            private int values[];
            private double time;
        }
    }

    private class NodeChannel {
        private String name;
        private int beforeState, afterState;

        private KeyValue<Vector3f> positionKey, scalingKey;
        private KeyValue<Quaternionf> rotationKey;
    }

    private class KeyValue<T> {
        private double time;
        private T value;
    }
}
