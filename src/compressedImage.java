import java.util.ArrayList;

import com.sun.org.apache.xml.internal.security.Init;
import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

public class compressedImage {
	private ArrayList<int[]> ACList;
	private ArrayList<Integer> DCList;
	private Block[][][] blocks;
	private ArrayList<Block> blocklist;
	private int height;
	private int width;
	private int dimension;
	private int innerImage[][][];
	
	public compressedImage(int height, int width, int dimension){
		this.height = height;
		this.width = width;
		this.dimension = dimension;
		
		this.ACList = new ArrayList<>();
		this.DCList = new ArrayList<>();
		this.blocklist = new ArrayList<>();
		this.blocks = new Block[dimension][this.height / 8][this.width / 8];
		
	}
	
	public void getBlock(){
//		YµÄ¿éÊý
		int Ynum = (height / 8) * (width / 8);
		int Cbnum = (height / 8) * (width / 8);
		int Crnum = (height / 8) * (width / 8);
		Block[][] yBlocks = new Block[height/8][width/8];
		Block[][] cbBlocks = new Block[height/8][width/8];
		Block[][] crBlocks = new Block[height/8][width/8];
		for(int i = 0;i < this.getExpectedACSize();i++){
			Block block = new Block();
			block.setAC(getAClist().get(i));
			block.setDC(getDClist().get(i));
			if(i < Ynum){
				block.setType(Block.DATA_TYPE[0]);
			}else if(i >= Ynum && i < Ynum + Cbnum){
				block.setType(block.DATA_TYPE[1]);
			}else{
				block.setType(block.DATA_TYPE[2]);
			}
			this.blocklist.add(block);
		}
		int k = 0;
		for(int i = 0;i < yBlocks.length;i++){
			for(int j = 0;j < yBlocks[0].length;j++){
				yBlocks[i][j] = blocklist.get(k++);
			}
		}
		
		for(int i = 0;i < cbBlocks.length;i++){
			for(int j = 0;j < cbBlocks[0].length;j++){
				cbBlocks[i][j] = blocklist.get(k++);
			}
		}
		
		for(int i = 0;i < crBlocks.length;i++){
			for(int j = 0;j < crBlocks[0].length;j++){
				crBlocks[i][j] = blocklist.get(k++);
			}
		}
		this.blocks[0] = yBlocks;
		this.blocks[1] = cbBlocks;
		this.blocks[2] = crBlocks;
		
	}
	
