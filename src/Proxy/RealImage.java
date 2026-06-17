package Proxy;

public class RealImage implements Image {
    private String fileName;

    public RealImage(String fileName){
        this.fileName = fileName;
        loadFromDisk();
    }

    private void loadFromDisk(){
        System.out.println("从磁盘加载：" + fileName);
    }

    @Override
    public void display(){
        System.out.println("显示图片：" + fileName);
    }
}
