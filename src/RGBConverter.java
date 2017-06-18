
public class RGBConverter {
	private double[][] YCBCRConversionMatrix =	{{1.0,0.000,1.400  },
												{1.000,-0.343,-0.711},
			 									{1.00,1.765,0.000}};

	
	
	public int[][][] convertfromYCBCR(Block[][][] inputImage){
		
		//The ajustement matrix
		int[] ajustmentMatrix = new int[] {0,-128,-128};
		
		//The Luminance data of the input image
		int[][] YData = new int[inputImage[0].length*8][inputImage[0][0].length*8];
		
		//The Cb data of the input image
		int[][] CbData = new int[inputImage[0].length*8][inputImage[0][0].length*8];
		
		//The Cr data of the input image
		int[][] CrData = new int[inputImage[0].length*8][inputImage[0][0].length*8];
		
		//Create the matrix for the converted image and the image to convert
		int[][][] imageToConvert = new int[inputImage.length][inputImage[0].length*8][inputImage[0][0].length*8];
		int[][][] convertedImage = new int[inputImage.length][inputImage[0].length*8][inputImage[0][0].length*8];
		
		//Get the luminance information of the input image
		for(int i = 0; i<inputImage[0].length*8;i++){
			for(int j = 0; j<inputImage[0][0].length*8;j++){
				
				YData[i][j] = inputImage[Block.Y][(int)i/8][(int)j/8].getDataAt(i%8, j%8);
			}
		}
		
		
		//Get the chrominance information of the input image in 4:2:0
		for(int i = 0; i<inputImage[0].length*8;i++){
			for(int j = 0; j<inputImage[0][0].length*8;j++){
				
				if(i%2 == 0 && j%2 == 0){
					CbData[i][j] = inputImage[Block.CB][(int)i/16][(int)j/16].getDataAt((i/2)%8, (j/2)%8);
					CrData[i][j] = inputImage[Block.CR][(int)i/16][(int)j/16].getDataAt((i/2)%8, (j/2)%8);
				}
			}
		}
		
		//Estimate the missing pixel horizontally
		for(int i = 0; i<inputImage[0].length*8;i++){
			for(int j = 0; j<inputImage[0][0].length*8;j++){
				
				//Select the even line if its the last row
				if(i % 2 == 0 && j % 2 != 0 && j == (inputImage[0][0].length*8 - 1)){
					
					//Set the missing pixel to the one before it
					CbData[i][j] = CbData[i][j-1];
					CrData[i][j] = CrData[i][j-1];
				}
				//It's not the lst row
				else if(i % 2 == 0 && j % 2 != 0){
					
					//Calculate the average between before and after pixel horizontally
					CbData[i][j] = (int)Math.round((CbData[i][j-1]+CbData[i][j+1])/2);
					CrData[i][j] = (int)Math.round((CrData[i][j-1]+CrData[i][j+1])/2);
				}
			}
		}
		
		//Estimate the missing pixel vertically
		for(int i = 0; i<inputImage[0].length*8;i++){
			for(int j = 0; j<inputImage[0][0].length*8;j++){
				
				//Select the even row and the odd line if its the last line
				if(i % 2 != 0 && j % 2 == 0 && i == (inputImage[0].length*8 -1)){
					
					//Estimate the missing pixel to the last one vertically
					CbData[i][j] = CbData[i-1][j];
					CrData[i][j] = CrData[i-1][j];
				}
				//Select the even row and the odd line
				else if(i % 2 != 0 && j % 2 == 0){
					
					//Set the missing pixel to the average of the before and after pixel vertically
					CbData[i][j] = (int)Math.round((CbData[i-1][j]+CbData[i+1][j])/2);
					CrData[i][j] = (int)Math.round((CrData[i-1][j]+CrData[i+1][j])/2);
				}
			}
		}
		
		//Estimate the missing pixel horizontally from the estimated one
		for(int i = 0; i<inputImage[0].length*8;i++){
			for(int j = 0; j<inputImage[0][0].length*8;j++){
				
				//Select the odd line and row if its the last row
				if(i % 2 != 0 && j % 2 != 0 && j == (inputImage[0][0].length*8-1)){
					
					//Set the missing pixel to the before estimated pixel horizontally
					CbData[i][j] = CbData[i][j-1];
					CrData[i][j] = CrData[i][j-1];
				}
				//Select the odd line and row
				else if(i % 2 != 0 && j % 2 != 0){
					
					//Set the missing pixel to the average of the before and after estimated pixel horizontally
					CbData[i][j] = (int)Math.round((CbData[i][j-1]+CbData[i][j+1])/2);
					CrData[i][j] = (int)Math.round((CrData[i][j-1]+CrData[i][j+1])/2);
				}
			}
		}
		
		//Set the Y. cb and cr 4:4:4 value to the image to convert
		imageToConvert[0] = YData;
		imageToConvert[1] = CbData;
		imageToConvert[2] = CrData;
		
		//Perform the matrix product
		for(int l=0;l<imageToConvert.length;l++){
			for(int i=0; i<imageToConvert[0].length;i++){
				for(int j=0; j<imageToConvert[0][0].length;j++){
					int k = 0;
					while(k< imageToConvert.length){

						convertedImage[l][i][j] += (int)(YCBCRConversionMatrix[l][k] * (imageToConvert[k][i][j] + ajustmentMatrix[k]));
						if(convertedImage[l][i][j] < 0){
							convertedImage[l][i][j] = 0;
						}
						else if(convertedImage[l][i][j] > 255){
							convertedImage[l][i][j] = 255;
						}
						k++;
					}
				}
			}
		}
			
		
		
		return convertedImage;
	}
}
