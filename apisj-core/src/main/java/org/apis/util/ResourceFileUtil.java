package org.apis.util;

import org.apis.contract.ContractLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ResourceFileUtil {
    private static Logger logger = LoggerFactory.getLogger("util");

    public static String readFile(String fileName) {
        try (InputStream is = ContractLoader.class.getClassLoader().getResourceAsStream(fileName)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder out = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null) {
                out.append(line).append("\n");
            }

            return out.toString();
        } catch (Exception e) {
            logger.error("Problem loading resource file from [" + fileName + "]");
            //e.printStackTrace();

            return null;
        }
    }
}
