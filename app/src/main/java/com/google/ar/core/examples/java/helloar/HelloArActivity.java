/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.ar.core.examples.java.helloar;

import android.animation.Animator;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.ar.core.Anchor;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Point;
import com.google.ar.core.Point.OrientationMode;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.core.examples.java.common.helpers.CameraPermissionHelper;
import com.google.ar.core.examples.java.common.helpers.DisplayRotationHelper;
import com.google.ar.core.examples.java.common.helpers.FullScreenHelper;
import com.google.ar.core.examples.java.common.helpers.ResolveDialogFragment;
import com.google.ar.core.examples.java.common.helpers.SnackbarHelper;
import com.google.ar.core.examples.java.common.helpers.TapHelper;
import com.google.ar.core.examples.java.common.rendering.BackgroundRenderer;
import com.google.ar.core.examples.java.common.rendering.ObjectRenderer;
import com.google.ar.core.examples.java.common.rendering.ObjectRenderer.BlendMode;
import com.google.ar.core.examples.java.common.rendering.PlaneRenderer;
import com.google.ar.core.examples.java.common.rendering.PointCloudRenderer;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.view.View;
import com.google.ar.core.examples.java.common.helpers.GraphHelper;
import com.google.ar.core.examples.java.helloar.ExportData;
/**
 * This is a simple example that shows how to create an augmented reality (AR) application using the
 * ARCore API. The application will display any detected planes and will allow the user to tap on a
 * plane to place a 3d model of the Android robot.
 */
public class HelloArActivity extends AppCompatActivity implements GLSurfaceView.Renderer {
  private static final String TAG = HelloArActivity.class.getSimpleName();

  // Rendering. The Renderers are created here, and initialized when the GL surface is created.
  private GLSurfaceView surfaceView;
private static int count=0;
  private boolean installRequested;
  private Button resolveButton;
  private Session session;
  private final SnackbarHelper messageSnackbarHelper = new SnackbarHelper();
  private DisplayRotationHelper displayRotationHelper;
  private TapHelper tapHelper;
  private TextView textView;
  static  HashMap<String,Anchor.CloudAnchorState> codes_hm=new HashMap<String, Anchor.CloudAnchorState>();
  static HashMap<String,Integer> id_code= new HashMap<>();
  private final BackgroundRenderer backgroundRenderer = new BackgroundRenderer();
  private final ObjectRenderer virtualObject = new ObjectRenderer();
  private final ObjectRenderer virtualObjectShadow = new ObjectRenderer();
  private final PlaneRenderer planeRenderer = new PlaneRenderer();
  private final PointCloudRenderer pointCloudRenderer = new PointCloudRenderer();
  private StorageManager storageManager;

    // Temporary matrix allocated here to reduce number of allocations for each frame.
  private final float[] anchorMatrix = new float[16];
  private static final float[] DEFAULT_COLOR = new float[] {0f, 0f, 0f, 0f};
  private static final float magenta [] = {255.0f, 0f, 255.0f, 255f};
  private static final float red [] = {200f, 0f, 0f, 255f};

  // View Matrix and Projection matrix used in onDrawFrame #function.
  float[] projmtx = new float[16];
  float[] viewmtx = new float[16];

    // Temporary matrices allocated here to reduce number of allocations for each frame.
    private final float[] modelMatrix = new float[16];
    private final float[] modelViewMatrix = new float[16];
    private final float[] modelViewProjectionMatrix = new float[16];
    private static boolean flag=false;

    // Anchors created from taps used for object placing with a given color.

    public static class ColoredAnchor {
    public final Anchor anchor;
    public final float[] color;

    private enum AppAnchorState {
        NONE,
        HOSTING,
        HOSTED,
        RESOLVING,
        RESOLVED
    }
    private AppAnchorState appAnchorState = AppAnchorState.NONE;

    public ColoredAnchor(Anchor a, float[] color4f, AppAnchorState state) {
      this.anchor = a;
      this.color = color4f;
      this.appAnchorState = state;
    }
  }

  public final ArrayList<ColoredAnchor> anchors = new ArrayList<>();
    private final ArrayList<String> shorties = new ArrayList<>();
    final static String fileName = "data.txt";
    final static String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/sujji/";

    private HashMap<Integer, GraphHelper> graphNodesMap = new HashMap<>();
    private HashMap<Integer, Integer> shortCodeLocationMap = new HashMap<>();
    private HashMap<Integer, Integer> graphHelperMap = new HashMap<>();
    private float[][] graphMatrix;
    //TinyDB tinydb;
    private static boolean isGraphUpdated = false;
    Button saveBT, getBT;
    DisplayMetrics displayMetrics = new DisplayMetrics();
    FileOutputStream outputStream;
  private final HashMap<Integer, String> anchorLocationHmap = new HashMap<>();
  private final HashMap<String, Anchor> anchorsInView = new HashMap<>();
  private final HashMap<Integer, float[]> anchorColor = new HashMap<>();

