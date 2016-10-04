package common.controller.restapi.client.admin;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import common.controller.restapi.client.AControllerRestServiceClient;
import common.controller.ui.modal.ControllerInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Singleton
public class ServerLogClient extends AControllerRestServiceClient {
    public static final int MAX_LENGTH = 100;
    private final String zipLogFilePath = "build/test-results/serverlog.zip";
    private final String buildTestResultLocation = "build/test-results/ServerLog";
    private final ControllerInfo controllerInfo;
    private final String rootPassWord;

    @Inject
    ServerLogClient(ControllerInfo controllerInfo, @Named("rootPassword") String rootPassWord) {
        super(controllerInfo);
        this.controllerInfo = controllerInfo;
        this.rootPassWord = rootPassWord;
    }

    public String giveMeRelatedLog(long time, int durationInMin) throws IOException {
        String file = collectControllerLog("/FileUploadServlet?action=CONTROLLER_LOGS");
        Map<Long, List<String>> logMap = readLogToMap(file);
        return "Controller's Server.log during this moment:" + System.lineSeparator()
                + getRelatedLog(logMap, time, durationInMin);
    }

    @Override
    public String getServiceUrl() {
        return "";
    }

    private String collectControllerLog(String urlPath) throws IOException {
        download_zip_file(controllerInfo.getControllerUrl() + urlPath);
        unZipIt(zipLogFilePath, buildTestResultLocation);
        return buildTestResultLocation + "/server.log";
    }

    private void download_zip_file(String logDownloadurl) throws IOException {
        URL url = new URL(logDownloadurl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("charset", "utf-8");
        connection.setRequestProperty("Cookie", getSessionCookie("root@system", rootPassWord));
        InputStream in = connection.getInputStream();
        FileOutputStream out = new FileOutputStream(zipLogFilePath);
        copy(in, out, 1024);
        out.close();
    }

    private static void copy(InputStream input, OutputStream output, int bufferSize) throws IOException {
        byte[] buf = new byte[bufferSize];
        int n = input.read(buf);
        while (n >= 0) {
            output.write(buf, 0, n);
            n = input.read(buf);
        }
        output.flush();
    }

    private void unZipIt(String zipFile, String outputFolder) {
        byte[] buffer = new byte[1024];

        try {

            //create output directory is not exists
            File folder = new File(outputFolder);
            if (!folder.exists()) {
                folder.mkdir();
            }

            //get the zip file content
            ZipInputStream zis =
                    new ZipInputStream(new FileInputStream(zipFile));
            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();

            while (ze != null) {

                String fileName = ze.getName();
                File newFile = new File(outputFolder + File.separator + fileName);

                // System.out.println("file unzip : " + newFile.getAbsoluteFile());

                //create all non exists folders
                //else you will hit FileNotFoundException for compressed folder
                new File(newFile.getParent()).mkdirs();

                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }

                fos.close();
                ze = zis.getNextEntry();
            }

            zis.closeEntry();
            zis.close();

            //  System.out.println("Done");

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private String getRelatedLog(Map<Long, List<String>> map, long time, int durationInMin) {
        long delta = durationInMin * 60 * 1000L;
        Iterator<Map.Entry<Long, List<String>>> entries = map.entrySet().iterator();
        StringBuilder sb = new StringBuilder();
        while (entries.hasNext()) {
            Map.Entry<Long, List<String>> entry = entries.next();
            if (entry.getKey() >= time - delta && entry.getKey() <= time) {
                for (String line : entry.getValue()) {
                    sb.append(breakDownLongStringToNewline(line)).append(System.lineSeparator());
                }
            }
        }
        return sb.toString();
    }

    private Map<Long, List<String>> readLogToMap(String fileName) {
        Map<Long, List<String>> map = new LinkedHashMap<Long, List<String>>();
        try {
            String line;
            // FileReader reads text files in the default encoding.
            FileReader fileReader = new FileReader(fileName);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            StringBuilder lineBuilder = new StringBuilder();

            Long lTime = null;
            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();
                lineBuilder.append(line);
                if (!line.isEmpty()) {
                    if (line.indexOf("[#|") == 0) {
                        if (line.indexOf("|", 4) > 4) {
                            try {
                                lTime = parseDateStringToTimeStamp(line.substring(3, line.indexOf("|", 4)));
                                if (!map.containsKey(lTime)) {
                                    map.put(lTime, new ArrayList<String>());
                                }
                            } catch (ParseException pe) {
                                //   pe.printStackTrace();
                            }
                        }
                    }
                    if (line.indexOf("|#]") == line.length() - 3) {
                        if (map.containsKey(lTime)) {
                            map.get(lTime).add(lineBuilder.toString());
                        }
                        lineBuilder.setLength(0);
                    }
                } else {
                    lineBuilder.append(System.lineSeparator());
                }

            }

            // Always close files.
            bufferedReader.close();
        } catch (FileNotFoundException ex) {
            //    System.out.println( "Unable to open file '" + fileName + "'");
        } catch (IOException ex) {
            //   System.out.println( "Error reading file '" + fileName + "'");
        }

        return map;
    }

    private Long parseDateStringToTimeStamp(String dateInString) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        Date date = formatter.parse(dateInString);
        return date.getTime();
    }

    private String breakDownLongStringToNewline(String s) {

        if (s.length() <= MAX_LENGTH + 3) {
            return s;
        } else {
            return s.substring(0, MAX_LENGTH) + System.lineSeparator()
                    + breakDownLongStringToNewline(s.substring(MAX_LENGTH));
        }
    }
}
