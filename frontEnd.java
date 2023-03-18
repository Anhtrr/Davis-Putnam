import java.io.*;
import java.util.*;

/**
 *  
 *  Artificial Inteligence - Programming Assignment #2 - Front End Implementation
 *  Professor Ernest Davis
 *  @author Anh Tran - 3/12/2023
 *  Due: 3/20/2023 - 11AM (EST)
 *  
 */
public class frontEnd {    
    // MAIN
    public static void main(String args[]){
        // store variables extracted from FEin.txt
        List<String> allNodes = new ArrayList<>();
        List<String> allTreasures = new ArrayList<>();
        List<String> mazeLines = new ArrayList<>();
        int allowedSteps = 0;
        
        // EXTRACT NODES, TREASURES AND ALLOWED STEPS FROM INPUT
        try{
            allowedSteps = readInput(allNodes, allTreasures, mazeLines);
        } catch (FileNotFoundException e){
            System.err.println("File FEin.txt was not found!");
            System.exit(0);
        }
        
        // CONSTRUCT ATOMS 
        HashMap<Integer, String[]> atoms = new HashMap<>();
        HashMap<String[], Integer> atomsOp = new HashMap<>();
        HashMap<Integer, String> atomsForPrint = new HashMap<>(); // WILL BE USED AS BACK MATTER 
        constructAtoms(atoms, atomsOp, atomsForPrint, allNodes, allTreasures, allowedSteps);

        // CONSTRUCT TREASURES AND EDGES FOR EACH NODE
        HashMap<String, List<String>> nodeEdges = new HashMap<>();
        HashMap<String, List<String>> nodeTreasures = new HashMap<>();
        constructEdges(nodeEdges, nodeTreasures, allNodes, mazeLines);
        
        // CONSTRUCT AND TRANSCRIBE CLAUSES
        List<List<Integer>> clauses = new ArrayList<>();
        category1(clauses, atoms, atomsOp);
        category2(clauses, atoms, atomsOp, nodeEdges);
        category3(clauses, atoms, atomsOp, nodeTreasures);
        category4(clauses, atoms, atomsOp);
        category5(clauses, atoms, atomsOp, nodeTreasures);
        category6(clauses, atoms, atomsOp);
        category7(clauses, atoms, atomsOp);
        category8(clauses, atoms, atomsOp, allowedSteps);

        // WRITE TO OUTPUT FILE
        try{
            writeResults(clauses, atomsForPrint);
        } catch (IOException e){
            System.err.println("FEout_DPin.txt is an existing directory " +
            "or is a file that cannot be opened in the current directory!");
            System.exit(0);
        }
    }
    
    // EXTRACT ALL NODES, TREASURES, AND SPECIFIED MAX STEPS
    public static int readInput(List<String> nodes, List<String> treasures,
    List<String> mazeLines) throws FileNotFoundException{
        File frontEndInFile = new File("FEin.txt");
        int steps = -1;
        try{
            Scanner inFile = new Scanner(frontEndInFile);
            // Construct Nodes ArrayList
            if (inFile.hasNextLine()){
                String nodesLine = inFile.nextLine().trim();
                String[] nodesAsArray = nodesLine.split("\\s+");
                for (String node : nodesAsArray){
                    nodes.add(node);
                }
            }
            // Construct Treasures ArrayList
            if (inFile.hasNextLine()){
                String treasuresLine = inFile.nextLine().trim();
                String[] treasuresAsArray = treasuresLine.split("\\s+");
                for (String treasure : treasuresAsArray){
                    treasures.add(treasure);
                }
            }
            // Get Steps Allowed
            if (inFile.hasNextLine()){
                steps = Integer.valueOf(inFile.nextLine().trim()); 
            }
            // Store Remaining Maze Lines
            while (inFile.hasNextLine()){
                String mazeLine = inFile.nextLine().trim();
                if(!mazeLine.equals("")){
                    mazeLines.add(mazeLine); 
                }
            }
            inFile.close();
        } catch (FileNotFoundException e) {
            System.err.println("File FEin.txt was not found!");
            System.exit(0);
        }
        return steps;
    }

