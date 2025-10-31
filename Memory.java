import java.util.*;

public class Memory {
    private Map<String, Integer> globalInts = new HashMap<>();
    private Map<String, Map<String, Integer>> globalObjs = new HashMap<>();
    private Map<String, String> globalObjDefaultKeys = new HashMap<>();
    private Deque<Map<String, String>> localObjDefaultKeyStack = new ArrayDeque<>();
    private Deque<Map<String, Integer>> localIntStack = new ArrayDeque<>();
    private Deque<Map<String, Map<String, Integer>>> localObjStack = new ArrayDeque<>();

    public void enterScope() {
        localIntStack.push(new HashMap<>());
        localObjStack.push(new HashMap<>());
        localObjDefaultKeyStack.push(new HashMap<>());
    }

    public void exitScope() {
        localIntStack.pop();
        localObjStack.pop();
        localObjDefaultKeyStack.pop();
    }

    public void declareInt(String id) {
        if (!localIntStack.isEmpty())
            localIntStack.peek().put(id, 0);
        else
            globalInts.put(id, 0);
    }

    public void setInt(String id, int val) {
        if (setLocalInt(id, val)) return;
        if (globalInts.containsKey(id)) globalInts.put(id, val);
        else throw new RuntimeException("Undefined variable: " + id);
    }

    private boolean setLocalInt(String id, int val) {
        for (Map<String, Integer> frame : localIntStack)
            if (frame.containsKey(id)) { frame.put(id, val); return true; }
        return false;
    }

    public int getInt(String id) {
        for (Map<String, Integer> frame : localIntStack)
            if (frame.containsKey(id)) return frame.get(id);
        if (globalInts.containsKey(id)) return globalInts.get(id);
        throw new RuntimeException("Undefined variable: " + id);
    }

    public void declareObj(String id) {
        if (!localObjStack.isEmpty()){
            localObjStack.peek().put(id, null);
            localObjDefaultKeyStack.peek().put(id, null);
        }else{
            globalObjs.put(id, null);
            globalObjDefaultKeys.put(id, null);
        }
    }

    public void setObjRef(String id, Map<String, Integer> obj) {
        if (setLocalObj(id, obj)) return;
        if (globalObjs.containsKey(id)) globalObjs.put(id, obj);
        else throw new RuntimeException("Undefined object: " + id);
    }

    private boolean setLocalObj(String id, Map<String, Integer> obj) {
        for (Map<String, Map<String, Integer>> frame : localObjStack)
            if (frame.containsKey(id)) { frame.put(id, obj); return true; }
        return false;
    }

    public Map<String, Integer> getObj(String id) {
        for (Map<String, Map<String, Integer>> frame : localObjStack)
            if (frame.containsKey(id)) return frame.get(id);
        if (globalObjs.containsKey(id)) return globalObjs.get(id);
        throw new RuntimeException("Undefined object: " + id);
    }

    public void setDefaultKey(String id, String key) {
        if (setLocalDefaultKey(id, key)) return;
        if (globalObjDefaultKeys.containsKey(id)) {
            globalObjDefaultKeys.put(id, key);
            return;
        }
        throw new RuntimeException("Undefined object: " + id);
    }

    private boolean setLocalDefaultKey(String id, String key) {
        for (Map<String, String> frame : localObjDefaultKeyStack) {
            if (frame.containsKey(id)) { frame.put(id, key); return true; }
        }
        return false;
    }

    public String getDefaultKey(String id) {
        for (Map<String, String> frame : localObjDefaultKeyStack) {
            if (frame.containsKey(id)) return frame.get(id);
        }
        if (globalObjDefaultKeys.containsKey(id)) return globalObjDefaultKeys.get(id);
        throw new RuntimeException("Undefined object: " + id);
    }

    public boolean hasIntVar(String id) {
    for (Map<String, Integer> frame : localIntStack)
        if (frame.containsKey(id)) return true;
    return globalInts.containsKey(id);
    }

    public boolean hasObjVar(String id) {
        for (Map<String, Map<String, Integer>> frame : localObjStack)
            if (frame.containsKey(id)) return true;
        return globalObjs.containsKey(id);
    }

    public void updateObjRefAndKeepDefault(String id, Map<String,Integer> newRef) {
        String oldDefault = null;
        try { oldDefault = getDefaultKey(id); } catch (RuntimeException ignored) {}
        setObjRef(id, newRef);
        if (oldDefault != null) setDefaultKey(id, oldDefault);
    }

}
