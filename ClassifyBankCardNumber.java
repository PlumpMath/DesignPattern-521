package mgsstablewriter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import mgcommon.DAO.DBManager;
import mgcommon.DAO.Session;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;

public class ClassifyBankCardNumber extends ClassifyAttribute {
    private final static String bankCardAttributeType = "借记卡号";
    private final static String creditCardAttributeType = "信用卡号";

    private static Map<String, UUID> nameToUUID = new HashMap<String, UUID>();

    public static Map<String, UUID> getNameToUUID() {
        return nameToUUID;
    }

    public static void initNameUUIDMap() {
        Session session = DBManager.getSession();
        String sql = "select * from mgmeta.propertysema";

        ResultSet rs = session.execute(sql);
        for (Row row : rs) {
            UUID id = row.getUUID("id");
            String name = row.getString("name");
            if (name.trim().equals(bankCardAttributeType) || name.trim().equals(creditCardAttributeType)) {
                nameToUUID.put(name, id);
            }
        }
    }

    @Override
    public String doClassifyAttribute(String attributeValue) {
        
        if(ClassifyBankCardNumber.getNameToUUID().isEmpty())
            ClassifyBankCardNumber.initNameUUIDMap();
        
        String valueType = "undefine";

        if (checkBankCard(attributeValue))
            valueType = ClassifyBankCardType(attributeValue);

        if (valueType.equals("undefine") && nextClassifyHandler != null) {
            valueType = nextClassifyHandler.doClassifyAttribute(attributeValue);
        }

        UUID uuid = nameToUUID.get(valueType);
        valueType = (uuid==null?"undefine":uuid.toString());
        return valueType;
    }

    /**
     * 校验银行卡卡号
     * 
     * @param cardId
     * @return
     */
    private static boolean checkBankCard(String cardId) {
        if (cardId == null || cardId.trim().length() == 0 || !cardId.matches("\\d+")) {
            return false;
        }
        char bit = getBankCardCheckCode(cardId.substring(0, cardId.length() - 1));
        return cardId.charAt(cardId.length() - 1) == bit;
    }

    private static String ClassifyBankCardType(String cardId) {
        String attributeType = "undefine";

        if (cardId.matches("\\d{19}"))
            attributeType = bankCardAttributeType;
        else if (cardId.matches("\\d{16}"))
            attributeType = creditCardAttributeType;

        return attributeType;
    }

    /**
     * 从不含校验位的银行卡卡号采用 Luhm 校验算法获得校验位
     * 
     * @param nonCheckCodeCardId
     * @return
     */
    private static char getBankCardCheckCode(String nonCheckCodeCardId) {
        char[] chs = nonCheckCodeCardId.trim().toCharArray();
        int luhmSum = 0;
        for (int i = chs.length - 1, j = 0; i >= 0; i--, j++) {
            int k = chs[i] - '0';
            if (j % 2 == 0) {
                k *= 2;
                k = k / 10 + k % 10;
            }
            luhmSum += k;
        }
        return (luhmSum % 10 == 0) ? '0' : (char) ((10 - luhmSum % 10) + '0');
    }

    public static void main(String[] args) {
        String card = "6214830104025472";
        System.out.println("      card: " + card);
        System.out.println("check code: " + getBankCardCheckCode(card.substring(0, card.length() - 1)));
        System.out.println(" is bank card number: " + checkBankCard(card));

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            checkBankCard(card);
        }
        long time = System.currentTimeMillis() - startTime;
        System.out.println("1000:" + time);
    }

}