	public int[][][] convert2RGB(){
		Block block[][][] = this.blocks;
		int [][][] convertedImage = new int[this.dimension][height][width];
		int heightnum = this.height / 8;
		int widthnum = this.width / 8;
		int i = 0;
		int j  =0;
		int x  =0;
		int y = 0;
		try{
			for(i = 0;i < heightnum;i ++){
				for(j = 0;j < widthnum;j++){
					for(y = 0;y < Block.SIZE;y ++){
						for(x = 0;x < Block.SIZE;x++){
							convertedImage[0][i * Block.SIZE + y][j * Block.SIZE + x] =(int) (1.164 *(block[0][i][j].getDataAt(y, x) - 16) + 1.596 * (block[2][i][j].getDataAt(y, x) - 128)); //R
							convertedImage[1][i * Block.SIZE + y][j * Block.SIZE + x] = (int)(1.164 *(block[0][i][j].getDataAt(y, x) - 16) - 0.813 * (block[2][i][j].getDataAt(y, x) - 128) - 0.392 * (block[1][i][j].getDataAt(y, x) - 128));//G
							convertedImage[2][i * Block.SIZE + y][j * Block.SIZE + x] = (int)(1.164 * (block[0][i][j].getDataAt(y, x) - 16) + 2.017 * (block[1][i][j].getDataAt(y, x) - 128));//B
							if(convertedImage[0][i * Block.SIZE + y][j * Block.SIZE + x] > 255)
								convertedImage[0][i * Block.SIZE + y][j * Block.SIZE + x] = 255;
							if(convertedImage[1][i * Block.SIZE + y][j * Block.SIZE + x] > 255)
								convertedImage[1][i * Block.SIZE + y][j * Block.SIZE + x] = 255;
							if(convertedImage[2][i * Block.SIZE + y][j * Block.SIZE + x] > 255)
								convertedImage[2][i * Block.SIZE + y][j * Block.SIZE + x] = 255;
							if(convertedImage[0][i * Block.SIZE + y][j * Block.SIZE + x] < 0)
								convertedImage[0][i * Block.SIZE + y][j * Block.SIZE + x] = -convertedImage[0][i * Block.SIZE + y][j * Block.SIZE + x];
							if(convertedImage[1][i * Block.SIZE + y][j * Block.SIZE + x] < 0)
								convertedImage[1][i * Block.SIZE + y][j * Block.SIZE + x] = -convertedImage[1][i * Block.SIZE + y][j * Block.SIZE + x];
							if(convertedImage[2][i * Block.SIZE + y][j * Block.SIZE + x] < 0)
								convertedImage[2][i * Block.SIZE + y][j * Block.SIZE + x] = -convertedImage[2][i * Block.SIZE + y][j * Block.SIZE + x];
						}
					}
				}
			}
		}catch (Exception e) {
			// TODO: handle exception
			System.out.println("x:" + x);
			System.out.println("y:"+y);

			System.out.println("i:"+i);
			System.out.println("j:"+j);
			System.out.println("heightnum:"+heightnum);
			System.out.println("widthnum:"+widthnum);
			System.out.println("block width:"+block[0][0].length);
			System.out.println("block height:"+block[0].length);
			System.out.println("converted width: "+ convertedImage[0][0].length);
			System.out.println("converted height: "+ convertedImage[0].length);
		
			try {
//				convertedImage[0][i * Block.SIZE + y][j * Block.SIZE + x] = 1;
				System.out.println("data1:"+(int) (1.164 *(block[0][i][j].getDataAt(y, x) - 16)  - 128));
				System.out.println("data2:"+(int)(+ 1.596 * (block[2][i][j].getDataAt(y, x))));
				System.out.println("after assign");
			} catch (Exception e2) {
				// TODO: handle exception
//				System.out.println("error col length:"+convertedImage[0][i * Block.SIZE + y].length);
//				System.out.println("error test:"+convertedImage[0][0][j * Block.SIZE + x]);
//				System.out.println("error row:"+(i * Block.SIZE + y));
//				System.out.println("error col:"+(j * Block.SIZE + x));
				block[2][i][j].printData();
				e.printStackTrace();
			}
			e.printStackTrace();
		}
		
		this.innerImage = convertedImage;
		return convertedImage;
	}
	
	public ArrayList<Block> getBlockList(){
		return this.blocklist;
	}
	
	public int getACSize(){
		return  this.ACList.size();
	}
	
	public int getDCSize(){
		return this.DCList.size();
	}
	
	public int getExpectedACSize(){
		return (height / 8) * (width / 8) * 3;
	}
	
	public ArrayList<int[]> getAClist(){
		return this.ACList;
	}
	
	public ArrayList<Integer> getDClist(){
		return this.DCList;
	}
	
	public int getExpectedDCSize(){
		return (height / 8) * (width / 8) * 3;
	}
	
	public void setDCList(ArrayList<Integer> dclist){
		this.DCList = dclist;
	}
	
	
	public  void printImage(){
		System.out.println("decode R");
		for(int i = 0;i < height;i++){
			for(int j = 0;j < width;j++){
				System.out.print(innerImage[0][i][j] + " ");
			}
			System.out.println();
		}
		
		System.out.println("decode G");
		for(int i = 0;i < height;i++){
			for(int j = 0;j < width;j++){
				System.out.print(innerImage[1][i][j] + " ");
			}
			System.out.println();
		}
		
		System.out.println("decode B");
		for(int i = 0;i < height;i++){
			for(int j = 0;j < width;j++){
				System.out.print(innerImage[2][i][j] + " ");
			}
			System.out.println();
		}
	}
	
	
}
