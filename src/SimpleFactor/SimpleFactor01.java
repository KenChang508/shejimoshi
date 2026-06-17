package SimpleFactor;

public class SimpleFactor01 {
    public static void main(String[] args) {
        Factory factory = new Factory();
        Fruit fruit = factory.CreatFruit("A");
        fruit.eat();
    }
    public String factory(String fruitname){
        if (fruitname.equals("Apple"))
            return "Apple";
        if (fruitname.equals("Banana"))
            return "Banana";
        return null;
    }
}
