package com.example.artassist;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Sensor;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.FileObserver;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.glass.media.CameraManager;
import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;



// We can rebuild him. Make him stronger, faster, better than what he was before.
//I AM PTERODACTYL. I'M GOING TO PTERO YOU A NEW ASSHOLE
public class ArtAssistActivity extends Activity {
	private AudioManager mAudioManager;
	public GestureDetector mGestureDetector;
    // private SensorManager mSensorManager;
    public Sensor mAccelerometer;
    public Sensor mGyroscope;
    private static final int SPEECH_REQUEST = 0;
    private final static int TAKE_PICTURE_REQUEST = 1;
    private Matrix matrix = new Matrix();
    // private Matrix savedMatrix = new Matrix();
    public int ZoomIn = 100;
    public int ZoomOut = 90;
    public Bitmap tutorialImage;
    public Bitmap workspaceImage;
    public Bitmap workspaceImageScaled;
    // private Uri picUri;
    public boolean isTutorialRunning = true;
    protected ProgressBar progressBar;
    public boolean isAboutRunning = false;
    public boolean isCameraPhoto = false;
    public int ratioHeight;
    public int ratioWidth;
    public int gcd;

    
  //TOTAL LIST OF STUFF TO DO
  	//TODO: Zoom In/Out Pan matrix with Frame Layout - MUST BUILD NATIVELY
  	//TODO: Develop Tutorial Image as beginning image
  	//TODO: Align Tutorial Image (PHOTOSHOP)
  	//TODO: Implement Custom Google search for Google Images
  	//TODO: Browse Hard Drive for Images
  	//TODO: Fix Web Browser Intent (NoActivityFound Exception) on About Activity
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_art_assist);
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mGestureDetector = new GestureDetector(this) 
                .setBaseListener(mBaseListener) // this sets the tap listener
        		.setTwoFingerScrollListener(ZoomListener); // This sets the 2 finger scrolling listener
        tutorialImage = BitmapFactory.decodeResource(getResources(), R.drawable.tutorial_image);
     //   tutorialImage = Bitmap.createScaledBitmap(tutorialImage, 1024, 1024, true);
        ImageView currentImage = (ImageView) findViewById(R.id.imageView1);
        currentImage.setImageBitmap(tutorialImage); // Bitmap is being currently used, but I may move it to Canvas for smoother experience
        currentImage.setImageMatrix(matrix); // not sure if matrix transformation is usable, as it's with an int
		
		
		}
	@Override //send motion events to GestureDetector - BaseListener and TwoFingerScrollListener
    public boolean onGenericMotionEvent(MotionEvent event) {
        return mGestureDetector.onMotionEvent(event); 
    }
	@Override
    public void onPause() { // If I use listeners, I'll unregister them here
    	super.onPause();
    	// TODO: Unregister Listeners
    }
	
	@Override
	public void onResume() { // If a call/text comes in while using the program, I'll have the program re-register listeners right away.
		super.onResume();
		// TODO: Re-Register Listeners
	}
	
	private GestureDetector.BaseListener mBaseListener = new GestureDetector.BaseListener() {
        @Override
        public boolean onGesture(Gesture gesture) {
      	
        	if (gesture == Gesture.TAP) { // Tapping opens up the options menu
        		mAudioManager.playSoundEffect(Sounds.TAP);
                openOptionsMenu();
                return true;        
        	}
                
        	else {
        		return false;
        	}
            	
        	}
            
	};
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// This class calls the MENU on any given activity/screen
		getMenuInflater().inflate(R.menu.art_assist, menu);
		return true;
	}
	  
	  // Menu options
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.Browse_Glass_HDD:
	            // TODO Find a way to browse the Glass hard drive for images
	        	
	            return true;
	            
	        case R.id.Take_Photo:
	        	takepicture(); // This runs the block of code at the bottom to take a picture, write the image to the device, 
	        	return true;   // and set the image as the bitmap so it can be used as the user's subject.
	        case R.id.Browse_Google:
	        	runGoogleImageSearch();
	            return true;
	            
	        case R.id.start_About:
	        	setContentView(R.layout.activity_about);
	        	isAboutRunning = true; // This create a boolean check so that TwoFingerScrollListener won't throw an NPE when it tries 
	        	return true;          // to manipulate an ImageView that doesn't exist.
	        	
	        case R.id.Exit_ArtAssist:
	        	finish(); //Exits the program
	        	return true;
	        case R.id.Tutorial:
	        	ImageView currentImage = (ImageView) findViewById(R.id.imageView1); // This option resets the tutorial so that the user can be refreshed
	        	TextView TutorialText  = (TextView) findViewById(R.id.textView1);   // on how to use the program
	        	currentImage.setVisibility(View.VISIBLE);
	        	TutorialText.setVisibility(View.INVISIBLE);
	        	isTutorialRunning = true; //This is a check to make sure the image will be scaled appropriately, as tutorial image and
	        	isCameraPhoto = false;    //camera photos are different aspect ratios
	        	currentImage.setImageBitmap(tutorialImage);
	        	
	        	return true;

	        	
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
private GestureDetector.TwoFingerScrollListener ZoomListener = new GestureDetector.TwoFingerScrollListener() {
		
		@Override
		public boolean onTwoFingerScroll(float displacement, float delta, float velocity) {
				
			if (delta > 8.0) { //This is the code for zooming in, when 2Finger motion is forward and faster than 8
			    // FUCKING ZOOM IN! 
				if (isTutorialRunning == true) // If tutorial is running, set aspect ratio  at 1:1 square, so it won't distort
				{
					ZoomOut = 90;
					ImageView currentImage = (ImageView) findViewById(R.id.imageView1);
					workspaceImageScaled = Bitmap.createScaledBitmap(tutorialImage, ZoomIn, ZoomIn, true);
					ratioHeight = workspaceImageScaled.getHeight();
					ratioWidth = workspaceImageScaled.getWidth();
					// getImageRatio();
					currentImage.setImageBitmap(workspaceImageScaled);
					ZoomIn = ZoomIn + 5;
					return true;
				}
				else if (isAboutRunning == true)
				{
					return true; //If about layout is running, DO NOTHING! Handle the gestures, but don't do shit!
				}
				
				else if (isCameraPhoto == true) { //This prevents the camera image from warping upon zoom. Aspect ratio is 64:43
				ZoomOut = 90;
				ImageView currentImage = (ImageView) findViewById(R.id.imageView1);
				Bitmap workspaceImageScaled = Bitmap.createScaledBitmap(workspaceImage, (ZoomIn * 64), (ZoomIn * 43), true);
				currentImage.setImageBitmap(workspaceImageScaled);
				ZoomIn = ZoomIn + 1;
				return true;
				}
				else { //This is the default zoom code for zooming 1:1
					ZoomOut = 90;
					ImageView currentImage = (ImageView) findViewById(R.id.imageView1);
					Bitmap workspaceImageScaled = Bitmap.createScaledBitmap(workspaceImage, (ZoomIn), (ZoomIn), true);
					currentImage.setImageBitmap(workspaceImageScaled);
					ZoomIn = ZoomIn + 1;
					return true;
				}

			}
			else if (delta < -8.0) { // FUCKING ZOOM OUT!
				if (isTutorialRunning == true) //Check for tutorial for aspect ratio
				{
					if (ZoomOut <= 5) {
						return true;
					}
					ZoomIn = 100; // Reset ZoomIn to 100, so that it doesn't zoom in too far and crash the program
					ImageView currentImage = (ImageView) findViewById(R.id.imageView1);
					Bitmap workspaceImageScaled = Bitmap.createScaledBitmap(tutorialImage, ZoomOut, ZoomOut, true);
					currentImage.setImageBitmap(workspaceImageScaled);
					ZoomOut = ZoomOut - 1;
					return true;
				}
				else if (isAboutRunning == true)
				{
					return true;
				}
				else {
					if (ZoomOut <= 5) {
						return true;
					}
				ZoomIn = 100;
				ImageView currentImage = (ImageView) findViewById(R.id.imageView1);
				
				Bitmap workspaceImageScaled = Bitmap.createScaledBitmap(workspaceImage, (ZoomOut * 64), (ZoomOut * 43), true);
				currentImage.setImageBitmap(workspaceImageScaled);
				ZoomOut = ZoomOut - 1;
				
				return true;
				}
			}

				
			
			
			else if (delta >= -8.0 && delta <= 8.0) {
			    // FUCKING PAN!
				
				/* Register Sensors
				mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
			    mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			    mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
			    */
				
				return true;
				
			}
		
			else {
				return false;
			}
			    
	
}
};

	private void  takepicture() { //This takes a picture and sends an intent to take a photo
		isAboutRunning = false;
		setContentView(R.layout.activity_art_assist);
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	    startActivityForResult(intent, TAKE_PICTURE_REQUEST);
	}
	
	
	private void runGoogleImageSearch() {
		isAboutRunning = false;
		setContentView(R.layout.activity_art_assist);
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		startActivityForResult(intent, SPEECH_REQUEST);
	}
	
