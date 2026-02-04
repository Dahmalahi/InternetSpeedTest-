import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.io.*;
import java.io.*;
import java.util.*;
import javax.microedition.io.file.*;

public class SpeedTestMIDlet extends MIDlet implements CommandListener {
    private Display display;
    private SpeedCanvas canvas;
    private Command startCommand, exitCommand, aboutCommand, consoleCommand;
    private Vector log = new Vector();

    public void startApp() {
        display = Display.getDisplay(this);
        canvas = new SpeedCanvas(this);
        startCommand = new Command("Start", Command.OK, 1);
        exitCommand = new Command("Exit", Command.EXIT, 1);
        aboutCommand = new Command("About", Command.HELP, 2);
        consoleCommand = new Command("Console", Command.SCREEN, 3);
        canvas.addCommand(startCommand);
        canvas.addCommand(aboutCommand);
        canvas.addCommand(consoleCommand);
        canvas.addCommand(exitCommand);
        canvas.setCommandListener(this);
        display.setCurrent(canvas);
    }

    public void pauseApp() {}

    public void destroyApp(boolean unconditional) {}

    public void commandAction(Command c, Displayable d) {
        if (c == exitCommand) {
            notifyDestroyed();
        } else if (c == startCommand) {
            log.removeAllElements();
            new Thread(new Runnable() {
                public void run() {
                    canvas.performSpeedTest();
                }
            }).start();
        } else if (c == aboutCommand) {
            showAbout();
        } else if (c == consoleCommand) {
            showConsole();
        }
    }

    private void showAbout() {
        Alert about = new Alert("About");
        about.setString("Creator: David\nVersion: 1.0\nDeveloped with help from Grok by xAI\n\nProcess:\n1. Connects to multiple HTTP servers.\n2. Downloads test files in parallel threads.\n3. Measures bytes downloaded and time taken per thread.\n4. Calculates individual speeds, min, max, average, and overall speed.\n5. Saves results to Result.txt in accessible storage.");
        about.setTimeout(Alert.FOREVER);
        display.setCurrent(about, canvas);
    }

    private void showConsole() {
        List consoleList = new List("Console", List.IMPLICIT);
        for (int i = 0; i < log.size(); i++) {
            consoleList.append((String) log.elementAt(i), null);
        }
        consoleList.addCommand(new Command("Back", Command.BACK, 1));
        consoleList.setCommandListener(new CommandListener() {
            public void commandAction(Command c, Displayable d) {
                display.setCurrent(canvas);
            }
        });
        display.setCurrent(consoleList);
    }

    public void addLog(String message) {
        log.addElement(new Date().toString() + ": " + message);
    }
}

class SpeedCanvas extends Canvas implements CommandListener {
    private SpeedTestMIDlet midlet;
    private boolean testing = false;
    private String result = "";
    private boolean success = false;
    private Timer timer;

    public SpeedCanvas(SpeedTestMIDlet midlet) {
        this.midlet = midlet;
        timer = new Timer();
        timer.schedule(new RepaintTask(), 0, 500); // For animation if needed
    }

    protected void paint(Graphics g) {
        int w = getWidth();
        int h = getHeight();
        g.setColor(0xFFFFFF);
        g.fillRect(0, 0, w, h);
        g.setColor(0x000000);

        if (testing) {
            drawStringCenter(g, "Testing...", w / 2, h / 2 - 10);
            // Simple progress animation
            g.fillRect(10, h - 20, (int) (System.currentTimeMillis() % (w - 20)), 10);
        } else if (!result.equals("")) {
            String[] lines = splitString(result, "\n");
            int y = 10;
            for (int i = 0; i < lines.length; i++) {
                g.drawString(lines[i], 10, y, Graphics.TOP | Graphics.LEFT);
                y += g.getFont().getHeight();
            }
            // Draw pixel art
            drawPixelArt(g, success ? getSuccessArt() : getFailureArt(), w - 40, h - 40, 4); // Scale 4
        } else {
            drawStringCenter(g, "Speed Test", w / 2, 20);
            drawStringCenter(g, "Press Start to test", w / 2, h / 2);
        }
    }