    // CONSTRUCT ATOMS 
    public static void constructAtoms(HashMap<Integer, String[]> atoms, 
    HashMap<String[], Integer> atomsOp, HashMap<Integer, String> atomsForPrint, 
    List<String> nodes, List<String> treasures, 
    int maxSteps){
        int refNumber = 1;
        // for each step
        for(int step = 0; step <= maxSteps; step++){
            // atoms: key: refNumber , value: [At , node, step] - for each node
            for(String node : nodes){
                int key = refNumber; 
                String[] value = {"At", node.trim(), Integer.toString(step)};
                String valueForPrint = "At(" + node.trim() + "," + Integer.toString(step) + ")";
                atoms.put(key, value);
                atomsOp.put(value, key);
                atomsForPrint.put(key, valueForPrint);
                refNumber+=1;
            }
        }
        for(int step = 0; step <= maxSteps; step++){
            // key: refNumber , value: [Has, treasure, step] - for each treasure
            for(String treasure : treasures){
                int key = refNumber;
                String[] value = {"Has", treasure.trim(), Integer.toString(step)};
                String valueForPrint = "Has(" + treasure.trim() + "," + Integer.toString(step) + ")";
                atoms.put(key, value);
                atomsOp.put(value, key);
                atomsForPrint.put(key, valueForPrint);
                refNumber+=1;
            }
        }
        return;
    }

    // CONSTRUCT EDGES 
    public static void constructEdges(HashMap<String, List<String>> nodeEdges,  
    HashMap<String, List<String>> nodeTreasures, List<String> allNodes, 
    List<String> mazeLines){
        // set nodeEdges and nodeTreasures
        //  - nodeEdges : { { key: node , value: [ adjNode1 , ... ] } ... }
        //  - nodeTreasures : { { key: node , value: [ treasure1@node , ... ] } ... }

        // Initiate an edge and treasure field for each node extracted
        for(String node : allNodes){
            List<String> adjNodes = new ArrayList<>();
            List<String> containsTreasures = new ArrayList<>();
            nodeEdges.put(node, adjNodes);
            nodeTreasures.put(node, containsTreasures);
        }
        // Add adjNodes containsTreasures to each edge by parsing mazeLines
        for (String mazeLine : mazeLines){
            String[] lineAsArray = mazeLine.trim().split("\\s+");
            // for each node listed in mazeLines
            String node = lineAsArray[0].trim();
            // construct containsTreasures list 
            int treasureStartIndex = 2;
            while(!(lineAsArray[treasureStartIndex].equals("NEXT"))){
                nodeTreasures.get(node).add(lineAsArray[treasureStartIndex]);
                treasureStartIndex+=1;
            }
            // construct adjNodes list
            int adjNodeStartIndex = treasureStartIndex+1; // index after "NEXT"
            for(int i = adjNodeStartIndex; i < lineAsArray.length; i++){
                nodeEdges.get(node).add(lineAsArray[i]);
            }
        }
        return;
    }

    // CATEGORY 1 
    public static void category1(List<List<Integer>> clauses, HashMap<Integer, String[]> atoms, 
    HashMap<String[], Integer> atomsOp){
        // for each atom
        for (int i = 1; i <= atoms.size(); i++){
            // 
            String[] firstPropositionArray = atoms.get(i);
            if(firstPropositionArray[0].equals("Has")){
                break;
            }
            else{
                String firstNode = firstPropositionArray[1].trim();
                String firstTime = firstPropositionArray[2].trim();      
                
                // Category 1
                for (int j = i+1; j <= atoms.size(); j++){
                    String[] secondPropositionArray = atoms.get(j);
                    if(secondPropositionArray[0].equals("Has")){
                        break;
                    }
                    else{
                        String secondNode = secondPropositionArray[1].trim();
                        String secondTime = secondPropositionArray[2].trim();
                        if(firstTime.equals(secondTime) && !firstNode.equals(secondNode)){
                            List<Integer> clause = new ArrayList<>();
                            int firstTranscription = atomsOp.get(firstPropositionArray);
                            int secondTranscription = atomsOp.get(secondPropositionArray);
                            // negate and add
                            clause.add(firstTranscription*-1);
                            clause.add(secondTranscription*-1);
                            clauses.add(clause);
                        }
                        else{
                            continue;
                        }
                    }
                }
            }
        }
        
        return;
    }
    
    // CATEGORY 2
    public static void category2(List<List<Integer>> clauses, HashMap<Integer, String[]> atoms, 
    HashMap<String[], Integer> atomsOp, HashMap<String, List<String>> nodeEdges){
        // for each atom "at"
        for (int i = 1; i <= atoms.size(); i++){
            String[] firstPropositionArray = atoms.get(i);
            if(firstPropositionArray[0].equals("Has")){
                break;
            }
            else{
                String firstNode = firstPropositionArray[1].trim();
                String firstTime = firstPropositionArray[2].trim();
                List<Integer> clause = new ArrayList<>();
                boolean addToClauses = false;
                clause.add(i * -1);
                // for each atom "at" that comes after the "i-th" atom
                for (int j = i+1; j <= atoms.size(); j++){
                    String[] secondPropositionArray = atoms.get(j);
                    if(secondPropositionArray[0].equals("Has")){
                        break;
                    }
                    else{
                        String secondNode = secondPropositionArray[1].trim();
                        String secondTime = secondPropositionArray[2].trim();
                        List<String> firstAdjList = nodeEdges.get(firstNode);
                        if ((Integer.valueOf(secondTime)) != (Integer.valueOf(firstTime)+1)){
                            continue;
                        }
                        else{
                            if (firstAdjList.contains(secondNode)){
                                clause.add(atomsOp.get(secondPropositionArray));
                                addToClauses = true;
                            }
                        }
                    }
                }
                if (addToClauses == true){
                    clauses.add(clause);
                }
            }
        }
        return;
    }