  private static int anchorCount = 0;
    boolean isFABOpen=false;

  //DisplayMetrics displayMetrics = new DisplayMetrics();
  public int screenHeight ;//= displayMetrics.heightPixels;
  public int screenWidth ;//= displayMetrics.widthPixels;

    final Context context = this;
    FloatingActionButton infoFab,fab1,fab2,fab3;
    RelativeLayout fabLayout1, fabLayout2, fabLayout3;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    surfaceView = findViewById(R.id.surfaceview);
    textView = findViewById(R.id.textView);
    textView.setBackgroundColor(Color.BLACK);
    resolveButton =  findViewById (R.id.resolve_button);
      resolveButton.setOnClickListener((view) -> onResolveButtonPress());
    displayRotationHelper = new DisplayRotationHelper(/*context=*/ this);
    infoFab = findViewById(R.id.floatingActionButton);
      fab1 = (FloatingActionButton) findViewById(R.id.fab);
      fab2= (FloatingActionButton) findViewById(R.id.fab2);
      fab3 = (FloatingActionButton) findViewById(R.id.fab3);
      fabLayout1= (RelativeLayout) findViewById(R.id.fabLayout2);
      fabLayout2= (RelativeLayout) findViewById(R.id.fabLayout3);
      fabLayout3= (RelativeLayout) findViewById(R.id.fabLayout);
    getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
    screenHeight = displayMetrics.heightPixels;
    screenWidth = displayMetrics.widthPixels;

      saveBT = findViewById(R.id.save);
      getBT = findViewById(R.id.get);
      saveBT.setOnClickListener(v -> {
          //tinydb.putListObject("anchors",anchors);
          if (isGraphUpdated) {
              // TODO: Put a toast here.
              return;
          }
          else {
              // TODO: Show UI for graph updation.
              createAdjacencyMatrix();
                ExportData writer = new ExportData();
                writer.writeData(context,shortCodeLocationMap,"shortcodes");
                writer.writeData(context,graphHelperMap,"graphhelper");
          }
      });
    // Set up tap listener.
    tapHelper = new TapHelper(/*context=*/ this);
    surfaceView.setOnTouchListener(tapHelper);

