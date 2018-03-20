
package james94jeans2.minimapsync.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;

/**
 * 
 * @version b0.8
 * @author james94jeans2 (Jens Leicht)
 * 
 */

public final class FileReader
{
    
	private Scanner scanner;
      
	private final String fileName;
  
	public FileReader(String pFileName) throws IOException
	{
	    fileName = pFileName;
	    scanner = new Scanner(new FileInputStream(fileName));
	}
  
	  public void closeScanner()
	  {
	      scanner.close();
	  }
	  
	  public boolean hasNextLine()
	  {
	      return scanner.hasNextLine();
	  }
	  
	  public String readNextLine()
	  {
	    return scanner.nextLine();       
	  }
  
}