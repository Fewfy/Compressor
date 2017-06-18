import java.util.ArrayList;

import sun.reflect.generics.tree.VoidDescriptor;

public abstract class SqueezelightConverter {
	protected String inputFile;
	protected String outputFile;
	protected YCrCbConverter yCrCbConverter;
	protected RGBConverter rgbConverter;
	protected int qualityFactor;
	protected int[][] resImage;
	
	protected ArrayList<Integer> DPCMlist;
	protected ArrayList<Integer> DCList;
	/** The qy. */
	protected  final int[][] QY = new int[][] 		{{ 16, 40, 40, 40, 40, 40, 51, 61},
										   	  	 	 { 40, 40, 40, 40, 40, 58, 60, 55},
										   	  	 	 { 40, 40, 40, 40, 40, 57, 69, 56},
										   	  	 	 { 40, 40, 40, 40, 51, 87, 80, 62},
										   	  	 	 { 40, 40, 40, 56, 68,109,103, 77},
										   	  	 	 { 40, 40, 55, 64, 81,104,113, 92},
										   	  	 	 { 49, 64, 78, 87,103,121,120,101},
										   	  	 	 { 72, 92, 95, 98,112,100,103, 95}};
	
	/** The Q cb cr. */
	protected final int[][] QCbCr = new int[][] 	{{ 17, 40, 40, 95, 95, 95, 95, 95},
											     	 { 17, 40, 40, 95, 95, 95, 95, 95},
											     	 { 40, 40, 40, 95, 95, 95, 95, 95},
											     	 { 40, 40, 40, 95, 95, 95, 95, 95},
											     	 { 40, 40, 95, 95, 95, 95, 95, 95},
											     	 { 95, 95, 95, 95, 95, 95, 95, 95},
											     	 { 95, 95, 95, 95, 95, 95, 95, 95},
											     	 { 95, 95, 95, 95, 95, 95, 95, 95}};
	
											     	 
	public SqueezelightConverter(String inputfile, String outputfile){
		this.inputFile = inputfile;
		this.outputFile = outputfile;
		this.qualityFactor = 1;
		this.DPCMlist = new ArrayList<>();
		this.DCList = new ArrayList<>();
	}
	
	protected double calculateAlpha(double qualityFactor) {
		
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
	
	abstract void execute();
}	
