package com.mertugrul.cargame01;

import java.lang.Math;
import java.util.Random;

import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

public class MainActivity extends AppCompatActivity {

    private ArFragment arFragment;
    private TransformableNode carNode;
    private AnchorNode anchornode;
    private Node[] stararray = new Node[10];
    private int count = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);



        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.arFragment);
        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
            Anchor anchor = hitResult.createAnchor();
            anchornode = new AnchorNode(anchor);


        ModelRenderable.builder()
                .setSource(this, Uri.parse("car_02.sfb"))
                .build()
                .thenAccept(modelRenderable -> addCar(modelRenderable))
                .exceptionally(throwable -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(throwable.getMessage())
                            .show();
                    return null;
                });
         for (int i = 0; i<10; i++){
            ModelRenderable.builder()
                    .setSource(this, Uri.parse("flying sacuer.sfb"))
                    .build()
                    .thenAccept(modelRenderable -> addStar(modelRenderable))
                    .exceptionally(throwable -> {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setMessage(throwable.getMessage())
                                .show();
                        return null;
                    });
        }

            arFragment.getArSceneView().getScene().addChild(anchornode);

        });

    }

    private void addCar(ModelRenderable modelRenderable) {
        carNode = new TransformableNode(arFragment.getTransformationSystem());
        carNode.setParent(anchornode);
        carNode.setRenderable(modelRenderable);
        carNode.select();
    }
    private void addStar(ModelRenderable modelRenderable) {
        Node newStar = new Node();
        newStar.setParent(anchornode);
        newStar.setRenderable(modelRenderable);
        Random rand = new Random();
        float x = rand.nextFloat();
        float z = rand.nextFloat();
        x -= 0.5f;
        z -= 0.5f;

        Vector3 pos = new Vector3(x,0.0f,z);
        newStar.setLocalPosition (pos);
        if (count < 10) {
            stararray[count]= newStar;
            count++; }
        else
            count = 0;
    }




}
