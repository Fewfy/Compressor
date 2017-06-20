
public class YCrCbConverter {
	private double[][] RGBConversionMatrix =	{{0.299,0.587,0.114},
			 									{-0.169,-0.331,0.500},
			 									{0.500,-0.419,-0.081}};
//	将4:4:4格式的RGB图像，转换成4:2:0的YCbCr格式
	public static int[][][] convertFromRGB(int [][] R, int [][] G, int[][]B){
		int width = R[0].length;
		int heigth = R.length;
		int Y = 0, Cb = 1, Cr = 2;
		int [][][] convertedImage = new int[3][heigth][width];
	
		for(int i = 0;i < heigth;i++){
			for(int j = 0;j < width;j ++){
				convertedImage[Y][i][j] = (int)(0.257 * R[i][j] + 0.564 * G[i][j] + 0.098 * B[i][j] + 16);
				convertedImage[Cb][i][j] = (int)(-0.148*R[i][j] - 0.291 * G[i][j] + 0.439 * B[i][j] + 128);
				convertedImage[Cr][i][j] = (int)(0.439 * R[i][j] - 0.368 * G[i][j] - 0.071*B[i][j] + 128);
				if(convertedImage[Y][i][j] < 0)
					convertedImage[Y][i][j] = -convertedImage[Y][i][j];
				if(convertedImage[Cb][i][j] < 0)
					convertedImage[Cb][i][j] = -convertedImage[Cb][i][j];
				if(convertedImage[Cr][i][j] < 0)
					convertedImage[Cr][i][j] = -convertedImage[Cr][i][j];
			}
		}
		return convertedImage;
	}
}
