import com.sun.org.apache.xml.internal.security.Init;

import jdk.internal.org.objectweb.asm.commons.StaticInitMerger;

public class Entropy {
	private static final int SIZE = 0;
	 private static final int AMPLITUDE = 1;
	 /* 
	 * CATEGORY  BASECODE       LENGTH BINARY VALUE
	 *        0  010                 3            2
	 *        1  011                 3            3
	 *        2  100                 3            4
	 *        3  00                  2            0
	 *        4  101                 3            5
	 *        5  110                 3            6
	 *        6  1110                4           14
	 *        7  11110               5           30
	 *        8  111110              6           62
	 *        9  1111110             7          126
	 *        A  11111110            8          254
	 *        B  111111110           9          510
	 *        C  1111111110         10         1022
	 *        D  11111111110        11         2046
	 *        
	 */
	private static final int[][] HUFFMAN_DC = {
				{ 3, 3, 3, 2, 3, 3,  4,  5,  6,  7,    8,   9,   10,   11}, /* size */
				{ 2, 3, 4, 0, 5, 6, 14, 30, 62, 126, 254, 510, 1022, 2046}  /* amplitude */
	};
	
	private static final int ZRL = 2041;
	private static final int ZRL_SIZE = 11;
	private static final int EOB = 10;
	private static final int EOB_SIZE = 4;
	private static final int[][][] HUFFMAN_AC = {
			{ /* size */
				{  2,  2,  3,  4,  5,  7,  8, 10, 16, 16},
				{  4,  5,  7,  9, 11, 16, 16, 16, 16, 16},
				{  5,  8, 10, 12, 16, 16, 16, 16, 16, 16},
				{  6,  9, 12, 16, 16, 16, 16, 16, 16, 16},
				{  6, 10, 16, 16, 16, 16, 16, 16, 16, 16},
				{  7, 11, 16, 16, 16, 16, 16, 16, 16, 16},
				{  7, 12, 16, 16, 16, 16, 16, 16, 16, 16},
				{  8, 12, 16, 16, 16, 16, 16, 16, 16, 16},
				{  9, 15, 16, 16, 16, 16, 16, 16, 16, 16},
				{  9, 16, 16, 16, 16, 16, 16, 16, 16, 16},
				{  9, 16, 16, 16, 16, 16, 16, 16, 16, 16},
				{ 10, 16, 16, 16, 16, 16, 16, 16, 16, 16},
				{ 10, 16, 16, 16, 16, 16, 16, 16, 16, 16},
				{ 11, 16, 16, 16, 16, 16, 16, 16, 16, 16},
				{ 16, 16, 16, 16, 16, 16, 16, 16, 16, 16},
				{ 16, 16, 16, 16, 16, 16, 16, 16, 16, 16}
			},
			{ /* amplitude */
				{    0,     1,     4,    11,    26,   120,   248,  1014, 65410, 65411},
				{   12,    27,   121,   502,  2038, 65412, 65413, 65414, 65415, 65416},
				{   28,   249,  1015,  4084, 65417, 65418, 65419, 65420, 65421, 65422},
				{   58,   503,  4085, 65423, 65424, 65425, 65426, 65427, 65428, 65429},
				{   59,  1016, 65430, 65431, 65432, 65433, 65434, 65435, 65436, 65437},
				{  122,  2039, 65438, 65439, 65440, 65441, 65442, 65443, 65444, 65445},
				{  123,  4086, 65446, 65447, 65448, 65449, 65450, 65451, 65452, 65453},
				{  250,  4087, 65454, 65455, 65456, 65457, 65458, 65459, 65460, 65461},
				{  504, 32704, 65462, 65463, 65464, 65465, 65466, 65467, 65468, 65469},
				{  505, 65470, 65471, 65472, 65473, 65474, 65475, 65476, 65477, 65478},
				{  506, 65479, 65480, 65481, 65482, 65483, 65484, 65485, 65486, 65487},
				{ 1017, 65488, 65489, 65490, 65491, 65492, 65493, 65494, 65495, 65496},
				{ 1018, 65497, 65498, 65499, 65500, 65501, 65502, 65503, 65504, 65505},
				{ 2040, 65506, 65507, 65508, 65509, 65510, 65511, 65512, 65513, 65514},
				{65515, 65516, 65517, 65518, 65519, 65520, 65521, 65522, 65523, 65524},
				{65525, 65526, 65527, 65528, 65529, 65530, 65531, 65532, 65533, 65534}
			}
		};	
	private static final int BUFFER_SIZE = 4096;
	private static byte[] writingBuffer;
	private static byte[] readingBuffer;
	private static int bitsLeftInByte;
	private static int currentByteInBuffer;
	private static final int BITS_IN_BYTE = 8;
	
	public static void flushBuffers(){
		writingBuffer = new byte[BUFFER_SIZE];
		bitsLeftInByte = BITS_IN_BYTE;
		currentByteInBuffer = 0;
	}
	
	private static void expandBuffer(){
		byte[] temp = new byte[writingBuffer.length + BUFFER_SIZE];
		System.arraycopy(writingBuffer, 0, temp, 0, writingBuffer.length);
		writingBuffer = temp;
	}
	
	private static boolean decrementBitsToRead(){
		bitsLeftInByte--;
		if(bitsLeftInByte == 0){
			currentByteInBuffer ++;
			if(currentByteInBuffer == readingBuffer.length)
				return false;
			bitsLeftInByte = BITS_IN_BYTE;
		}
		return true;
	}
	
