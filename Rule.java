import java.util.ArrayList;

public class Rule {
    CustomString lhs;
    ArrayList<ArrayList<CustomString>> rhs;

    public Rule(CustomString lhs, ArrayList<ArrayList<CustomString>> rhs){
        this.lhs = lhs;
        this.rhs = rhs;
    }
}