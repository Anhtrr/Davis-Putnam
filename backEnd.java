import java.io.*;
import java.util.*;

/**
 *  
 *  Artificial Inteligence - Programming Assignment #2 - Back End Implementation
 *  Professor Ernest Davis
 *  @author Anh Tran - 3/12/2023
 *  Due: 3/20/2023 - 11AM (EST)
 *  
 */
public class backEnd {
    // MAIN
    public static void main(String args[]){
        // EXTRACT NODES, TREASURES AND ALLOWED STEPS FROM INPUT
        boolean solutionFound = false;
        // Stores ID/refNum of every proposition that is true
        List<Integer> truthList = new ArrayList<>();
        // { { key: time (int) }, { value: node (string) } }
        HashMap<Integer, String> solutionMap = new HashMap<>();
        // READ INPUT AND CONSTRUCT SOLUTION MAP
        try{
            solutionFound = readInput(truthList, solutionMap);
        } catch (FileNotFoundException e){
            System.err.println("File DPout_BEin.txt was not found!");
            System.exit(0);
        }
        // WRITE OUTPUT WHETHER FAIL OR SUCCESSFUL
        try{
            writeOutput(solutionFound, solutionMap);
        } catch (IOException e){
            System.err.println("BEout.txt is an existing directory " +
            "or is a file that cannot be opened in the current directory!");
            System.exit(0);
        }
    }

    // READ INPUT AND CONSTRUCT SOLUTION AS HASHMAP
    public static boolean readInput(List<Integer> truthList, 
    HashMap<Integer, String> solutionMap) throws FileNotFoundException{
        File backEndInFile = new File("DPout_BEin.txt");
        try{
            Scanner inFile = new Scanner(backEndInFile);
            // CHECK FIRST LINE FOR FAILURE
            if(inFile.hasNextLine()){
                String checkerLine = inFile.nextLine().trim();
                if(checkerLine.equals("0")){
                    inFile.close();
                    return false;
                }
                else{
                    // CONSTRUCT TRUTH LIST
                    String idOnFirstLine = checkerLine.split("\\s+", -1)[0].trim();
                    String truthOnFirstLine = checkerLine.split("\\s+", -1)[1].trim();
                    if(truthOnFirstLine.equals("T")){
                        truthList.add(Integer.valueOf(idOnFirstLine));
                    }
                    while(inFile.hasNextLine()){
                        String truthLine = inFile.nextLine().trim();
                        if(truthLine.equals("0")){
                            break;
                        }
                        else{
                            String idOnLine = truthLine.split("\\s+", -1)[0].trim();
                            String truthOnLine = truthLine.split("\\s+", -1)[1].trim();
                            if(truthOnLine.equals("T")){
                                truthList.add(Integer.valueOf(idOnLine));
                            }
                        }
                    }
                    // CONSTRUCT NODE MAP
                    while(inFile.hasNextLine()){
                        String propositionLine = inFile.nextLine().trim();
                        if(!propositionLine.equals("")){
                            int idOnLine = Integer.valueOf(propositionLine.split("\\s+", -1)[0].trim());
                            String propositionOnLine = propositionLine.split("\\s+", -1)[1].trim();
                            if(propositionOnLine.contains("Has")){
                                continue;
                            }
                            else if(propositionOnLine.contains("At") && truthList.contains(idOnLine)){
                                String propositionContents = propositionOnLine.replace("At(", "").replace(")", "").trim();
                                String nodeInProposition = propositionContents.split(",", -1)[0].trim();
                                int timeInProposition =  Integer.valueOf(propositionContents.split(",", -1)[1].trim());
                                solutionMap.put(timeInProposition, nodeInProposition);
                            }
                            else{
                                continue;
                            }
                        }
                        else{
                            continue;
                        }
                    }
                }
            }
            inFile.close();
        } catch (FileNotFoundException e){
            System.err.println("File DPout_BEin.txt was not found!");
            System.exit(0);
        }
        return true;
    }
    
    // READ SOLUTION AND WRITE TO OUTPUT FILE
    public static void writeOutput(boolean solutionFound, HashMap<Integer, String> solutionMap) throws IOException{
        File backEndOutFile = new File("BEout.txt");
        try{
            FileWriter fw = new FileWriter(backEndOutFile);
            if (solutionFound == false){
                fw.write("NO SOLUTION");
            }
            else{
                String answerChainLine = "";
                for(int i = 0; i < solutionMap.size(); i++){
                    answerChainLine += solutionMap.get(i) + " ";
                }
                fw.write(answerChainLine.trim());
            }
            fw.close();
        } catch (IOException e){
            System.err.println("BEout.txt is an existing directory " +
            "or is a file that cannot be opened in the current directory!");
            System.exit(0);
        }
        return;
    }
}
