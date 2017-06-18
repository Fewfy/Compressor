import sun.text.resources.cldr.fr.FormatData_fr;

// 每个处理块
public class Block {
	public static final int SIZE = 8;
	public static final int Y=0,CB=1,CR=2;
	public static final String[] DATA_TYPE = new String[]{"Y","Cb","Cr"};
	
	private int[][] data;
//	交流系数
	private int[] AC;
	
	private String dataType;
	
	public Block(){
		this.data = new int[SIZE][SIZE];
		this.AC = new int[SIZE * SIZE - 1];
	}
	
	public Block(int data[][] ){
		this.data = new int[SIZE][SIZE];
		for(int i = 0;i < SIZE ;i ++){
			for(int j = 0;j < SIZE ;j++){
				this.data[i][j] = data[i][j];
			}
		}
	}
	
	public int getDataAt(int i, int j){
		return this.data[i][j];
	}
	
	public void setDataAt(int i, int j, int Data){
		this.data[i][j] = Data;
	}
	
	public int[][] setData(){
		return this.data;
	}
	
	public void setData(int Data[][]){
		this.data = Data;
	}
	
	public int getDC(){
		return this.data[0][0];
	}
	
	public void setDC(int Data){
		this.data[0][0] = Data;
	}
	
	public void setAC(int[] AC){
		this.AC = AC;
	}
	
	public int[] getAC(){
		return this.AC;
	}
	
	public String getType(){
		return this.dataType;
	}

	public void setType(String type){
		this.dataType = type;
	}
	public void printData(){
		for(int i = 0;i < Block.SIZE;i++){
			for(int j = 0;j < Block.SIZE;j++){
				System.out.print(this.getDataAt(i, j) + " ");
			}
			System.out.println();
		}
	}
	
	public static Block[] createBlocks(int [][]inputImage, int width, int height,String Type){
		Block [] createdBlock = new Block[(height / 8) * (width / 8)];
		int heightNum = height / 8;
		int widthNum = width / 8;
		int num = 0;
		for(int i = 0;i < heightNum;i ++){
			for(int j = 0;j < widthNum;j ++){
				createdBlock[num] = new Block();
				for(int x = 0;x < 8;x ++){
					for(int y = 0;y < 8;y ++){
						createdBlock[num].setDataAt(y, x,inputImage[i * 8 + y][j * 8 + x]);
					}
				}
				createdBlock[num].setType(Type);
				num ++;
			}
		}
		return createdBlock;
	}
}
