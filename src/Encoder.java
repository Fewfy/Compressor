import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import javax.imageio.ImageIO;

public class Encoder extends SqueezelightConverter{
	
	public static int[][] red;
	public static int[][] green;
	public static int[][] blue;
	private int width;
	private int height;
	public Block[] redBlocklist;
	public Block[] blueBlocklist;
	public Block[] greenBlocklist;
	public Block[] YBlocklist;
	public Block[] CrBlocklist;
	public Block[] CbBlocklist;
	public Block[][] YCbCrlist;
	public int[][][] YCrCb;
	private double alpha;
	
	public Encoder(String inputfile, String outputfile){
		super(inputfile, outputfile);
		this.rgbConverter = new RGBConverter();
		this.yCrCbConverter = new YCrCbConverter();
		readFile();
//		this.redBlocklist = Block.createBlocks(this.red, width, height);
//		this.greenBlocklist = Block.createBlocks(this.green, width, height);
//		this.blueBlocklist = Block.createBlocks(this.blue, width, height);

		
	}
	@Override
	public void execute(){
		this.alpha = 1.0;
		this.YCrCb = YCrCbConverter.convertFromRGB(this.red,this.green,this.blue);
		this.YBlocklist = Block.createBlocks(YCrCb[0], width, height,"Y");
		this.CrBlocklist = Block.createBlocks(YCrCb[2], width, height,"Cr");
		this.CbBlocklist = Block.createBlocks(YCrCb[1], width, height,"Cb");
		this.YCbCrlist = new Block[3][];
		this.YCbCrlist[0] = this.YBlocklist;
		this.YCbCrlist[1] = this.CbBlocklist;
		this.YCbCrlist[2] = this.CrBlocklist;
		
		for(Block[] blocklist:this.YCbCrlist){
			for(Block block : blocklist){
//				System.out.println("initial:");
//				block.printData();
				
				DCT(block);
//				System.out.println("dct:");
//				block.printData();
				if(block.getType() == "Y")
					quantify(block, QY,alpha);
				else
					quantify(block, QCbCr, alpha);
//				System.out.println("quantified");
//				block.printData();
				
				zigzag(block);
				RLC(block);
			}
			
		}
		
		
		for(int DPCM:this.DPCMlist){
			Entropy.writeDC(DPCM);
		}
		File output = new File(this.outputFile);
		writeFile(output, height, width, 1);
		
	}
	
