package tobbe.android.skolschema;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Toast;

public class ScheduleActivity extends Activity {

	public static final String BASE_DIR = "Skolschema";
	public static final String INFO_FILE = "info.dat";
	public static final String SCHEDULE_FILE = "schema.png";
	public static final String MAIN_SCHEDULE_FILE = "huvudschema.dat";
	
	// Request code
	private static final int REQUEST_SCHEDULE = 0;
	private static final int REQUEST_SCHEDULE_SETTINGS = 1;
	
	// Menu item IDs
	private static final int MENU_CHOOSE_SCHEDULE = 0;
	private static final int MENU_UPDATE_SCHEDULE = 1;
	private static final int MENU_SCHEDULE_SETTINGS = 2;
	private static final int MENU_ABOUT = 3;
	
	// Path to the folder of the open schedule
	private File openSchedule = null;
	
	private WebView scheduleView;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize logging and load settings
        Logger.initialize(this);
        Settings.loadSettings();
        
        // Set to fullscreen
        if(Settings.getBoolean(Settings.FULLSCREEN_MODE)) {
	        requestWindowFeature(Window.FEATURE_NO_TITLE);
	        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
	                                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        
        setContentView(R.layout.schedule);
        
        // Prepare the WebView
        scheduleView = (WebView) findViewById(R.id.scheduleView);

        scheduleView.getSettings().setBuiltInZoomControls(true);
        scheduleView.getSettings().setSupportZoom(true);

        // Create a .nomedia file if it doesn't exist already
        File noMedia = new File(new File(Environment.getExternalStorageDirectory(), BASE_DIR), ".nomedia");

        if(!noMedia.exists()) {
        	try {
				noMedia.createNewFile();
			} catch (IOException e) {
				
				Logger.log(getResources().getString(R.string.couldNotCreatenomedia));
			}
        }

        // Load a schedule
        loadSchedule();
    }
    
