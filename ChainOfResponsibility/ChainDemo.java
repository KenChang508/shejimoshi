package ChainOfResponsibility;

public class ChainDemo {
    public static void main(String[] args) {
        Logger infoLogger = new InfoLogger(Logger.INFO);
        Logger debugLogger = new DebugLogger(Logger.DEBUG);
        Logger errorLogger = new ErrorLogger(Logger.ERROR);

        infoLogger.setNextLogger(debugLogger);
        debugLogger.setNextLogger(errorLogger);

        infoLogger.logMessage(Logger.INFO, "这是一条信息日志");
        System.out.println("---");
        infoLogger.logMessage(Logger.DEBUG, "这是一条调试日志");
        System.out.println("---");
        infoLogger.logMessage(Logger.ERROR, "这是一条错误日志");
    }
}
