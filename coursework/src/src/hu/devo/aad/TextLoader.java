package hu.devo.aad;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

/**
 * Created by Barnabas on 30/12/2015.
 */
public class TextLoader {

    /**
     * Loads the first {@link Settings#KBS_TO_LOAD} KBs of the file specified in {@link
     * Settings#FILE_TO_LOAD} into a string. The file should use UTF-8 encoding.
     *
     * @return the file's contents.
     */
    static String load() {
        try {
            //get an input channel to the data
            File f = new File(Settings.DATA_PATH + Settings.FILE_TO_LOAD);
            FileInputStream fis = new FileInputStream(f);
            FileChannel fc = fis.getChannel();

            //open a KB sized buffer
            ByteBuffer bb = ByteBuffer.allocateDirect(1024);

            StringBuilder sb = new StringBuilder();
            //fill up the buffer at most KBS_TO_LOAD times
            for (int i = 0; i < Settings.KBS_TO_LOAD && fc.read(bb) != -1; i++) {
                bb.flip();
                //append the buffers contents to the buffer
                sb.append(Charset.forName("UTF8").decode(bb));
                bb.clear();
            }

            //close the channel
            fc.close();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}