    private void drawStringCenter(Graphics g, String str, int x, int y) {
        int sw = g.getFont().stringWidth(str);
        g.drawString(str, x - sw / 2, y, Graphics.TOP | Graphics.LEFT);
    }

    private String[] splitString(String str, String delim) {
        Vector v = new Vector();
        int pos = 0;
        while (pos < str.length()) {
            int end = str.indexOf(delim, pos);
            if (end == -1) end = str.length();
            v.addElement(str.substring(pos, end));
            pos = end + delim.length();
        }
        String[] arr = new String[v.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (String) v.elementAt(i);
        }
        return arr;
    }

    public void performSpeedTest() {
        testing = true;
        repaint();
        midlet.addLog("Starting speed test");

        String[] downloadUrls = {
            "http://speedtest.tele2.net/1MB.zip",
            "http://proof.ovh.net/files/1Mb.dat",
            "http://ipv4.download.thinkbroadband.com/1MB.zip"
        };

        long overallStart = System.currentTimeMillis();
        long totalBytes = 0;
        Vector threadSpeeds = new Vector(); // Long kbps

        DownloadThread[] threads = new DownloadThread[downloadUrls.length];
        for (int i = 0; i < downloadUrls.length; i++) {
            threads[i] = new DownloadThread(downloadUrls[i], midlet);
            threads[i].start();
        }

        for (int i = 0; i < threads.length; i++) {
            try {
                threads[i].join();
                long bytes = threads[i].getBytesDownloaded();
                totalBytes += bytes;
                long speedKbps = threads[i].getSpeedKbps();
                if (speedKbps > 0) {
                    threadSpeeds.addElement(new Long(speedKbps));
                }
            } catch (InterruptedException e) {}
        }

        long overallEnd = System.currentTimeMillis();
        long overallDurationMs = overallEnd - overallStart;
        if (overallDurationMs <= 0) overallDurationMs = 1;

        long overallBits = totalBytes * 8L;
        long overallBps = (overallBits * 1000L) / overallDurationMs;
        long overallKbps = overallBps / 1024L;

        long minKbps = Long.MAX_VALUE;
        long maxKbps = 0;
        long sumKbps = 0;
        int count = threadSpeeds.size();
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                long s = ((Long) threadSpeeds.elementAt(i)).longValue();
                sumKbps += s;
                if (s < minKbps) minKbps = s;
                if (s > maxKbps) maxKbps = s;
            }
        } else {
            minKbps = 0;
            maxKbps = 0;
        }
        long avgKbps = count > 0 ? sumKbps / count : 0;

        String overallSpeedStr = formatSpeed(overallKbps);
        String minStr = formatSpeed(minKbps);
        String maxStr = formatSpeed(maxKbps);
        String avgStr = formatSpeed(avgKbps);

        String res = "Overall Speed: " + overallSpeedStr + "\n";
        res += "Min Speed: " + minStr + "\n";
        res += "Max Speed: " + maxStr + "\n";
        res += "Avg Speed: " + avgStr + "\n";
        res += "Time: " + new Date().toString() + "\n";
        res += "RAM Total: " + Runtime.getRuntime().totalMemory() / 1024 + " KB\n";
        res += "RAM Free: " + Runtime.getRuntime().freeMemory() / 1024 + " KB\n";
        res += "Network: Unknown (2G/GSM assumed)\n";

        success = totalBytes > 0;
        result = res;
        saveResultToFile(res);
        midlet.addLog("Test completed: " + (success ? "Success" : "Failure"));

        testing = false;
        repaint();
    }

    private String formatSpeed(long kbps) {
        if (kbps >= 1024) {
            long mbpsTenths = (kbps * 10L) / 1024L;
            long whole = mbpsTenths / 10;
            long frac = mbpsTenths % 10;
            return whole + "." + frac + " Mbps";
        } else {
            return kbps + " kbps";
        }
    }

    private void saveResultToFile(String res) {
        String[] possibleRoots = {
            "file:///TFCard/",
            "file:///c:/",
            "file:///e:/",
            "file:///PhoneMemory/",
            "file:///sdcard/"
        };
        boolean saved = false;
        for (int i = 0; i < possibleRoots.length && !saved; i++) {
            try {
                String fileUrl = possibleRoots[i] + "Result.txt";
                FileConnection fc = (FileConnection) Connector.open(fileUrl);
                if (!fc.exists()) {
                    fc.create();
                }
                OutputStream os = fc.openOutputStream();
                os.write(res.getBytes());
                os.close();
                fc.close();
                midlet.addLog("Saved to " + fileUrl);
                saved = true;
            } catch (Exception e) {
                midlet.addLog("Failed to save to " + possibleRoots[i] + ": " + e.toString());
            }
        }
        if (!saved) {
            midlet.addLog("Failed to save result to any location.");
        }
    }

    // Pixel art: 8x8 grid for success (check mark)
    private boolean[][] getSuccessArt() {
        boolean[][] art = new boolean[8][8];
        // Simple check mark
        art[7][0] = true; art[6][0] = true;
        art[5][1] = true; art[4][1] = true;
        art[3][2] = true; art[2][2] = true;
        art[1][3] = true; art[0][3] = true;
        art[1][4] = true; art[0][4] = true;
        art[2][5] = true; art[3][5] = true;
        art[4][6] = true; art[5][6] = true;
        art[6][7] = true; art[7][7] = true;
        return art;
    }

    // Pixel art: 8x8 grid for failure (X mark)
    private boolean[][] getFailureArt() {
        boolean[][] art = new boolean[8][8];
        // Simple X
        art[0][0] = true; art[1][1] = true; art[0][7] = true; art[1][6] = true;
        art[7][0] = true; art[6][1] = true; art[7][7] = true; art[6][6] = true;
        art[2][2] = true; art[3][3] = true; art[4][4] = true; art[5][5] = true;
        art[2][5] = true; art[3][4] = true; art[4][3] = true; art[5][2] = true;
        return art;
    }

    private void drawPixelArt(Graphics g, boolean[][] art, int x, int y, int scale) {
        g.setColor(0x00FF00); // Green for success, but set per art if needed
        for (int i = 0; i < art.length; i++) {
            for (int j = 0; j < art[i].length; j++) {
                if (art[i][j]) {
                    g.fillRect(x + j * scale, y + i * scale, scale, scale);
                }
            }
        }
    }

    public void commandAction(Command c, Displayable d) {} // Not used

    class RepaintTask extends TimerTask {
        public void run() {
            if (testing) {
                repaint();
            }
        }
    }
}

