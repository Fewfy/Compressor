import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import com.sun.xml.internal.ws.org.objectweb.asm.Label;

import sun.tools.jar.resources.jar;

public class Compressor {
	
	double[] c = {(double)(1/Math.sqrt(2)),1,1,1,1,1,1,1};
	static JFrame frame = new JFrame();
	public static int latency = 0;
	
	public static int[] xACMapping = {0,1,0,0,1,2,3,2,
			1,0,0,1,2,3,4,5,
			4,3,2,1,0,0,1,2,
			3,4,5,6,7,6,5,4,
			3,2,1,0,1,2,3,4,
			5,6,7,7,6,5,4,3,
			2,3,4,5,6,7,7,6,
			5,4,5,6,7,7,6,7};

		public static int[] yACMapping = {0,0,1,2,1,0,0,1,
			2,3,4,3,2,1,0,0,
			1,2,3,4,5,6,5,4,
			3,2,1,0,0,1,2,3,
			4,5,6,7,7,6,5,4,
			3,2,1,2,3,4,5,6,
			7,7,6,5,4,3,4,5,
			6,7,7,6,5,6,7,7};

		public static int[] regXMapping = {0,1,2,3,4,5,6,7,
			0,1,2,3,4,5,6,7,
			0,1,2,3,4,5,6,7,
			0,1,2,3,4,5,6,7,
			0,1,2,3,4,5,6,7,
			0,1,2,3,4,5,6,7,
			0,1,2,3,4,5,6,7,
			0,1,2,3,4,5,6,7};

		public static int[] regYMapping = {0,0,0,0,0,0,0,0,
			1,1,1,1,1,1,1,1,
			2,2,2,2,2,2,2,2,
			3,3,3,3,3,3,3,3,
			4,4,4,4,4,4,4,4,
			5,5,5,5,5,5,5,5,
			6,6,6,6,6,6,6,6,
			7,7,7,7,7,7,7,7};

		public static int[] intMapMask = {0x80000000,0xC0000000,0xE0000000,0xF0000000,
			0xF8000000,0xFC000000,0xFE000000,0xFF000000,
			0xFF800000,0xFFC00000,0xFFE00000,0xFFF00000,
			0xFFF80000,0xFFFC0000,0xFFFE0000,0xFFFF0000,
			0xFFFF8000,0xFFFFC000,0xFFFFE000,0xFFFFF000,
			0xFFFFF800,0xFFFFFC00,0xFFFFFE00,0xFFFFFF00,
			0xFFFFFF80,0xFFFFFFC0,0xFFFFFFE0,0xFFFFFFF0,
			0xFFFFFFF8,0xFFFFFFFC,0xFFFFFFFE,0xFFFFFFFF};
	
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
		int temp = bytes.length;
//		读取图片
//		buffer.read(bytes);
//		while(numread < bytes.length && (numread = is.read(bytes, offset, bytes.length-offset)) >= 0){
//			offset += numread;
//		}
		
		int ind = 0;
		for(int i = 0;i < height;i ++){
			for(int j = 0;j < width;j++){
//				red[i][j] = (short)(bytes[ind] & 0xff);
//				green[i][j] = (short)(bytes[ind + height *width] & 0xff);
//				blue[i][j] = (short)(bytes[ind + height * width * 2] & 0xff);
//				int pixel = 0xff000000 | (red[i][j] & 0xff) << 16 | (green[i][j] & 0xff) << 8 | (blue[i][j] & 0xff); 
//				image.setRGB(j, i, pixel);
//				ind ++;	
				int pixel = buffer.getRGB(i, j);
				red[i][j] = (short)((pixel & 0xff0000) >> 16);
				green[i][j] = (short)((pixel & 0xff00) >> 8);
				blue[i][j] = (short)(pixel & 0xff);
			}
		}
		
		double [][][] reddctencode = encodeDCT(red, width, height);
		double [][][] greendctencode = encodeDCT(green, width, height);
		double [][][] bluedctencode = encodeDCT(blue, width, height);
		
		short [][][] reddecode = decodeDCT(reddctencode, width, height);
		short [][][] greendecode = decodeDCT(greendctencode, width, height);
		short [][][] bluedecode = decodeDCT(bluedctencode, width, height);
		
