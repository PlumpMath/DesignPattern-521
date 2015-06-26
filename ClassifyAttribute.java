package mgsstablewriter;

//属性识别，被抽象成职责链地方式
//现有处理者有正则表达式，信用卡，银行卡
public abstract class ClassifyAttribute {

    protected ClassifyAttribute nextClassifyHandler;

    public void setNextClassifyHandler(ClassifyAttribute handler) {
        this.nextClassifyHandler = handler;
    }

    public abstract String doClassifyAttribute(String attributeValue);

}
