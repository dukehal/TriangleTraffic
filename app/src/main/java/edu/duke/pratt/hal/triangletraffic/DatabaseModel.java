package edu.duke.pratt.hal.triangletraffic;

import java.util.Collection;
import java.util.HashMap;

/**
 * Created by tedzhu on 5/28/15.
 */
public class DatabaseModel {

    protected static HashMap<Integer, DatabaseModel> map;
    private int id;

//    public DatabaseModel(int id) {
//        //this.map.put(id, this);
//    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public static DatabaseModel find(int id) {
        return map.get(id);
    }

    public static int size() {
        return map.size();
    }

    public static Collection<?> asCollection() {
        return (Collection<?>)map.values();
    }

}
