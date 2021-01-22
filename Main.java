import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Map;
import java.util.HashMap;
import java.util.Stack;
import java.util.ArrayList;
import java.util.Iterator;

public class Main {
    static HashMap<String, Rule> grammarRules = new HashMap<>();
    static Stack<Character> inputStack = new Stack<>();
    static Stack<CustomString> grammarStack = new Stack<>();
    static Stack<ArrayList<CustomString>> historyStack = new Stack<>();
    static int grammarRulesCounter = 0;
    static CustomString parent;
    static boolean kleene_plus_mode = false;
    static boolean required = true;

    public static void createRules(){
        BufferedReader reader;

        try{
            reader = new BufferedReader(new FileReader("./Grammar.txt"));

            while(reader.ready()){
                String line = reader.readLine();

                if(!line.equals("")){
                    line = line.replace(";", "");

                    String[] tokens = line.split(":");

                    String[] rhsProductions = tokens[1].split("\\|");

                    ArrayList<ArrayList<CustomString>> rhs = new ArrayList<>();

                    ArrayList<CustomString> splitRules = new ArrayList<>();
                    
                    String productionsContainEpsilon = "NO EPSILON";

                    for(String rhsProduction : rhsProductions){
                        splitRules = new ArrayList<>();

                        String hasEpsilon = "NO EPSILON";

                        if(rhsProduction.trim().equals("")){
                            hasEpsilon = "HAS EPSILON";
                            productionsContainEpsilon = "HAS EPSILON";
                        }

                        String formattedRhsProduction = rhsProduction.trim().replace("\'", "");
                        String[] formattedRhsProductionArray = formattedRhsProduction.split(" ");

                        for(int i = 0; i < formattedRhsProductionArray.length; i++){
                            int lastCharIndex = formattedRhsProductionArray[i].length() - 1;

                            try{
                                if(!(formattedRhsProductionArray[i].charAt(lastCharIndex) == '+')){
                                    splitRules.add(new CustomString(formattedRhsProductionArray[i], hasEpsilon));
                                }
                                else{
                                    if(formattedRhsProductionArray[i].length() != 1){
                                        formattedRhsProductionArray[i] = formattedRhsProductionArray[i].replace("+", "");
                                        splitRules.add(new CustomString(formattedRhsProductionArray[i], "KLEENE PLUS"));
                                    }

                                    else{
                                        splitRules.add(new CustomString(formattedRhsProductionArray[i], hasEpsilon));
                                    }
                                }
                            } catch(StringIndexOutOfBoundsException e){
                                splitRules.add(new CustomString(formattedRhsProductionArray[i], hasEpsilon));
                            }
                        }

                        rhs.add(splitRules);
                    }

                    grammarRules.put(tokens[0].trim(), new Rule(new CustomString(tokens[0].trim(), productionsContainEpsilon), rhs));
                }
            }

            reader.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void printMap(Map map){
        Iterator iterator = map.entrySet().iterator();

        while(iterator.hasNext()){
            Map.Entry pair = (Map.Entry)iterator.next();

            System.out.print("Key is: '" + pair.getKey() + "'. ");

            Rule rule = (Rule)(pair.getValue());

            if(rule.lhs.type.equals("HAS EPSILON"))
                System.out.print(rule.lhs.symbol + " (Contains epsilon) -> ");

            else if(rule.lhs.type.equals("NO EPSILON"))
                System.out.print(rule.lhs.symbol + " (Does not contain epsilon) -> ");

            for(int i = 0; i < rule.rhs.size(); i++){
                for(int j = 0; j < rule.rhs.get(i).size(); j++){
                    if(j != rule.rhs.get(i).size() - 1)
                        System.out.print(rule.rhs.get(i).get(j).symbol + " ");
                    else
                        System.out.print(rule.rhs.get(i).get(j).symbol);
                }

                if(i != rule.rhs.size() - 1)
                    System.out.print(" | ");
            }

            System.out.println("");
        }
    }

    public static void main(String[] args){
        createRules();

        // printMap(grammarRules);

        BufferedReader reader;
        FileWriter writer;

        try{
            reader = new BufferedReader(new FileReader("./Input.txt"));
            writer = new FileWriter("./Output.txt");

            while(reader.ready()){
                inputStack = new Stack<>();
                grammarStack = new Stack<>();
                historyStack = new Stack<>();
                grammarRulesCounter = 0;
                parent = new CustomString();
                kleene_plus_mode = false;
                required = true;

                String line = reader.readLine();
                String origLine = line;

                char[] lineCharArray;

                if(!line.equals("")){
                    line = line.replace(" ", "");
                    lineCharArray = line.toCharArray();

                    for(int i = line.length() - 1; i != -1; i--){
                        inputStack.push(lineCharArray[i]);
                    }

                    grammarStack.push(grammarRules.get("start").rhs.get(0).get(0));

                    while(!grammarStack.isEmpty()){
                        int tempCounter = 0;

                        if(grammarStack.peek().type.equals("KLEENE PLUS")){
                            kleene_plus_mode = true;
                        }

                        if(!Character.isUpperCase(grammarStack.peek().symbol.charAt(0))){
                            Rule rule;

                            if(kleene_plus_mode)
                                rule = grammarRules.get(grammarStack.peek().symbol);
                            else
                                rule = grammarRules.get(grammarStack.pop().symbol);

                            ArrayList<ArrayList<CustomString>> rhsList = rule.rhs;

                            if(rhsList.size() > 1){
                                ArrayList<CustomString> firstRhsProduction = rhsList.get(0);

                                for(int i = firstRhsProduction.size() - 1; i != -1; i--){
                                    grammarStack.push(firstRhsProduction.get(i));

                                    tempCounter++;
                                }

                                parent = rule.lhs;

                                for(int i = rhsList.size() - 1; i != 0; i--){
                                    if(!rhsList.get(i).get(0).symbol.equals(""))
                                        historyStack.push(rhsList.get(i));
                                }
                            }
                    
                            else{
                                ArrayList<CustomString> rhsProduction = rhsList.get(0);

                                for(int i = rhsProduction.size() - 1; i != -1; i--){
                                    grammarStack.push(rhsProduction.get(i));

                                    tempCounter++;
                                }
                            }

                            grammarRulesCounter = tempCounter;
                        }

                        else{
                            Rule rule = grammarRules.get(grammarStack.pop().symbol);

                            ArrayList<ArrayList<CustomString>> rhsList = rule.rhs;

                            ArrayList<CustomString> terminalProduction = rhsList.get(0);

                            grammarStack.push(terminalProduction.get(0));

                            Character topOfInputStack = '$';

                            if(inputStack.size() > 0)
                                topOfInputStack = inputStack.peek();

                            if(topOfInputStack == grammarStack.peek().symbol.charAt(0)){
                                inputStack.pop();
                                grammarStack.pop();

                                grammarRulesCounter = 0;

                                historyStack.clear();

                                parent = new CustomString();

                                if(kleene_plus_mode)
                                    required = false;
                            }

                            else{
                                for(int i = grammarRulesCounter; i != 0; i--){
                                    grammarStack.pop();
                                }

                                grammarRulesCounter = 0;

                                if(historyStack.size() > 0){
                                    ArrayList<CustomString> firstHistoryProduction = historyStack.pop();
    
                                    for(int i = firstHistoryProduction.size() - 1; i != -1; i--){
                                        grammarStack.push(firstHistoryProduction.get(i));
    
                                        grammarRulesCounter++;
                                    }
                                }

                                else{
                                    if(kleene_plus_mode){
                                        if(!required){
                                            grammarStack.pop();

                                            required = true;
                                            kleene_plus_mode = false;

                                            parent = new CustomString();

                                            grammarRulesCounter = 0;

                                            historyStack.clear();
                                        }

                                        else{
                                            break;
                                        }
                                    }

                                    else{
                                        if(parent.type != null){
                                            if(parent.type.equals("HAS EPSILON")){
                                                grammarRulesCounter = 0;
                                                parent = new CustomString();
                                            }
        
                                            else{
                                                break;
                                            }
                                        }
    
                                        else{
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if(grammarStack.isEmpty() && inputStack.isEmpty()){
                        System.out.println(origLine + " - ACCEPT");
                        writer.write(origLine + " - ACCEPT");
                        writer.write(System.lineSeparator());
                    }

                    else if(!grammarStack.isEmpty() && inputStack.isEmpty()){
                        System.out.println(origLine + " - REJECT. Missing token '" + grammarStack.peek().symbol + "'");
                        writer.write(origLine + " - REJECT. Missing token '" + grammarStack.peek().symbol + "'");
                        writer.write(System.lineSeparator());
                    }

                    else{
                        System.out.println(origLine + " - REJECT. Offending token '" + inputStack.peek() + "'");
                        writer.write(origLine + " - REJECT. Offending token '" + inputStack.peek() + "'");
                        writer.write(System.lineSeparator());
                    }
                }
            }

            reader.close();
            writer.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }
}