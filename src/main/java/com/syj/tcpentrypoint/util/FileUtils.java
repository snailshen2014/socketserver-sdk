package com.syj.tcpentrypoint.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;

import org.apache.log4j.spi.LoggerFactory;
import org.slf4j.Logger;

/**
 * Title: 文件操作工具类<br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class FileUtils {
    
    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);

    /**
     * 得到USER_HOME目录
     *
     * @param base
     *         用户目录下文件夹
     * @return 得到用户目录
     */
    public static String getUserHomeDir(String base) {
        String userhome = System.getProperty("user.home");
        File file = new File(userhome, base);
        if (file.exists()) {
            if (!file.isDirectory()) {
                LOGGER.error("{} exists, but not directory", file.getAbsolutePath());
                try {
					throw new Exception(file.getAbsolutePath() + " exists, but not directory");
				} catch (Exception e) {
					e.printStackTrace();
				}
            }
        } else {
            file.mkdirs(); // 可能创建不成功
        }
        return file.getAbsolutePath();
    }

    /**
     * 读取文件内容
     *
     * @param file
     *            文件
     * @return 文件内容
     */
    public static String file2String(File file) throws IOException {
        if (file == null || !file.exists() || !file.isFile() || !file.canRead()) {
            return null;
        }
        FileReader reader = null;
        StringWriter writer = null;
        try {
            reader = new FileReader(file);
            writer = new StringWriter();
            char[] cbuf = new char[1024];
            int len = 0;
            while ((len = reader.read(cbuf)) != -1) {
                writer.write(cbuf, 0, len);
            }
            return writer.toString();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
            if (writer != null) {
                try {
                    writer.close();

                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * 字符流写文件 较快
     *
     * @param file
     *            文件
     * @param data
     *            数据
     */
    public static boolean string2File(File file, String data) throws IOException {
        FileWriter writer = null;
        try {
            writer = new FileWriter(file, false);
            writer.write(data);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
        return true;
    }
}