/*	public void getImageRatio() {
		if (ratioWidth > ratioHeight) {
			while (ratioHeight != 0) {
				ratioHeight = ratioHeight % ratioWidth;
				
			}
			
			gcd = ratioWidth;
			ratioWidth = workspaceImageScaled.getWidth();
			ratioHeight = workspaceImageScaled.getHeight();
		}
		else {
			
		}
		}
		
	*/
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
	        Intent data) {                                              // Speech request for Google Images!
	    if (requestCode == SPEECH_REQUEST && resultCode == RESULT_OK) { // 
	        List<String> results = data.getStringArrayListExtra(
	                RecognizerIntent.EXTRA_RESULTS);
	        String spokenText = results.get(0);
	        // DEBUG FODDER
	        TextView SearchTerm = (TextView) findViewById(R.id.textView1);
	        SearchTerm.setText("You searched for: " + spokenText + System.getProperty("line.separator") + 
	        		"This Feature is Pending!" + System.getProperty("line.separator") + "Thanks for being patient!");
	        SearchTerm.setVisibility(View.VISIBLE);
	        ImageView currentImage = (ImageView) findViewById(R.id.imageView1);
	        currentImage.setVisibility(View.INVISIBLE);
	        //TODO Find a way to parse JSON data from Google Image Search
	        // DO THIS IN THE BACKGROUND THREAD!!!!
	    }
	    else if (requestCode == TAKE_PICTURE_REQUEST && resultCode == RESULT_OK) {
	    	String picturePath = data.getStringExtra(
	                CameraManager.EXTRA_PICTURE_FILE_PATH);

	        processPictureWhenReady(picturePath);
	        
	        // 06-08 13:41:08.767: W/OpenGLRenderer(3551): Bitmap too large to be uploaded into a texture (2528x1856, max=2048x2048)


	    }
	    super.onActivityResult(requestCode, resultCode, data);
	}
	
	
	
	private void processPictureWhenReady(final String picturePath) {
	    final File pictureFile = new File(picturePath);

	    if (pictureFile.exists()) { // this processes the photo when it's ready!
	    	progressBar.setVisibility(View.INVISIBLE);
	    	Bitmap rawCameraCapture = BitmapFactory.decodeFile(picturePath);
		    Log.d("CURRENT FILE PATH", picturePath);
		    Log.d("rawCameraCapture OLD Dimensions", rawCameraCapture.getWidth() + " ," + rawCameraCapture.getHeight());
		    workspaceImage = Bitmap.createScaledBitmap(rawCameraCapture, 2048, 1376, true);
		    Log.d("rawCameraCapture NEW Dimensions", rawCameraCapture.getWidth() + " ," + rawCameraCapture.getHeight());
		    ImageView currentImage = (ImageView) findViewById(R.id.imageView1);
		    currentImage.setVisibility(View.VISIBLE);
		    currentImage.setImageBitmap(workspaceImage);
		    TextView currentText = (TextView) findViewById(R.id.textView1);
		    currentText.setVisibility(View.INVISIBLE);
		    Log.d("RESULTS", "File should be visible now");
	        mAudioManager.playSoundEffect(Sounds.SUCCESS);
	        isTutorialRunning = false;
	        isCameraPhoto = true;
	        
	        
	        
	    } else {
	        ImageView currentImage = (ImageView) findViewById(R.id.imageView1);
	        currentImage.setVisibility(View.INVISIBLE);
	        progressBar = (ProgressBar) findViewById(R.id.progressBar1);
	        progressBar.setVisibility(View.VISIBLE);
	        
	        

	        final File parentDirectory = pictureFile.getParentFile();
	        FileObserver observer = new FileObserver(parentDirectory.getPath(),
	                FileObserver.CLOSE_WRITE | FileObserver.MOVED_TO) {
	            // Protect against additional pending events after CLOSE_WRITE
	            // or MOVED_TO is handled.
	            private boolean isFileWritten;

	            @Override
	            public void onEvent(int event, String path) {
	                if (!isFileWritten) {
	                    // For safety, make sure that the file that was created in
	                    // the directory is actually the one that we're expecting.
	                    File affectedFile = new File(parentDirectory, path);
	                    isFileWritten = affectedFile.equals(pictureFile);
	                    Log.d("FILE STATUS", "File is not ready!");
	                    Log.d("parentDirectory", parentDirectory.toString());
	                    Log.d("Current File path", path);
	                    if (isFileWritten) {
	                    	
	                        stopWatching();

	                        // Now that the file is ready, recursively call
	                        // processPictureWhenReady again (on the UI thread).
	                        runOnUiThread(new Runnable() {
	                            @Override
	                            public void run() {
	                                processPictureWhenReady(picturePath);
	                            }
	                        });
	                    }
	                }
	            }
	        };
	        observer.startWatching();
	    }
	}
}




