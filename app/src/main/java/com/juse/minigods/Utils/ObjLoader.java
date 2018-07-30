package com.juse.minigods.Utils;

import android.content.res.AssetManager;
import android.opengl.GLES31;

import com.juse.minigods.rendering.Material.MaterialBuilder;
import com.juse.minigods.reporting.CrashManager;

import org.joml.Vector3f;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ObjLoader {
    private static final String MODEL_PATH = "models/%s.obj";
    private static final String MTL_LIB = "mtllib", MTL_USE = "usemtl",
            VERTEX = "v", VERTEX_NORMAL = "vn", FACE = "f", SPLIT = " ", FACE_SPLIT = "/";
    private static final int FACE_LEN = 3, UNAVAILABLE = -1;

    private String name;
    private String mtlLib, mtl;

    private ArrayList<Vector3f> vertices, normals, texCoords /* NYI */;
    private ArrayList<ArrayList<FacePart>> faces;

    public ObjLoader(String name) {
        this.name = name;

        vertices = new ArrayList<>();
        normals = new ArrayList<>();
        texCoords = new ArrayList<>();

        faces = new ArrayList<>();
    }

    public MaterialBuilder load(AssetManager assetManager) {
        try(InputStream stream = assetManager.open(String.format(MODEL_PATH, name))) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                applyLine(line);
            }

            return buildModelBuilder();
        } catch (IOException e) {
            CrashManager.ReportCrash(CrashManager.CrashType.IO, "Error loading file" + name, e);
            return null;
        }
    }

    private MaterialBuilder buildModelBuilder() {
        MaterialBuilder materialBuilder = new MaterialBuilder();

        // OpenGl normal and vertex has to be on the same "node", in obj they can have different indices
        ArrayList<FacePart> filteredFaceParts = new ArrayList<>();
        ArrayList<Integer> facePartIndices = new ArrayList<>();
        faces.forEach(faceParts ->
            faceParts.forEach(facePart -> {
                int index = filteredFaceParts.indexOf(facePart);
                if(index == -1) {
                    facePartIndices.add(filteredFaceParts.size());
                    filteredFaceParts.add(facePart);
                } else {
                    facePartIndices.add(index);
                }
            })
        );

        addVertexData(filteredFaceParts, materialBuilder);

        materialBuilder.setIndices(
                DataUtils.ToBuffer(facePartIndices.toArray(new Integer[]{})),
                facePartIndices.size(), 0
        );

        return materialBuilder;
    }

    private void addVertexData(ArrayList<FacePart> filteredFaceParts, MaterialBuilder builder) {
        ArrayList<Vector3f> vertexData = new ArrayList<>();

        if (!normals.isEmpty()) {
            // if it has normals
            filteredFaceParts.forEach(facePart -> {
                vertexData.add(vertices.get(facePart.vertexIndex - 1));
                vertexData.add(normals.get(facePart.normalIndex - 1));
            });

            builder.setVertices(DataUtils.ToBuffer(toVec3Array(vertexData)), vertexData.size(),
                    GLES31.GL_STATIC_DRAW, new int[] {3, 3}, new int[] {0, 1},
                    new int[] {Float.BYTES * 6, Float.BYTES * 6}, new int[] {0, Float.BYTES * 3});
        } else {
            // if only vertices
            filteredFaceParts.forEach(facePart -> vertexData.add(vertices.get(facePart.vertexIndex)));
        }
    }

    private Vector3f[] toVec3Array(ArrayList<Vector3f> vertices) {
        return vertices.toArray(new Vector3f[]{});
    }

    private void applyLine(String line) {
        String split[] = line.split(SPLIT);

        switch (split[0]) {
            case MTL_LIB:
                mtlLib = split[1];
                break;
            case MTL_USE:
                mtl = split[1];
                break;
            case VERTEX:
                vertices.add(toVec3(split, 1));
                break;
            case VERTEX_NORMAL:
                normals.add(toVec3(split, 1).normalize());
                break;
            case FACE:
                ArrayList<FacePart> faceParts = new ArrayList<>();
                for (int i = 1; i < split.length; i++) {
                    faceParts.add(getFace(split, i));
                }
                faces.add(faceParts);
                break;
        }
    }

    private FacePart getFace(String[] split, int i) {
        String faceSplit[] = split[i].split(FACE_SPLIT);
        int vertex = UNAVAILABLE, tex = UNAVAILABLE, normal = UNAVAILABLE;
        
        // inefficient but should be converted to simple code by compilor
        for (int j = 0; j < FACE_LEN; j++) {
            String number = faceSplit[j];
            if (!number.isEmpty()) {
                switch (j) {
                    case 0:
                        vertex = Integer.parseInt(number);
                        break;
                    case 1:
                        tex = Integer.parseInt(number);
                        break;
                    case 2:
                        normal = Integer.parseInt(number);
                        break;
                }
            }
        }
        
        return new FacePart(vertex, tex, normal);
    }

    private Vector3f toVec3(String[] split, int offset) {
        return new Vector3f(
                Float.parseFloat(split[offset]),
                Float.parseFloat(split[offset + 1]),
                Float.parseFloat(split[offset + 2])
        );
    }

    private class FacePart {
        public int vertexIndex, texIndex, normalIndex;
        public FacePart(int vertexIndex, int texIndex, int normalIndex) {
            this.vertexIndex = vertexIndex;
            this.texIndex = texIndex;
            this.normalIndex = normalIndex;
        }
    }

    private class FaceVertex {
        public Vector3f vertex, texCoord, normal;
        public FaceVertex(Vector3f vertex, Vector3f texCoord, Vector3f normal) {
            this.vertex = vertex;
            this.texCoord = texCoord;
            this.normal = normal;
        }
    }
}