    // CATEGORY 3
    public static void category3(List<List<Integer>> clauses, HashMap<Integer, String[]> atoms, 
    HashMap<String[], Integer> atomsOp, HashMap<String, List<String>> nodeTreasures){
        // for each atom "at"
        for (int i = 1; i <= atoms.size(); i++){
            String[] firstPropositionArray = atoms.get(i);
            if(firstPropositionArray[0].equals("Has")){
                break;
            }
            else{
                String firstNode = firstPropositionArray[1].trim();
                String firstTime = firstPropositionArray[2].trim();
                List<Integer> clause = new ArrayList<>();
                boolean addToClauses = false;
                clause.add(i * -1);
                // for each atom "at" that comes after the "i-th" atom
                for (int j = i+1; j <= atoms.size(); j++){
                    String[] secondPropositionArray = atoms.get(j);
                    if(secondPropositionArray[0].equals("At")){
                        continue;
                    }
                    else{
                        String secondTreasure = secondPropositionArray[1].trim();
                        String secondTime = secondPropositionArray[2].trim();
                        List<String> firstTreasureList = nodeTreasures.get(firstNode);
                        if (firstTreasureList.contains(secondTreasure) && (firstTime.equals(secondTime))){
                            clause.add(atomsOp.get(secondPropositionArray));
                            addToClauses = true;
                        }
                    }
                }
                if (addToClauses == true){
                    clauses.add(clause);
                }
            }
        }
        return;
    }

    // CATEGORY 4
    public static void category4(List<List<Integer>> clauses, HashMap<Integer, String[]> atoms, 
    HashMap<String[], Integer> atomsOp){
        // for each atom "has"
        for (int i = 1; i <= atoms.size(); i++){
            String[] firstPropositionArray = atoms.get(i);
            if(firstPropositionArray[0].equals("At")){
                continue;
            }
            else{
                String firstTreasure = firstPropositionArray[1].trim();
                String firstTime = firstPropositionArray[2].trim();
                List<Integer> clause = new ArrayList<>();
                boolean addToClauses = false;
                clause.add(i * -1);
                // for each atom "has" that comes after the "i-th" atom
                for (int j = i+1; j <= atoms.size(); j++){
                    String[] secondPropositionArray = atoms.get(j);
                    if(secondPropositionArray[0].equals("At") || j==i){
                        continue;
                    }
                    else{
                        String secondTreasure = secondPropositionArray[1].trim();
                        String secondTime = secondPropositionArray[2].trim();
                        if (firstTreasure.equals(secondTreasure) && ((Integer.valueOf(secondTime)) == (Integer.valueOf(firstTime)+1))){
                            clause.add(atomsOp.get(secondPropositionArray));
                            addToClauses = true;
                            break;
                        }
                    }
                }
                if (addToClauses == true){
                    clauses.add(clause);
                }
            }
        }
        return;
    }

    // CATEGORY 5
    public static void category5(List<List<Integer>> clauses, HashMap<Integer, String[]> atoms, 
    HashMap<String[], Integer> atomsOp, HashMap<String, List<String>> nodeTreasures){
        // for each atom "has"
        for (int i = 1; i <= atoms.size(); i++){
            String[] firstPropositionArray = atoms.get(i);
            if(firstPropositionArray[0].equals("At")){
                continue;
            }
            else{
                String firstTreasure = firstPropositionArray[1].trim();
                String firstTime = firstPropositionArray[2].trim();
                List<Integer> clause = new ArrayList<>();
                boolean addToClauses = false;
                clause.add(i);
                // for each atom "has" thats not same as the first atom
                for (int j = i+1; j <= atoms.size(); j++){
                    String[] secondPropositionArray = atoms.get(j);
                    if(secondPropositionArray[0].equals("At") || j==i){
                        continue;
                    }
                    else{
                        String secondTreasure = secondPropositionArray[1].trim();
                        String secondTime = secondPropositionArray[2].trim();
                        if (firstTreasure.equals(secondTreasure) && ((Integer.valueOf(secondTime)) == (Integer.valueOf(firstTime)+1))){
                            clause.add(atomsOp.get(secondPropositionArray) * -1);
                            addToClauses = true;
                            break;
                        }
                    }
                }
                if (addToClauses == true){
                    addToClauses = false;
                    for (int k = 1; k <= atoms.size(); k++){
                        String[] thirdPropositionArray = atoms.get(k);
                        if(thirdPropositionArray[0].equals("Has")){
                            break;
                        }
                        else{
                            String thirdNode = thirdPropositionArray[1].trim();
                            String thirdTime = thirdPropositionArray[2].trim();
                            List<String> thirdTreasureList = nodeTreasures.get(thirdNode);
                            if(((Integer.valueOf(thirdTime)) == (Integer.valueOf(firstTime)+1)) && 
                            thirdTreasureList.contains(firstTreasure)){
                                clause.add(atomsOp.get(thirdPropositionArray));
                                addToClauses = true;
                            }
                        }
                    }
                }
                if (addToClauses == true){
                    clauses.add(clause);
                }
            }
        }
        return;
    }

