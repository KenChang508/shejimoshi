package Facade;

public class ComputerFacade {
    private CPU cpu;
    private Memory memory;
    private HardDrive hardDrive;

    public ComputerFacade(){
        cpu = new CPU();
        memory = new Memory();
        hardDrive = new HardDrive();
    }

    public void start(){
        System.out.println("=== 电脑启动 ===");
        cpu.start();
        memory.load();
        hardDrive.read();
        System.out.println("=== 启动完成 ===");
    }

    public void shutdown(){
        System.out.println("=== 电脑关闭 ===");
        hardDrive.write();
        memory.unload();
        cpu.shutdown();
        System.out.println("=== 关闭完成 ===");
    }
}
