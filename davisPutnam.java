import java.io.*;
import java.util.*;

/**
 *  
 *  Artificial Inteligence - Programming Assignment #2 - Davis Putnam Implementation
 *  Professor Ernest Davis
 *  @author Anh Tran - 3/12/2023
 *  Due: 3/20/2023 - 11AM (EST)
 *  
 */
public class davisPutnam {
    // MAIN
    public static void main(String args[]){
        // used to store all unique clauses from input 
        List<List<Integer>> clauses = new ArrayList<>();
        // used to store all unique atoms from input
        HashSet<Integer> atoms = new HashSet<>();
        // used to store all back-matter lines
        List<String> backMatterLines = new ArrayList<>();
        try{
            // EXTRACT INPUT
            readInput(clauses, atoms, backMatterLines);
        } catch (FileNotFoundException e){
            System.err.println("File FEout_DPin.txt was not found!");
            System.exit(0);
        }
        // start with empty bindings
        List<String> bindings = new ArrayList<>();
        // store bindings from DPLL Algo
        List<String> dpllResult = DPLL(clauses, bindings, atoms);
        // WRITE OUTPUT
        try{
            if (dpllResult.size() > 0 && dpllResult.get(0).trim().equals("FAIL")){
                writeResults(false, null, backMatterLines);
            }
            else{
                Collections.sort(dpllResult, new Comparator<String>() {
                    public int compare(String s1, String s2){
                        int s1_int_length = s1.length()-2;
                        int s2_int_length = s2.length()-2;
                        int s1_cmp = Integer.valueOf(s1.substring(0, s1_int_length));
                        int s2_cmp = Integer.valueOf(s2.substring(0, s2_int_length));
                        return (s1_cmp - s2_cmp);
                    }
                });
                writeResults(true, dpllResult, backMatterLines);
            }
        } catch (IOException e){
            System.err.println("DPout_BEin.txt is an existing directory " +
            "or is a file that cannot be opened in the current directory!");
            System.exit(0);
        } 
    }

    // READ INPUT AND CONSTRUCT SET OF CLAUSES
    public static void readInput(List<List<Integer>> clauses, 
    HashSet<Integer> atoms, List<String> backMatterLines) throws FileNotFoundException{
        // Scan files with path: "DPinput.txt"
        File clausesFile = new File("FEout_DPin.txt");
        try{
            Scanner inFile = new Scanner(clausesFile);
            // Extract clauses
            while (inFile.hasNextLine()){
                String fileLine = inFile.nextLine().trim();
                // End of clauses
                if(fileLine.equals("0")){
                    break;
                }
                // Find all clauses and atoms 
                else{
                    // new clause for each line
                    List<Integer> clause = new ArrayList<>();
                    // split string by space
                    String[] atomsInString = fileLine.split("\\s+");
                    for(String atom : atomsInString){
                        int atomAsInt = Integer.valueOf(atom);
                        clause.add(atomAsInt);
                        atoms.add(atomAsInt);
                    }
                    clauses.add(clause);
                }
            }
            // Extract back-matter
            backMatterLines.add("0"); 
            while (inFile.hasNextLine()){
                String backMatterLine = inFile.nextLine();
                // End of back matter lines
                if(backMatterLine.equals("") || backMatterLine == null){
                    break;
                }
                else{
                    backMatterLines.add(backMatterLine);
                }
            }
            inFile.close();
        } catch (FileNotFoundException e) {
            System.err.println("File FEout_DPin.txt was not found!");
            System.exit(0);
        }
        return;
    }

