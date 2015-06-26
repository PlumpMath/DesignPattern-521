package mgsstablewriter;

import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mgcommon.DAO.DBManager;
import mgcommon.DAO.Session;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;

public class ClassifyAttributeByRegex extends ClassifyAttribute {
    private static HashMap<String, String> attributeType = new HashMap<String,String>();

    public static HashMap<String, String> getAttributeType() {
        return attributeType;
    }

    // 此处假设attributeValue仅可能属于一个类别
    public String doClassifyAttribute(String attributeValue) {
        if (ClassifyAttributeByRegex.getAttributeType().isEmpty()){
            ClassifyAttributeByRegex.initRegexPropertySemaIDMap();
        }
        
        attributeValue = attributeValue.trim();
        String valueType = "undefine";
        Set<String> keys = attributeType.keySet();
        for (String key : keys) {
            Pattern pattern = Pattern.compile(key);
            Matcher matcher = pattern.matcher(attributeValue);
            if (matcher.find()) {
                // System.out.format("I found the text" +
                // " \"%s\" starting at " +
                // "index %d and ending at index %d.%n",
                // matcher.group(),
                // matcher.start(),
                // matcher.end());
                // System.out.println(key+":"+attributeValue+":"+matcher.group());
                if (attributeValue == matcher.group()) {
                    valueType = attributeType.get(key);
                    break;
                }
            }
        }

        if (valueType.equals("undefine") && nextClassifyHandler != null) {
            valueType = nextClassifyHandler.doClassifyAttribute(attributeValue);
        }

        return valueType;
    }

    /**
     * 从数据库中获取正则表达式和属性语义ID的对应关系，用于属性识别
     */
    public static void initRegexPropertySemaIDMap() {
        Session session = DBManager.getSession();
        String sql = "select * from mgconf.property_regex_config";

        ResultSet rs = session.execute(sql);
        for (Row row : rs) {
            String id = row.getUUID("id").toString();
            String regex = row.getString("regex");
            attributeType.put(regex, id);
        }
    }
}
