package Parser;

import java.util.ArrayList;

public class SQLVisitorJoinConditions {
    ArrayList<String> tableNames;
    ArrayList<Integer> joinTypes;
    ArrayList<ArrayList<ArrayList<ArrayList>>> conditions;

    public SQLVisitorJoinConditions(ArrayList<String> tableNames,
            ArrayList<Integer> joinTypes,
            ArrayList<ArrayList<ArrayList<ArrayList>>> conditions)
    {
        this.tableNames = tableNames;
        this.joinTypes = joinTypes;
        this.conditions = conditions;
    }
}