    // EXECUTE DPLL
    @SuppressWarnings("unchecked")
    public static List<String> DPLL(List<List<Integer>> clauses, 
    List<String> bindings, HashSet<Integer> atoms){
        // loop {
        //     if (empty(CS)) return B;
        //     if (emptyClause in CS) return Fail;
        //     if (easyCaseIn(CS,B)) [CS,B] = easyCase(CS,B);
        // } % No easy cases
        while (true){
            // BC1 - if no clauses
            if (clauses.size() == 0){
                // bindings set to bindings
                if (atoms.size() != 0){
                    for (int atom : atoms){
                        if (atom < 0){
                            int negatedAtom = atom * -1;
                            String newBinding = (Integer.toString(negatedAtom).trim() + " T").trim();
                            bindings.add(newBinding);
                            break;
                        }
                        else{
                            String newBinding = (Integer.toString(atom).trim() + " T").trim();
                            bindings.add(newBinding);
                            break;
                        }
                    }
                }                
                return bindings;
            }
            else{
                // BC2 - if empty clause in sets of clauses
                for (List<Integer> clause : clauses){
                    if (clause.size() == 0){
                        List<String> failed = new ArrayList<>();
                        failed.add("FAIL");
                        return failed;
                    }
                }
                // BC3 - if there exists easy case
                int[] easyCheck = new int[2];
                easyCheck = easyCaseCheck(clauses, bindings, atoms);
                if(easyCheck != null){
                    Object[] ezCase = new Object[3];
                    //execute easyCaseHandler
                    ezCase = handleEasyCase(easyCheck, clauses, bindings, atoms);
                    clauses = (List<List<Integer>>) ezCase[0];
                    bindings = (List<String>) ezCase[1];
                    atoms = (HashSet<Integer>) ezCase[2];
                }
                else{
                    break;
                }
            }
        }
        // CSCopy = copy(CS); BCopy=Copy(B);
        List<List<Integer>> clausesCOPY = new ArrayList<>();
        for (List<Integer> clause : clauses){
            List<Integer> clauseCOPY = new ArrayList<>(clause);
            clausesCOPY.add(clauseCOPY);
        }
        List<String> bindingsCOPY = new ArrayList<>(bindings);
        HashSet<Integer> atomsCOPY = new HashSet<>(atoms);
        // P = choose an unbound atom;
        int chosenAtom = 0;
        for(int atom: atomsCOPY){
            chosenAtom = atom;
            // if negated 
            if(chosenAtom < 0){
                chosenAtom *= -1;
            }
            break;
        }
        // [CSCopy, BCopy] = propagate(CSCopy, BCopy, P, True); - [clauses, bindings, atoms]
        Object[] checkPropagate = new Object[3];
        checkPropagate = propagate(clausesCOPY, bindingsCOPY, atomsCOPY, chosenAtom, "T");
        // answer = dpll(CSCopy,BCopy);
        List<List<Integer>> clausesCOPY2 = (List<List<Integer>>) checkPropagate[0];
        List<String> bindingsCOPY2 = (List<String>) checkPropagate[1];
        HashSet<Integer> atomsCOPY2 = (HashSet<Integer>) checkPropagate[2];
        List<String> answer = new ArrayList<>();
        answer = DPLL(clausesCOPY2, bindingsCOPY2, atomsCOPY2);
        // if (answer != Fail) return answer;
        if(answer.size()>0 && !(answer.get(0).equals("FAIL"))){
            return answer;
        }
        // [CS, B] = propagate(CS, B, P, False);
        Object[] checkPropagate2 = new Object[3];
        checkPropagate2 = propagate(clauses, bindings, atoms, chosenAtom, "F");
        clauses = (List<List<Integer>>) checkPropagate2[0];
        bindings = (List<String>) checkPropagate2[1];
        atoms = (HashSet<Integer>) checkPropagate2[2];
        // return dpll(CS,B)
        return DPLL(clauses, bindings, atoms);
    }

    // PROPAGATE
    public static Object[] propagate(
    List<List<Integer>> clauses, List<String> bindings, HashSet<Integer> atoms, 
    int chosenAtom, String sign){
        int negatedChosenAtom = chosenAtom * -1; 
        List<List<Integer>> newClauses = new ArrayList<>();
        List<String> newBindings = new ArrayList<>(bindings);
        HashSet<Integer> newAtoms = new HashSet<>(atoms);
        // Remove atoms from atoms list
        if(newAtoms.contains(chosenAtom)){
            newAtoms.remove(chosenAtom);
        }
        if(newAtoms.contains(negatedChosenAtom)){
            newAtoms.remove(negatedChosenAtom);
        }
        // B = add <A,V> to B;
        String newBinding = (Integer.toString(chosenAtom).trim() + " " + sign.trim()).trim();
        newBindings.add(newBinding);
        // for (each clause C in CS) {
        for (List<Integer> clause : clauses){
            List<Integer> clauseCOPY = new ArrayList<>(clause);   
            if (sign.equals("T")){
                // if (A appears in C with sign V) delete C from CS;
                if(clauseCOPY.contains(chosenAtom)){
                    continue;
                }
                else{
                    // if (A appears in C with sign !V) delete that literal from C;
                    if(clauseCOPY.contains(negatedChosenAtom)){
                        int indexToRemove = clauseCOPY.indexOf(negatedChosenAtom);
                        clauseCOPY.remove(indexToRemove);
                    }
                }
            }
            else if (sign.equals("F")){
                // if (A appears in C with sign V) delete C from CS;
                if(clauseCOPY.contains(negatedChosenAtom)){
                    continue;
                }
                else{
                    // if (A appears in C with sign !V) delete that literal from C;
                    if(clauseCOPY.contains(chosenAtom)){
                        int indexToRemove = clauseCOPY.indexOf(chosenAtom);
                        clauseCOPY.remove(indexToRemove);
                    }
                }
            }
            newClauses.add(clauseCOPY);
        }
        // return (CS,B);
        Object[] result = new Object[3]; // [clauses, bindings, atoms]
        result[0] = newClauses;
        result[1] = newBindings;
        result[2] = newAtoms;
        return result;
    }