class DownloadThread extends Thread {
    private String url;
    private long bytesDownloaded = 0;
    private long speedKbps = 0;
    private SpeedTestMIDlet midlet;

    public DownloadThread(String url, SpeedTestMIDlet midlet) {
        this.url = url;
        this.midlet = midlet;
    }

    public void run() {
        long start = System.currentTimeMillis();
        try {
            midlet.addLog("Connecting to " + url);
            HttpConnection conn = (HttpConnection) Connector.open(url);
            conn.setRequestMethod(HttpConnection.GET);
            int code = conn.getResponseCode();
            midlet.addLog("Response code for " + url + ": " + code);
            if (code == HttpConnection.HTTP_OK) {
                InputStream is = conn.openInputStream();
                byte[] buffer = new byte[4096];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    bytesDownloaded += len;
                }
                is.close();
            }
            conn.close();
        } catch (Exception e) {
            midlet.addLog("Error for " + url + ": " + e.toString());
        }
        long end = System.currentTimeMillis();
        long durationMs = end - start;
        if (durationMs > 0) {
            long bits = bytesDownloaded * 8L;
            long bps = (bits * 1000L) / durationMs;
            speedKbps = bps / 1024L;
        }
        midlet.addLog("Downloaded from " + url + ": " + bytesDownloaded + " bytes, speed: " + speedKbps + " kbps");
    }

    public long getBytesDownloaded() {
        return bytesDownloaded;
    }

    public long getSpeedKbps() {
        return speedKbps;
    }
}