    // Set up renderer.
    surfaceView.setPreserveEGLContextOnPause(true);
    surfaceView.setEGLContextClientVersion(2);
    surfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0); // Alpha used for plane blending.
    surfaceView.setRenderer(this);
    surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

    installRequested = false;
    infoFab.setOnClickListener((View v) -> {
        if(!isFABOpen){
            showFABMenu();
        }else{
            closeFABMenu();
        }

    });
    fab1.setOnClickListener(view -> {
        registerDialogClick();
    });
    storageManager = new StorageManager(this);

  }

    private void closeFABMenu() {
        isFABOpen=false;
        //fabBGLayout.setVisibility(View.GONE);
        infoFab.animate().rotationBy(-180);
        fabLayout1.setVisibility(View.GONE);
        fabLayout2.setVisibility(View.GONE);
        fabLayout3.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        if(isFABOpen){
            closeFABMenu();
        }else{
            super.onBackPressed();
        }
    }

    private void showFABMenu() {
        isFABOpen=true;

        fabLayout1.setVisibility(View.VISIBLE);
        fabLayout2.setVisibility(View.VISIBLE);
        fabLayout3.setVisibility(View.VISIBLE);

        infoFab.animate().rotationBy(180);

    }

    public void onResolveButtonPress() {
        flag=true;

        ArrayList<String> data=storageManager.getAllShortCodes(anchorcodes -> {
            ArrayList<String> code=anchorcodes;
            Log.e(TAG,"indu code"+code);
            for(String shorty : code){
                int shortcode=Integer.parseInt(shorty);
            storageManager.getCloudAnchorId(shortcode,cloudAnchorId -> {
                if(cloudAnchorId!=null) {
                    Log.e(TAG,"indu string "+shortcode+cloudAnchorId);
                    Anchor resolvedAnchor = session.resolveCloudAnchor(cloudAnchorId);
                    float[] objColor = new float[]{66.0f, 133.0f, 244.0f, 255.0f};
                   // if(resolvedAnchor.getCloudAnchorState() == Anchor.CloudAnchorState.SUCCESS){
                        Log.e(TAG,"indu cloudanchor state "+resolvedAnchor.getCloudAnchorState()+resolvedAnchor.getCloudAnchorId());
                       // if(!codes_hm.containsKey(cloudAnchorId)){
                            codes_hm.put(cloudAnchorId,resolvedAnchor.getCloudAnchorState());
                            id_code.put(cloudAnchorId,shortcode);
                            //}else{
                      //  codes_hm.put(shorty,0);
                    //}
                    anchors.add(new ColoredAnchor(resolvedAnchor, objColor, ColoredAnchor.AppAnchorState.RESOLVING));
                    Toast.makeText(getApplicationContext(), "Now resolving anchor..", Toast.LENGTH_SHORT).show();
                }
            });}
        });

        /*String AnchorId="";
        File file = new File(path+ fileName);

        FileReader fileReader = null;

        try {
            fileReader = new FileReader(file);

        BufferedReader bufferedReader = new BufferedReader(fileReader);
        StringBuffer stringBuffer = new StringBuffer();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            //stringBuffer.append(line);
            //AnchorId = storageManager.getCloudAnchorID(this, Integer.parseInt(line));
            int shortcode=Integer.parseInt(line);
            storageManager.getCloudAnchorId(shortcode,cloudAnchorId -> {
                if(cloudAnchorId!=null) {
                    Log.e(TAG,"indu string "+shortcode+cloudAnchorId);
                    Anchor resolvedAnchor = session.resolveCloudAnchor(cloudAnchorId);
                    float[] objColor = new float[]{66.0f, 133.0f, 244.0f, 255.0f};

                    anchors.add(new ColoredAnchor(resolvedAnchor, objColor, ColoredAnchor.AppAnchorState.RESOLVING));
                    Toast.makeText(getApplicationContext(), "Now resolving anchor..", Toast.LENGTH_SHORT).show();
                }
            });
            //
            //Log.e(TAG,"indu string "+line+cloudAnchorId);
        }

        fileReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
       }


    private float[] getAnchorColor(Anchor.CloudAnchorState state) {
      if (state.isError())
          return red;
      if (state == Anchor.CloudAnchorState.TASK_IN_PROGRESS) {
          Log.e("vaib::","return magenta Color");
          return magenta;
      }
      else if (state == Anchor.CloudAnchorState.NONE || state == Anchor.CloudAnchorState.SUCCESS) {
          Log.e("vaib::","return null");
          return null;
      }
      Log.e("vaib::","return red");
      return red;
  }

  public void registerDialogClick() {
      final Dialog dialog = new Dialog(context);
      dialog.setContentView(R.layout.dialogbox);
      dialog.setTitle("Anchor hosting states..");
      TextView text = dialog.findViewById(R.id.label);
     // storageManager.getAllShortCodes(anchorcodes -> Log.e("indu shortcodes:",anchorcodes));
      /*storageManager.getAllShortCodes(
              (shortCodes) -> {
                  if (shortCodes != null) {
                      StringBuilder builder = new StringBuilder();
                      Log.e("indu shortcodes:",shortCodes);
                      text.setText(shortCodes);
                  }
              }
      );*/

      if (anchors.size() > 0) {
          ArrayList<String> anchorsArray =new ArrayList<>();
          for (ColoredAnchor anc : anchors) {
              String index = "Anchor "+anchorLocationHmap.get(anc.anchor.hashCode());
              String cloudstate = anc.anchor.getCloudAnchorState().toString();
              anchorsArray.add(index+"\t"+cloudstate);
          }

          StringBuilder builder = new StringBuilder();
          builder.append(text.getText()+"\n");
          for(String s : anchorsArray) {
              builder.append(s+"\n");
          }
          String str = builder.toString();
          text.setText(str);
          /*ArrayAdapter adapter = new ArrayAdapter<String>(getApplicationContext(),
                  R.layout.anchor_list_view, anchorsArray);
          boolean adap = adapter == null ? true:false;
          Log.e("kala:","AnchorsArray size=" +anchorsArray.size()+"Adapeter NULL="+adap);

          ListView ancListView = findViewById(R.id.dialogboxListView);
          ancListView.setAdapter(adapter);*/
      } else
          text.setText("No anchors are present in this session");
      Button dialogButton = dialog.findViewById(R.id.dialogButtonOK);
      // if button is clicked, close the custom dialog
      dialogButton.setOnClickListener(v -> dialog.dismiss());

      dialog.show();
      Toast.makeText(this,"Size=",Toast.LENGTH_LONG).show();

  }
  @Override
  protected void onResume() {
    super.onResume();

    if (session == null) {
      Exception exception = null;
      String message = null;
      try {
        switch (ArCoreApk.getInstance().requestInstall(this, !installRequested)) {
          case INSTALL_REQUESTED:
            installRequested = true;
            return;
          case INSTALLED:
            break;
        }

        // ARCore requires camera permissions to operate. If we did not yet obtain runtime
        // permission on Android M and above, now is a good time to ask the user for it.
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
          CameraPermissionHelper.requestCameraPermission(this);
          return;
        }

        // Create the session.
        session = new Session(/* context= */ this);

      } catch (UnavailableArcoreNotInstalledException
          | UnavailableUserDeclinedInstallationException e) {
        message = "Please install ARCore";
        exception = e;
      } catch (UnavailableApkTooOldException e) {
        message = "Please update ARCore";
        exception = e;
      } catch (UnavailableSdkTooOldException e) {
        message = "Please update this app";
        exception = e;
      } catch (UnavailableDeviceNotCompatibleException e) {
        message = "This device does not support AR";
        exception = e;
      } catch (Exception e) {
        message = "Failed to create AR session";
        exception = e;
      }

      if (message != null) {
        messageSnackbarHelper.showError(this, message);
        Log.e(TAG, "Exception creating session", exception);
        return;
      }
    }

    // Note that order matters - see the note in onPause(), the reverse applies here.
    try {
      session.resume();
    } catch (CameraNotAvailableException e) {
      // In some cases (such as another camera app launching) the camera may be given to
      // a different app instead. Handle this properly by showing a message and recreate the
      // session at the next iteration.
      messageSnackbarHelper.showError(this, "Camera not available. Please restart the app.");
      session = null;
      return;
    }

    surfaceView.onResume();
    displayRotationHelper.onResume();

    messageSnackbarHelper.showMessage(this, "Searching for surfaces...");
      Config config = new Config(session);
      config.setCloudAnchorMode(Config.CloudAnchorMode.ENABLED); // Add this line.
      session.configure(config);
  }

  @Override
  public void onPause() {
    super.onPause();
    if (session != null) {
      // Note that the order matters - GLSurfaceView is paused first so that it does not try
      // to query the session. If Session is paused before GLSurfaceView, GLSurfaceView may
      // still call session.update() and get a SessionPausedException.
      displayRotationHelper.onPause();
      surfaceView.onPause();
      session.pause();
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
    if (!CameraPermissionHelper.hasCameraPermission(this)) {
      Toast.makeText(this, "Camera permission is needed to run this application", Toast.LENGTH_LONG)
          .show();
      if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
        // Permission denied with checking "Do not ask again".
        CameraPermissionHelper.launchPermissionSettings(this);
      }
      finish();
    }
  }

  @Override
  public void onWindowFocusChanged(boolean hasFocus) {
    super.onWindowFocusChanged(hasFocus);
    FullScreenHelper.setFullScreenOnWindowFocusChanged(this, hasFocus);
  }

  @Override
  public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);

    // Prepare the rendering objects. This involves reading shaders, so may throw an IOException.
    try {
      // Create the texture and pass it to ARCore session to be filled during update().
      backgroundRenderer.createOnGlThread(/*context=*/ this);
      planeRenderer.createOnGlThread(/*context=*/ this, "models/trigrid.png");
      pointCloudRenderer.createOnGlThread(/*context=*/ this);

      virtualObject.createOnGlThread(/*context=*/ this, "models/andy.obj", "models/andy.png");
      virtualObject.setMaterialProperties(0.0f, 2.0f, 0.5f,   6.0f);

      virtualObjectShadow.createOnGlThread(
          /*context=*/ this, "models/andy_shadow.obj", "models/andy_shadow.png");
      virtualObjectShadow.setBlendMode(BlendMode.Shadow);
      virtualObjectShadow.setMaterialProperties(1.0f, 0.0f, 0.0f, 1.0f);

    } catch (IOException e) {
      Log.e(TAG, "Failed to read an asset file", e);
    }
  }

  @Override
  public void onSurfaceChanged(GL10 gl, int width, int height) {
    displayRotationHelper.onSurfaceChanged(width, height);
    GLES20.glViewport(0, 0, width, height);
  }

  @Override
  public void onDrawFrame(GL10 gl) {
    // Clear screen to notify driver it should not load any pixels from previous frame.
     // onResolveButtonPress();
    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

    if (session == null) {
      return;
    }
    // Notify ARCore session that the view size changed so that the perspective matrix and
    // the video background can be properly adjusted.
    displayRotationHelper.updateSessionIfNeeded(session);

    try {
      session.setCameraTextureName(backgroundRenderer.getTextureId());

      // Obtain the current frame from ARSession. When the configuration is set to
      // UpdateMode.BLOCKING (it is by default), this will throttle the rendering to the
      // camera framerate.
      Frame frame = session.update();
      Camera camera = frame.getCamera();
      count++;
      if(count>300){
          resolving_anchors();
          count=0;
      }
      // Handle one tap per frame.
      handleTap(frame, camera);
        //camera.getProjectionMatrix();
        Collection<Anchor> al = frame.getUpdatedAnchors();
        for (Anchor a : al)
            a.getPose();

      // Draw background.
      backgroundRenderer.draw(frame);

      // If not tracking, don't draw 3d objects.
      if (camera.getTrackingState() == TrackingState.PAUSED) {
        return;
      }

      // Get projection matrix.
      camera.getProjectionMatrix(projmtx, 0, 0.1f, 100.0f);

      // Get camera matrix and draw.
      camera.getViewMatrix(viewmtx, 0);


      // Compute lighting from average intensity of the image.
      // The first three components are color scaling factors.
      // The last one is the average pixel intensity in gamma space.
      final float[] colorCorrectionRgba = new float[4];
      frame.getLightEstimate().getColorCorrection(colorCorrectionRgba, 0);

      // Visualize tracked points.
      PointCloud pointCloud = frame.acquirePointCloud();
      pointCloudRenderer.update(pointCloud);
      pointCloudRenderer.draw(viewmtx, projmtx);

      // Application is responsible for releasing the point cloud resources after
      // using it.
      pointCloud.release();


      // Check if we detected at least one plane. If so, hide the loading message.
      if (messageSnackbarHelper.isShowing()) {
        for (Plane plane : session.getAllTrackables(Plane.class)) {
          if (plane.getTrackingState() == TrackingState.TRACKING) {
            messageSnackbarHelper.hide(this);
            break;
          }
        }
      }

      // Visualize planes.
      planeRenderer.drawPlanes(
          session.getAllTrackables(Plane.class), camera.getDisplayOrientedPose(), projmtx);

      // Visualize anchors created by touch.
      float scaleFactor = 1.0f;
        anchorsInView.clear();

        for (ColoredAnchor coloredAnchor : anchors) {
            if (coloredAnchor.anchor.getTrackingState() != TrackingState.TRACKING) {
              continue;
            }

            //Log.e("vai:","anchor="+coloredAnchor.toString());

            // Get the current pose of an Anchor in world space. The Anchor pose is updated
            // during calls to session.update() as ARCore refines its estimate of the world.+
            coloredAnchor.anchor.getPose().toMatrix(anchorMatrix, 0);
            // Get the latest Pose of each anchor and store it in anchorMatrix
            // Now pass this anchorMatrix to openGL functions to draw the 3D object.

            // Update and draw the model and its shadow.
            virtualObject.updateModelMatrix(anchorMatrix, scaleFactor);
            virtualObjectShadow.updateModelMatrix(anchorMatrix, scaleFactor);

            float[] world2screenMatrix = virtualObject.getMyScreenMatrix(anchorMatrix,viewmtx, projmtx);
            double[] anchor_2d = world2Screen(screenWidth,screenHeight,world2screenMatrix);

            Anchor.CloudAnchorState state = coloredAnchor.anchor.getCloudAnchorState();
            float[] color;
            if (state == Anchor.CloudAnchorState.SUCCESS) {
                color = coloredAnchor.color;
                if (coloredAnchor.appAnchorState == ColoredAnchor.AppAnchorState.HOSTING) {
                    storeAnchorIdToDB(coloredAnchor);
                    coloredAnchor.appAnchorState = ColoredAnchor.AppAnchorState.HOSTED;
                }
            }
            else
                color = getAnchorColor(state);

            virtualObject.draw(viewmtx, projmtx, colorCorrectionRgba, color);
            virtualObjectShadow.draw(viewmtx, projmtx, colorCorrectionRgba, color);

            if((anchor_2d[0] > 0 && anchor_2d[0] < screenWidth) && (anchor_2d[1] > 0 && anchor_2d[1] < screenHeight)) {
                Log.e("apeks", "Anchor is visible on the screen::"+coloredAnchor.anchor.hashCode());
                String anchorId = anchorLocationHmap.get(coloredAnchor.anchor.hashCode());
                //Log.e("vaibh", "You're seeing Anchor: " + anchorId);
                anchorsInView.put(anchorId, coloredAnchor.anchor);
                //messageSnackbarHelper.showMessage(this,"You're seeing Anchor: " + anchorLocationHmap.get(coloredAnchor.anchor.hashCode()));
            }
            Log.e("apeks", "Anchor is NOT visible on the screen::"+coloredAnchor.anchor.hashCode());
        }

      /**
        This is just a temporarily logic for demonstration pursposes...
       */
        //Log.e("vaibh", "Anchors in View size = "+anchorsInView.size());
        if(anchorsInView.size() > 0) {
            String displayAnchorIds = "";

            for (Map.Entry<String, Anchor> entry : anchorsInView.entrySet())
                displayAnchorIds = displayAnchorIds + entry.getKey() + ", ";

            displayAnchorIds = displayAnchorIds.substring(0,displayAnchorIds.length()-2);

            if(anchorsInView.size() > 0){
                String nearestAnchorId = getClosestAnchor(frame, camera, anchorsInView);
                displayAnchorIds += "\nNearest Anchor: "+nearestAnchorId.split(":")[0
                        ] +String.format("\nAt %.3f",Float.valueOf(nearestAnchorId.split(":")[1]))+" meters away.";
            }
            final String anchorsDisplayed = displayAnchorIds;
            runOnUiThread(() -> textView.setText("I'm seeing Anchor: " + anchorsDisplayed));
        }
        else
            runOnUiThread(() -> textView.setText("I don't see any markers on the screen :("));

    } catch (Throwable t) {
      // Avoid crashing the application due to unhandled exceptions.
      Log.e(TAG, "Exception on the OpenGL thread", t);
    }
  }

    private void resolving_anchors() {
        Log.e(TAG,"indu resolving _anchors");

      if(flag) {
             Log.e(TAG,"indu flag is true");
              ArrayList<Integer> code=new ArrayList<>();
              code = updateanchorstate();

              Log.e(TAG,"indu code"+code);
              //Retreive short code from vaibahvs function
              for(int shortcode : code){
                  storageManager.getCloudAnchorId(shortcode,cloudAnchorId -> {
                      if(cloudAnchorId!=null) {

                          Log.e(TAG,"indu shorty "+shortcode+cloudAnchorId);
                          Log.e(TAG,"indu cloud id"+cloudAnchorId+" \t state"+codes_hm.get(cloudAnchorId));
                        /* if(codes_hm.containsValue(cloudAnchorId))
                              Log.e(TAG,"indu contains value");
                          if(codes_hm.containsKey(cloudAnchorId))
                              Log.e(TAG,"indu contains");
                          if(!codes_hm.get(cloudAnchorId).equals( Anchor.CloudAnchorState.SUCCESS))
                              Log.e(TAG,"indu state is not success");*/
                          if(codes_hm.containsKey(cloudAnchorId)&& !codes_hm.get(cloudAnchorId).equals( Anchor.CloudAnchorState.SUCCESS)  ) {
                              Anchor resolvedAnchor = session.resolveCloudAnchor(cloudAnchorId);
                              float[] objColor = new float[]{66.0f, 133.0f, 244.0f, 255.0f};

                              anchors.add(new ColoredAnchor(resolvedAnchor, objColor, ColoredAnchor.AppAnchorState.RESOLVING));
                              Toast.makeText(getApplicationContext(), "Now resolving anchor..", Toast.LENGTH_SHORT).show();
                          }
                      }
                  });}
      }

    }

    private ArrayList<Integer> updateanchorstate() {
      Log.e(TAG,"indu updateanchorstate");
        ArrayList<Integer> scodes=new ArrayList<>();
        for(HashMap.Entry<String,Anchor.CloudAnchorState> hm:codes_hm.entrySet()){

                for(ColoredAnchor canchor:anchors){
                    String id=canchor.anchor.getCloudAnchorId();
                    Anchor.CloudAnchorState state=canchor.anchor.getCloudAnchorState();
                        if(id.equals(hm.getKey())){
                            Log.e(TAG,"indu setting anchor status" +state);
                            hm.setValue(state);
                            if(state== Anchor.CloudAnchorState.SUCCESS) {
                                Log.e(TAG,"indu idcode"+id_code);
                                if(id_code.containsKey(id)){
                                    Log.e(TAG,"indu getshortcodes");
                                    scodes=getNextShortCodes(id_code.get(id));
                                }
                            }
                        }
                }

        }
        return scodes;
    }

    private void storeAnchorIdToDB(ColoredAnchor coloredAnchor) {
      storageManager.nextShortCode(
              (shortCode) -> {
                  if (shortCode == null) {
                      //snackbarHelper.showMessageWithDismiss(this, "Could not obtain a short code.");
                      Log.e(TAG, "Could not obtain a short code.");
                      return;
                  }
                  storageManager.storeUsingShortCode(shortCode, coloredAnchor.anchor.getCloudAnchorId());
                  //Log.e(TAG,"indu saving in hashmap"+shortCode+coloredAnchor.anchor.getCloudAnchorId());
                 // id_code.put(coloredAnchor.anchor.getCloudAnchorId(),shortCode);
                  //TODO Snackbar Implementation.
                  //snackbarHelper.showMessageWithDismiss(
                  //        this, "Anchor hosted successfully! Cloud Short Code: " + shortCode);
                  String toastMsg = "Anchor hosted successfully! Cloud Short Code: "+ shortCode;

                  Toast.makeText(getApplicationContext(),toastMsg , Toast.LENGTH_SHORT).show();
                  Log.e(TAG, toastMsg);
                  //TODO: For location details ask user input or take from arrayList.
                  int locId = Integer.parseInt(anchorLocationHmap.get(coloredAnchor.anchor.hashCode()));
                  graphNodesMap.put(locId, new GraphHelper(shortCode, locId, coloredAnchor.anchor));
                  shortCodeLocationMap.put(shortCode,locId);
                  graphHelperMap.put(locId,shortCode);
              });
  }

    private void savetofile(String data) {
        try {
            Log.e(TAG,"indu path"+path);
            new File(path).mkdir();
            File file = new File(path+ fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(file,true);

            fileOutputStream.write((data + System.getProperty("line.separator")).getBytes());

        }  catch(FileNotFoundException ex) {
            Log.d(TAG, ex.getMessage());
        }  catch(IOException ex) {
            Log.d(TAG, ex.getMessage());
        }
      Log.e(TAG,"indu,saving to file");
    }

    double[] world2Screen(int screenWidth, int screenHeight, float[] world2cameraMatrix)
  {
    float[] origin = {0f, 0f, 0f, 1f};
    float[] deviceScreenMatrix = new float[4];
    Matrix.multiplyMV(deviceScreenMatrix, 0,  world2cameraMatrix, 0,  origin, 0);

    deviceScreenMatrix[0] = deviceScreenMatrix[0]/deviceScreenMatrix[3];
    deviceScreenMatrix[1] = deviceScreenMatrix[1]/deviceScreenMatrix[3];

    double[] pos_2d = new double[]{0,0};
    pos_2d[0] = screenWidth  * ((deviceScreenMatrix[0] + 1.0)/2.0);
    //pos_2d[1] = screenWidth  * ((deviceScreenMatrix[1] + 1.0)/2.0);
    pos_2d[1] = screenHeight * (( 1.0 - deviceScreenMatrix[1])/2.0); // Optimised formula (not mine).

    return pos_2d;
  }

  // Handle only one tap per frame, as taps are usually low frequency compared to frame rate.
  private void handleTap(Frame frame, Camera camera) {
    MotionEvent tap = tapHelper.poll();
    if (tap == null) Log.e("kala:","Null taps");
    else Log.e("kala:","Good taps");
    if (tap != null && camera.getTrackingState() == TrackingState.TRACKING) {
      for (HitResult hit : frame.hitTest(tap)) {
        // Check if any plane was hit, and if it was hit inside the plane polygon
        Trackable trackable = hit.getTrackable();
          /**
           *  trackable.getAnchors() returns anchors attached to it.
           *  So, check if in the current frame any anchors are there.
           *  God hope this works, I need this very to work badly......
           */
        // Creates an anchor if a plane or an oriented point was hit.
        if ((trackable instanceof Plane
                && ((Plane) trackable).isPoseInPolygon(hit.getHitPose())
                && (PlaneRenderer.calculateDistanceToPlane(hit.getHitPose(), camera.getPose()) > 0))
            || (trackable instanceof Point
                && ((Point) trackable).getOrientationMode()
                    == OrientationMode.ESTIMATED_SURFACE_NORMAL)) {
          // Hits are sorted by depth. Consider only closest hit on a plane or oriented point.
          // Cap the number of objects created. This avoids overloading both the
          // rendering system and ARCore.
          if (anchors.size() >= 25) {
            anchors.get(0).anchor.detach();
            anchorLocationHmap.remove(anchors.remove(0).anchor.hashCode()); // Remove corresponding entry here.
          }

          // Assign a color to the object for rendering based on the trackable type
          // this anchor attached to. For AR_TRACKABLE_POINT, it's blue color, and
          // for AR_TRACKABLE_PLANE, it's green color.
          float[] objColor;
          if (trackable instanceof Point) {
            objColor = new float[] {66.0f, 133.0f, 244.0f, 255.0f};
          } else if (trackable instanceof Plane) {
            objColor = new float[] {139.0f, 195.0f, 74.0f, 255.0f};
          } else {
            objColor = DEFAULT_COLOR;
          }

          //Refer DetailedInfo line 6
          Anchor anc = session.hostCloudAnchor(hit.createAnchor());

          anchors.add(new ColoredAnchor(anc, objColor, ColoredAnchor.AppAnchorState.HOSTING));
          Log.e("TAG","Putting "+anc.hashCode() +":::"+ Integer.toString(anchorLocationHmap.size()));
          anchorLocationHmap.put(anc.hashCode(), Integer.toString(++anchorCount)); // Store anchor hash and its distance from camera

          /*Log.e("TAG","New size anchorLocHM= "+anchorLocationHmap.size()+":::anchors list="+anchors.size());
          for (Map.Entry<Integer, String> e : anchorLocationHmap.entrySet())
              Log.e("TAG","Key: "+e.getKey()+" Val: "+e.getValue());
          */

          break;

          // Fuck this'nt not working........
        }
      }
    }
  }

    /**
     *  Function to find the anchor closest to the device based on the set of visible anchors.
     */
    private String getClosestAnchor(Frame frame, Camera camera, HashMap<String, Anchor> anchorsIdsInView) {
        float nearestAnchorDistance = Float.MAX_VALUE;
        String nearestAnchorId = "Unknown";
        Pose devicePose = camera.getPose();
        Pose markerPose;

        for (Map.Entry<String, Anchor> entry : anchorsIdsInView.entrySet()) {
            markerPose = entry.getValue().getPose();
            float dx = markerPose.tx() - devicePose.tx();
            float dy = markerPose.ty() - devicePose.ty();
            float dz = markerPose.tz() - devicePose.tz();

            float distanceMeters = (float) Math.sqrt(dx*dx + dy*dy + dz*dz);

            if (distanceMeters < nearestAnchorDistance) {
                nearestAnchorDistance = distanceMeters;
                nearestAnchorId = entry.getKey();
                //Log.e("apeks:","Near ID: "+nearestAnchorId + " Distance= "+nearestAnchorDistance);
            }
        }
        return nearestAnchorId+":"+nearestAnchorDistance;
    }
    public void createAdjacencyMatrix() {
        int totalNodes = graphNodesMap.size();
        Log.e("NODES:",""+totalNodes);
        if ( totalNodes < 1)
            return;
        graphMatrix = new float[totalNodes][totalNodes];
        int i = 0, j = 0;
        for (; i < totalNodes; i++) {
            for (; j < totalNodes; j++) {
                /*if (i==j) {
                    graphMatrix[i][i] = 0;
                }*/
                if (i == 0 && j == totalNodes-1)
                    continue;
                if (Math.abs(i-j) == 1) {
                    //Anchor start = graphNodesMap.get(i).getAnchor();
                    //Anchor end = graphNodesMap.get(j).getAnchor();
                    graphMatrix[i][j] = 1;//getDistance(start, end);
                }
            }
        }
        new ExportData().writeMatrixData(context,graphMatrix,"adjacencyMatrix");
    }
    public float getDistance(Anchor start, Anchor end) {
        Pose startPose = start.getPose();
        Pose endPose = end.getPose();
        float dx = startPose.tx() - endPose.tx();
        float dy = startPose.ty() - endPose.ty();
        float dz = startPose.tz() - endPose.tz();
        return (float) Math.sqrt(dx*dx + dy*dy + dz*dz);
    }
    public ArrayList<Integer> getNextShortCodes(int currentShortCode) {

        shortCodeLocationMap = new ExportData().readData(context,"shortcodes");
        graphHelperMap = new ExportData().readData(context,"graphhelper");
        graphMatrix = new ExportData().readMatrixData(context,"adjacencyMatrix", graphHelperMap.size());
        Log.e("MATRIX:","CUr shortcode="+currentShortCode);

        int currentLocation = shortCodeLocationMap.get(currentShortCode);
        Log.e("MATRIX:","curLoc="+currentLocation);
        ArrayList<Integer> nextShortCodes = new ArrayList<>();
        for (int i = 0; i < graphHelperMap.size(); i++) {
            if (graphMatrix[currentLocation][i] != 0 ) {
                nextShortCodes.add(_getnextShortCode(i));
            }
        }
        return nextShortCodes;
    }
    public int _getnextShortCode(int location) {
        for (Map.Entry<Integer, GraphHelper> i : graphNodesMap.entrySet()) {
            if ( i.getValue().getLocationId() == location)
                return i.getValue().getAnchorShortCode();
        }
        Log.e("FATAL","Exception in geting shortcode");
        return 0;
    }
}
