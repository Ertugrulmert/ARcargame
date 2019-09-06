package com.mertugrul.cargame01;

import java.lang.Math;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Switch;
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
import com.google.ar.sceneform.rendering.PlaneRenderer;
import com.google.ar.sceneform.rendering.Texture;
import com.google.ar.sceneform.ux.ArFragment;

public class MainActivity extends AppCompatActivity {

    ScoreRepository scoreRepository;

    private ArFragment arFragment;
    private Anchor startAnchor = null;
    private Node startNode;
    private Node carNode;
    private AnchorNode anchornode = null;
    private Node[] stararray = new Node[10];
    private final float BOARDSIZE = 1.2f;
    private Scene scene;
    private Camera camera;

    private ConstraintLayout constraintLayout;
    private Switch directionSwitch;

    private CountDownTimer gameTimer;
    private int count = 0;
    private int score = 0;
    private TextView scoreText;
    private TextView timeText;
    private TextView highScoreText;
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
                        drive.end();
                        //setting forward or reverse direction
                        if (directionSwitch.isChecked()) {
                            increment = new Vector3((float) (-1 * Math.sin(direction)), 0, (float) (-1 * Math.cos(direction)));
                            drive.setObjectValues(carNode.getWorldPosition(), Vector3.add(carNode.getWorldPosition(), carNode.getRight().scaled(0.003f)));
                        }
                        else {
                            increment = new Vector3((float) (Math.sin(direction)), 0, (float) (Math.cos(direction)));
                            drive.setObjectValues(carNode.getWorldPosition(), Vector3.add(carNode.getWorldPosition(), carNode.getLeft().scaled(0.003f)));
                        }

                        //carNode.setWorldRotation(Quaternion.axisAngle(new Vector3(0.0f, 1.0f, 0.0f), (float)direction));
                      //  if (joystick.getMoving())

                       turnAngle = Vector3.angleBetweenVectors(carNode.getLeft(),increment);
                       if (Vector3.cross(carNode.getLeft(),increment).y < 0)
                           turnAngle = -turnAngle;
                       turnAngle *= 0.011f;
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
                score=0;
                scoreText.setVisibility(View.INVISIBLE);
                timeText.setVisibility(View.INVISIBLE);
                highScoreText.setVisibility(View.INVISIBLE);
                constraintLayout.setVisibility(View.INVISIBLE);

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

        constraintLayout = findViewById(R.id.cons);
        constraintLayout.setVisibility(View.INVISIBLE);
        directionSwitch = findViewById(R.id.switch2);
        directionSwitch.setClickable(false);

        scoreText = findViewById(R.id.textView);
        scoreText.setTextColor(Color.WHITE);
        scoreText.setVisibility(View.INVISIBLE);

        timeText = findViewById(R.id.textView2);
        timeText.setTextColor(Color.WHITE);
        timeText.setVisibility(View.INVISIBLE);

        highScoreText = findViewById(R.id.highscore);
        highScoreText.setTextColor(Color.WHITE);
        highScoreText.setVisibility(View.INVISIBLE);

        joystick = findViewById(R.id.joystick);
        joystick.setVisibility(View.INVISIBLE);

        gameTimer = new CountDownTimer(25000,1000) {
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
        startGame();





    }

    private void startGame(){
        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
            if (!createdGame) {
                if (anchornode != null ) onBackPressed();
                createdGame = true;
                scoreText.setText("Score:" + score);
                //  startNode.setParent(null);
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

                scoreText.setVisibility(View.VISIBLE);
                timeText.setVisibility(View.VISIBLE);
                joystick.setVisibility(View.VISIBLE);
                highScoreText.setVisibility(View.INVISIBLE);
                constraintLayout.setVisibility(View.VISIBLE);
                directionSwitch.setClickable(true);

                gameTimer.start();
                directionHandler = new Handler();
                directionChecker.run();
            }
        });



    }

    private void endScreen() {
        drive.end();
        directionSwitch.setClickable(false);
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
        fall.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {}
            @Override
            public void onAnimationEnd(Animator animator) {
                Node endnode2 = new Node();
                ModelRenderable.builder()
                        .setSource(getApplicationContext(), Uri.parse("timeout.sfb"))
                        .build()
                        .thenAccept(modelRenderable -> {
                            endnode2.setRenderable(modelRenderable);
                            anchornode.addChild(endnode2);
                        })
                        .exceptionally(throwable -> {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
                            builder.setMessage(throwable.getMessage())
                                    .show();
                            return null;
                        });
                endnode2.setLocalPosition(Vector3.add(carNode.getLocalPosition(),new Vector3(0f,1.0f,0)));

                ObjectAnimator fall2 = new ObjectAnimator();
                fall2.setObjectValues(endnode2.getLocalPosition(),Vector3.add(carNode.getLocalPosition(),new Vector3(0.059f,0.050f,0.22f)));
                fall2.setPropertyName("localPosition");
                fall2.setEvaluator(new Vector3Evaluator());
                fall2.setRepeatCount(0);
                fall2.setInterpolator(new AccelerateDecelerateInterpolator());
                fall2.setDuration(1000);
                fall2.setTarget(endnode2);
                fall2.start();
            }
            @Override
            public void onAnimationCancel(Animator animator) {}
            @Override
            public void onAnimationRepeat(Animator animator) {}
        });
        fall.start();


        scoreRepository= new ScoreRepository(getApplicationContext());
        //  scoreRepository.insertScore(15);

        scoreRepository.getScore().observe( this, new Observer<List<HighScore>>() {
            @Override
            public void onChanged(@Nullable List<HighScore> scores) {
                int newHigh = score;
                for(HighScore highScore : scores) {
                    System.out.println("-----------------------");
                    System.out.println(highScore.getHighScore());
                    if (highScore.getHighScore() > newHigh){
                        newHigh = highScore.getHighScore();
                        System.out.println("score changd:"+ newHigh);
                    }
                    else
                        scoreRepository.deleteScore(highScore);
                }
                scoreRepository.insertScore(newHigh);
                highScoreText.setText("HighScore: "+ newHigh);
            }
        });

        highScoreText.setTextColor(Color.WHITE);
        highScoreText.setVisibility(View.VISIBLE);
    }


    /**private void startScreen(){
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
 **/




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
