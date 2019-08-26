package com.mertugrul.cargame01;

import java.lang.Math;
import java.util.ArrayList;
import java.util.Random;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.collision.Box;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.QuaternionEvaluator;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.math.Vector3Evaluator;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;

public class MainActivity extends AppCompatActivity  {

    private ArFragment arFragment;
    private Node carNode;
    private AnchorNode anchornode;
    private Node[] stararray = new Node[10];
    private final float BOARDSIZE = 1.2f;
    private int count = 0;
    private int score = 0;
    private TextView scoreText;
    private boolean createdGame = false;
    private MediaPlayer mp;


    Joystick joystick;
    private double direction = 0;
    private Handler directionHandler;
    private ObjectAnimator drive;
    Vector3 increment = new Vector3(0,0,0);


    // orientation values
    Quaternion[] orientations = {
    Quaternion.axisAngle(new Vector3(0.0f, 1.0f, 0.0f), 0),
    Quaternion.axisAngle(new Vector3(0.0f, 1.0f, 0.0f), 60),
    Quaternion.axisAngle(new Vector3(0.0f, 1.0f, 0.0f), 120),
    Quaternion.axisAngle(new Vector3(0.0f, 1.0f, 0.0f), 180),
    Quaternion.axisAngle(new Vector3(0.0f, 1.0f, 0.0f), 240),
     Quaternion.axisAngle(new Vector3(0.0f, 1.0f, 0.0f), 300),
     Quaternion.axisAngle(new Vector3(0.0f, 1.0f, 0.0f), 360) };


    Runnable directionChecker = new Runnable() {
        @Override
        public void run() {
            try {
                if( createdGame && joystick != null && carNode != null) {
                    direction = joystick.getDegrees();
                    increment = new Vector3((float) (Math.sin(direction)),0,(float)  (Math.cos(direction)));
                    carNode.setLookDirection(new Vector3((float) (-1*Math.cos(direction)),0,(float)  (Math.sin(direction))));
                    System.out.println("increment:" + increment.toString() + "location:"+carNode.getWorldPosition().toString());
                    if (drive != null && joystick.getMoving()) {
                        drive.end();
                        drive.setObjectValues(carNode.getWorldPosition(), Vector3.add(carNode.getWorldPosition(), increment.scaled(0.00035f)));
                        //carNode.setWorldRotation(Quaternion.axisAngle(new Vector3(0.0f, 1.0f, 0.0f), (float)direction));
                      //  if (joystick.getMoving())
                        drive.start();
                    }
                    ArrayList<Node> overlappedNodes = arFragment.getArSceneView().getScene().overlapTestAll(carNode);
                    for (Node node : overlappedNodes) {
                        if ( !node.equals(carNode)&& !node.equals(anchornode)) {
                            System.out.println("NODE REMOVED"+node.getLocalPosition().toString());
                            node.setParent(null);

                            score++;
                            scoreText.setText("Score: " + score);

                            try {
                                if (mp.isPlaying()) {
                                    mp.stop();
                                    mp.release();
                                    mp = MediaPlayer.create(getApplicationContext(), R.raw.money);
                                }
                                mp.start();
                            } catch (Exception e) {e.printStackTrace();}


                        }
                    }
                }
            } finally {

                directionHandler.postDelayed(directionChecker, 1);
            }
        }
    };


    @Override
    public void onBackPressed() {
            //Remove an anchor node
            if (anchornode != null) {
                arFragment.getArSceneView().getScene().removeChild(anchornode);
                anchornode.getAnchor().detach();
                anchornode.setParent(null);
                anchornode = null;
                createdGame = false;
                directionHandler.removeCallbacks(directionChecker);
                drive.end();

                Toast.makeText(this, "Anchornode deleted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Anchornode already null", Toast.LENGTH_SHORT).show();
                super.onBackPressed();
            }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mp= MediaPlayer.create(getApplicationContext(), R.raw.money);
        mp.setVolume(1, 1);
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.arFragment);
        scoreText = findViewById(R.id.textView);
        scoreText.setText("Score:" + score);

        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
            if (!createdGame) {
                createdGame = true;
                Anchor anchor = hitResult.createAnchor();
                anchornode = new AnchorNode(anchor);

                //rendering the car
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
                //rendering randomly generated stars
                buildStars();

                arFragment.getArSceneView().getScene().addChild(anchornode);
                //create joystick
                joystick = findViewById(R.id.joystick);
                directionHandler = new Handler();
                directionChecker.run();
            }
        });

    }


    /**private void startScreen(){
         Anchor startAnchor;
        for (Plane plane : arFragment.getArSceneView().getSession().getAllTrackables(Plane.class)) {
            if (plane.getType() == Plane.Type.HORIZONTAL_UPWARD_FACING
                && plane.getTrackingState() == TrackingState.TRACKING)
            {
             startAnchor = plane.createAnchor(plane.getCenterPose());
                break;
            }
        }
        if (startAnchor == null)return;
        else {
            anchornode = new AnchorNode(startAnchor);
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

        }
    }**/





    private void buildStars() {
        count = 0;
        for (int i = 0; i<10; i++){
            ModelRenderable.builder()
                    .setSource(this, Uri.parse("model.sfb"))
                    .build()
                    .thenAccept(modelRenderable -> addStar(modelRenderable))
                    .exceptionally(throwable -> {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setMessage(throwable.getMessage())
                                .show();
                        return null;
                    });
        }

    }

    private void addCar(ModelRenderable modelRenderable) {
        carNode = new Node();
        carNode.setParent(anchornode);
        carNode.setRenderable(modelRenderable);

        //car translation animation
        drive = new ObjectAnimator();
        drive.setObjectValues(carNode.getWorldPosition(),Vector3.add(carNode.getWorldPosition(),increment.scaled(0.00035f)));
        drive.setPropertyName("worldPosition");
        drive.setEvaluator(new Vector3Evaluator());
        drive.setRepeatCount(ObjectAnimator.INFINITE);
        drive.setRepeatMode(ObjectAnimator.RESTART);
        drive.setInterpolator(new LinearInterpolator());
        drive.setDuration(10);
        drive.setTarget(carNode);
    }
    private void addStar(ModelRenderable modelRenderable) {
        Node newStar = new Node();
        newStar.setParent(anchornode);
        newStar.setRenderable(modelRenderable);
        Random rand = new Random();
        float x = ( rand.nextFloat() - 0.5f )*BOARDSIZE;
        float z = (rand.nextFloat() - 0.5f )*BOARDSIZE;
        Vector3 pos = new Vector3(x,0.0f,z);
        newStar.setLocalPosition(pos);

        //rotation animation
        ObjectAnimator rotator = new ObjectAnimator();
        rotator.setObjectValues(orientations[0],orientations[1],orientations[2],orientations[3],orientations[4],orientations[5]);
        rotator.setPropertyName("localRotation");
        rotator.setEvaluator(new QuaternionEvaluator());
        rotator.setRepeatCount(ObjectAnimator.INFINITE);
        rotator.setRepeatMode(ObjectAnimator.RESTART);
        rotator.setInterpolator(new LinearInterpolator());
        rotator.setDuration(1200);
        rotator.setAutoCancel(true);
        rotator.setTarget(newStar);
        rotator.start();

        //store in array
        if (count < 10) {
            stararray[count]= newStar;
            count++; }
        else
            count = 0;
    }




}
