package perfecto.mobileTTS;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

public class MobileTTS {
	RemoteWebDriver driver;
	private URL BASE_URL;
	private static final String HTTPS = "https://";
	private static final String MEDIA_REPOSITORY = "/services/repositories/media/";
	private static final String UPLOAD_OPERATION = "operation=upload&overwrite=true";
	private static final String DELETE_OPERATION = "operation=delete";
	private static final String AUDIO_FILE_PATH = "PRIVATE:temp/tts_";
	private static final String UTF_8 = "UTF-8";
	private final String TTS_PARAM_NAME = "src";
	private final String KEY_PARAM_NAME = "key";
	private final String LANGUAGE_PARAM_NAME = "hl";
	private final String AUDIO_FORMAT_NAME = "f";
	private String key;
	private String language = "en-us";
	private String tts;
	private String audioFormat = "44khz_16bit_mono";
	private byte[] stream;
	private String host;
	private String username;
	private String password;
	private String fileSuffix;
	
	private MobileTTS(){
	}
	public MobileTTS(RemoteWebDriver driver, String password, String key, LANGUAGE language) throws MalformedURLException{
		this.driver = driver;
		setUserInfo(password);
		BASE_URL  = new URL("http://api.voicerss.org/?");
		//setTTS(tts);
		setKey(key);
		setLanguage(language);
		setFileSuffix();
	}
	public void setUserInfo(String password){
		Map<String, ?> map = driver.getCapabilities().asMap();
		setHost(map.get("host").toString());
		setUsername(map.get("executionId").toString().split("_")[0]);
		setPassword(password);
	}
	public void setHost(String host){
		this.host = host;
	}
	public void setUsername(String username){
		this.username = username;
	}
	
