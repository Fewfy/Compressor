import java.awt.BorderLayout;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class showImage {
	private int[][] R;
	private int[][] G;
	private int[][] B;
	private int height;
	private int width;
	private JFrame frame;
	private JPanel panel;
	
	public showImage(int[][][] rgb){
		this.frame = new JFrame("img");
		frame.setLayout(new BorderLayout());
		frame.setSize(800, 600);
		frame.setVisible(true);
		this.R = rgb[0];
		this.G = rgb[1];
		this.B = rgb[0];
		this.height = rgb[0].length;
		this.width = rgb[0][0].length;
		System.out.println("height:" + height);
		System.out.println("width: " + width);
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		JLabel label = new JLabel(new ImageIcon(image));
		frame.getContentPane().add(label,BorderLayout.EAST);
		for(int i = 0;i < height;i++){
			for(int j = 0;j < width;j++){
				int pixel = 0xff000000 | ((R[i][j]& 0xff) << 16)  | (G[i][j] & 0xff) << 8 | B[i][j] & 0xff;
				try{
					image.setRGB(j, i, pixel);
				}catch (Exception e) {
					// TODO: handle exception
					System.out.println("i:"+i);
					System.out.println("j:"+j);
					e.printStackTrace();
				}
				
				label.setIcon(new ImageIcon(image));
			}
		}
		frame.setVisible(true);
	}
}
