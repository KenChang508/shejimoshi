package Singleton;

public class President {
    private static President instance;
    private String name;

    private President(){
        this.name = "未命名总统";
    }

    public static President getInstance(){
        if (instance == null){
            instance = new President();
        }
        return instance;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getName(){
        return this.name;
    }

    public void showInfo(){
        System.out.println("总统姓名：" + name);
    }
}