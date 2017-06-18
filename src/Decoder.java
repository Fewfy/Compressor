import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.omg.CORBA.PUBLIC_MEMBER;

public class Decoder extends SqueezelightConverter {
	
	private int height;
	private int width;
	private int color;
	private int quality;
	private compressedImage image;
	private double alpha;
	public static int[][][] resImage;
	public Decoder(String inputfile, String outputfile){
		super(inputfile, outputfile);
		try {
			readfile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void execute(){
		this.image = new compressedImage(this.height, this.width, this.color);
		this.alpha = 1.0;
		IRLC();
		IDPCM();
		this.image.getBlock();
		
		for(Block block : this.image.getBlockList()){
//			System.out.println("decode initial:");
//			block.printData();
			Izigzag(block);
			if(block.getType() == "Y"){
				unquantify(block, QY, alpha);
			}else{
				unquantify(block, QCbCr, alpha);
			}
//			System.out.println("decode unquantified:");
//			block.printData();
			
			IDCT(block);
//			System.out.println("decode IDCT:");
//			block.printData();
		}
		this.resImage = this.image.convert2RGB();
		new showImage(this.resImage);
//		this.image.printImage();
	}
	
	public void showImage(int[][][] img){
		int imgh = this.height / 8;
		imgh *= 8;
		int imgw = this.width / 8;
		imgw *= 8;
		BufferedImage image = new BufferedImage(imgw, imgh, BufferedImage.TYPE_INT_RGB);
		
	}
	
	public void IDCT(Block Bloc){
		int[][] treatedData = new int[Bloc.SIZE][Bloc.SIZE];
		//Needed for the algorithm
		double CU = 0;
		double CV = 0;
		double sum1 = 0;
		double sum2 = 0;
		
		for(int i=0; i<Bloc.SIZE;i++){
			
			for(int j=0; j<Bloc.SIZE;j++){
				sum1 = 0;
				
				for(int u=0; u<Bloc.SIZE;u++){
					sum2 = 0;
					
					for(int v=0; v<Bloc.SIZE;v++){
						
						CU = u == 0 ? (1/Math.sqrt(2)): 1;
						CV = v == 0 ? (1/Math.sqrt(2)): 1;
						int temp = Bloc.getDataAt(u, v);
						sum2 += (((CU * CV)/4)*(Math.cos(((2 * i + 1) * u * Math.PI)/16) *
								Math.cos(((2 * j + 1) * v * Math.PI)/16) * Bloc.getDataAt(u, v)));
					}
					
					sum1 += sum2;
				}
				
				treatedData[i][j] = (int)Math.round((sum1));
			}
		}
		//Update the bloc data
		Bloc.setData(treatedData);
	}
	
	public void unquantify(Block bloc,int[][] quantificationTable, double alpha) {
		
		//For every pixels
		for(int i=0; i<Block.SIZE;i++){
			for(int j=0;j<Block.SIZE;j++){
				
				//Calculate the unquantify value
				double temp = alpha * quantificationTable[i][j];
				int value = (int)(alpha == 100 ? bloc.getDataAt(i, j):
					Math.round((bloc.getDataAt(i, j)*(alpha*quantificationTable[i][j]))));
				//Update the bloc's value
				bloc.setDataAt(i, j, value);
			}
		}
		
	}
	
	public void Izigzag(Block block){
		int[] AC = block.getAC();
		int i = 0, j = 0;
		int k = 0;
		boolean midway = false;
		while(i != block.SIZE && j != block.SIZE){
	
			if(i == 0){
				block.setDataAt(i, ++j, AC[k++]);
				while(j > 0){
					block.setDataAt(++i, --j, AC[k++]);
				}
			}
			else if(j == 0 && i != Block.SIZE - 1){
				block.setDataAt(++i, j, AC[k++]);
				while(i > 0){
					block.setDataAt(--i, ++j, AC[k++]);
				}
			}
			else if(i == Block.SIZE - 1){
				block.setDataAt(i, ++j, AC[k++]);
				if(j == Block.SIZE - 1 && i == Block.SIZE - 1)
					break;
				while(j < Block.SIZE - 1){
					block.setDataAt(--i, ++j, AC[k++]);
				}
			}
			else if(j == Block.SIZE - 1){
				block.setDataAt(++i, j, AC[k++]);
				while(i < Block.SIZE - 1){
					block.setDataAt(++i, --j, AC[k++]);
				}
			}
		}
	}
	
	public void IDPCM(){
		this.DCList = this.image.getDClist();
		while(DCList.size() < image.getExpectedDCSize()){
			if(DCList.isEmpty()){
				DCList.add(Entropy.readDC());
			}
			else{
				DCList.add(DCList.get(DCList.size() - 1) + Entropy.readDC());
			}
		}
		this.image.setDCList(this.DCList);
	}
	
	public void IRLC(){
		int[] AC;
		int[] tempAC;
		
		while(this.image.getACSize() != this.image.getExpectedACSize()){
			int RUNLENGTH = 0, DATA = 1, EOB = 0;
			AC = new int[Block.SIZE * Block.SIZE - 1];
			int acLength = 0;
			while(acLength < AC.length){
				tempAC = Entropy.readAC();
				
				if(tempAC[DATA] == EOB){
					for(int i = acLength;i < AC.length;i++){
						AC[i] = 0;
						acLength++;
					}
				}
				else if(tempAC[RUNLENGTH] != 0){
					for(int i = 0;i < tempAC[RUNLENGTH];i++){
						AC[acLength++] = 0;
					}
					AC[acLength++] = tempAC[DATA];
				}
				else {
					AC[acLength++] = tempAC[DATA];
				}
			}
			this.image.getAClist().add(AC);
		}
		
//		this.image.convert2RGB();
	}
	
	
	public void readfile() throws IOException{
		File file = new File(this.inputFile);
		
		if(file.exists()){
			try {
				DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
				this.height = (in.readByte() << 8) + (in.readByte() & 0xff);
				this.width = (in.readByte() << 8 ) + (in.readByte() & 0xff);
				this.color = in.readByte() & 0xff;
				this.quality = in.readByte() & 0xff;
				byte[] buffer = new byte[in.available()];
				in.read(buffer);
				Entropy.loadBitstream(buffer);
				in.close();
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
