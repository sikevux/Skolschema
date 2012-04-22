package tobbe.android.skolschema;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

public class ScheduleFetcher {

	private static final String BASE_URL = "http://www.novasoftware.se/ImgGen/schedulegenerator.aspx?";
	
	public static String buildURL(String schoolID, String password, String studentID,
										String period, String week, String day, int width, int height) {
		String url = BASE_URL + "format=png";
		url += "&schoolid=" + schoolID;
		url += "&id=" + studentID;
		
		if(password != "") {
			url += '|' + password;
		}

		url += "&period=" + period;
		
		if(!schoolID.equals("87850")) {
			url += "&week=" + week;
		} else {
			url += "&week=";
		}
		
		url += "&day=" + day;
		url += "&width=" + String.valueOf(width);
		url += "&height=" + String.valueOf(height);
		
		return url;
	}
	
	/* Downloads and saves a schedule as an image. */
	public static boolean fetchSchedule(String url, File outputFile) {
		try {
			URL u = new URL(url);
			BufferedInputStream in = new BufferedInputStream(u.openStream());
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile));
			
			int b = 0;
			while((b = in.read()) != -1) {
				out.write(b);
			}
			
			out.close();
			in.close();
			
		} catch(IOException e) {
			return false;
		}
		
		return true;
	}
	
}
