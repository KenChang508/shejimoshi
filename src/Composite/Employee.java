package Composite;

import java.util.ArrayList;
import java.util.List;

public class Employee {
    private String name;
    private String dept;
    private List<Employee> subordinates;

    public Employee(String name, String dept){
        this.name = name;
        this.dept = dept;
        subordinates = new ArrayList<>();
    }

    public void add(Employee e){
        subordinates.add(e);
    }

    public void remove(Employee e){
        subordinates.remove(e);
    }

    public List<Employee> getSubordinates(){
        return subordinates;
    }

    public void printInfo(){
        System.out.println("姓名：" + name + "，部门：" + dept);
    }
}
