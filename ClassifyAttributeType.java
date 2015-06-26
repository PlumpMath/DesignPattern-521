package mgsstablewriter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class ClassifyAttributeType {
    private static final float typeClassifyLowerBounder = (float) 0.5;
    // private static final float classifyProperyLowerBounder=(float) 0.6;
    @SuppressWarnings("unused")
    private  String columnName;

    private int blankTotal;
    private HashSet<String> exceptionAttributeSet;
    private HashMap<String, Integer> attributeTypeMap;

    public ClassifyAttributeType(String columnName) {
        this.columnName = columnName;

        blankTotal = 0;
        exceptionAttributeSet = new LinkedHashSet<String>();
        attributeTypeMap = new LinkedHashMap<String, Integer>();
    }

    public void statisticAttribteType(String attributeValue) {
        if (filterEmptyValue(attributeValue))
            return;
        String attributeType = doClassifyAttributeType(attributeValue);
        if ("undefine" == attributeType)
            exceptionAttributeSet.add(attributeValue);

        Integer freq = attributeTypeMap.get(attributeType);
        attributeTypeMap.put(attributeType, (freq == null) ? 1 : freq + 1);
    }

    private String doClassifyAttributeType(String attributeValue) {
        ClassifyAttribute handlerByRegex = new ClassifyAttributeByRegex();
        ClassifyAttribute handlerByBankCardNumber = new ClassifyBankCardNumber();
        handlerByRegex.setNextClassifyHandler(handlerByBankCardNumber);

        return handlerByRegex.doClassifyAttribute(attributeValue);
    }

    public void printStatus() {
        List<Map.Entry<String, Integer>> entries = new ArrayList<Map.Entry<String, Integer>>(
                attributeTypeMap.entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> obj1, Map.Entry<String, Integer> obj2) {
                return obj2.getValue() - obj1.getValue();
            }
        });
        System.out.println("Blank:" + blankTotal);
        for (Map.Entry<String, Integer> entry : entries) {
            System.out.println(entry.getKey() + ":" + entry.getValue());
        }

        System.out.println("undefine:" + exceptionAttributeSet);
    }

    public String getAttributeType() {
        // 出现频率最高，并且出现频率超过typeClassifyLowerBounder才认为被识别，否则是"undefine"
        int validPropertyCount = 0;
        int maxPropertyCount = 0;
        String attributeType = "undefine";
        for (Map.Entry<String, Integer> entry : attributeTypeMap.entrySet()) {
            if (entry.getValue() > maxPropertyCount) {
                attributeType = entry.getKey();
                maxPropertyCount = entry.getValue();
            }
            validPropertyCount += entry.getValue();
        }
        float ratio = (float) maxPropertyCount / validPropertyCount;
        if (ratio < typeClassifyLowerBounder)
            attributeType = "undefine";
        return attributeType;
    }

    public float getUndefineRatio() {
        int validPropertyCount = 0;
        for (int count : attributeTypeMap.values())
            validPropertyCount += count;
        int undefineCount = attributeTypeMap.get("undefine") == null ? 0 : attributeTypeMap.get("undefine");
        return undefineCount / (float) (validPropertyCount + blankTotal);
    }

    public boolean filterEmptyValue(String attributeValue) {
        if (attributeValue == null || attributeValue.isEmpty()) {
            ++blankTotal;
            return true;
        }
        return false;
    }
}