    // CATEGORY 6
    public static void category6(List<List<Integer>> clauses, HashMap<Integer, String[]> atoms, 
    HashMap<String[], Integer> atomsOp){
        // for each atom "at"
        for (int i = 1; i <= atoms.size(); i++){
            String[] firstPropositionArray = atoms.get(i);
            if(firstPropositionArray[0].equals("Has")){
                break;
            }
            else{
                String firstNode = firstPropositionArray[1].trim();
                String firstTime = firstPropositionArray[2].trim();
                if(firstNode.equals("START") && 
                (Integer.valueOf(firstTime) == 0)){
                    List<Integer> startClause = new ArrayList<>();
                    startClause.add(atomsOp.get(firstPropositionArray));
                    clauses.add(startClause);
                    break;
                }     
            }
        }
        return;
    }

    // CATEGORY 7
    public static void category7(List<List<Integer>> clauses, HashMap<Integer, String[]> atoms, 
    HashMap<String[], Integer> atomsOp){
        // for each "has"
        for (int i = 1; i <= atoms.size(); i++){
            String[] firstPropositionArray = atoms.get(i);
            if(firstPropositionArray[0].equals("At")){
                continue;
            }
            else{
                String firstTime = firstPropositionArray[2].trim();
                if(Integer.valueOf(firstTime) == 0){
                    List<Integer> clause = new ArrayList<>();
                    clause.add(atomsOp.get(firstPropositionArray) * -1);
                    clauses.add(clause);
                }
            }
        }
        return;
    }

    // CATEGORY 8
    public static void category8(List<List<Integer>> clauses, HashMap<Integer, String[]> atoms, 
    HashMap<String[], Integer> atomsOp, int maxSteps){
        // for each "has"
        for (int i = 1; i <= atoms.size(); i++){
            String[] firstPropositionArray = atoms.get(i);
            if(firstPropositionArray[0].equals("At")){
                continue;
            }
            else{
                String firstTime = firstPropositionArray[2].trim();
                if(Integer.valueOf(firstTime) == maxSteps){
                    List<Integer> clause = new ArrayList<>();
                    clause.add(atomsOp.get(firstPropositionArray));
                    clauses.add(clause);
                }
            }
        }
        return;
    }

    // HELPER 1 - Write results to file
    public static void writeResults(List<List<Integer>> clauses, 
    HashMap<Integer, String> atomsForPrint) throws IOException{
        File outputFile = new File("FEout_DPin.txt");
        try{
            FileWriter fw = new FileWriter(outputFile);
            // Write transcribed clauses first
            for(List<Integer> clause : clauses){
                String lineInFile = "";
                for(int atom : clause){
                    lineInFile+= String.valueOf(atom) + " ";
                }
                fw.write(lineInFile.trim() + "\n");
            }
            // Write 0 that separates back matter
            fw.write("0\n");
            // Write Back Matter
            int largestKey = atomsForPrint.size();
            int largestKeyDigits = String.valueOf(largestKey).length();
            for(int i = 1; i <= largestKey; i++){
                String key = String.valueOf(i);
                int currentKeyDigits = key.length();
                String proposition = atomsForPrint.get(i);
                String backMatterLine = "";
                for(int j = 0; j < largestKeyDigits-currentKeyDigits; j++){
                    backMatterLine += " ";
                }
                if (i == largestKey){
                    fw.write(backMatterLine + key + " " + proposition);
                }
                else{
                    fw.write(backMatterLine + key + " " + proposition + "\n");
                }
            }
            fw.close();
        } catch (IOException e){
            System.err.println("FEout_DPin.txt is an existing directory " +
            "or is a file that cannot be opened in the current directory!");
            System.exit(0);
        } 
        return;
    }
}

// CITATIONS
//      https://docs.oracle.com/javase/8/docs/api/java/util/HashMap.html