	public static void decrementBitsToWrite(){
		bitsLeftInByte--;
		if(bitsLeftInByte <= 0){
			currentByteInBuffer ++;
			if(currentByteInBuffer == writingBuffer.length){
				expandBuffer();
			}
			bitsLeftInByte = BITS_IN_BYTE;
		}
	}
	
	public static byte[] getBitstream(){
		return writingBuffer;
	}
	
	public static int getNumberofBytesUsed(){
		return currentByteInBuffer;
	}
	
	public static void loadBitstream(byte[] buffer){
		readingBuffer = buffer;
		flushBuffers();
	}
	
	private static void write(int bits, int length){
		if(writingBuffer == null){
			flushBuffers();
		}
		for(int i = length;i > 0 ;i --){
			int bit = ((bits >> (i-1)) & 0x01) << (bitsLeftInByte -1);
			if(currentByteInBuffer >= writingBuffer.length)
				expandBuffer();
			writingBuffer[currentByteInBuffer] |= bit;
			decrementBitsToWrite();
		}
	}
	
	public static void writeDC(int value){
		int [] pair = getSizeAmplitudePair(value);
		
		int binary = 0;
		binary = pair[1];
		binary |= HUFFMAN_DC[AMPLITUDE][pair[0]] << pair[0];
		
		write(binary, pair[0] + HUFFMAN_DC[SIZE][pair[0]]);
	}
	
	public static int readDC(){
		if(readingBuffer == null)
			return 0xffffffff;
		int value = 0;
		int bitsRead = 0;
		while(currentByteInBuffer < readingBuffer.length){
			value |= (readingBuffer[currentByteInBuffer] >> (bitsLeftInByte - 1)) & 0x01;
			bitsRead ++;
			if(!decrementBitsToRead())
				return 0xffffffff;
			for(int i = 0;i < HUFFMAN_DC[AMPLITUDE].length;i++){
				if(value == HUFFMAN_DC[AMPLITUDE][i] && bitsRead == HUFFMAN_DC[SIZE][i]){
					int size = 0;
					for(int j = 0;j < i;j++){
						size <<= 1;
						size |= (readingBuffer[currentByteInBuffer] >> (bitsLeftInByte - 1)) & 0x01;
						if(!decrementBitsToRead())
							return 0xffffffff;
					}
					return getValue(i, size);
				}
			}
			
			value <<= 1;
		}
		return 0xffffffff;
	}
	
	public static void writeAC(int runlength, int value){
		if(value == 0){
			write(EOB, EOB_SIZE);
		}else{
			int pair[] = getSizeAmplitudePair(value);
			while(runlength >= 0){
				if(runlength <= 15){
					int binary = pair[1];
					binary |= HUFFMAN_AC[AMPLITUDE][runlength][pair[0]-1] << pair[0];
					write(binary, pair[0] + HUFFMAN_AC[SIZE][runlength][pair[0] - 1]);
				}else{
					write(ZRL, ZRL_SIZE);
				}
				runlength -= 16;
			}
		}
	}
	
	public static int[] readAC(){
		if(readingBuffer == null)
			return null;
		int value = (readingBuffer[currentByteInBuffer] >> (bitsLeftInByte - 1)) & 0x01;
		int bitsRead = 1;
		int zrlCount = 0;
		decrementBitsToRead();
		
		while(currentByteInBuffer < readingBuffer.length){
			value <<= 1;
			value |= (readingBuffer[currentByteInBuffer] >> (bitsLeftInByte - 1)) & 0x01;
			bitsRead ++;
			decrementBitsToRead();
			if(value == ZRL && bitsRead == ZRL_SIZE){
				zrlCount ++;
				value = (readingBuffer[currentByteInBuffer] >> (bitsLeftInByte - 1)) & 0x01;
				bitsRead = 1;
				decrementBitsToRead();
			}else if(value == EOB && bitsRead == EOB_SIZE){
				int pair[] = {0,0};
				return pair;
			}else{
				for(int i = 0;i < HUFFMAN_AC[AMPLITUDE].length;i++){
					for(int j = 0;j < HUFFMAN_AC[AMPLITUDE][0].length;j++){
						if(value == HUFFMAN_AC[AMPLITUDE][i][j]){
							int runlength = i;
							int size = j + 1;
							int amplitude = 0;
							for(int k = 0;k < size;k++){
								amplitude <<= 1;
								amplitude |= (readingBuffer[currentByteInBuffer] >> (bitsLeftInByte - 1))&0x01;
								decrementBitsToRead();
							}
							int pair[] = {runlength + zrlCount * 16, getValue(size, amplitude)};
							return pair;
						
							}else if(value < HUFFMAN_AC[AMPLITUDE][i][j]){
								j = HUFFMAN_AC[AMPLITUDE][0].length;
							}
						}
					}
				}
			}
		
		return null;
	}
	
	
	public static int[] getSizeAmplitudePair(int value){
		int pair[] = new int[2];
		if(value == 0)
			return pair;
		/*二进制的位数*/
		pair[0] = Integer.toBinaryString(Math.abs(value)).length();
		/*如果是负数则存储他的补码*/
		if(value < 0)
			pair[1] = 2 * (int)Math.pow(2, pair[0]-1) -1 - Math.abs(value);
		else
			pair[1] = value;
		return pair;
	}
	
	public static int getValue(int size, int amplitude){
		int min = (int)Math.pow(2, size - 1);
		/* 是负数*/
		if(amplitude < min){
			return -(2 * min - 1 - Math.abs(amplitude));
		}
		else{
			return amplitude;
		}
	}
	
	
}