		reconstructImage(reddecode, greendecode, bluedecode, width, height);
	}
	
	public short[][][] decodeDCT(double[][][] encodedBlocks, int w, int h)
	{
		int totalNumBlocks = (int)(w*h/64);
		int nb=0;
		double[][] deqDCT = new double[8][8];
		short[][][] iDCTBlocks = new short[totalNumBlocks][8][8];

		for(nb=0;nb<totalNumBlocks; nb++)
		{
			deqDCT = dequantizeDCT(encodedBlocks[nb]);
			iDCTBlocks[nb] = doIDCT(deqDCT);//;
		}
		return iDCTBlocks;
	}

	
	public double[][] dequantizeDCT(double[][] dctBlock)
	{
		int i=0,j=0;
		double[][] dqDCTBlock = new double[8][8];
		int mapCount = 0;

		for(i=0;i<8;i++)
		{
			for(j=0;j<8;j++,mapCount++)
			{
				dqDCTBlock[i][j] = dctBlock[yACMapping[mapCount]][xACMapping[mapCount]]*Math.pow(2.0, 2.2);
			}
		}

		return dqDCTBlock; 
	}
	
	public short[][] doIDCT(double[][] dqDCTBlock)
	{
		int x=0,y=0,u=0,v=0;
		double cu=0.0, cv=0.0;
		short[][] iDCT = new short[8][8];
		double idct = 0.0;

		for(y=0; y<8; y++)
		{
			for(x=0;x<8;x++)
			{
				idct = 0.0;
				for(u=0;u<8;u++)
				{
					for(v=0;v<8;v++)
					{
						/*cu = 1;
						cv = 1;
						if(u==0)
						{
							cu = 1/(Math.sqrt(2));
						}
						if(v==0)
						{
							cv = 1/(Math.sqrt(2));
						}*/
						idct += (c[u]*c[v]*(dqDCTBlock[u][v])*(Math.cos(((2*x + 1)*v*Math.PI)/16))*(Math.cos(((2*y + 1)*u*Math.PI)/16)));
					}
				}
				if(Math.round((idct/4)) > 255)
					iDCT[y][x] = 255;
				else if(Math.round((idct/4)) < 0)
					iDCT[y][x] = 0;
				else	
					iDCT[y][x] = (short)Math.round((idct/4));
			}
		}

		return iDCT;
	}
	
	public void reconstructImage(short[][][] red, short [][][] green, short [][][] blue, int width, int height){
		int numBlock = (int)((width-8) * (height-8) / 64);
		int imgX = 0, imgY = 0;
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		JLabel label = new JLabel(new ImageIcon(image));
		frame.getContentPane().add(label,BorderLayout.EAST);
		frame.pack();
		frame.setVisible(true);
		int num = 0;
		int y = 0;
		int x = 0;
		try {
			for(num = 0;num < numBlock;num++){
				if(imgX == width){
					imgX = 0;
					imgY += 8;
				}
				if(imgY == height)
					break;
				for(y = 0;y < 8;y ++){
					for(x = 0;x < 8;x ++){
						int pixel = 0xff000000 | ((red[num][y][x] & 0xff) << 16) | ((green[num][y][x] & 0xff) << 8) | ((blue[num][y][x] & 0xff));
						if(imgX + x < width && imgY + y < height)
							image.setRGB(imgX + x, imgY + y, pixel);
						else 
							break;
					}
				}
				imgX += 8;
				
				try{
					Thread.currentThread();
					Thread.sleep(latency);
				}
				catch(InterruptedException ie){
				}
				label.setIcon(new ImageIcon(image));
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		int temp = 1;
	}
	
	public double[][][] encodeDCT(short[][] pixelArray,int width, int height){
		short[][] pixelBlock = new short[8][8];
		double[][][] quantizeValue = new double[(int)(width * height / 64)][8][8];
		double [][] dctRes = new double[8][8];
		int i  = 0;
		int j = 0;
		int y = 0;
		int x = 0;
		int num = 0;
//		分块,初步想法是对于余下的块反正也就8个像素点的宽度干脆忽略掉了
		try{
			for(i = 0;i < height - 8;i += 8){
				for(j = 0;j < width - 8;j += 8){
//					每个块进行DCT编码
					for(y = 0;y < 8;y ++){
						for(x = 0;x < 8;x++){
							pixelBlock[y][x] = pixelArray[y+i][x+j];
						}
					}
					dctRes = dct(pixelBlock);
//					for(y = 0;y < 8;y ++){
//						for(x = 0; x < 8;x ++){
//							System.out.print(dctRes[y][x] + ",");
//						}
//						System.out.println();
//					}
					quantizeValue[num++] = quantize(dctRes);
				}
			}
			
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		return quantizeValue;
	}
	
	public double [][] quantize(double [][] pixelBlock){
		double[][] quantizeRes = new double[8][8];
		
		int mapCount = 0;
		double qFactor = 2.2;
		
		for(int i = 0;i < 8;i ++){
			for(int j = 0;j < 8;j ++){
				quantizeRes[yACMapping[mapCount]][xACMapping[mapCount]] = Math.round(pixelBlock[i][j] / (Math.pow(2.0, qFactor)));
			}
		}
		
		return quantizeRes;
	}
	
	public double[][] dct(short[][] pixelBlock){
		double [][] dctRes = new double[8][8];
		for(int y = 0;y < 8;y++){
			for(int x = 0;x < 8;x++){
				for(int i = 0;i < 8;i ++){
					for(int j = 0;j < 8;j++){
//						F(u) = C(u)/2 sigma(cos((2i+1)u x pi / 16) f(i))
						dctRes[y][x] += (Math.cos((2 * i + 1) * y * Math.PI) / 16) * (Math.cos((2 * j + 1) * x * Math.PI / 16)) * pixelBlock[i][j];
					}
				}
				dctRes[y][x] *= (c[y] / 2) * (c[x] /2 );
			}
		}
		return dctRes;
	}
	
	public static void main(String []args){

		Encoder encoder = new Encoder("img/logo.jpg", "img/res.fy");
		encoder.execute();
		Decoder decoder = new Decoder("img/res.fy", "img/after");
		decoder.execute();
	}
}
