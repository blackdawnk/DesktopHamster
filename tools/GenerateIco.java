import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

/**
 * Standalone tool to generate hamster.ico for jpackage.
 * Run: javac tools/GenerateIco.java && java -cp tools GenerateIco output.ico
 */
public class GenerateIco {

    public static void main(String[] args) throws Exception {
        String output = args.length > 0 ? args[0] : "hamster.ico";

        int[] sizes = {16, 32, 48, 256};
        byte[][] pngDatas = new byte[sizes.length][];

        for (int i = 0; i < sizes.length; i++) {
            BufferedImage img = createHamsterIcon(sizes[i]);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "png", baos);
            pngDatas[i] = baos.toByteArray();
        }

        writeIco(output, sizes, pngDatas);
        System.out.println("Generated: " + output);
    }

    static BufferedImage createHamsterIcon(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        double scale = size / 16.0;
        g2.scale(scale, scale);

        // Body
        g2.setColor(new Color(230, 180, 120));
        g2.fillOval(1, 3, 14, 12);

        // Belly
        g2.setColor(new Color(255, 235, 205));
        g2.fillOval(4, 6, 8, 7);

        // Ears
        g2.setColor(new Color(230, 180, 120));
        g2.fillOval(2, 0, 5, 5);
        g2.fillOval(9, 0, 5, 5);

        // Ear inner
        g2.setColor(new Color(240, 160, 160));
        g2.fillOval(3, 1, 3, 3);
        g2.fillOval(10, 1, 3, 3);

        // Eyes
        g2.setColor(new Color(30, 30, 30));
        g2.fillOval(4, 6, 2, 2);
        g2.fillOval(10, 6, 2, 2);

        // Eye highlights
        g2.setColor(Color.WHITE);
        g2.fillRect(5, 6, 1, 1);
        g2.fillRect(11, 6, 1, 1);

        // Nose
        g2.setColor(new Color(200, 120, 120));
        g2.fillOval(7, 8, 2, 2);

        // Cheeks
        g2.setColor(new Color(245, 180, 170, 120));
        g2.fillOval(2, 8, 3, 2);
        g2.fillOval(11, 8, 3, 2);

        g2.dispose();
        return img;
    }

    static void writeIco(String path, int[] sizes, byte[][] pngDatas) throws IOException {
        try (DataOutputStream dos = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(path)))) {

            int count = sizes.length;

            // ICO header (6 bytes)
            writeLE16(dos, 0);     // reserved
            writeLE16(dos, 1);     // type: 1 = icon
            writeLE16(dos, count); // image count

            // Calculate offsets: header(6) + directory(count*16) = start of data
            int dataOffset = 6 + count * 16;
            int[] offsets = new int[count];
            for (int i = 0; i < count; i++) {
                offsets[i] = dataOffset;
                dataOffset += pngDatas[i].length;
            }

            // Directory entries (16 bytes each)
            for (int i = 0; i < count; i++) {
                int w = sizes[i] >= 256 ? 0 : sizes[i]; // 0 means 256
                int h = sizes[i] >= 256 ? 0 : sizes[i];
                dos.writeByte(w);              // width
                dos.writeByte(h);              // height
                dos.writeByte(0);              // color palette
                dos.writeByte(0);              // reserved
                writeLE16(dos, 1);             // color planes
                writeLE16(dos, 32);            // bits per pixel
                writeLE32(dos, pngDatas[i].length); // image size
                writeLE32(dos, offsets[i]);     // offset
            }

            // Image data
            for (byte[] data : pngDatas) {
                dos.write(data);
            }
        }
    }

    static void writeLE16(DataOutputStream dos, int value) throws IOException {
        dos.writeByte(value & 0xFF);
        dos.writeByte((value >> 8) & 0xFF);
    }

    static void writeLE32(DataOutputStream dos, int value) throws IOException {
        dos.writeByte(value & 0xFF);
        dos.writeByte((value >> 8) & 0xFF);
        dos.writeByte((value >> 16) & 0xFF);
        dos.writeByte((value >> 24) & 0xFF);
    }
}
