package Composite;

public class CompositeDemo {
    public static void main(String[] args) {
        Employee CEO = new Employee("张三", "CEO");
        Employee manager = new Employee("李四", "经理");
        Employee developer = new Employee("王五", "开发");

        CEO.add(manager);
        manager.add(developer);

        CEO.printInfo();

        for (Employee emp : CEO.getSubordinates()){
            emp.printInfo();
            for (Employee e : emp.getSubordinates()){
                e.printInfo();
            }
        }
    }
}
