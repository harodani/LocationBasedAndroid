package my.location;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import org.apache.http.util.ByteArrayBuffer;

public class XMLService { 
	
	private static final String REMOTE_PATH = "http://user.it.uu.se/~toma1029/";

	public static void downloadXML(String remoteFileName, String localFileName) {
		
		try {
			URL remoteURL = new URL (REMOTE_PATH + remoteFileName);
			File file = new File(localFileName);
			file.createNewFile();
			
			URLConnection urlConn = remoteURL.openConnection();
			
			BufferedInputStream bis = new BufferedInputStream(urlConn.getInputStream());
			
			ByteArrayBuffer baf = new ByteArrayBuffer(100);
			int current = 0;
            while ((current = bis.read()) != -1) {
            	baf.append((byte) current);
            }
            
            FileOutputStream fos = new FileOutputStream(file);
			fos.write(baf.toByteArray());
			fos.close();			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}