    public void loadSchedule() {
    	// Check so that the base directory exists
    	File baseDir = new File(Environment.getExternalStorageDirectory(), BASE_DIR);
    	
    	if(!baseDir.exists()) {
    		if(!baseDir.mkdir()) {
    			Logger.log(getResources().getString(R.string.couldNotCreateBaseDir));
    			finish();
    		}
    	}
    	
    	// Check for schedules (if this device has no saved schedules let the user add a new one)
    	boolean hasSchedules = false;
    	
    	for(File f : baseDir.listFiles()) {
    		if(f.isDirectory()) {
    			hasSchedules = true;
    			break;
    		}
    	}
    	
    	if(hasSchedules) {
    		// Look in the main schedule file
    		File mainSchedule = new File(baseDir, MAIN_SCHEDULE_FILE);
    		
    		if(mainSchedule.exists()) {
    			try {
	    			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(mainSchedule)));
	    			
	    			String mainSchedulePath = in.readLine();
	    			openSchedule = new File(mainSchedulePath);
	    			
	    			in.close();
	    			
	    			// Update
	    			showSchedule();
    			} catch(IOException e) {
    				Logger.log(getResources().getString(R.string.corruptMainScheduleFile));
    				mainSchedule.delete();
    			}
    		} else {
    			// Let the user pick the schedule to open
    			Intent i = new Intent(this, ScheduleListActivity.class);
    			startActivityForResult(i, REQUEST_SCHEDULE);
    		}
    		
    	} else {
    		// Let the user add a schedule
			Intent i = new Intent(this, AddScheduleActivity.class);
			startActivityForResult(i, REQUEST_SCHEDULE);
    	}
    	
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if(requestCode == REQUEST_SCHEDULE && data != null) {
	    	String schedulePath = data.getExtras().getString("schedule");
	    	openSchedule = new File(schedulePath);
	    	
	    	// Try to update the schedule
	    	showSchedule();
	    	
    	} else if(requestCode == REQUEST_SCHEDULE_SETTINGS){
    		showSchedule();
    	}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	menu.add(0, MENU_CHOOSE_SCHEDULE, 0, getResources().getString(R.string.menuChooseSchedule));
    	menu.add(0, MENU_UPDATE_SCHEDULE, 0, getResources().getString(R.string.menuUpdateSchedule));
    	menu.add(0, MENU_SCHEDULE_SETTINGS, 0, getResources().getString(R.string.menuScheduleSettings));
    	menu.add(0, MENU_ABOUT, 0, getResources().getString(R.string.menuAbout));
    	
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	// Handle item selection
        switch (item.getItemId()) {
            case MENU_CHOOSE_SCHEDULE:
        		// Let the user select which schedule to open
        		Intent i = new Intent(this, ScheduleListActivity.class);
        		startActivityForResult(i, 0);
        		
            	break;
            case MENU_UPDATE_SCHEDULE:
            	if(openSchedule != null) {
            		updateSchedule();
            	}
            	
            	break;
            case MENU_SCHEDULE_SETTINGS:
            	if(openSchedule != null) {
            		Intent intent = new Intent(this, ScheduleSettingsActivity.class);
            		intent.putExtra("schedule", openSchedule.getAbsolutePath());
            		startActivityForResult(intent, REQUEST_SCHEDULE_SETTINGS);
            	}
            	
            	break;
            case MENU_ABOUT:
            	aboutDialog();
            	break;
            default:
                return super.onOptionsItemSelected(item);
        }
        
        return true;
    }

    public void showSchedule() {	
		// Get active network type
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		
		// Load cached version if settings don't allow updating schedules on mobile network
		if(ni != null && ni.getType() == ConnectivityManager.TYPE_MOBILE && !Settings.getBoolean(Settings.UPDATE_SCHEDULES_ON_MOBILE_NETWORK))
		{	
	    	// Tell the user we use the cached version
	    	Toast.makeText(this, getResources().getString(R.string.usingCachedAndMobileOn), Toast.LENGTH_LONG).show();
		
			// Show the schedule
		    refreshImage();
		
		} else {
    		updateSchedule();
    	}
    }
    
    public void refreshImage() {
	    File scheduleImg = new File(openSchedule, SCHEDULE_FILE);
	    scheduleView.loadUrl("file://" + scheduleImg.getPath());
    }
    
    public void updateSchedule() {
    	
    	File scheduleFile = new File(openSchedule, SCHEDULE_FILE);
    	File infoFile = new File(openSchedule, INFO_FILE);
    	
    	String url = null;
    	
    	try {
    		// Read from the info file
    		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(infoFile)));
    		
			String schoolID = in.readLine();
			String password = in.readLine();
			String studentID = in.readLine();
			String type = in.readLine();
    		
    		// Update the schedule
    		Calendar cal = Calendar.getInstance();
    		
    		if(type.equals("weekly")) {
				url = ScheduleFetcher.buildURL(schoolID, password, studentID,
	  					                              "", String.valueOf(cal.get(Calendar.WEEK_OF_YEAR)),
	  					                              "0", 800, 600);
    		} else if(type.equals("daily")) {
				// Get the day number ( -2 to because of Sunday and to make Monday == 0
				int day = cal.get(Calendar.DAY_OF_WEEK) - 2;
				int dayEncoded = 1 << day;
				int week = cal.get(Calendar.WEEK_OF_YEAR);
				
				// Show the schedule for monday if it is currently weekend
				if(day == -1 || day > 4) {
					week++;
					
					if(week > 52)
						week = 1;
					
					dayEncoded = 1;
				}
				
				url = ScheduleFetcher.buildURL(schoolID, password, studentID,
	  					  "", String.valueOf(week), String.valueOf(dayEncoded), 300, 600);
				
    		} else if(type.equals("sweek")){
    			// Read the specific week
    			String specificWeek = in.readLine();
    			
        		url = ScheduleFetcher.buildURL(schoolID, password, studentID,
	                                           "", specificWeek, "0", 800, 600);
    		}
    		
    		in.close();
				
    	} catch(IOException e) {
    		Toast.makeText(this, getResources().getString(R.string.corruptScheduleInfoFile), Toast.LENGTH_LONG).show();
    	}
    	
    	// Download the schedule
    	if(url != null) {
    		new DownloadSchedule().execute(url, scheduleFile.getAbsolutePath());
    	} else {
    		Toast.makeText(this, getResources().getString(R.string.corruptScheduleInfoFile), Toast.LENGTH_LONG).show();
    	}
    }
    
    
	private void aboutDialog() {
		final String items[] = getResources().getStringArray(R.array.aboutDialog);
		final ScheduleActivity thisActivity = this;

		AlertDialog.Builder ab = new AlertDialog.Builder(this);
		
		ab.setTitle(getResources().getString(R.string.aboutDialogTitle));
		ab.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface d, int choice) {
				if(choice == 0) {
					// Start the help activity
	            	Intent intent = new Intent(thisActivity, HelpActivity.class);
	            	startActivity(intent);
				} else if(choice == 1) {
					// Start the settings activity
	            	Intent intent = new Intent(thisActivity, AppSettingsActivity.class);
	            	startActivity(intent);
				}
			}
		});
		
		ab.show();
	}
	
	private class DownloadSchedule extends AsyncTask <String, Integer, String> {

		@Override
		protected String doInBackground(String... params) {
			if(!ScheduleFetcher.fetchSchedule(params[0], new File(params[1])))
				publishProgress(-1);
				
			return params[1];
		}	
		
		@Override
		protected void onProgressUpdate(Integer... integers) {
			if(integers[0] == -1)
				Toast.makeText(ScheduleActivity.this, getResources().getString(R.string.couldNotUpdateScheduleCached), Toast.LENGTH_LONG).show();
		}
		
		@Override
		protected void onPostExecute(String s) {
			refreshImage();
		}
	}
}