	public void setPassword(String password){
		this.password = password;
	}
	public void setTTS(String tts){
		this.tts = tts;
	}
	public void setKey(String key){
		this.key = key;
	}
	public void setLanguage(LANGUAGE language){
		switch(language){
		case ENGLISH_US:
			this.language = "en-us";
			break;
		case ENGLISH_CA:
			this.language = "en-ca";
			break;
		case ENGLISH_GB:
			this.language = "en-gb";
			break;
		case GERMAN:
			this.language = "de-de";
			break;
		case SPANISH:
			this.language = "es-es";
			break;
		case CHINESE:
			this.language = "zh-cn";
			break;
			default:
				break;
		}
	}
	private void setFileSuffix(){
		fileSuffix = String.valueOf(System.nanoTime()/1000) + ".mp3";
	}
	public void injectAudioFromText(String tts) throws Exception{
		try {
			generateAudio(tts);
			uploadAudio();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		pushAudio();
		try {
			deleteAudio();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public byte[] generateAudio(String tts) throws Exception{
		URL url = null;
		setTTS(tts);
		//byte[] stream = null;
		try {
			url = new URL(BASE_URL + KEY_PARAM_NAME + "=" + URLEncoder.encode(key, "UTF-8")
					+ "&" + LANGUAGE_PARAM_NAME + "=" + URLEncoder.encode(language, "UTF-8")
					+ "&" + TTS_PARAM_NAME + "=" + URLEncoder.encode(tts, "UTF-8")
					+ "&" + AUDIO_FORMAT_NAME + "=" +URLEncoder.encode(audioFormat, "UTF-8"));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(url != null){
			//System.out.println(url);
			//url = URLEncoder.encode(url, "UTF-8");
			//System.out.println("Sending request for audio file");
			stream = getStream(url);
			String stringStream = new String(stream);
			if(stringStream.contains("ERROR")){
				throw new Exception(stringStream);
			}
			//System.out.println("Audio file received");
			return stream;
			
		}
		return null;
		
	}
	private byte[] getStream(URL url) throws IOException{
		
    //Code to download
		 InputStream in = new BufferedInputStream(url.openStream());
		 ByteArrayOutputStream out = new ByteArrayOutputStream();
		 byte[] buf = new byte[1024];
		 int n = 0;
		 while (-1!=(n=in.read(buf)))
		 {
		    out.write(buf, 0, n);
		 }
		 out.close();
		 in.close();
		 byte[] response = out.toByteArray();
		 return response;
		 /*
		 FileOutputStream fos = new FileOutputStream(fileName);
		 fos.write(response);
		 fos.close();
		 */
    //End download code
	}
	public void injectVoice(String tts){
		
	}
	/**
	 * Uploads content to the media repository.
	 * Example:
	 * uploadMedia("demo.perfectomobile.com", "john@perfectomobile.com", "123456", content, "PRIVATE:apps\\ApiDemos.apk");
	 */
	public void uploadAudio() throws UnsupportedEncodingException, MalformedURLException, IOException {
		if (stream != null) {
			String encodedUser = URLEncoder.encode(username, "UTF-8");
			String encodedPassword = URLEncoder.encode(password, "UTF-8");
			String urlStr = HTTPS + host + MEDIA_REPOSITORY + AUDIO_FILE_PATH + fileSuffix + "?" + UPLOAD_OPERATION + "&user=" + encodedUser + "&password=" + encodedPassword;
			URL url = new URL(urlStr);
			//System.out.println("Uploading audio file to MCM Repository");
			sendRequest(stream, url);
			//System.out.println("Upload complete");
		}
	}


	private static void sendRequest(byte[] content, URL url) throws IOException {
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		connection.setDoOutput(true);
		connection.setRequestProperty("Content-Type", "application/octet-stream");
		connection.connect();
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		outStream.write(content);
		outStream.writeTo(connection.getOutputStream());
		outStream.close();
		int code = connection.getResponseCode();
		if (code > HttpURLConnection.HTTP_OK) {
			handleError(connection);
		}
	}

	private static void handleError(HttpURLConnection connection) throws IOException {
		String msg = "Failed to upload media.";
		InputStream errorStream = connection.getErrorStream();
		if (errorStream != null) {
			InputStreamReader inputStreamReader = new InputStreamReader(errorStream, UTF_8);
			BufferedReader bufferReader = new BufferedReader(inputStreamReader);
			try {
				StringBuilder builder = new StringBuilder();
				String outputString;
				while ((outputString = bufferReader.readLine()) != null) {
					if (builder.length() != 0) {
						builder.append("\n");
					}
					builder.append(outputString);
				}
				String response = builder.toString();
				msg += "Response: " + response;
			}
			finally {
				bufferReader.close();
			}
		}
		throw new RuntimeException(msg);
	}
	public void deleteAudio() throws IOException{
		String encodedUser = URLEncoder.encode(username, "UTF-8");
		String encodedPassword = URLEncoder.encode(password, "UTF-8");
		String urlStr = HTTPS + host + MEDIA_REPOSITORY + AUDIO_FILE_PATH + fileSuffix + "?" + DELETE_OPERATION + "&user=" + encodedUser + "&password=" + encodedPassword;
		URL url = new URL(urlStr);
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		//connection.setDoOutput(true);
		//connection.setRequestProperty("Content-Type", "application/octet-stream");
		connection.connect();
		int code = connection.getResponseCode();
		if (code > HttpURLConnection.HTTP_OK) {
			handleError(connection);
		}
		//URLConnection connection = new URL(url + "?" + query).openConnection();
	}
	public void pushAudio(){
		String command = "mobile:audio:inject";
    	Map params = new HashMap<>();
    	params.put("key", AUDIO_FILE_PATH + fileSuffix);
    	params.put("wait", "Wait");
    	System.out.println("Injecting audio to device");
    	driver.executeScript(command, params);
	}
	public enum LANGUAGE{
		ENGLISH_US, ENGLISH_CA, ENGLISH_GB, GERMAN, SPANISH, CHINESE
	}
}
