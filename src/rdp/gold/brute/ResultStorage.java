package rdp.gold.brute;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class ResultStorage {
    private static final Logger logger = Logger.getLogger(ResultStorage.class);
    private static final Queue<String> validQueue = new java.util.concurrent.ConcurrentLinkedQueue();
    private static final Queue<String> invalidQueue = new java.util.concurrent.ConcurrentLinkedQueue();
    private static final Queue<String> notSupportedIpQueue = new java.util.concurrent.ConcurrentLinkedQueue();
    private static File fileValid;
    private static File fileLog;
    private static FileOutputStream fileOutputStreamValid;
    private static FileOutputStream fileOutputStreamLog;
    private static AtomicInteger counterValid = new AtomicInteger();
    private static AtomicInteger counterInvalid = new AtomicInteger();
    private static java.util.List<Class> allowedContextLog = new java.util.ArrayList();

    static {
        if (Config.IS_WRITE_RESULT_TO_FILE) {
            try {
                fileValid = new File(Config.WRITE_RESULT_TO_FILE);
                if (!fileValid.exists()) {
                    fileValid.createNewFile();
                }

                fileOutputStreamValid = new FileOutputStream(fileValid);
            } catch (FileNotFoundException e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new java.io.PrintWriter(sw));
                logger.error(e + " " + sw);
                System.err.println(e.getMessage());
                e.printStackTrace();

                System.exit(1);
            } catch (IOException e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new java.io.PrintWriter(sw));
                logger.error(e + " " + sw);
                System.err.println(e.getMessage());
                e.printStackTrace();

                System.exit(1);
            }
        }

        if (Config.IS_ENABLE_DEBUG.booleanValue()) {
            try {
                fileLog = new File(Config.LOG_PATH);
                if (!fileLog.exists()) {
                    fileLog.createNewFile();
                }

                fileOutputStreamLog = new FileOutputStream(fileLog);
            } catch (FileNotFoundException e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new java.io.PrintWriter(sw));
                logger.error(e + " " + sw);
                System.err.println(e.getMessage());
                e.printStackTrace();

                System.exit(1);
            } catch (IOException e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new java.io.PrintWriter(sw));
                logger.error(e + " " + sw);
                System.err.println(e.getMessage());
                e.printStackTrace();

                System.exit(1);
            }
        }
    }

    public static Queue<String> getValidQueue() {
        return validQueue;
    }

    public static Queue<String> getInvalidQueue() {
        return invalidQueue;
    }

    public static Queue<String> getNotSupportedIpQueue() {
        return notSupportedIpQueue;
    }

    public static void saveSuccess(String host, String login, String password) {
        StringBuilder resultStringBuilder = new StringBuilder();

        resultStringBuilder.append(host);
        resultStringBuilder.append(";");
        resultStringBuilder.append(login);
        resultStringBuilder.append(";");
        resultStringBuilder.append(password);

        validQueue.add(resultStringBuilder.toString());
        counterValid.incrementAndGet();

        if (Config.IS_WRITE_RESULT_TO_FILE) {
            synchronized (counterValid) {
                writeSuccess((resultStringBuilder.toString() + "\r\n").getBytes());
            }
        }
    }

    public static void saveSuccess(String ip) {
        validQueue.add(ip);
        counterValid.incrementAndGet();

        if (Config.IS_WRITE_RESULT_TO_FILE) {
            synchronized (validQueue) {
                writeSuccess((ip + "\r\n").getBytes());
            }
        }
    }

    public static void saveInvalid(String ip) {
        invalidQueue.add(ip);
        counterInvalid.incrementAndGet();
    }

    public static void saveNotSupportedIp(String ip) {
        notSupportedIpQueue.add(ip);
    }

    public static synchronized void saveLog(String log) {
        saveLog(log, null, null);
    }

    public static synchronized void saveLog(String log, Class context) {
        saveLog(log, context, null);
    }

    public static synchronized void saveLogAny(String log, Class context) {
        saveLogAny(log, context, null);
    }

    public static synchronized void saveLog(String log, Class context, String ip) {
        if (Config.IS_ENABLE_DEBUG.booleanValue()) {
            try {
                if (allowedContextLog.contains(context)) {
                    fileOutputStreamLog.write(("[" + String.format("%tc", new Object[] { new java.util.Date() }) + "]\t[" + context.getName() + "]\t[" + ip + "]\t" + log + "\r\n\r\n\r\n").getBytes());
                }
            } catch (IOException e) {
                logger.error("Failed to write log info", e);
            }
        }
    }

    public static synchronized void saveLogAny(String log, Class context, String ip) {
        if (Config.IS_ENABLE_DEBUG.booleanValue()) {
            try {
                fileOutputStreamLog.write(("[" + String.format("%tc", new Object[] { new java.util.Date() }) + "]\t[" + context.getName() + "]\t[" + ip + "]\t" + log + "\r\n").getBytes());
            } catch (IOException e) {
                logger.error("Failed to write log info", e);
            }
        }
    }

    private static void writeSuccess(byte[] bytes) {
        try {
            fileOutputStreamValid.write(bytes);
        } catch (IOException e) {
            reinitFileOutputStreamValid();

            logger.error("Failed to write valid info", e);
        }
    }

    private static void reinitFileOutputStreamValid() {
        try {
            synchronized (fileOutputStreamValid) {
                fileOutputStreamValid.close();
            }
        } catch (IOException e) {
            logger.error("Failed close fileOutputStreamValid", e);
        }
        try {
            synchronized (fileOutputStreamValid) {
                fileOutputStreamValid = new FileOutputStream(fileValid, true);
            }
        } catch (IOException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new java.io.PrintWriter(sw));
            logger.error(e + " " + sw);
            System.err.println(e.getMessage());
            e.printStackTrace();

            System.exit(1);
        }
    }

    @SuppressWarnings("unused")
    private static void reinitFileOutputStreamLog() {
        try {
            synchronized (fileOutputStreamLog) {
                fileOutputStreamLog.close();
            }
        } catch (IOException e) {
            logger.error("Failed close fileOutputStreamLog", e);
        }
        try {
            synchronized (fileOutputStreamLog) {
                fileOutputStreamLog = new FileOutputStream(fileLog, true);
            }
        } catch (IOException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new java.io.PrintWriter(sw));
            logger.error(e + " " + sw);
            System.err.println(e.getMessage());
            e.printStackTrace();

            System.exit(1);
        }
    }
}