    // EASY CASE CHECK 
    public static int[] easyCaseCheck(List<List<Integer>> clauses, 
    List<String> bindings, HashSet<Integer> atoms){ 
        // - singleton: [ [1] , [S_nodes] ] 
        // - pureLiteral: [ [2] , [PL_nodes] ] 
        // - none: null
        boolean singleton = false;
        boolean pureLiteral = false;
        int result = 0;
        int S_node = 0;
        int PL_node = 0;
        // Singleton Check
        for (List<Integer> clause : clauses){
            if(clause.size() == 1){
                singleton = true;
                S_node = clause.get(0);
                break;
            }
        }
        // If no singleton 
        if (singleton == false){
            // Pure Literal Check
            for (Integer atom : atoms){
                int negatedAtom = atom * -1;
                if (atoms.contains(negatedAtom)){
                    continue;
                }
                else{
                    pureLiteral = true;
                    PL_node = atom;
                    break;
                }
            }
        }
        // RETURN CASES
        if(singleton){
            List<HashSet<Integer>> singletonCase = new ArrayList<>();
            result = 1;
            int[] res = new int[2];
            res[0] = result;
            res[1] = S_node;
            return res;
        }
        else if(pureLiteral){
            // !singleton && pureLiteral
            List<HashSet<Integer>> plCase = new ArrayList<>();
            result = 2;
            int[] res = new int[2];
            res[0] = result;
            res[1] = PL_node;
            return res;
        }
        else{
            // !singleton && !pureLiteral
            return null;
        }
    }

    // HANDLE EASY CASE
    public static Object[] handleEasyCase(int[] easyCase, List<List<Integer>> clauses, 
    List<String> bindings, HashSet<Integer> atoms){
        int typeOfEC = easyCase[0];
        int chosenAtom = easyCase[1];
        int negatedChosenAtom = chosenAtom * -1;
        if(chosenAtom < 0){
            return propagate(clauses, bindings, atoms, negatedChosenAtom, "F");
        }
        else{
            return propagate(clauses, bindings, atoms, chosenAtom, "T");
        }
    }

    // HELPER 1 - Write results to file
    public static void writeResults(boolean success, List<String> dpllResult, 
    List<String> backMatterLines) throws IOException{
        File outputFile = new File("DPout_BEin.txt");
        try{
            FileWriter fw = new FileWriter(outputFile);
            // Success DPLL
            if(success == true){
                for(String result : dpllResult){
                    fw.write(result + "\n");
                }
            }
            // Both Success & Failed DPLL
            for (int i = 0; i < backMatterLines.size(); i++){
                if (i == backMatterLines.size()-1){
                    fw.write(backMatterLines.get(i));
                }
                else{
                    fw.write(backMatterLines.get(i) + "\n");
                }
            }
            fw.close();
        } catch (IOException e){
            System.err.println("DPout_BEin.txt is an existing directory " +
            "or is a file that cannot be opened in the current directory!");
            System.exit(0);
        } 
        return;
    }
}

// CITATIONS
//  - https://www.baeldung.com/java-suppresswarnings
//  - https://www.geeksforgeeks.org/how-to-create-array-of-objects-in-java/
//  - https://docs.oracle.com/javase/8/docs/api/java/io/FileWriter.html
//  - https://www.geeksforgeeks.org/collections-sort-java-examples/
//  - https://howtodoinjava.com/java/sort/collections-sort/
//  - Slides on course website: Pseudocode for Davis-Putnam