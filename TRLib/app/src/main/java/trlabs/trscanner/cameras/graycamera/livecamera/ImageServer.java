package trlabs.trscanner.cameras.graycamera.livecamera;
/**
 * Created by intern2 on 18/11/2014.
 */
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfInt;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;

import java.io.ByteArrayInputStream;

import trlabs.trscanner.cameras.graycamera.livecamera.lib.NanoHTTPD;
import trlabs.trscanner.cameras.graycamera.livecamera.lib.NanoHTTPD.Response.Status;
import trlabs.trscanner.utils.FileTools;

import trlabs.trscanner.utils.FileTools;

public class ImageServer extends NanoHTTPD {
        Mat image;
        String text;
        int jpeg_quality = 80;

        public ImageServer(int port) {
            super(port);
            image = new Mat();
        }

        public int getJpegQuality() {
            return jpeg_quality;
        }

        public void setJpegQuality(int val) {
            if (val < 1) val = 1;
            if (val > 100) val = 100;
            this.jpeg_quality = val;
        }

        @Override
        public Response serve(IHTTPSession session) {
            String uri = session.getUri();
            String[] paths = uri.split("\\?");

            if (paths != null && "/log.txt".equals(paths[0])) {
                return serveCameraImage(session);
                //return serveLogFile( session);
            }

            // index.html
            String html = "<html><head><script>function reload(){document.camaera_jpg.src='camera.jpg?t='+new Date().getTime();}function init() {setInterval(reload, 300);}</script></head><body onLoad='init()'><img name='camaera_jpg' src='camera.jpg'/></body><html>";
            //String html = "<html><head><title>Log file viewer</title></head><body> log.txt </body></html>";
            return new NanoHTTPD.Response(html);
        }

        private Response serveLogFile(IHTTPSession session) {
            String time = FileTools.getTimeStamp();
            NanoHTTPD.Response res = new Response(Status.OK, "text/plain", time);
            return res;
        }


        private Response serveCameraImage(IHTTPSession session) {
            Mat image = getImage();
            if (image.empty()) {
                image.create(new Size(640, 480), CvType.CV_8UC3);
                image.setTo(new Scalar(255, 0, 0));
            }

            MatOfInt params = new MatOfInt(Highgui.IMWRITE_JPEG_QUALITY, jpeg_quality);
            MatOfByte mat_of_buf = new MatOfByte();
            Highgui.imencode(".jpg", image, mat_of_buf, params);
            byte[] byteArray = mat_of_buf.toArray();
            ByteArrayInputStream bis = new ByteArrayInputStream(byteArray);

            NanoHTTPD.Response res = new NanoHTTPD.Response(Status.OK, "image/jpeg", bis);
            res.addHeader("X-Content-Type-Options", "nosniff");
            res.addHeader("Access-Control-Allow-Origin", "*");


            return res;

        }

        public synchronized void setImage(Mat image) {
            image.copyTo(this.image);
        }
        public synchronized void setText(String text) { this.text = text; }

        public synchronized Mat getImage() {
            Mat tmp = new Mat();
            image.copyTo(tmp);
            return tmp;
        }


        public synchronized String getText() {
            return this.text;
        }


        public String getIpAddress(Context context) {

            WifiManager wifi_manager = (WifiManager) context.getSystemService(
                    Context.WIFI_SERVICE);
            WifiInfo info = wifi_manager.getConnectionInfo();

            int ip = info.getIpAddress();
            if (ip == 0) {
                return "ERROR: please check wifi connection...";
            }
            // parse ip
            String ip_str = "" + ((ip >> 0) & 0xFF) + "." + ((ip >> 8) & 0xFF)
                    + "." + ((ip >> 16) & 0xFF) + "." + ((ip >> 24) & 0xFF);

            return ip_str;
        }
    }



