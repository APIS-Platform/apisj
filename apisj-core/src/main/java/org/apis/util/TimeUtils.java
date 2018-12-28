package org.apis.util;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;

/**
 * @author Mikhail Kalinin
 * @since 10.08.2015
 */
public class TimeUtils {
    private static final Logger logger = LoggerFactory.getLogger("TimeUtils");
    /**
     * Converts minutes to millis
     *
     * @param minutes time in minutes
     * @return corresponding millis value
     */
    public static long minutesToMillis(long minutes) {
        return minutes * 60 * 1000;
    }

    /**
     * Converts seconds to millis
     *
     * @param seconds time in seconds
     * @return corresponding millis value
     */
    public static long secondsToMillis(long seconds) {
        return seconds * 1000;
    }

    /**
     * Converts millis to minutes
     *
     * @param millis time in millis
     * @return time in minutes
     */
    public static long millisToMinutes(long millis) {
        return Math.round(millis / 60.0 / 1000.0);
    }

    /**
     * Converts millis to seconds
     *
     * @param millis time in millis
     * @return time in seconds
     */
    public static long millisToSeconds(long millis) {
        return Math.round(millis / 1000.0);
    }

    /**
     * Returns timestamp in the future after some millis passed from now
     *
     * @param millis millis count
     * @return future timestamp
     */
    public static long timeAfterMillis(long millis) {
        return System.currentTimeMillis() + millis;
    }

    private static final String[] TIME_SERVERS = new String[] {
            "time.google.com"
    };

    /**
     * The difference between the time loaded from the NTP server and the current time
     */
    private static long timeDiff = 0;

    /** 마지막으로 서버와 시간을 동기화 한 시간 */
    private static long lastSyncedTime = 0;

    /** 1시간마다 시간을 업데이트할 수 있도록 한다. */
    private static final long PERIOD_TIME_SYNC = 60*60*1_000;

    /**
     * Get the current timestamp from the server and return it.
     *
     * Ref. [ https://stackoverflow.com/a/31131202 ]
     * @return unix timestamp (ms)
     */
    private static long getNtpTimestamp() {
        try {
            lastSyncedTime = System.currentTimeMillis();

            NTPUDPClient timeClient = new NTPUDPClient();
            timeClient.setDefaultTimeout(5000);

            String serverName = getTimeServerName();
            InetAddress inetAddress = InetAddress.getByName(serverName);
            TimeInfo timeInfo = timeClient.getTime(inetAddress);
            timeDiff = timeInfo.getMessage().getTransmitTimeStamp().getTime() - System.currentTimeMillis();

            long realTime = System.currentTimeMillis() + timeDiff;

            if(timeClient.isOpen()) {
                timeClient.close();
            }

            return realTime;
            //return timeInfo.getMessage().getTransmitTimeStamp().getTime();
        } catch (IOException e) {
            logger.error("Read NTP server time error", e);
            return System.currentTimeMillis() + timeDiff;
        }
    }

    /**
     * The actual time is returned by applying the time difference between the computer time and the NTP server.
     */
    public static long getRealTimestamp() {
        long realTime = System.currentTimeMillis() + timeDiff;

        if(System.currentTimeMillis() - lastSyncedTime > PERIOD_TIME_SYNC) {
            return getNtpTimestamp();
        }

        return realTime;
    }

    private static String getTimeServerName() {
        return TIME_SERVERS[(int) (System.currentTimeMillis() % TIME_SERVERS.length)];
    }
}
