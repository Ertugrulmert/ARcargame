package com.mertugrul.cargame01;

import java.lang.Math;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.Anchor;

import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.QuaternionEvaluator;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.math.Vector3Evaluator;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;

public class MainActivity extends AppCompatActivity {

    private ArFragment arFragment;
    private Anchor startAnchor = null;
    private Node startNode;
    private Node carNode;
    private AnchorNode anchornode = null;
    private Node[] stararray = new Node[10];
    private final float BOARDSIZE = 1.2f;
    private Scene scene;
    private Camera camera;


    private CountDownTimer gameTimer;
    private int count = 0;
    private int score = 0;
    private TextView scoreText;
    private TextView timeText;
    private boolean createdGame = false;
    private MediaPlayer mp;


    Joystick joystick;
    private double direction = 0;
    private Handler directionHandler;
    Vector3 increment = new Vector3(0,0,0);

    private ObjectAnimator fall;
    private ObjectAnimator drive;

    private Quaternion turnQuaternion;
    private float turnAngle;


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
                /**if( arFragment.getArSceneView().getArFrame() != null &&!createdGame && startAnchor == null
                        && arFragment.getArSceneView().getArFrame().getCamera().getTrackingState() == TrackingState.TRACKING )  {
                    startScreen();
                     System.out.println("inside if"); }
                **/
                if( createdGame && joystick != null && carNode != null) {

                    if (drive != null && joystick.getMoving()) {
                        direction = joystick.getDegrees();
                        increment = new Vector3((float) (Math.sin(direction)),0,(float)  (Math.cos(direction)));
                        drive.end();
                        drive.setObjectValues(carNode.getWorldPosition(), Vector3.add(carNode.getWorldPosition(), carNode.getLeft().scaled(0.003f)));
                        //carNode.setWorldRotation(Quaternion.axisAngle(new Vector3(0.0f, 1.0f, 0.0f), (float)direction));
                      //  if (joystick.getMoving())

                       turnAngle = Vector3.angleBetweenVectors(carNode.getLeft(),increment);
                       if (Vector3.cross(carNode.getLeft(),increment).y < 0)
                           turnAngle = -turnAngle;
                       turnAngle *= 0.009f;
                       turnQuaternion = Quaternion.multiply(carNode.getLocalRotation(),new Quaternion(new Vector3(0,1.0f,0),turnAngle));
                        carNode.setLocalRotation(turnQuaternion);

                        drive.start();


                        System.out.println("increment:" + increment.toString() + "location:"+carNode.getWorldPosition().toString());



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

                directionHandler.postDelayed(directionChecker, 3);
            }
        }
    };

    @Override
    public void onBackPressed() {
            //Remove an anchor node
            if (anchornode != null) {
                gameTimer.cancel();
                arFragment.getArSceneView().getScene().removeChild(anchornode);
                anchornode.getAnchor().detach();
                anchornode.setParent(null);
                anchornode = null;
                createdGame = false;
                directionHandler.removeCallbacks(directionChecker);
                drive.end();
                startScreen();
                score=0;
                scoreText.setText("");
                timeText.setText("");

            } else {
                System.out.println("Quitting game");
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
        scoreText.setTextColor(Color.WHITE);
        scoreText.setText("");
        timeText = findViewById(R.id.textView2);
        timeText.setTextColor(Color.WHITE);
        timeText.setText("");
        gameTimer = new CountDownTimer(10000,1000) {
            @Override
            public void onTick(long l) {
                timeText.setText("Time Left: "+ l/1000);
            }

            @Override
            public void onFinish() {
                drive.end();
                createdGame = false;
                endScreen();
            }
        };

        scene = arFragment.getArSceneView().getScene();
        camera = scene.getCamera();
        startScreen();

        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
            if (!createdGame) {
                createdGame = true;
                scoreText.setText("Score:" + score);
                startNode.setParent(null);
                Anchor anchor = hitResult.createAnchor();
                anchornode = new AnchorNode(anchor);
                score = 0;
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
                gameTimer.start();
                directionHandler = new Handler();
                directionChecker.run();
            }
        });



    }

    private void endScreen() {
        drive.end();
        Node endnode = new Node();
        ModelRenderable.builder()
                .setSource(this, Uri.parse("end.sfb"))
                .build()
                .thenAccept(modelRenderable -> {
                    endnode.setRenderable(modelRenderable);
                    anchornode.addChild(endnode);
                })
                .exceptionally(throwable -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(throwable.getMessage())
                            .show();
                    return null;
                });
        endnode.setLocalPosition(Vector3.add(carNode.getLocalPosition(),new Vector3(0f,1.0f,0)));
        endnode.setLocalRotation(Quaternion.axisAngle(new Vector3(1.0f, 0.0f, 0), -90f));
        fall = new ObjectAnimator();
        fall.setObjectValues(endnode.getLocalPosition(),Vector3.add(carNode.getLocalPosition(),new Vector3(0.059f,0,0.22f)));
        fall.setPropertyName("localPosition");
        fall.setEvaluator(new Vector3Evaluator());
        fall.setRepeatCount(0);
        fall.setInterpolator(new AccelerateDecelerateInterpolator());
        fall.setDuration(1000);
        fall.setTarget(endnode);
        fall.start();

    }


    private void startScreen(){
        Plane plane;
       // if (arFragment.getArSceneView().getArFrame().getUpdatedAnchors().iterator().hasNext())
          //  Anchor = arFragment.getArSceneView().getArFrame().getUpdatedTrackables(Class Plane).iterator().next();
        System.out.println("inside start screen");
        Pose pose = Pose.makeTranslation(0.0f,0.0f,0.5f);

                ModelRenderable.builder()
                        .setSource(this, Uri.parse("tinker.sfb"))
                        .build()
                        .thenAccept(modelRenderable -> {

                            startNode = new Node();
                            startNode.setRenderable(modelRenderable);
                            scene.addChild(startNode);
                            startNode.setLocalPosition(Vector3.add(Vector3.add(camera.getLocalPosition(),camera.getDown()),camera.getForward()));
                            startNode.setLocalRotation(Quaternion.axisAngle(new Vector3(1.0f, 0.0f, 0), -80f));



                        })
                        .exceptionally(throwable -> {
                            AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            builder.setMessage(throwable.getMessage())
                                    .show();
                            return null;
                        });
                System.out.println("start built");
        }





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
