package com.montefiore.gaulthiergain.simongameadhoc;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.Toast;

import com.montefiore.gaulthiergain.adhoclibrary.appframework.ListenerApp;
import com.montefiore.gaulthiergain.adhoclibrary.appframework.TransferManager;
import com.montefiore.gaulthiergain.adhoclibrary.datalink.exceptions.DeviceException;
import com.montefiore.gaulthiergain.adhoclibrary.datalink.service.AdHocDevice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * <p>This class represents the game context and manage all the features of the Simon game.</p>
 *
 * @author Gaulthier Gain
 * @version 1.0
 */
public class Game {

    private static final String TAG = "[AdHoc][Game]";

    private static final int ACK = 2;
    private static final int COLOR = 3;
    private static final int SCORE = 4;

    private static final int DURATION_CLICK = 900;
    private static final int DELAY = 5000;

    private AdHocDevice remoteAdHoc;
    private HashMap<String, String> mapPlayerScore;

    private boolean server;

    private AppCompatActivity activity;
    private TransferManager transferManager;

    private ColorSet globalColorSet = new ColorSet();
    private ColorSet autoColorSet;

    private int nbPlayer;
    private int rcvResponse;
    private int score;
    private int nbCombi;
    private String name;
    private ConnectionClosedListener listener;

    private boolean playersDone;
    private boolean selfDone;

    /**
     * Constructor
     *
     * @param activity        an AppCompatActivity object which represents the current activity.
     * @param transferManager a TransferManager object which allows ad hoc communications.
     */
    Game(AppCompatActivity activity, TransferManager transferManager, int nbPlayer) {
        this.activity = activity;
        this.transferManager = transferManager;
        this.mapPlayerScore = new HashMap<>();
        this.score = 0;
        this.nbPlayer = 0;
        this.nbCombi = 2;
        this.rcvResponse = 0;
        this.server = true;
        this.name = Util.getDeviceName(); // Customize name
        this.nbPlayer = nbPlayer;
        this.playersDone = false;
        this.selfDone = false;
    }

    /**
     * Constructor
     *
     * @param activity        an AppCompatActivity object which represents the current activity.
     * @param transferManager a TransferManager object which allows ad hoc communications.
     * @param adHocDevice     an AdHocDevice object which represents the remote node.
     * @param listener        a ConnectionClosedListener interface for connection closed callback.
     */
    Game(AppCompatActivity activity, TransferManager transferManager, AdHocDevice adHocDevice,
         ConnectionClosedListener listener) {
        this.activity = activity;
        this.transferManager = transferManager;
        this.mapPlayerScore = new HashMap<>();
        this.score = 0;
        this.nbPlayer = 0;
        this.nbCombi = 2;
        this.rcvResponse = 0;
        this.server = false;
        this.listener = listener;
        this.name = Util.getDeviceName(); // Customize name
        this.remoteAdHoc = adHocDevice;
        this.playersDone = false;
        this.selfDone = false;
    }

