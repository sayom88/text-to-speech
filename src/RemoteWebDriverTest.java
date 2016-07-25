import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.*;
import org.openqa.selenium.html5.*;
import org.openqa.selenium.logging.*;
import org.openqa.selenium.remote.*;
import org.openqa.selenium.Cookie.Builder;

import perfecto.mobileTTS.MobileTTS;
import perfecto.mobileTTS.MobileTTS.LANGUAGE;

import com.perfectomobile.selenium.util.EclipseConnector;

public class RemoteWebDriverTest {

    public static void main(String[] args) throws MalformedURLException, IOException {
        System.out.println("Run started");

        String browserName = "mobileOS";
        DesiredCapabilities capabilities = new DesiredCapabilities(browserName, "", Platform.ANY);
        String host = "demo.perfectomobile.com";
        String user = "myUser";
        String perfectoPassword = "myPassword";
        String voiceRSSKey = "APIKey";
        capabilities.setCapability("user", user);
        capabilities.setCapability("password", perfectoPassword);

        //capabilities.setCapability("deviceName", "3133BB296C46FA2250362A227BA462A56ED11A45");
        capabilities.setCapability("deviceName", "1115FBD16FEF0303");

        // Use the automationName capability to define the required framework - Appium (this is the default) or PerfectoMobile.
        capabilities.setCapability("automationName", "PerfectoMobile");

        // Call this method if you want the script to share the devices with the Perfecto Lab plugin.
        setExecutionIdCapability(capabilities, host);

        RemoteWebDriver driver = new RemoteWebDriver(new URL("https://" + host + "/nexperience/perfectomobile/wd/hub"), capabilities);
        driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);

        try {
            // Instantiate a MobileTTS object 
        	MobileTTS mTTS = new MobileTTS(driver, perfectoPassword, voiceRSSKey, LANGUAGE.ENGLISH_US );
        	
        	// Launch the Google application
        	launchApp(driver, "com.google.android.googlequicksearchbox");
        	
        	//CLick the voice button
        	driver.findElement(By.xpath("//*[@resourceid='com.google.android.googlequicksearchbox:id/clear_or_voice_button']")).click();
        	
        	// Inject the voice command from the text
        	mTTS.injectAudioFromText("What's the answer to life, the universe and everything?");

        	

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                driver.close();

                // In case you want to down the report or the report attachments, do it here.
                // PerfectoLabUtils.downloadReport(driver, "pdf", "C:\\test\\report");
                // PerfectoLabUtils.downloadAttachment(driver, "video", "C:\\test\\report\\video", "flv");
                // PerfectoLabUtils.downloadAttachment(driver, "image", "C:\\test\\report\\images", "jpg");

            } catch (Exception e) {
                e.printStackTrace();
            }

            driver.quit();
        }

        System.out.println("Run ended");
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
    }
    private static void launchApp(RemoteWebDriver driver, String appName){
    	switchToContext(driver, "NATIVE_APP");
		Map<String, String> params = new HashMap<String, String>();
		params.put("name", appName);
		try{
			driver.executeScript("mobile:application:close", params);
		}
		catch(Exception e){
			
		}
		driver.executeScript("mobile:application:open", params);
		sleep(1000);
    }

    private static void switchToContext(RemoteWebDriver driver, String context) {
        RemoteExecuteMethod executeMethod = new RemoteExecuteMethod(driver);
        Map<String,String> params = new HashMap<String,String>();
        params.put("name", context);
        executeMethod.execute(DriverCommand.SWITCH_TO_CONTEXT, params);
    }

    private static String getCurrentContextHandle(RemoteWebDriver driver) {
        RemoteExecuteMethod executeMethod = new RemoteExecuteMethod(driver);
        String context =  (String) executeMethod.execute(DriverCommand.GET_CURRENT_CONTEXT_HANDLE, null);
        return context;
    }

    private static List<String> getContextHandles(RemoteWebDriver driver) {
        RemoteExecuteMethod executeMethod = new RemoteExecuteMethod(driver);
        List<String> contexts =  (List<String>) executeMethod.execute(DriverCommand.GET_CONTEXT_HANDLES, null);
        return contexts;
    }

    private static void setExecutionIdCapability(DesiredCapabilities capabilities, String host) throws IOException {
        EclipseConnector connector = new EclipseConnector();
        String eclipseHost = connector.getHost();
        if ((eclipseHost == null) || (eclipseHost.equalsIgnoreCase(host))) {
            String executionId = connector.getExecutionId();
            capabilities.setCapability(EclipseConnector.ECLIPSE_EXECUTION_ID, executionId);
        }
    }
}
