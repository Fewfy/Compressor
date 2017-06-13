import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;

public class Compressor {
	private File file;
	
	
	public Compressor(){
		
	}
	
	public void setFile(File f){
		this.file = f;
	}
	
	public void startCompress() throws Exception{
		InputStream is = new FileInputStream(this.file);
		BufferedImage buffer = ImageIO.read(is);
		int height = buffer.getHeight();
		int width = buffer.getWidth();
		long length = this.file.length();
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
		byte[]bytes = new byte[(int)length];
		
		short[][] red = new short[height][width];
		short[][] green = new short[height][width];
		short[][] blue = new short[height][width];
		
		int offset = 0;
		int numread = 0;
//		∂¡»°Õº∆¨
		while(numread < bytes.length && (numread = is.read(bytes, offset, bytes.length-offset)) >= 0){
			offset += numread;
		}
		
		int ind = 0;
		for(int i = 0;i < height;i ++){
			for(int j = 0;j < width;j++){
				red[i][j] = (short)(bytes[ind] & 0xff);
				green[i][j] = (short)(bytes[ind + height *width] & 0xff);
				blue[i][j] = (short)(bytes[ind + height * width * 2] & 0xff);
				int pixel = 0xff000000 | (red[i][j] & 0xff) << 16 | (green[i][j] & 0xff) << 8 | (blue[i][j] & 0xff); 
				image.setRGB(j, i, pixel);
			}
		}
	}
	
	public static void main(String []args){
		Compressor pressor = new Compressor();
		File filename = new File(args[1]);
		pressor.setFile(filename);
		try {
			pressor.startCompress();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
