package org.apis.util;

import org.apis.config.SystemProperties;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.charset.StandardCharsets;

public class CurrentStateUtil {

    private static String configDirPath = null;
    private static final String filenamePid = "/pid.info";
    private static final String filenameLatestBlock = "/latestBlock.info";
    private static final String filenameLatestMinedBlock = "/latestMinedBlock.info";

    private static void checkConfigDir() {
        if(configDirPath != null) {
            return;
        }

        final SystemProperties config = SystemProperties.getDefault();
        if(config == null) {
            System.out.println("Failed to load config");
            System.exit(0);
        }

        File configDir = new File(config.configDir());
        if(!configDir.exists()) {
            if(!configDir.mkdirs()) {
                ConsoleUtil.printlnRed("Failed to create configuration file.");
                System.exit(1);
            }
        }

        configDirPath = config.configDir();
    }

    /**
     * Find the PID value of the current program and save it as pid.info file in the config path.
     * @throws IOException If an error occurs while saving the file
     */
    public static void storePid() throws IOException {
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();

        String jvmName = runtimeBean.getName();
        //System.out.println("JVM Name = " + jvmName);
        String pid = jvmName.split("@")[0];
        //System.out.println("JVM PID  = " + pid);

        checkConfigDir();

        String pathPid = configDirPath + filenamePid;
        storeFile(pathPid, pid);
    }

    public static void storeLatestBlock(long blockNumber) throws IOException {
        checkConfigDir();

        String path = configDirPath + filenameLatestBlock;
        String content = TimeUtils.getRealTimestamp() + "\t" + blockNumber;
        storeFile(path, content);
    }

    public static void storeLatestMinedBlock(long blockNumber) throws IOException {
        checkConfigDir();

        String path = configDirPath + filenameLatestMinedBlock;
        String content = TimeUtils.getRealTimestamp() + "\t" + blockNumber;
        storeFile(path, content);
    }


    private static void storeFile(String path, String content) throws IOException {
        OutputStream output;
        output = new FileOutputStream(path, false);
        output.write(content.getBytes(StandardCharsets.UTF_8));
        output.flush();
        output.close();
    }


    /**
     * Remove the pid information when the program terminates.
     */
    public static void deleteCurrentInfo() {
        checkConfigDir();

        String pathPid = configDirPath + filenamePid;
        deleteFile(pathPid);

        String pathLatestBlock = configDirPath + filenameLatestBlock;
        deleteFile(pathLatestBlock);

        String pathLatestMinedBlock = configDirPath + filenameLatestMinedBlock;
        deleteFile(pathLatestMinedBlock);
    }

    private static void deleteFile(String path) {
        File file = new File(path);

        if(file.exists()) {
            file.delete();
        }
    }

}