	public void writeFile(File outputfile,int height, int width, int quality){
		try{
			DataOutputStream out= new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputfile)));
			System.out.println(height >> 8);
			out.writeByte((height >> 8) & 0xff);
			out.writeByte(height & 0xff);
			out.writeByte((width >> 8) & 0xff);
			out.writeByte(width & 0xff);
			out.writeByte((byte)(3 & 0xff));
			out.writeByte(quality & 0xff);
			out.write(Entropy.getBitstream(),0,Entropy.getNumberofBytesUsed() + 1);
			out.close();
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	public void RLC(Block block){
		int runlength = 0;
		int [] AC = block.getAC();
		
		for(int i = 0;i < AC.length;i++){
			if(AC[i] == 0 && i == AC.length - 1){
				Entropy.writeAC(runlength, 0);
			}
			else if(AC[i] == 0){
				runlength ++;
			}else{
				Entropy.writeAC(runlength, AC[i]);
				runlength = 0;
			}
		}
	}
	
	public void zigzag(Block block){
		int [] ACData = new int[(Block.SIZE * Block.SIZE) - 1];
		int i = 0, j = 0, k = 0;
		boolean midway = false;
		
		while(i != (Block.SIZE - 1) || j != (Block.SIZE - 1)){
			if(i == (Block.SIZE - 1) || j == (block.SIZE - 1)){
				midway = true;
			}
			
			if(!midway){
				if(i == 0){
					ACData[k++] = block.getDataAt(i, ++j);
					while(j != 0){
						ACData[k++] = block.getDataAt(++i, --j);
					}
				}
				else if(j == 0){
					ACData[k++] = block.getDataAt(++i, j);
					while(i != 0){
						ACData[k++] = block.getDataAt(--i, ++j);
					}
				}
			}
			else{
				if(i == Block.SIZE - 1){
					ACData[k++] = block.getDataAt(i, ++j);
					while(j != Block.SIZE - 1){
						ACData[k++] = block.getDataAt(--i, ++j);
					}
				}else if(j == Block.SIZE - 1){
					ACData[k++] = block.getDataAt(++i, j);
					while(i != Block.SIZE - 1){
						ACData[k++] = block.getDataAt(++i, --j);
					}
				}
			}
		}
		DPCM(block.getDC());
		block.setAC(ACData);
	}
	
	public void DPCM(int  data){
		if(this.DCList.isEmpty()){
			this.DCList.add(data);
			this.DPCMlist.add(data);
		}else{
			this.DPCMlist.add(data - this.DCList.get(this.DCList.size() - 1));
			this.DCList.add(data);
		}
	}
	
	
	public void readFile(){
		File file = new File(this.inputFile);
		int i = 0;
		int j = 0;
		if(!file.exists()){
			System.out.println("File not found");
		}
		try {
			BufferedImage bufferimage = ImageIO.read(file);
			this.width = bufferimage.getWidth();
			this.height = bufferimage.getHeight();
			red = new int[height][width];
			green = new int[height][width];
			blue = new int[height][width];
			for(i = 0;i < height;i++){
				for(j = 0;j < width;j++){
					int pixel = bufferimage.getRGB(j, i);
					red[i][j] = (pixel & 0xff0000) >> 16;
					green[i][j] = (pixel & 0xff00) >> 8;
					blue[i][j] = pixel & 0xff;
				}
			}
			int[][][] testimg = new int[3][][];
			testimg[0] = red;
			testimg[1] = green;
			testimg[2] = blue;
			new showImage(testimg);
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("i:"+i);
			System.out.println("j:"+j);
			e.printStackTrace();
		}
	}
	
	 public double calculateAlpha(int qualityFactor) {
			
			double alpha = 0;
			
			if(qualityFactor >= 1 && qualityFactor <= 50){
				
				alpha = 50.0/qualityFactor;
			}
			else if(qualityFactor <= 99){
				
				alpha = ((200.0-2*qualityFactor)/100.0);
			}
			else if(qualityFactor == 100){
				
				alpha = qualityFactor;
			}
			
			return alpha;
		}
	 
	 public void DCT(Block block){
		 int [][] treatedData = new int[block.SIZE][block.SIZE];
		 double CU = 0;
		 double CV = 0;
		 double sum1 = 0;
		 double sum2 = 0;
		 
		 for(int u = 0;u < Block.SIZE;u++){
			 for(int v = 0;v < Block.SIZE;v ++){
				 sum1 = 0;
				 for (int i = 0;i < Block.SIZE;i ++){
					 sum2 = 0;
					 for(int j = 0;j < Block.SIZE; j++){
						 sum2 += (Math.cos(((2 * i + 1) * u * Math.PI)/16) *
									Math.cos(((2 * j + 1) * v * Math.PI)/16) * block.getDataAt(i, j));
					 }
					 sum1 += sum2;
				}
				CU = u == 0 ? (1/Math.sqrt(2)): 1;
				CV = v == 0 ? (1/Math.sqrt(2)): 1;
					
				treatedData[u][v] = (int)Math.round((((CU * CV)/4) * sum1));
			 }
		 }
		 block.setData(treatedData);
	 }
	
		public void quantify(Block bloc,int[][] quantificationTable, double alpha) {
			
			//For every pixel in the bloc
			for(int i=0; i<Block.SIZE;i++){
				for(int j=0;j<Block.SIZE;j++){
					
					//Quantify the pixel
					double temp = alpha * quantificationTable[i][j];
					int value = (int)(alpha == 100 ? bloc.getDataAt(i, j):
						Math.round((bloc.getDataAt(i, j)/(alpha*quantificationTable[i][j]))));
					bloc.setDataAt(i, j, value);
				}
			}
		}
}

























