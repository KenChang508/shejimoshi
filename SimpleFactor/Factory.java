package SimpleFactor;

public class Factory {
    //构造的一个水果工厂
    public Fruit CreatFruit(String str){
        if (str.equals("A"))
            return new Apple();
        if (str.equals("B"))
            return new Banana();
        return null;
    }
}