    /**
     * Method allowing to setup the GUI and the basic stuff.
     */
    private void setupGame() {
        Log.d(TAG, "setupGame()");

        Button btnBlue = activity.findViewById(R.id.btnBlue);
        btnBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.0F);
                buttonClick.setDuration(DURATION_CLICK);
                globalColorSet.addColor(new Color(Color.BLUE));
                v.startAnimation(buttonClick);
            }
        });

        Button btnGreen = activity.findViewById(R.id.btnGreen);
        btnGreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.0F);
                buttonClick.setDuration(DURATION_CLICK);
                globalColorSet.addColor(new Color(Color.GREEN));
                v.startAnimation(buttonClick);
            }
        });

        Button btnOrange = activity.findViewById(R.id.btnOrange);
        btnOrange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.0F);
                buttonClick.setDuration(DURATION_CLICK);
                globalColorSet.addColor(new Color(Color.ORANGE));
                v.startAnimation(buttonClick);
            }
        });

        Button btnRed = activity.findViewById(R.id.btnRed);
        btnRed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.0F);
                buttonClick.setDuration(DURATION_CLICK);
                globalColorSet.addColor(new Color(Color.RED));
                v.startAnimation(buttonClick);
            }
        });

        // Update btn state
        updateBtnAction(R.string.waitGame, false);

        // Initialize the send button with a listener for click events
        final Button mSendButton = activity.findViewById(R.id.btnAction);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                String scoreString;
                if (checkResponse(autoColorSet)) {
                    scoreString = "All is correct";
                    score++;
                } else {
                    scoreString = "Incorrect";
                    score--;
                }

                Toast.makeText(activity.getApplicationContext(), scoreString +
                        " -> Your score: " + score, Toast.LENGTH_LONG).show();

                // Add score to map
                mapPlayerScore.put(name, String.valueOf(score));

                if (!server) {
                    try {
                        // Send score to server
                        transferManager.sendMessageTo(new Msg(SCORE, String.valueOf(score), name), remoteAdHoc);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (DeviceException e) {
                        e.printStackTrace();
                    }
                } else {
                    selfDone = true;
                    try {
                        // Broadcast server score to all devices
                        transferManager.broadcast(new Msg(SCORE, String.valueOf(score), name));
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (DeviceException e) {
                        e.printStackTrace();
                    }
                    // Update btn state
                    updateBtnAction(R.string.waitGame, false);

                    if (selfDone && playersDone) {
                        timerAutoChoice(DELAY);
                    }
                }
            }
        });

        if (server) {
            timerAutoChoice(DELAY);
        }
    }

    /**
     * Method allowing to launch a timer performing the choices of Colors.
     *
     * @param delay an integer value which represents the delay in milliseconds before task
     *              is to be executed.
     */
    private void timerAutoChoice(int delay) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                activity.runOnUiThread(new Thread(new Runnable() {
                    public void run() {
                        randomGame();
                        animGame(autoColorSet);
                        nbCombi++;
                        try {
                            transferManager.broadcast(new Msg(COLOR, autoColorSet, name));
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (DeviceException e) {
                            e.printStackTrace();
                        }

                        // Upate Btn action
                        updateBtnAction(R.string.check, true);

                        // Reset flags
                        selfDone = false;
                        if (nbPlayer != 0) {
                            playersDone = false;
                        }
                    }
                }));
            }
        }, delay);
    }

    /**
     * Method allowing to update a Button state.
     *
     * @param id      an integer value which represents the button identifier.
     * @param enabled a boolean value which represents the button state.
     */
    private void updateBtnAction(int id, boolean enabled) {
        Button btnAction = activity.findViewById(R.id.btnAction);
        btnAction.setText(id);
        btnAction.setEnabled(enabled);
    }

    /**
     * Method allowing to Generate random colors. It is incremented one by one with the time.
     */
    private void randomGame() {
        autoColorSet = new ColorSet();

        int min = 1;
        int max = 4;

        Random random = new Random();

        for (int i = 0; i < nbCombi; i++) {
            switch (random.nextInt(max - min + 1) + min) {
                case 1:
                    autoColorSet.addColor(new Color(Color.BLUE));
                    break;
                case 2:
                    autoColorSet.addColor(new Color(Color.GREEN));
                    break;
                case 3:
                    autoColorSet.addColor(new Color(Color.ORANGE));
                    break;
                case 4:
                default:
                    autoColorSet.addColor(new Color(Color.RED));
            }
        }
    }

    /**
     * Main method to init the game
     */
    public void init() {

        // Setup the game
        setupGame();

        transferManager.updateListenerApp(new ListenerApp() {
            @Override
            public void onReceivedData(AdHocDevice adHocDevice, Object pdu) {

                Log.d(TAG, "MESSAGE RECEIVED: " + pdu);

                if (server) {

                    Msg msg = (Msg) pdu;

                    if (msg.getType() == Game.SCORE) {

                        String toastMsg = "Score from" + msg.getName() + " is " + msg.getMsg();
                        Toast.makeText(activity.getApplicationContext(), toastMsg, Toast.LENGTH_LONG).show();

                        // Add score to map
                        mapPlayerScore.put(msg.getName(), msg.getMsg());

                        try {
                            // Send ACK to remote host
                            transferManager.sendMessageTo(new Msg(ACK, "", name), adHocDevice);

                            // Broadcast Score to remote hosts
                            transferManager.broadcastExcept(msg, adHocDevice);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (DeviceException e) {
                            e.printStackTrace();
                        }

                        // Update response
                        rcvResponse++;
                        if (rcvResponse == nbPlayer) {
                            // All responses are received
                            rcvResponse = 0;
                            // Reset the colorSet
                            globalColorSet = new ColorSet();
                            // Update flag
                            playersDone = true;
                        }

                        if (selfDone && playersDone) {
                            timerAutoChoice(DELAY);
                        }
                    }
                } else {

                    Msg msg = (Msg) pdu;

                    if (msg.getType() == Game.ACK) {

                        // Reset the globalColorSet
                        globalColorSet = new ColorSet();

                        // Update btn state
                        updateBtnAction(R.string.waitGame, false);

                    } else if (msg.getType() == Game.COLOR) {

                        // Get the ColorSet choices by IA
                        autoColorSet = ((Msg) pdu).getColorSet();

                        // Anim Game
                        animGame(autoColorSet);

                        // Update btn state
                        updateBtnAction(R.string.check, true);

                        globalColorSet = new ColorSet();
                    } else if (msg.getType() == Game.SCORE) {

                        String toastMsg = "Score from" + msg.getName() + " is " + msg.getMsg();
                        Toast.makeText(activity.getApplicationContext(), toastMsg, Toast.LENGTH_LONG).show();

                        // Add score to map
                        mapPlayerScore.put(msg.getName(), msg.getMsg());
                    }
                }

            }

            @Override
            public void onForwardData(AdHocDevice adHocDevice, Object pdu) {
                // Ignored
            }

            @Override
            public void onConnection(AdHocDevice adHocDevice) {
                if (server) {
                    // Accept new clients and send RUN_GAME message
                    try {
                        transferManager.sendMessageTo(MainActivity.RUN_GAME, adHocDevice);
                        nbPlayer++;
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (DeviceException e) {
                        e.printStackTrace();
                    }
                } else {
                    // Send message to tell who is the server
                    try {
                        transferManager.sendMessageTo(remoteAdHoc.getDeviceName(), adHocDevice);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (DeviceException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onConnectionFailed(Exception e) {

            }

            @Override
            public void onConnectionClosed(AdHocDevice adHocDevice) {

                Toast.makeText(activity.getApplicationContext(), "Connection closed with "
                                + adHocDevice.getLabel() + "(" + adHocDevice.getDeviceName() + ")",
                        Toast.LENGTH_SHORT).show();

                if (server) {
                    nbPlayer--;
                    if (nbPlayer == 0) {
                        playersDone = true;
                    }
                } else {
                    Toast.makeText(activity.getApplicationContext(), "Disconnected from host",
                            Toast.LENGTH_LONG).show();
                    listener.connectionClosed();
                }
            }

            @Override
            public void onConnectionClosedFailed(Exception e) {
                e.printStackTrace();
            }

            @Override
            public void processMsgException(Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Method allowing to check color.
     *
     * @param colorSetRcv a ColorSet which contains a list of Colors.
     * @return a boolean value which is true if responses are correct otherwise false.
     */
    private boolean checkResponse(ColorSet colorSetRcv) {

        // Check response pdu
        if (colorSetRcv == null || colorSetRcv.getArrayList() == null) {
            return false;
        }

        // Check if the two colorSet have the same size
        ArrayList<Color> arrayListColor = colorSetRcv.getArrayList();
        if (arrayListColor.size() != globalColorSet.getArrayList().size()) {
            return false;
        }

        // Check color per color
        for (int i = 0; i < arrayListColor.size(); i++) {
            if (arrayListColor.get(i).getColor() != globalColorSet.getArrayList().get(i).getColor()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Method allowing to perform the animation.
     *
     * @param colorSetLocal a ColorSet which contains a list of Colors.
     */
    private void animGame(ColorSet colorSetLocal) {

        final Button btnRed = activity.findViewById(R.id.btnRed);
        final Button btnGreen = activity.findViewById(R.id.btnGreen);
        final Button btnBlue = activity.findViewById(R.id.btnBlue);
        final Button btnOrange = activity.findViewById(R.id.btnOrange);
        final Button[] allBtn = {btnRed, btnGreen, btnBlue, btnOrange};

        int i = 1;
        final ArrayList<ColorAnim> colorAnimArrayList = new ArrayList<>();

        // Add all animation
        for (final Color color : colorSetLocal.getArrayList()) {
            Animation anim = new AlphaAnimation(1F, 0.0F);
            anim.setDuration(DURATION_CLICK);
            anim.setStartTime(i * DURATION_CLICK);
            colorAnimArrayList.add(new ColorAnim(color, anim));
        }

        for (int j = 0; j < colorAnimArrayList.size(); j++) {
            Animation animation = colorAnimArrayList.get(j).getAnim();
            int colorIndex = colorAnimArrayList.get(j).getColor().getColor();
            Log.d(TAG, "\tColorBtn: " + switchColor(colorIndex));
            final int finalJ = j;
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    Log.d(TAG, "\t[START] Start Animation: " + switchColor(colorAnimArrayList.get(finalJ).getColor().getColor()));
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (finalJ < colorAnimArrayList.size() - 1) {
                        Log.d(TAG, "\t[END] Start next Animation: " + switchColor(colorAnimArrayList.get(finalJ + 1).getColor().getColor()));
                        allBtn[colorAnimArrayList.get(finalJ + 1).getColor().getColor()].startAnimation(colorAnimArrayList.get(finalJ + 1).getAnim());
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            if (j == 0) {
                // Start the first animation
                allBtn[colorIndex].startAnimation(animation);
            }
        }
    }

    /**
     * Method allowing to convert a integer color to a color name.
     *
     * @param color an integer which represents the color identifier.
     * @return a String which represents the color name.
     */
    private String switchColor(int color) {
        switch (color) {
            case 0:
                return "RED";
            case 1:
                return "GREEN";
            case 2:
                return "BLUE";
            case 3:
                return "ORANGE";
        }
        return "NA";
    }

    public HashMap<String, String> getScores() {
        return mapPlayerScore;
    }
}

