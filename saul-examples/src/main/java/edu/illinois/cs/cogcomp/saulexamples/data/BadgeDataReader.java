/**
 * 
 */
package edu.illinois.cs.cogcomp.saulexamples.data;

import edu.illinois.cs.cogcomp.lbjava.parse.Parser;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads documents, given a directory
 * 
 * @author Gourab Kundu
 * 
 */
public class BadgeDataReader implements Parser {

    private List<String> lines;

    private int currentline;
    private BufferedReader br;
    
    /**
     * 
     */
    public BadgeDataReader(String file) {
        lines = new ArrayList<>();
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String str = br.readLine();
            while (str != null) {
                lines.add(str);
                str = br.readLine();
            }
            br.close();
        } catch (Exception e) {
            
        }
    }
    
    public void close() {
    }
    
    public Object next() {
        
        if (currentline == lines.size()) {
            return null;
        } else {
            currentline++;
            return lines.get(currentline - 1);
        }
    }
    
    public void reset() {
        currentline = 0;
    }
}
